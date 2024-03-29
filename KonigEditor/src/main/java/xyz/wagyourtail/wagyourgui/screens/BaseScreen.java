package xyz.wagyourtail.wagyourgui.screens;

import xyz.wagyourtail.wagyourgui.elements.BaseElement;
import xyz.wagyourtail.wagyourgui.glfw.GLFWSession;
import xyz.wagyourtail.wagyourgui.glfw.Window;

import java.util.*;

public abstract class BaseScreen extends BaseElement {
    public final GLFWSession session;
    public final Deque<BaseElement> elements = new ArrayDeque<>();
    public BaseElement focusedElement = null;
    public BaseElement hoveredElement = null;

    public BaseScreen(GLFWSession session) {
        this.session = session;
    }

    public <T extends BaseElement> T addElement(T element) {
        elements.add(element);
        return element;
    }

    public void onWindowResize(Window window) {
        elements.clear();
        init(window);
    }

    private final float[] sX = new float[6];
    private final float[] sY = new float[6];

    public void onMouseDrag(float x, float y, int button) {
        if (button < 6) {
            onDrag(x, y, x - sX[button], y - sY[button], button);
            sX[button] = x;
            sY[button] = y;
        }
    }

    public void onMousePos(float x, float y) {
        for (BaseElement element : elements) {
            if (element.isMouseOver(x, y)) {
                if (hoveredElement != null && hoveredElement != element) hoveredElement.onHoverLost();
                element.onHover(x, y);
                hoveredElement = element;
                return;
            }
        }
        if (hoveredElement != null) hoveredElement.onHoverLost();
        hoveredElement = null;
    }

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
        }
    }

    @Override
    public boolean onClick(float x, float y, int button) {
        for (BaseElement element : List.copyOf(elements)) {
            if (element.shouldFocus(x, y)) {
                if (focusedElement != element) {
                    BaseElement old = focusedElement;
                    focusedElement = element;
                    if (old != null) {
                        old.onFocusLost(element);
                    }
                    focusedElement.onFocus(old);
                }
                break;
            }
        }
        if (focusedElement != null && !focusedElement.shouldFocus(x, y)) {
            BaseElement old = focusedElement;
            focusedElement = null;
            old.onFocusLost(this);
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
        Iterator<BaseElement> it = elements.descendingIterator();
        while (it.hasNext()) {
            BaseElement e = it.next();
            e.onRender(mouseX, mouseY);
        }
    }

    public abstract void init(Window window);

}
