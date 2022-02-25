package xyz.wagyourtail.konig.structure.headers.blocks;

import xyz.wagyourtail.konig.structure.code.KonigBlockReference;
import xyz.wagyourtail.konig.structure.headers.KonigBlock;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public class ConstBlock extends KonigBlock {

    @Override
    public Function<Map<String, CompletableFuture<Object>>, Map<String, CompletableFuture<Object>>> compile(KonigBlockReference self) {
        return (args) -> Collections.singletonMap("out", CompletableFuture.completedFuture(self.value));
    }

}
