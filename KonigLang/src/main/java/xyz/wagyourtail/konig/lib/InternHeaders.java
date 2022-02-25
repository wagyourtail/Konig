package xyz.wagyourtail.konig.lib;

import xyz.wagyourtail.konig.structure.headers.KonigHeaders;

import java.lang.reflect.Method;
import java.util.HashMap;

public class InternHeaders extends KonigHeaders {
    public InternHeaders() {
        super(null);
    }

    public void parseClass(Class<?> clazz, String defaultGroup) {
        for (Method method : clazz.getMethods()) {
            if (!method.isAnnotationPresent(Block.class)) continue;
            InternBlock block = new InternBlock();
            block.parseMethod(method, defaultGroup);
            getBlocks().computeIfAbsent(block.group, k -> new HashMap<>()).put(block.name, block);
        }
    }

}
