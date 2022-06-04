package xyz.wagyourtail.konig.editor.canvas;

import org.lwjgl.opengl.GL11;
import xyz.wagyourtail.konig.structure.code.Code;
import xyz.wagyourtail.wagyourgui.Font;
import xyz.wagyourtail.wagyourgui.elements.DrawableHelper;
import xyz.wagyourtail.wagyourgui.elements.ElementContainer;

import java.util.ArrayList;
import java.util.List;

public class RenderCode extends ElementContainer {

    protected final Code code;
    protected final Font font;

    protected final float x;
    protected final float y;
    protected final float width;
    protected final float height;


    protected final float viewportX;
    protected final float viewportY;
    protected final float viewportWidth;
    protected final float viewportHeight;



    public final List<RenderWire> compileWires = new ArrayList<>();
    public final List<RenderBlock> compileBlocks = new ArrayList<>();

    public RenderCode(int x, int y, int width, int height, Code code, Font font) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;

        this.code = code;
        this.font = font;


        this.viewportX = -1;
        this.viewportY = -1;

        // scale view to ratio of width and height
        float ratio = (float) width / (float) height;
        if (ratio > 1) {
            this.viewportWidth = 10;
            this.viewportHeight = 10 / ratio;
        } else {
            this.viewportWidth = 10 * ratio;
            this.viewportHeight = 10;
        }
        compileCode();
    }

    @Override
    public boolean onClick(float x, float y, int button) {
        float scaledMouseX = (x - this.x) * viewportWidth / width + viewportX;
        float scaledMouseY = (y - this.y) * viewportHeight / height + viewportY;
        return super.onClick(scaledMouseX, scaledMouseY, button);
    }

    @Override
    public boolean onDrag(float x, float y, float dx, float dy, int button) {
        float scaledMouseX = (x - this.x) * viewportWidth / width + viewportX;
        float scaledMouseY = (y - this.y) * viewportHeight / height + viewportY;
        float scaledMouseDX = dx * viewportWidth / width;
        float scaledMouseDY = dy * viewportHeight / height;
        return super.onDrag(scaledMouseX, scaledMouseY, scaledMouseDX, scaledMouseDY, button);
    }

    @Override
    public boolean onRelease(float x, float y, int button) {
        float scaledMouseX = (x - this.x) * viewportWidth / width + viewportX;
        float scaledMouseY = (y - this.y) * viewportHeight / height + viewportY;
        return super.onRelease(scaledMouseX, scaledMouseY, button);
    }

    @Override
    public boolean onScroll(float x, float y, float dx, float dy) {
        float scaledMouseX = (x - this.x) * viewportWidth / width + viewportX;
        float scaledMouseY = (y - this.y) * viewportHeight / height + viewportY;
        return super.onScroll(scaledMouseX, scaledMouseY, dx, dy);
    }

    @Override
    public boolean shouldFocus(float mouseX, float mouseY) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }

    public void compileCode() {
        compileWires.clear();
        compileBlocks.clear();
        elements.clear();
        compileWires.addAll(RenderWire.compile(code.getWires(), font, this));
        compileBlocks.addAll(RenderBlock.compile(code.getBlocks(), font, this));
        elements.addAll(compileWires);
        elements.addAll(compileBlocks);
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
        GL11.glScalef(width / viewportWidth, height / viewportHeight, 1);
        GL11.glTranslatef(-viewportX, -viewportY, 0);

        float scaledMouseX = (mouseX - x) * viewportWidth / width + viewportX;
        float scaledMouseY = (mouseY - y) * viewportHeight / height + viewportY;

        super.onRender(scaledMouseX, scaledMouseY);

        GL11.glPopMatrix();
    }

}
