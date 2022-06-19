package xyz.wagyourtail.konig.editor.canvas;

import xyz.wagyourtail.konig.lib.Block;
import xyz.wagyourtail.konig.structure.code.Wire;

import java.util.Set;

public interface RenderBlockParent {
    float viewportX();
    float viewportY();
    float viewportWidth();
    float viewportHeight();
    Set<RenderWire> getWires();
    RenderWire addWireForPort(double x, double y, int blockid, String port);
}
