package xyz.wagyourtail.wagyourgui.elements;

import xyz.wagyourtail.wagyourgui.Font;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class Selector extends ElementContainer {
    protected final Button dropDown;
    protected final Font font;
    protected final Consumer<Selector> onSelect;

    protected String selected;
    protected final List<String> options;
    public float x;
    public float y;
    public float width;
    public float height;

    public float buttonHeight;

    public Selector(float x, float y, float w, float h, float bh, Font font, String text, int color, int hoverColor, int textColor, int hoverTextColor, Consumer<Selector> onSelect) {
        this.x = x;
        this.y = y;
        this.width = w;
        this.height = h;
        this.buttonHeight = bh;
        dropDown = new Button(x, y, w, bh, font, text, color, hoverColor, textColor, hoverTextColor, 0, (btn) -> openDropDown());
        elements.add(dropDown);
        selected = text;
        this.font = font;
        this.onSelect = onSelect;
        options = new ArrayList<>();
    }

    public Selector(float x, float y, float w, float h, float bh, Font font, String text, int color, int hoverColor, int textColor, Consumer<Selector> onSelect) {
        this(x, y, w, h, bh, font, text, color, hoverColor, textColor, textColor, onSelect);
    }

    public void setOptions(List<String> options) {
        this.options.clear();
        this.options.addAll(options);
    }

    public void addOption(String option) {
        options.add(option);
    }

    public void removeOption(String option) {
        options.remove(option);
    }

    public void openDropDown() {
        int i = 0;
        for (String option : options) {
            if (option.equals(selected)) {
                continue;
            }
            ++i;
            // TODO: add scrollbar if longer than height
            Button btn = new Button(x, y + i * buttonHeight, width, buttonHeight, font, option, dropDown.color, dropDown.hoverColor, dropDown.textColor, dropDown.hoverTextColor, dropDown.borderColor, (btn2) -> {
                selected = option;
                dropDown.setText(option);
                onSelect.accept(this);
                closeDropDown();
            });
            elements.add(btn);
        }
    }

    @Override
    public void onFocusLost(BaseElement nextFocus) {
        super.onFocusLost(nextFocus);
        closeDropDown();
    }

    public void closeDropDown() {
        elements.removeIf((btn) -> btn != dropDown);
    }

    public String getSelected() {
        return selected;
    }

    @Override
    public void onRender(float mouseX, float mouseY) {
        super.onRender(mouseX, mouseY);
    }

}
