package xyz.wagyourtail.konig.editor.canvas.blocks;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;
import xyz.wagyourtail.MathHelper;
import xyz.wagyourtail.konig.editor.canvas.RenderBlock;
import xyz.wagyourtail.konig.editor.canvas.RenderBlockParent;
import xyz.wagyourtail.konig.editor.canvas.RenderCode;
import xyz.wagyourtail.konig.editor.canvas.RenderWire;
import xyz.wagyourtail.konig.structure.code.KonigBlockReference;
import xyz.wagyourtail.wagyourgui.Font;
import xyz.wagyourtail.wagyourgui.GLBuilder;
import xyz.wagyourtail.wagyourgui.elements.BaseElement;
import xyz.wagyourtail.wagyourgui.elements.DrawableHelper;

public class RenderConstBlock extends RenderBlock {
    public static final float FONT_SCALE = .1f;

    public boolean hilighted = false;

    public RenderConstBlock(KonigBlockReference block, Font font, RenderBlockParent code) {
        super(block, font, code);
    }

    @Override
    public void onFocus(BaseElement prevFocus) {
        super.onFocus(prevFocus);
        hilighted = true;
    }

    @Override
    public void onFocusLost(BaseElement nextFocus) {
        super.onFocusLost(nextFocus);
        hilighted = false;
    }

    @Override
    public boolean onChar(int key) {
        if (hilighted) block.value = "" + (char) key;
        else block.value += (char) key;
        hilighted = false;
        return true;
    }

    @Override
    public boolean onKey(int keycode, int scancode, int action, int mods) {
        if (action != 1) return false;
        switch (keycode) {
            case GLFW.GLFW_KEY_BACKSPACE:
                if (hilighted) {
                    block.value = "";
                } else if (block.value.length() > 0) {
                    block.value = block.value.substring(0, block.value.length() - 1);
                }
            default:
        }
        return super.onKey(keycode, scancode, action, mods);
    }

    @Override
    public void onRender(float mouseX, float mouseY) {

        MathHelper.Rectangle rect = clip();

        if (rect == null) {
            return;
        }

        // render background
        DrawableHelper.rect(
            rect.x1(),
            rect.y1(),
            rect.x2(),
            rect.y2(),
            0xFF777777
        );

        float centerY = rect.y1() + (rect.y2() - rect.y1()) / 2;

        // render textbox
        DrawableHelper.rect(
            rect.x1() + FONT_SCALE / 2,
            centerY - FONT_SCALE,
            rect.x2() - FONT_SCALE / 2,
            centerY + FONT_SCALE,
            0xFFFFFFFF
        );

        // render text
        DrawableHelper.drawTrimmedStringAtScale(
            font,
            block.value,
            rect.x1() + FONT_SCALE,
            rect.y1() + (rect.y2() - rect.y1()) / 2,
            rect.x2() - rect.x1() - FONT_SCALE * 2,
            FONT_SCALE,
            0xFF000000
        );

        // render hilight
        if (hilighted) {
            DrawableHelper.rect(
                rect.x1() + FONT_SCALE,
                rect.y1() + (rect.y2() - rect.y1()) / 2 - FONT_SCALE / 2,
                rect.x1() + FONT_SCALE + Math.min(DrawableHelper.getScaledWidth(font, block.value, FONT_SCALE), rect.x2() - rect.x1() - FONT_SCALE * 2),
                rect.y1() + (rect.y2() - rect.y1()) / 2 + FONT_SCALE / 2,
                0x4F00FFFF
            );
        }



        if (isFocused()) {
            GL11.glLineWidth(code.getWireWidth() * 2);
            GLBuilder.getBuilder().begin(GL11.GL_LINE_STRIP)
                .color(0xFF00FFFF)
                .vertex(rect.x1(), rect.y1())
                .vertex(rect.x2(), rect.y1())
                .vertex(rect.x2(), rect.y2())
                .vertex(rect.x1(), rect.y2())
                .vertex(rect.x1(), rect.y1())
                .end();
        }
//
//        // real font height to height
//        GL11.glPushMatrix();
//        // translate to textbox centered in box
//        GL11.glTranslatef(rect.x1() + FONT_SCALE, rect.y1() + (rect.y2() - rect.y1()) / 2 + FONT_SCALE / 2, 0);
//        // scale to font height
//        GL11.glScalef(1 / (font.FONT_HEIGHT / FONT_SCALE), 1 / (font.FONT_HEIGHT / FONT_SCALE), 1);
//        // render text
//        // scale width to match font scaling
//        float width = (rect.x2() - rect.x1() - FONT_SCALE * 2) * (font.FONT_HEIGHT / FONT_SCALE);
//        DrawableHelper.drawTrimmedString(font, block.value, 0, 0, width,  0xFF000000);
//        GL11.glPopMatrix();
        renderSubElements(mouseX, mouseY);
    }

}
