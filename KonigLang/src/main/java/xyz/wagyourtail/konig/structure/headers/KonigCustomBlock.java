package xyz.wagyourtail.konig.structure.headers;

import org.w3c.dom.Node;
import xyz.wagyourtail.konig.structure.code.Code;
import xyz.wagyourtail.konig.structure.code.KonigBlockReference;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
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
    public Function<Map<String, CompletableFuture<Object>>, Map<String, CompletableFuture<Object>>> jitCompile(KonigBlockReference self) {
        Function<Map<String, Object>, CompletableFuture<Map<String, Object>>> compiledInner = code.jitCompile();
        return (inputs) -> {
            CompletableFuture<Map<String, Object>> cf = CompletableFuture.allOf(inputs.values().toArray(CompletableFuture[]::new)).thenApplyAsync((f) -> {
                Map<String, Object> inputsMap = new HashMap<>();
                for (Map.Entry<String, CompletableFuture<Object>> input : inputs.entrySet()) {
                    inputsMap.put(input.getKey(), input.getValue().join());
                }
                return inputsMap;
            }, self.parent.executor).thenCompose(compiledInner);

            Map<String, CompletableFuture<Object>> outputs = new HashMap<>();
            for (BlockIO.IOElement output : io.outputs) {
                outputs.put(output.name, cf.thenApply(e -> e.get(output.name)));
            }
            return outputs;
        };
    }

}
