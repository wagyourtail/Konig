package xyz.wagyourtail.wagyourgui.elements;

import org.lwjgl.opengl.GL11;
import xyz.wagyourtail.wagyourgui.Font;
import xyz.wagyourtail.wagyourgui.GLBuilder;

public interface DrawableHelper {
    GLBuilder builder = GLBuilder.getBuilder();


    static void rect(float x1, float y1, float x2, float y2, int color) {
        if (x1 > x2) {
            float temp = x1;
            x1 = x2;
            x2 = temp;
        }

        if (y1 > y2) {
            float temp = y1;
            y1 = y2;
            y2 = temp;
        }
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_COLOR);

        builder.begin(GL11.GL_TRIANGLE_STRIP)
            .color(color)
            .vertex(x1, y1)
            .vertex(x2, y1)
            .vertex(x1, y2)
            .vertex(x2, y2)
            .end();
    }

    static void rect(int x1, int y1, int x2, int y2, int color) {
        rect((float)x1, (float)y1, (float)x2, (float)y2, color);
    }

    static void drawCenteredString(Font font, String text, float x, float y, int color) {
        builder.color(color);
        float width = font.getWidth(text);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        font.drawString(text, x - width / 2, y);
    }

    static void drawString(Font font, String text, float x, float y, int color) {
        builder.color(color);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        font.drawString(text, x, y);
    }

    static void drawTrimmedString(Font font, String text, float x, float y, float width, int color) {
        builder.color(color);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        font.drawTrimmed(text, x, y, width);
    }
}
