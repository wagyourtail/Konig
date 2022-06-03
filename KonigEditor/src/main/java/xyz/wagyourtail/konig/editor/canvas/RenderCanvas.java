package xyz.wagyourtail.konig.editor.canvas;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;
import xyz.wagyourtail.MathHelper;
import xyz.wagyourtail.konig.structure.code.Code;
import xyz.wagyourtail.konig.structure.code.KonigBlockReference;
import xyz.wagyourtail.konig.structure.code.Wire;
import xyz.wagyourtail.wagyourgui.Font;
import xyz.wagyourtail.wagyourgui.elements.BaseElement;
import xyz.wagyourtail.wagyourgui.elements.DrawableHelper;

public class RenderCanvas extends BaseElement {
    protected float x;
    protected float y;
    protected float width;
    protected float height;

    protected float viewX;
    protected float viewY;
    protected float viewWidth;
    protected float viewHeight;

    protected final Code code;

    protected final Font font;


    public RenderCanvas(int x, int y, int width, int height, Code code, Font font) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;

        this.viewX = -1;
        this.viewY = -1;

        // scale view to ratio of width and height
        float ratio = (float) width / (float) height;
        if (ratio > 1) {
            this.viewWidth = 10;
            this.viewHeight = 10 / ratio;
        } else {
            this.viewWidth = 10 * ratio;
            this.viewHeight = 10;
        }

        this.code = code;
        this.font = font;
    }

    @Override
    public boolean onDrag(float x, float y, float dx, float dy, int button) {
        // scale the x and y to the view then translate the view
        if (button == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
            float scale = width / viewWidth;
            this.viewX -= dx / scale;
            this.viewY -= dy / scale;
        }
        return true;
    }

    @Override
    public boolean shouldFocus(float mouseX, float mouseY) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }

    @Override
    public void onRender(float mouseX, float mouseY) {
        // render stuff withing view from code
        // scale canvas to view
        GL11.glPushMatrix();
        GL11.glLineWidth(2.5f);

        //  render bg
        DrawableHelper.rect(x, y, x + width, y + height, 0xFFFFFFFF);

        // render border
        DrawableHelper.rect(x, y, x + 1, y + height, 0xFF000000);
        DrawableHelper.rect(x + width - 1, y, x + width, y + height, 0xFF000000);
        DrawableHelper.rect(x, y, x + width, y + 1, 0xFF000000);
        DrawableHelper.rect(x, y + height - 1, x + width, y + height, 0xFF000000);

        // render code
        GL11.glTranslatef(x, y, 0);
        GL11.glScalef(width / viewWidth, height / viewHeight, 1);
        GL11.glTranslatef(-viewX, -viewY, 0);

        float scaledMouseX = (mouseX - x) * viewWidth / width + viewX;
        float scaledMouseY = (mouseY - y) * viewHeight / height + viewY;

        // render wires
        renderWires(code, scaledMouseX, scaledMouseY);

        // render blocks
        renderBlocks(code, scaledMouseX, scaledMouseY);

        GL11.glPopMatrix();
    }

    protected void renderWires(Code code, float mouseX, float mouseY) {
        MathHelper.Rectangle viewport = new MathHelper.Rectangle(viewX, viewY, viewX + viewWidth, viewY + viewHeight);
        for (Wire wire : code.getWires()) {
            WireRenderer.render(wire, viewport, mouseX, mouseY, font);
        }
    }

    protected void renderBlocks(Code code, float mouseX, float mouseY) {
        MathHelper.Rectangle viewport = new MathHelper.Rectangle(viewX, viewY, viewX + viewWidth, viewY + viewHeight);
        for (KonigBlockReference block : code.getBlocks()) {
            BlockRendererRegistry.render(block, viewport, mouseX, mouseY, font);
        }
    }

}
