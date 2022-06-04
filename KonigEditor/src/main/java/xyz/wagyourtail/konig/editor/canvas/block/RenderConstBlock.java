package xyz.wagyourtail.konig.editor.canvas.block;

import org.lwjgl.opengl.GL11;
import xyz.wagyourtail.MathHelper;
import xyz.wagyourtail.konig.editor.canvas.RenderBlock;
import xyz.wagyourtail.konig.editor.canvas.RenderCode;
import xyz.wagyourtail.konig.structure.code.KonigBlockReference;
import xyz.wagyourtail.wagyourgui.Font;
import xyz.wagyourtail.wagyourgui.elements.DrawableHelper;

public class RenderConstBlock extends RenderBlock {

    public RenderConstBlock(KonigBlockReference block, Font font, RenderCode code) {
        super(block, font, code);
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

        // render textbox
        float fontHeight = .1f;

        // real font height to height
        GL11.glPushMatrix();
        // translate to textbox centered in box
        GL11.glTranslatef(rect.x1(), rect.y1() + (rect.y2() - rect.y1()) / 2 + fontHeight / 2, 0);
        // scale to font height
        GL11.glScalef(1 / (font.FONT_HEIGHT / fontHeight), 1 / (font.FONT_HEIGHT / fontHeight), 1);
        // render text
        // scale width to match font scaling
        float width = (rect.x2() - rect.x1()) * (font.FONT_HEIGHT / fontHeight);
        DrawableHelper.drawTrimmedString(font, block.value, 0, 0, width,  0xFF000000);
        GL11.glPopMatrix();
    }

}
