package xyz.wagyourtail.konig.structure.headers.blocks;

import xyz.wagyourtail.konig.lib.stdlib.Generic;
import xyz.wagyourtail.konig.structure.code.KonigBlockReference;
import xyz.wagyourtail.konig.structure.headers.KonigBlock;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public class StopBlock extends KonigBlock {
    @Override
    public Function<Map<String, CompletableFuture<Object>>, Map<String, CompletableFuture<Object>>> compile(KonigBlockReference self) {
        return (inp) -> Collections.singletonMap("$void", inp.get("stop").thenApply(o -> {
            if (Generic.toBoolean(o)) {
                self.parent.executor.shutdownNow();
            }
            return null;
        }));
    }

}
