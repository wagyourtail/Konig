package xyz.wagyourtail.konig.editor.canvas;

import org.lwjgl.opengl.GL11;
import xyz.wagyourtail.MathHelper;
import xyz.wagyourtail.konig.structure.code.Wire;
import xyz.wagyourtail.wagyourgui.Font;
import xyz.wagyourtail.wagyourgui.GLBuilder;
import xyz.wagyourtail.wagyourgui.elements.BaseElement;
import xyz.wagyourtail.wagyourgui.elements.ElementContainer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class RenderWire extends ElementContainer {
    public final RenderCode code;
    public final Font font;
    public final float wireId;


    protected RenderWire(Wire wire, Font font, RenderCode code) {
        this.font = font;
        this.code = code;
        this.wireId = wire.id;
        compileWire(wire);
    }

    public static List<RenderWire> compile(List<Wire> wires, Font font, RenderCode code) {
        List<RenderWire> renderWires = new ArrayList<>();
        for (Wire wire : wires) {
            renderWires.add(new RenderWire(wire, font, code));
        }
        return renderWires;
    }

    public void compileWire(Wire wire) {
        RenderWireSegment prev = null;
        for (Wire.WireSegment segment : wire.getSegments()) {
            RenderWireSegment segRend = new RenderWireSegment(segment);
            if (prev != null) {
                prev.next = segRend;
            }
            segRend.prev = prev;
            if (segment instanceof Wire.WireBranch) {
                compileBranch((Wire.WireBranch) segment, segRend);
                segRend.branch = segRend.next;
            }
            prev = segRend;
            elements.add(segRend);
        }
    }

    public void compileBranch(Wire.WireBranch branch, RenderWireSegment branchSegment) {
        RenderWireSegment prev = branchSegment;
        for (Wire.WireSegment segment : branch.subSegments) {
            RenderWireSegment segRend = new RenderWireSegment(segment);
            if (prev != null) {
                prev.next = segRend;
            }
            segRend.prev = prev;
            if (segment instanceof Wire.WireBranch) {
                compileBranch((Wire.WireBranch) segment, segRend);
                segRend.branch = segRend.next;
            }
            prev = segRend;
            elements.add(segRend);
        }
    }


    protected class RenderWireSegment extends BaseElement {
        protected static final float LINE_WIDTH = 1f;
        Wire.WireSegment segment;
        public RenderWireSegment prev;
        public RenderWireSegment next;
        public RenderWireSegment branch;

        public RenderWireSegment(Wire.WireSegment segment) {
            this.segment = segment;
        }

        @Override
        public void onRender(float mouseX, float mouseY) {
            if (prev != null) {

                // check if intersects view box
                AtomicReference<MathHelper.LineSegment> ls = new AtomicReference<>(new MathHelper.LineSegment(
                    (float) prev.segment.x,
                    (float) prev.segment.y,
                    (float) segment.x,
                    (float) segment.y
                ));
                if (!MathHelper.clipLine(ls, new MathHelper.Rectangle(code.viewportX, code.viewportY, code.viewportX + code.viewportWidth, code.viewportY + code.viewportHeight))) {
                    return;
                }

                // draw line
                MathHelper.LineSegment segment1 = ls.get();

                GL11.glLineWidth(LINE_WIDTH * 2);
                GLBuilder.getBuilder().begin(GL11.GL_LINES)
                    .color(0xFF000000)
                    .vertex(segment1.x1(), segment1.y1())
                    .vertex(segment1.x2(), segment1.y2())
                    .end();
            }
        }

    }

}
