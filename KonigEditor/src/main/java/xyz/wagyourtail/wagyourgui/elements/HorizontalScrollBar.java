package xyz.wagyourtail.wagyourgui.elements;

import java.util.function.Consumer;

public class HorizontalScrollBar extends BaseElement {
    protected float x;
    protected float y;
    protected float width;
    protected float height;
    protected float scroll;
    protected float scrollPages;

    Consumer<Float> onChange;

    public HorizontalScrollBar(float x, float y, float width, float height, float scroll, float scrollMax, Consumer<Float> onChange) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.scroll = scroll;
        this.scrollPages = scrollMax;
        this.onChange = onChange;
    }

    public void setScroll(float scroll) {
        this.scroll = scroll;
    }

    public void setScrollPages(float pages) {
        this.scrollPages = pages;
    }

    public float getScroll() {
        return scroll;
    }

    public float getScrollbarWidth() {
        return width / scrollPages;
    }

    @Override
    public boolean shouldFocus(float mouseX, float mouseY) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }

    @Override
    public boolean onDrag(float x, float y, float dx, float dy, int button) {
        // move scrollbar scroll to click position
        float scrollbarWidth = getScrollbarWidth() / 2;
        if (x - scrollbarWidth < this.x) {
            scroll = 0;
        } else if (x + scrollbarWidth > this.x + width) {
            scroll = scrollPages - 1;
        } else {
            scroll = (x - this.x - scrollbarWidth) / width * scrollPages;
        }
        if (onChange != null) {
            onChange.accept(scroll);
        }
        return true;
    }

    @Override
    public void onRender(float mouseX, float mouseY) {
        DrawableHelper.rect(x, y, x + width, y + height, 0xFF000000);
        DrawableHelper.rect(x + scroll * width / scrollPages, y, x + (scroll + 1) * width / scrollPages, y + height, 0xFFFFFFFF);
    }

}
