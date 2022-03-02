package xyz.wagyourtail.konig.structure.headers;

import org.w3c.dom.Node;
import xyz.wagyourtail.konig.structure.code.Code;
import xyz.wagyourtail.konig.structure.code.KonigBlockReference;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ForkJoinPool;
import java.util.function.BiFunction;
import java.util.function.Function;

public class KonigCustomBlock extends KonigBlock {
    public final KonigHeaders parent;
    public final Code code;

    public KonigCustomBlock(KonigHeaders parent) {
        this.parent = parent;
        this.code = new Code(parent);
    }

    @Override
    protected boolean parseChild(Node child) throws IOException {
        if (child.getNodeName().equals("code")) {
            code.parseXML(child);
            return true;
        }
        return super.parseChild(child);
    }

    @Override
    public BiFunction<ForkJoinPool, Map<String, CompletableFuture<Object>>, Map<String, CompletableFuture<Object>>> jitCompile(KonigBlockReference self) {
        BiFunction<ForkJoinPool, Map<String, Object>, CompletableFuture<Map<String, Object>>> compiledInner = code.jitCompile();
        return (async, inputs) -> {
            if (async != null) {
                return inputsToOutputsAsync(inputs, compiledInner, async);
            } else {
                return  inputsToOutputs(inputs, compiledInner);
            }
        };
    }

    public Map<String, CompletableFuture<Object>> inputsToOutputsAsync(Map<String, CompletableFuture<Object>> inputs, BiFunction<ForkJoinPool, Map<String, Object>, CompletableFuture<Map<String, Object>>> compiledInner, ForkJoinPool executor) {
        CompletableFuture<Map<String, Object>> cf = CompletableFuture.allOf(inputs.values().toArray(CompletableFuture[]::new))
            .thenApplyAsync((f) -> compiledMethodArgsProcessor(inputs), executor)
            .thenCompose((a) -> compiledInner.apply(executor, a));

        Map<String, CompletableFuture<Object>> outputs = new HashMap<>();
        for (BlockIO.IOElement output : io.outputs) {
            outputs.put(output.name, cf.thenApply(e -> e.get(output.name)));
        }
        return outputs;
    }

    public Map<String, CompletableFuture<Object>> inputsToOutputs(Map<String, CompletableFuture<Object>> inputs, BiFunction<ForkJoinPool, Map<String, Object>, CompletableFuture<Map<String, Object>>> compiledInner) {
        CompletableFuture<Map<String, Object>> cf = CompletableFuture.allOf(inputs.values().toArray(CompletableFuture[]::new))
            .thenApply((f) -> compiledMethodArgsProcessor(inputs))
            .thenCompose((a) -> compiledInner.apply(null, a));

        Map<String, CompletableFuture<Object>> outputs = new HashMap<>();
        for (BlockIO.IOElement output : io.outputs) {
            outputs.put(output.name, cf.thenApply(e -> e.get(output.name)));
        }
        return outputs;
    }

    public Map<String, Object> compiledMethodArgsProcessor(Map<String, CompletableFuture<Object>> inputs) {
        Map<String, Object> inputsMap = new HashMap<>();
        for (Map.Entry<String, CompletableFuture<Object>> input : inputs.entrySet()) {
            inputsMap.put(input.getKey(), input.getValue().join());
        }
        return inputsMap;
    }

}
