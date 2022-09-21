package xyz.wagyourtail.wagyourgui.elements;

public abstract class BaseElement implements DrawableHelper {
    private boolean focused = false;

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

    public boolean isMouseOver(float x, float y) { return false; }

    public void onHover(float x, float y) {}

    public void onHoverLost() {}

    public boolean shouldFocus(float mouseX, float mouseY) {
        return false;
    }

    public void onFocus(BaseElement prevFocus) {
        focused = true;
    }

    public void onFocusLost(BaseElement nextFocus) {
        focused = false;
    }

    public boolean isFocused() {
        return focused;
    }

    public abstract void onRender(float mouseX, float mouseY);
}
