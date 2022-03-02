package xyz.wagyourtail.konig.lib;

import xyz.wagyourtail.MethodHandleUtils;
import xyz.wagyourtail.konig.structure.code.InnerCode;
import xyz.wagyourtail.konig.structure.code.KonigBlockReference;
import xyz.wagyourtail.konig.structure.headers.BlockIO;
import xyz.wagyourtail.konig.structure.headers.Hollow;
import xyz.wagyourtail.konig.structure.headers.KonigBlock;

import java.lang.invoke.*;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public class InternBlock extends KonigBlock {
    MethodHandle method;
    int paramCount;
    Function<Object[], Object> resolved;
    Block block;

    public void parseMethod(Method method, String defaultGroup) {
        block = method.getAnnotation(Block.class);
        if (block == null) throw new IllegalArgumentException("Method must be annotated with @Block");

        try {
            this.method = lookup.unreflect(method);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException(e);
        }
        paramCount = method.getParameterCount();
        resolved = MethodHandleUtils.toArgsFunction(this.method);

        this.name = block.name();
        this.group = block.group().equals("") ? defaultGroup : block.group();
        this.image = block.image().equals("") ? null : Path.of(block.image());
        for (Block.Generic generic : block.generics()) {
            BlockIO.Generic g = new BlockIO.Generic(generic.name(), generic.extend().equals("") ? null : generic.extend(), generic.supers().equals("") ? null : generic.supers());
            generics.put(g.name, g);
        }
        parseIO(block.inputs(), block.outputs(), io);
        for (Block.Hollow hollow : block.hollows()) {
            Hollow h = new Hollow();
            h.name = hollow.name();
            h.group = hollow.group().equals("") ? "$ungrouped$" + hollow.name() : hollow.group();
            h.paddingTop = hollow.paddingTop();
            h.paddingBottom = hollow.paddingBottom();
            h.paddingLeft = hollow.paddingLeft();
            h.paddingRight = hollow.paddingRight();
            parseIO(hollow.inputs(), hollow.outputs(), h);
            hollowsByGroupName.computeIfAbsent(h.group, k -> new HashMap<>()).put(h.name, h);
            hollowsByName.put(h.name, h);
        }
    }

    public void parseIO(Block.Input[] inputs, Block.Output[] outputs, BlockIO io) {
        for (Block.Input input : inputs) {
            BlockIO.Input inp = new BlockIO.Input(input.side(), input.justify(), input.name(), input.type(), false);
            io.inputs.add(inp);
            io.byName.put(inp.name, inp);
            io.elements.computeIfAbsent(inp.side, k -> new HashMap<>()).computeIfAbsent(inp.justify,
                k -> new ArrayList<>()
            ).add(inp);
        }
        for (Block.Output output : outputs) {
            BlockIO.Output out = new BlockIO.Output(output.side(), output.justify(), output.name(), output.type());
            io.outputs.add(out);
            io.byName.put(out.name, out);
            io.elements.computeIfAbsent(out.side, k -> new HashMap<>()).computeIfAbsent(out.justify,
                k -> new ArrayList<>()
            ).add(out);
        }
    }

    private static final MethodHandles.Lookup lookup = MethodHandles.lookup();

    @Override
    public Function<Map<String, CompletableFuture<Object>>, Map<String, CompletableFuture<Object>>> jitCompile(KonigBlockReference self, boolean async) {
        if (resolved == null || method == null) {
            throw new IllegalStateException("Method not parsed");
        }
        Map<String, Function<Map<String, Object>, CompletableFuture<Map<String, Object>>>> compiledInnerCode = new HashMap<>();

        for (Map.Entry<String, InnerCode> stringInnerCodeEntry : self.innerCodeMap.entrySet()) {
            String name = stringInnerCodeEntry.getKey();
            InnerCode innerCode = stringInnerCodeEntry.getValue();
            Function<Map<String, Object>, CompletableFuture<Map<String, Object>>> compiled = innerCode.jitCompile(async);
            compiledInnerCode.put(name, compiled);
        }

        // pre-check for illegal stuff in these indexes, also pre-compile the absolute index map
        Object[] params = new Object[paramCount];
        {
            int i = -1;
            for (Block.Input input : block.inputs()) {
                if (input.index() == -1) ++i;
                else i = input.index();
                if (i >= paramCount) {
                    throw new IllegalArgumentException("input "  + input.name() + " index out of bounds.");
                }
                if (params[i] != null) {
                    throw new IllegalArgumentException("Input index " + i + " already used");
                }
                params[i] = input;
            }
            for (Block.Hollow hollow : block.hollows()) {
                if (hollow.index() == -1)
                    while (params[++i] != null) ;
                else {
                    i = hollow.index();
                }
                if (i >= paramCount) {
                    throw new IllegalArgumentException("hollow "  + hollow.name() + " index out of bounds.");
                }
                if (params[i] != null) {
                    throw new IllegalArgumentException("Hollow index " + i + " already used");
                }
                params[i] = hollow;
            }

        }
        if (async) {
            return (inputs) -> inputsToOutputsAsync(inputs, params, compiledInnerCode, self);
        } else {
            return (inputs) -> inputsToOutputs(inputs, params, compiledInnerCode);
        }
    }

    public Map<String, CompletableFuture<Object>> inputsToOutputsAsync(Map<String, CompletableFuture<Object>> inputs, Object[] params, Map<String, Function<Map<String, Object>, CompletableFuture<Map<String, Object>>>> compiledInnerCode, KonigBlockReference self) {
        Map<String, CompletableFuture<Object>> outputs = new ConcurrentHashMap<>();

        CompletableFuture<Object> cf = CompletableFuture.allOf(inputs.values().toArray(CompletableFuture[]::new))
            .thenApplyAsync((f) -> compiledMethodArgsProcessor(params, inputs, compiledInnerCode, outputs), self.parent.executor)
            .thenApply(args -> {
                try {
                    return resolved.apply(args);
                } catch (Throwable e) {
                    e.printStackTrace();
                    throw new RuntimeException(e);
                }
            });
        if (block.outputs().length == 0) outputs.put("$void", cf);
        if (block.outputs().length == 1) outputs.put(block.outputs()[0].name(), cf);
        else {
            for (Block.Output output : block.outputs()) {
                outputs.put(output.name(), cf.thenApply(o -> {
                    if (o instanceof Map) return ((Map) o).get(output.name());
                    else return o; //TODO: split this somehow
                }));
            }
        }
        return outputs;
    }

    public Map<String, CompletableFuture<Object>> inputsToOutputs(Map<String, CompletableFuture<Object>> inputs, Object[] params, Map<String, Function<Map<String, Object>, CompletableFuture<Map<String, Object>>>> compiledInnerCode) {
        Map<String, CompletableFuture<Object>> outputs = new ConcurrentHashMap<>();

        CompletableFuture<Object> cf = CompletableFuture.allOf(inputs.values().toArray(CompletableFuture[]::new))
            .thenApply((f) -> compiledMethodArgsProcessor(params, inputs, compiledInnerCode, outputs))
            .thenApply(args -> {
                try {
                    return resolved.apply(args);
                } catch (Throwable e) {
                    e.printStackTrace();
                    throw new RuntimeException(e);
                }
            });
        if (block.outputs().length == 0) outputs.put("$void", cf);
        if (block.outputs().length == 1) outputs.put(block.outputs()[0].name(), cf);
        else {
            for (Block.Output output : block.outputs()) {
                outputs.put(output.name(), cf.thenApply(o -> {
                    if (o instanceof Map) return ((Map) o).get(output.name());
                    else return o; //TODO: split this somehow
                }));
            }
        }
        return outputs;
    }

    public Object[] compiledMethodArgsProcessor(Object[] params, Map<String, CompletableFuture<Object>> inputs, Map<String, Function<Map<String, Object>, CompletableFuture<Map<String, Object>>>> compiledInnerCode, Map<String, CompletableFuture<Object>> outputs) {
        Object[] args = new Object[method.type().parameterCount()];
        for (int i = 0; i < args.length; ++i) {
            if (params[i] instanceof Block.Input) {
                args[i] = inputs.get(((Block.Input) params[i]).name()).join();
            } else if (params[i] instanceof Block.Hollow) {

                int finalI = i;
                Function<Object[], Map<String, Object>> wrappedInner = (obj) -> {

                    Map<String, Object> innerInputs = new ConcurrentHashMap<>();
                    for (Map.Entry<String, CompletableFuture<Object>> input : inputs.entrySet()) {
                        innerInputs.put(input.getKey(), input.getValue().join());
                    }

                    int j = -1;
                    for (Block.Input input : ((Block.Hollow) params[finalI]).inputs()) {
                        if (input.index() == -1) ++j;
                        else j = input.index();
                        innerInputs.put(input.name(), obj[j]);
                    }

                    Map<String, Object> result = compiledInnerCode.get(((Block.Hollow) params[finalI]).name()).apply(innerInputs).join();

                    // loopback virtual inputs
                    for (Map.Entry<String, Object> entry : result.entrySet()) {
                        if (entry.getKey().startsWith("virtual") && entry.getKey().endsWith("$loopback")) {
                            inputs.put(entry.getKey().substring(0, entry.getKey().length() - "$loopback".length()), CompletableFuture.completedFuture(entry.getValue()));
                        } else {
                            outputs.put(entry.getKey(), CompletableFuture.completedFuture(entry.getValue()));
                        }
                    }
                    return result;
                };

                args[i] = wrappedInner;
            } else {
                throw new IllegalStateException("Unknown parameter type");
            }
        }
        return args;
    }
}
