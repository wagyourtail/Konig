package xyz.wagyourtail.konig.structure.headers.blocks;

import xyz.wagyourtail.konig.structure.code.KonigBlockReference;
import xyz.wagyourtail.konig.structure.headers.KonigBlock;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public class GlobalOutput extends KonigBlock {
    @Override
    public Function<Map<String, CompletableFuture<Object>>, Map<String, CompletableFuture<Object>>> jitCompile(KonigBlockReference self) {
        return (map) -> Collections.singletonMap(self.value, map.get("in"));
    }

}
