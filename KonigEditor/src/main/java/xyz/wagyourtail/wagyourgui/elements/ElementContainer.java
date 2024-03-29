package xyz.wagyourtail.wagyourgui.elements;

import java.util.*;

public abstract class ElementContainer extends BaseElement {
    public final Deque<BaseElement> elements = new ArrayDeque<>();
    public BaseElement focusedElement = null;
    public BaseElement hoveredElement = null;


    @Override
    public boolean shouldFocus(float mouseX, float mouseY) {
        for (BaseElement element : List.copyOf(elements)) {
            if (element.shouldFocus(mouseX, mouseY)) return true;
        }
        return false;
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
    public boolean onDrag(float x, float y, float dx, float dy, int button) {
        if (focusedElement != null) {
            return focusedElement.onDrag(x, y, dx, dy, button);
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
    public boolean onScroll(float x, float y, float dx, float dy) {
        if (focusedElement != null) {
            return focusedElement.onScroll(x, y, dx, dy);
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
    public boolean onKey(int keycode, int scancode, int action, int mods) {
        if (focusedElement != null) {
            return focusedElement.onKey(keycode, scancode, action, mods);
        }
        return false;
    }

    @Override
    public void onFocusLost(BaseElement nextFocus) {
        super.onFocusLost(nextFocus);
        if (focusedElement != null) {
            focusedElement.onFocusLost(nextFocus);
        }
        focusedElement = null;
    }

    @Override
    public void onRender(float mouseX, float mouseY) {
        Iterator<BaseElement> it = elements.descendingIterator();
        while (it.hasNext()) {
            it.next().onRender(mouseX, mouseY);
        }
    }

    @Override
    public boolean isMouseOver(float x, float y) {
        for (BaseElement element : List.copyOf(elements)) {
            if (element.isMouseOver(x, y)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void onHover(float x, float y) {
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

    @Override
    public void onHoverLost() {
        if (hoveredElement != null) {
            hoveredElement.onHoverLost();
            hoveredElement = null;
        }
    }

}
