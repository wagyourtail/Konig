package xyz.wagyourtail.wagyourgui.elements;

import org.lwjgl.opengl.GL11;
import xyz.wagyourtail.wagyourgui.Font;

import java.util.function.Consumer;
import java.util.regex.Pattern;

import static org.lwjgl.glfw.GLFW.*;

public class TextInput extends Button {
    public Consumer<String> onChange;
    public Pattern regex = Pattern.compile("^.*$");
    public String content;

    protected int selColor;

    protected float selStart;
    public int selStartIndex;

    protected float selEnd;
    public int selEndIndex;

    protected int arrowCursor;
    protected float font_scale = .8f;
    protected boolean focused = false;

    public TextInput(
        float x,
        float y,
        float width,
        float height,
        Font textRenderer,
        int color,
        int borderColor,
        int hilightColor,
        int textColor,
        String message,
        Consumer<Button> onClick,
        Consumer<String> onChange
    ) {
        super(
            x,
            y,
            width,
            height,
            textRenderer,
            "",
            color,
            borderColor,
            textColor,
            borderColor,
            onClick
        );

        this.selColor = hilightColor;
        this.content = message;
        this.onChange = onChange;

        updateSelStart(content.length());
        updateSelEnd(content.length());
        arrowCursor = content.length();
    }

    public void setMessage(String message) {
        this.content = message;
    }

    public void updateSelStart(int startIndex) {
        selStartIndex = startIndex;
        if (startIndex == 0) { selStart = x + 1; }
        else { selStart = x + 2 + font.getWidth(content.substring(0, startIndex)) * font_scale; }
    }

    public void updateSelEnd(int endIndex) {
        selEndIndex = endIndex;
        if (endIndex == 0) { selEnd = x + 2; }
        else { selEnd = x + 3 + font.getWidth(content.substring(0, endIndex)) * font_scale; }
    }

    @Override
    public boolean onClick(float mouseX, float mouseY, int btn) {
        if (focused) {
            int pos = font.trimToWidth(content, mouseX - x - 2).length();
            updateSelStart(pos);
            updateSelEnd(pos);
            arrowCursor = pos;
        }
        return super.onClick(mouseX, mouseY, btn);
    }

    @Override
    public boolean onDrag(float x, float y, float dx, float dy, int button) {
        if (focused) {
            int pos = font.trimToWidth(content, x - this.x - 2).length();
            updateSelEnd(pos);
            arrowCursor = pos;
        }
        return true;
    }

    protected void swapStartEnd() {
        int temp1 = selStartIndex;
        updateSelStart(selEndIndex);
        updateSelEnd(temp1);
    }

    @Override
    public boolean onKey(int key, int scancode, int action, int mods) {
        boolean ctrl_pressed = (mods & GLFW_MOD_CONTROL) != 0;
        if (this.focused && action == GLFW_PRESS) {
            if (selEndIndex < selStartIndex) swapStartEnd();
            if (ctrl_pressed && key == GLFW_KEY_A) {
                updateSelStart(0);
                updateSelEnd(content.length());
            } else if (ctrl_pressed && key == GLFW_KEY_C) {
                //TODO
            } else if (ctrl_pressed && key == GLFW_KEY_X) {
                //TODO
            } else if (ctrl_pressed && key == GLFW_KEY_V) {
                //TODO
            }
            switch (key) {
                case GLFW_KEY_BACKSPACE:
                    if (selStartIndex == selEndIndex && selStartIndex > 0) updateSelStart(selStartIndex - 1);
                    content = content.substring(0, selStartIndex) +
                        (selEndIndex >= content.length() ? "" : content.substring(selEndIndex));
                    onChange.accept(content);
                    updateSelEnd(selStartIndex);
                    arrowCursor = selStartIndex;
                    break;
                case GLFW_KEY_DELETE:
                    if (selStartIndex == selEndIndex && selEndIndex < content.length()) updateSelEnd(selEndIndex + 1);
                    content = content.substring(0, selStartIndex) + content.substring(selEndIndex);
                    onChange.accept(content);
                    updateSelEnd(selStartIndex);
                    arrowCursor = selStartIndex;
                    break;
                case GLFW_KEY_HOME:
                    updateSelStart(0);
                    updateSelEnd(selStartIndex);
                    arrowCursor = selStartIndex;
                    break;
                case GLFW_KEY_END:
                    updateSelStart(content.length());
                    updateSelEnd(selStartIndex);
                    arrowCursor = selStartIndex;
                    break;
                case GLFW_KEY_LEFT:
                    if (arrowCursor > 0) {
                        if (arrowCursor < selEndIndex) {
                            updateSelStart(--arrowCursor);
                            if (!ctrl_pressed) updateSelEnd(selStartIndex);
                        } else if (arrowCursor >= selEndIndex) {
                            updateSelEnd(--arrowCursor);
                            if (!ctrl_pressed) updateSelStart(selEndIndex);
                        }
                    }
                    break;
                case GLFW_KEY_RIGHT:
                    if (arrowCursor < content.length()) {
                        if (arrowCursor < selEndIndex) {
                            updateSelStart(++arrowCursor);
                            if (!ctrl_pressed) updateSelEnd(selStartIndex);
                        } else {
                            updateSelEnd(++arrowCursor);
                            if (!ctrl_pressed) updateSelStart(selEndIndex);
                        }
                    }
                default:
                    break;
            }
        }
        return true;
    }

    @Override
    public boolean onChar(int codepoint) {
        if (selEndIndex < selStartIndex) swapStartEnd();
        String newContent;
        if (selEndIndex >= content.length()) {
            newContent = content.substring(0, selStartIndex) + (char) codepoint;
        } else {
            newContent = content.substring(0, selStartIndex) + (char) codepoint + content.substring(selEndIndex);
        }
        if (regex.matcher(newContent).matches()) {
            content = newContent;
            onChange.accept(content);
            updateSelStart(selStartIndex + 1);
            arrowCursor = selStartIndex;
            updateSelEnd(arrowCursor);
        }
        return true;
    }

    @Override
    public void onFocus(BaseElement prevFocus) {
        focused = true;
    }

    @Override
    public void onFocusLost(BaseElement nextFocus) {
        focused = false;
    }

    @Override
    public void onRender(float mouseX, float mouseY) {
        boolean hover = mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;

        // Draw background
        DrawableHelper.rect(x, y, x + width, y + height, hover ? hoverColor : color);

        // Draw border
        DrawableHelper.rect(x, y, x + 1, y + height, borderColor);
        DrawableHelper.rect(x + width - 1, y, x + width, y + height, borderColor);
        DrawableHelper.rect(x + 1, y, x + width - 1, y + 1, borderColor);
        DrawableHelper.rect(x + 1, y + height - 1, x + width - 1, y + height, borderColor);

        // Draw text
        float scaled_font_height = font.FONT_HEIGHT * font_scale;
        DrawableHelper.rect(
            selStart,
            height > scaled_font_height ? y + 2 : y,
            Math.min(selEnd, x + width - 2),
            (height > scaled_font_height ? y + 2 : y) + scaled_font_height + 4,
            selColor
        );
        // push a matrix
        GL11.glPushMatrix();
        // translate to the right spot and scale
        GL11.glTranslatef(x + 4, y + (height > scaled_font_height ? 2 : 0), 0);
        GL11.glScalef(font_scale, font_scale, 1);
        // draw the text
        DrawableHelper.drawTrimmedString(font, content, 0, 0, (width - 4) / font_scale, textColor);
        // pop the matrix
        GL11.glPopMatrix();
    }

}
