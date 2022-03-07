package xyz.wagyourtail.konig.editor.glfw;

public interface MouseListener {

    void onMouseButton(int button, int action, int mods);

    void onScroll(double dx, double dy);

}
