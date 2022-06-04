package xyz.wagyourtail.konig.editor.canvas.blocks;

import org.lwjgl.opengl.GL11;
import xyz.wagyourtail.MathHelper;
import xyz.wagyourtail.konig.structure.code.KonigBlockReference;
import xyz.wagyourtail.wagyourgui.Font;
import xyz.wagyourtail.wagyourgui.elements.DrawableHelper;

public class ConstBlock implements BlockRenderer {

    @Override
    public void render(KonigBlockReference block, MathHelper.Rectangle clippedBlock, MathHelper.Rectangle viewport, float mouseX, float mouseY, Font font) {
        // render background
        DrawableHelper.rect(clippedBlock.x1(), clippedBlock.y1(), clippedBlock.x2(), clippedBlock.y2(), 0xFF777777);

        // draw textbox background
        float textTop = block.x + block.scaleX / 2 - .2f / 2f;
        DrawableHelper.rect(Math.max(clippedBlock.x1(), block.x + .05f), Math.max(clippedBlock.y1(), textTop), Math.min(clippedBlock.x2(), block.x + block.scaleX - .05f), Math.min(clippedBlock.y2(), textTop + .2f), 0xFFFFFFFF);

        // draw text
        GL11.glPushMatrix();
        GL11.glTranslatef(block.x + .05f, textTop + .15f, 0);
        GL11.glScalef( .1f / (font.FONT_HEIGHT), .1f / (font.FONT_HEIGHT), 1);
        float blockWidthAtScale = font.FONT_HEIGHT / .1f * (block.scaleX - .1f);

        DrawableHelper.drawTrimmedString(font, block.value, 0, 0, blockWidthAtScale, 0xFF000000);
        GL11.glPopMatrix();
    }

}
