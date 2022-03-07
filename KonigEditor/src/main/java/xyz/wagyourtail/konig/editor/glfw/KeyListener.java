package xyz.wagyourtail.konig.editor.glfw;

public interface KeyListener {

    void onKey(int key, int scancode, int action, int mods);
    void onChar(int codepoint);

}
