package xyz.wagyourtail.konig.editor.canvas;

import org.lwjgl.opengl.GL11;
import xyz.wagyourtail.MathHelper;
import xyz.wagyourtail.konig.structure.code.Wire;
import xyz.wagyourtail.wagyourgui.Font;
import xyz.wagyourtail.wagyourgui.GLBuilder;

import java.util.concurrent.atomic.AtomicReference;

public class WireRenderer {
    protected static final float LINE_WIDTH = 1f;

    public static void render(Wire wire, MathHelper.Rectangle viewport, float mouseX, float mouseY, Font font) {
        Wire.WireSegment prev = null;
        for (Wire.WireSegment segment : wire.getSegments()) {
            if (segment instanceof Wire.WireBranch s) {
                renderBranch(s, viewport, mouseX, mouseY, font);
            }
            renderWireSegment(prev, segment, viewport, mouseX, mouseY, font);
            prev = segment;
        }
    }

    protected static void renderBranch(Wire.WireBranch branch, MathHelper.Rectangle viewport, float mouseX, float mouseY, Font font) {
        Wire.WireSegment prev = branch;
        for (Wire.WireSegment segment : branch.subSegments) {
            if (segment instanceof Wire.WireBranch s) {
                renderBranch(s, viewport, mouseX, mouseY, font);
            }
            renderWireSegment(prev, segment, viewport, mouseX, mouseY, font);
            prev = segment;
        }
    }

    protected static void renderWireSegment(Wire.WireSegment prev, Wire.WireSegment segment, MathHelper.Rectangle viewport, float mouseX, float mouseY, Font font) {
        if (prev == null) {
            return;
        }

        // check if intersects view box
        AtomicReference<MathHelper.LineSegment> ls = new AtomicReference<>(new MathHelper.LineSegment(
            (float) prev.x,
            (float) prev.y,
            (float) segment.x,
            (float) segment.y
        ));
        if (!MathHelper.clipLine(ls, viewport)) {
            return;
        }

        MathHelper.LineSegment segment1 = ls.get();

        GL11.glLineWidth(LINE_WIDTH * 2);
        GLBuilder.getBuilder().begin(GL11.GL_LINES)
            .color(0xFF000000)
            .vertex(segment1.startX(), segment1.startY())
            .vertex(segment1.endX(), segment1.endY())
            .end();

    }


}
