package xyz.wagyourtail.konig.editor.canvas;

import java.util.Set;

public interface RenderBlockParent {
    float viewportX();
    float viewportY();
    float viewportWidth();
    float viewportHeight();
    Set<RenderWire> getWires();
}
