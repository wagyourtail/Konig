package xyz.wagyourtail.konig.editor.screens;

import xyz.wagyourtail.konig.editor.elements.BaseElement;
import xyz.wagyourtail.konig.editor.glfw.GLFWSession;
import xyz.wagyourtail.konig.editor.glfw.Window;

import java.util.LinkedHashSet;
import java.util.Set;

public abstract class BaseScreen extends BaseElement {
    public final GLFWSession session;
    public final Set<BaseElement> elements = new LinkedHashSet<>();
    public BaseElement focusedElement = null;

    public BaseScreen(GLFWSession session) {
        this.session = session;
    }

    public void onWindowResize(Window window) {
        elements.clear();
        init(window);
    }

    private final float[] sX = new float[6];
    private final float[] sY = new float[6];

    public void onMouseButton(float x, float y, int button, int action, int mods) {
        switch (action) {
            case 0:
                onRelease(x, y, button);
                break;
            case 1:
                onClick(x, y, button);
                if (button < 6) {
                    sX[button] = x;
                    sY[button] = y;
                }
                break;
            case 2:
                if (button < 6) {
                    onDrag(x, y, x - sX[button], y - sY[button], button);
                    sX[button] = x;
                    sY[button] = y;
                }
                break;
        }
    }

    @Override
    public boolean onClick(float x, float y, int button) {
        for (BaseElement e : elements) {
            if (e.shouldFocus(x, y)) {
                BaseElement old = focusedElement;
                focusedElement = e;
                if (old != null) {
                    old.onFocusLost();
                }
                focusedElement.onFocus();
            }
        }
        if (focusedElement != null) {
            return focusedElement.onClick(x, y, button);
        }
        return false;
    }

    @Override
    public boolean onScroll(float x, float y, float dx, float dy) {
        if (focusedElement != null) {
            return focusedElement.onScroll(x, y, dx, dy);
        }
        return false;
    }

    @Override
    public boolean onDrag(float x, float y, float dx, float dy, int button) {
        if (focusedElement != null) {
            return focusedElement.onDrag(x, y, dx, dy, button);
        }
        return false;
    }

    @Override
    public boolean onChar(int key) {
        if (focusedElement != null) {
            return focusedElement.onChar(key);
        }
        return false;
    }

    @Override
    public boolean onRelease(float x, float y, int button) {
        if (focusedElement != null) {
            return focusedElement.onRelease(x, y, button);
        }
        return false;
    }

    @Override
    public boolean onKey(int keycode, int scancode, int action, int mods) {
        if (focusedElement != null) {
            return focusedElement.onKey(keycode, scancode, action, mods);
        }
        return false;
    }

    @Override
    public void onRender(float mouseX, float mouseY) {
        for (BaseElement e : elements) {
            e.onRender(mouseX, mouseY);
        }
    }

    public abstract void init(Window window);
}
