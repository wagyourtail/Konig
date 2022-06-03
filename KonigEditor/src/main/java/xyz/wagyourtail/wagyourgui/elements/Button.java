package xyz.wagyourtail.wagyourgui.elements;

import xyz.wagyourtail.wagyourgui.Font;

import java.util.function.Consumer;

public class Button extends BaseElement {
    protected float x;
    protected float y;
    protected float width;
    protected float height;
    protected final Font font;
    protected String text;
    protected int color;
    protected int hoverColor;
    protected int textColor;
    protected int hoverTextColor;
    protected int borderColor;
    protected Consumer<Button> onClick;

    public Button(float x, float y, float w, float h, Font font, String text, int color, int hoverColor, int textColor, int hoverTextColor, int borderColor, Consumer<Button> onClick) {
        this.x = x;
        this.y = y;
        this.width = w;
        this.height = h;
        this.font = font;
        this.text = text;
        this.color = color;
        this.hoverColor = hoverColor;
        this.textColor = textColor;
        this.hoverTextColor = hoverTextColor;
        this.borderColor = borderColor;
        this.onClick = onClick;
    }

    public Button(float x, float y, float w, float h, Font font, String text, int color, int hoverColor, int textColor, int borderColor, Consumer<Button> onClick) {
        this(x, y, w, h, font, text, color, hoverColor, textColor, textColor, borderColor, onClick);
    }

    public void setOnClick(Consumer<Button> onClick) {
        this.onClick = onClick;
    }

    public void setPos(float x, float y, float w, float h) {
        this.x = x;
        this.y = y;
        this.width = w;
        this.height = h;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public void setHoverColor(int hoverColor) {
        this.hoverColor = hoverColor;
    }

    public void setTextColor(int textColor) {
        this.textColor = textColor;
    }

    public void setHoverTextColor(int hoverTextColor) {
        this.hoverTextColor = hoverTextColor;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public float getWidth() {
        return width;
    }

    public float getHeight() {
        return height;
    }

    public String getText() {
        return text;
    }

    public int getColor() {
        return color;
    }

    public int getHoverColor() {
        return hoverColor;
    }

    public int getTextColor() {
        return textColor;
    }

    public int getHoverTextColor() {
        return hoverTextColor;
    }

    public Consumer<Button> getOnClick() {
        return onClick;
    }

    public int getBorderColor() {
        return borderColor;
    }

    @Override
    public boolean shouldFocus(float mouseX, float mouseY) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }

    @Override
    public boolean onClick(float x, float y, int button) {
        onClick.accept(this);
        return true;
    }

    @Override
    public void onRender(float mouseX, float mouseY) {
        boolean hover = mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;

        DrawableHelper.rect(x, y, x + width, y + height, hover ? hoverColor : color);

        DrawableHelper.rect(x, y, x + 1, y + height, borderColor);
        DrawableHelper.rect(x + width - 1, y, x + width, y + height, borderColor);
        DrawableHelper.rect(x + 1, y, x + width - 1, y + 1, borderColor);
        DrawableHelper.rect(x + 1, y + height - 1, x + width - 1 , y + height, borderColor);

        float w = font.getWidth(text);
        if (w < width - 4) {
            DrawableHelper.drawCenteredString(font, text, x + width / 2, y + height / 2 - font.FONT_HEIGHT / 2f, hover ? hoverTextColor : textColor);
        } else {
            DrawableHelper.drawTrimmedString(font, text, x + 2, y + height / 2 - font.FONT_HEIGHT / 2f, width - 4, hover ? hoverTextColor : textColor);
        }



    }

}
