package xyz.wagyourtail.konig.structure.headers.blocks;

import xyz.wagyourtail.konig.lib.stdlib.Generic;
import xyz.wagyourtail.konig.structure.code.KonigBlockReference;
import xyz.wagyourtail.konig.structure.headers.KonigBlock;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ForkJoinPool;
import java.util.function.BiFunction;
import java.util.function.Function;

public class StopBlock extends KonigBlock {
    @Override
    public BiFunction<ForkJoinPool, Map<String, CompletableFuture<Object>>, Map<String, CompletableFuture<Object>>> jitCompile(KonigBlockReference self) {
        //TODO: figure out how to make this work with non-async
        return (async, inp) -> Collections.singletonMap("$void", inp.get("stop").thenApply(o -> {
            if (Generic.toBoolean(o)) {
                async.shutdownNow();
            }
            return null;
        }));
    }

}
