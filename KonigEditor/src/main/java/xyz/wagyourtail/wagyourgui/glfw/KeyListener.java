package xyz.wagyourtail.wagyourgui.glfw;

public interface KeyListener {

    void onKey(int key, int scancode, int action, int mods);
    void onChar(int codepoint);

}
