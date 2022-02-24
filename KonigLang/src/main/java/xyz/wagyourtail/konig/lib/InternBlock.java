package xyz.wagyourtail.konig.lib;

import xyz.wagyourtail.konig.structure.code.InnerCode;
import xyz.wagyourtail.konig.structure.code.KonigBlockReference;
import xyz.wagyourtail.konig.structure.headers.BlockIO;
import xyz.wagyourtail.konig.structure.headers.Hollow;
import xyz.wagyourtail.konig.structure.headers.KonigBlock;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public class InternBlock extends KonigBlock {
    Method method;
    Block block;

    public void parseMethod(Method method, String defaultGroup) {
        block = method.getAnnotation(Block.class);
        if (block == null) throw new IllegalArgumentException("Method must be annotated with @Block");
        this.method = method;
        this.name = block.name();
        this.group = block.group().equals("") ? defaultGroup : block.group();
        this.image = Path.of(block.image());
        for (Block.Generic generic : block.generics()) {
            BlockIO.Generic g = new BlockIO.Generic(generic.name(), generic.extend(), generic.supers());
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
            io.byName.put(name, inp);
            io.elements.computeIfAbsent(inp.side, k -> new HashMap<>()).computeIfAbsent(inp.justify,
                k -> new ArrayList<>()
            ).add(inp);
        }
        for (Block.Output output : outputs) {
            BlockIO.Output out = new BlockIO.Output(output.side(), output.justify(), output.name(), output.type());
            io.outputs.add(out);
            io.byName.put(name, out);
            io.elements.computeIfAbsent(out.side, k -> new HashMap<>()).computeIfAbsent(out.justify,
                k -> new ArrayList<>()
            ).add(out);
        }
    }

    @Override
    public Function<Map<String, CompletableFuture<Object>>, Map<String, CompletableFuture<Object>>> compile(KonigBlockReference self) throws IllegalAccessException {
        MethodHandles.Lookup lookup = MethodHandles.lookup();
        MethodHandle handle = lookup.unreflect(method);

        Map<String, Function<Map<String, Object>, CompletableFuture<Map<String, Object>>>> compiledInnerCode = new HashMap<>();

        for (Map.Entry<String, InnerCode> stringInnerCodeEntry : self.innerCodeMap.entrySet()) {
            String name = stringInnerCodeEntry.getKey();
            InnerCode innerCode = stringInnerCodeEntry.getValue();
            Function<Map<String, Object>, CompletableFuture<Map<String, Object>>> compiled = innerCode.compile();
            compiledInnerCode.put(name, compiled);
        }

        // pre-check for illegal stuff in these indexes, also pre-compile the absolute index map
        Map<Object, Integer> paramIndexes = new HashMap<>();
        {
            int i = -1;
            int p = method.getParameterCount();
            for (Block.Input input : block.inputs()) {
                if (input.index() == -1) ++i;
                else i = input.index();
                if (i >= p) {
                    throw new IllegalArgumentException("input "  + input.name() + " index out of bounds.");
                }
                paramIndexes.put(input, i);
            }
            for (Block.Hollow hollow : block.hollows()) {
                if (hollow.index() == -1)
                    while (paramIndexes.values().contains(++i)) ;
                else {
                    i = hollow.index();
                    if (paramIndexes.values().contains(i)) {
                        throw new IllegalArgumentException("Hollow index " + i + " already used");
                    }
                }
                if (i >= p) {
                    throw new IllegalArgumentException("hollow "  + hollow.name() + " index out of bounds.");
                }
                paramIndexes.put(hollow, i);
            }

        }

        return (inputs) -> {
            CompletableFuture<Object> cf = CompletableFuture.supplyAsync(() -> {
                Object[] args = new Object[method.getParameterCount()];
                Map<String, Object> inputsMap = new HashMap<>();
                for (Map.Entry<String, CompletableFuture<Object>> input : inputs.entrySet()) {
                    inputsMap.put(input.getKey(), input.getValue().join());
                }
                for (Block.Input input : block.inputs()) {
                    args[paramIndexes.get(input)] = inputsMap.get(input.name());
                }
                for (Block.Hollow hollow : block.hollows()) {
                    // technically we're leaking some inputs that don't need to be there (the non-virtual ones)
                    Map<String, Object> innerInputs = new HashMap<>(inputsMap);

                    Function<Object[], Map<String, Object>> wrappedInner = (obj) -> {
                        int j = -1;
                        for (Block.Input input : hollow.inputs()) {
                            if (input.index() == -1) ++j;
                            else j = input.index();
                            innerInputs.put(input.name(), obj[j]);
                        }

                        Map<String, Object> result = compiledInnerCode.get(hollow.name()).apply(innerInputs).join();

                        // loopback virtual inputs
                        for (Map.Entry<String, Object> entry : result.entrySet()) {
                            if (entry.getKey().startsWith("virtual")) {
                                innerInputs.put(entry.getKey(), entry.getValue());
                            }
                        }
                        return result;
                    };

                    args[paramIndexes.get(hollow)] = wrappedInner;
                }
                return args;
            }).thenApply(args -> {
                try {
                    return handle.invokeWithArguments(args);
                } catch (Throwable e) {
                    throw new RuntimeException(e);
                }
            });

            if (block.outputs().length == 1) return Collections.singletonMap(block.outputs()[0].name(), cf);
            else {
                Map<String, CompletableFuture<Object>> map = new HashMap<>();
                for (Block.Output output : block.outputs()) {
                    map.put(output.name(), cf.thenApply(o -> {
                        if (o instanceof Map) return ((Map) o).get(output.name());
                        else return o; //TODO: split this somehow
                    }));
                }
                return map;
            }
        };
    }
}
