package xyz.wagyourtail.wagyourgui.elements;

public abstract class BaseElement implements DrawableHelper {
    public boolean onClick(float x, float y, int button) {
        return false;
    }

    public boolean onDrag(float x, float y, float dx, float dy, int button) {
        return false;
    }

    public boolean onRelease(float x, float y, int button) {
        return false;
    }

    public boolean onScroll(float x, float y, float dx, float dy) {
        return false;
    }

    public boolean onChar(int key) {
        return false;
    }

    public boolean onKey(int keycode, int scancode, int action, int mods) {
        return false;
    }

    public boolean shouldFocus(float mouseX, float mouseY) {
        return false;
    }

    public void onFocus() {}

    public void onFocusLost() {}

    public abstract void onRender(float mouseX, float mouseY);
}
