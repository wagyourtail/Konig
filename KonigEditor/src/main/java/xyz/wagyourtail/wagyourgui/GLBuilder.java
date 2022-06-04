package xyz.wagyourtail.wagyourgui;

import org.lwjgl.opengl.GL11;

public class GLBuilder {
    public static GLBuilder builder = new GLBuilder();
    public boolean state;

    private GLBuilder() {
        state = false;
    }

    public GLBuilder begin(int mode) {
        if (state) throw new IllegalStateException("already began building");
        GL11.glBegin(mode);
        state = true;
        return this;
    }

    public GLBuilder vertex(float x, float y) {
        if (!state) throw new IllegalStateException("not building");
        GL11.glVertex2f(x, y);
        return this;
    }

    public GLBuilder uv(float u, float v) {
        if (!state) throw new IllegalStateException("not building");
        GL11.glTexCoord2f(u, v);
        return this;
    }

    public GLBuilder uv(float u, float v, float w, float h) {
        if (!state) throw new IllegalStateException("not building");
        GL11.glTexCoord2f(u / w, v / h);
        return this;
    }

    public GLBuilder color(int r, int g, int b, int a) {
        GL11.glColor4f(r/255f, g/255f, b/255f, a/255f);
        return this;
    }

    public GLBuilder color(float r, float g, float b, float a) {
        GL11.glColor4f(r, g, b, a);
        return this;
    }

    public GLBuilder color(int rgb, float a) {
        int r = (rgb >> 16) & 255;
        int g = (rgb >> 8) & 255;
        int b = rgb & 255;

        GL11.glColor4f(r / 255f, g / 255f, b / 255f, a);
        return this;
    }

    public GLBuilder color(int argb) {
        int a = (argb >> 24) & 255;
        int r = (argb >> 16) & 255;
        int g = (argb >> 8) & 255;
        int b = argb & 255;

        GL11.glColor4f(r / 255f, g / 255f, b / 255f, a / 255f);
        return this;
    }

    public void end() {
        if (!state) throw new IllegalStateException("not building");
        GL11.glEnd();
        state = false;
    }

    public static GLBuilder getBuilder() {
        return builder;
    }
}