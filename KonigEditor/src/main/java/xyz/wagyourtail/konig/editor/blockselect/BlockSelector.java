package xyz.wagyourtail.konig.editor.blockselect;

import org.lwjgl.opengl.GL11;
import xyz.wagyourtail.konig.Konig;
import xyz.wagyourtail.konig.editor.canvas.RenderBlock;
import xyz.wagyourtail.konig.editor.canvas.RenderBlockParent;
import xyz.wagyourtail.konig.editor.canvas.RenderWire;
import xyz.wagyourtail.konig.structure.code.Code;
import xyz.wagyourtail.konig.structure.code.KonigBlockReference;
import xyz.wagyourtail.konig.structure.headers.KonigBlock;
import xyz.wagyourtail.wagyourgui.Font;
import xyz.wagyourtail.wagyourgui.elements.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class BlockSelector extends ElementContainer implements RenderBlockParent {
    private final Font font;

    private float x;
    private float y;
    private float width;
    private float height;

    private float viewportX = 0;
    private final float viewportY = -.5f;
    private final float viewportWidth;
    private final float viewportHeight = 2;

    private HorizontalScrollBar scrollBar;

    private final Map<String, Map<String, KonigBlock>> blockMap = new HashMap<>();

    private Consumer<KonigBlockReference> onSelect;

    public BlockSelector(float x, float y, float width, float height, Font font, Consumer<KonigBlockReference> onSelect) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.viewportWidth = width / height * 2;
        this.height = height;
        this.onSelect = onSelect;
        this.font = font;
        init();
    }

    public void setScroll(float scroll) {
        this.viewportX = scroll * viewportWidth;
    }

    @Override
    public boolean shouldFocus(float mouseX, float mouseY) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }

    private void init() {
        // add scrollbar at bottom
        elements.add(scrollBar = new HorizontalScrollBar(x, y + height - 20, width, 20, 0, 1, this::setScroll));

        // make block tree
        blockMap.putAll(Konig.intern.get("stdlib").getBlocks());

        // add tabs
        int x = 0;
        for (String category : blockMap.keySet()) {
            elements.add(new Button(x, y, font.getWidth(category) + 4, font.FONT_HEIGHT + 12, font, category, 0xFF000000, 0x3F7F7F7F, 0xFFFFFFFF, 0xFF000000, b -> onTabClick(category)));
            x += font.getWidth(category) + 6;
        }

        onTabClick(blockMap.keySet().stream().findFirst().get());
    }

    public void onTabClick(String tab) {
        Set<BaseElement> toRemove = elements.stream().filter(e -> e instanceof RenderBlock).collect(Collectors.toSet());
        elements.removeAll(toRemove);
        float x = .5f;
        for (KonigBlock block : blockMap.get(tab).values()) {
            KonigBlockReference ref = new KonigBlockReference(new Code(Konig.getInternHeaders("stdlib")));
            ref.x = x;
            ref.y = 0;
            ref.scaleX = 1;
            ref.scaleY = 1;
            ref.rotation = 0;
            ref.name = block.name;
            ref.id = -1;
            elements.add(RenderBlock.compile(ref, font, this));
            x += 1.5f;
        }

        // calculate pages for scrollbar
        float pages = x / viewportWidth;
        scrollBar.setScrollPages(pages);
    }

    @Override
    public boolean onClick(float x, float y, int button) {
        float scaledMouseX = (x - this.x) * viewportWidth / width + viewportX;
        float scaledMouseY = (y - this.y) * viewportHeight / height + viewportY;
        for (BaseElement element : elements) {
            if (element instanceof RenderBlock && element.shouldFocus(scaledMouseX, scaledMouseY)) {
                if (onSelect != null) onSelect.accept(((RenderBlock) element).getBlock().copy(new Code(Konig.getInternHeaders("stdlib"))));
                if (focusedElement != null && !focusedElement.shouldFocus(x, y)) {
                    BaseElement old = focusedElement;
                    focusedElement = null;
                    old.onFocusLost(this);
                }
                return true;
            }
        }
        for (BaseElement element : elements) {
            if (!(element instanceof RenderBlock) && element.shouldFocus(x, y) && focusedElement != element) {
                BaseElement old = focusedElement;
                focusedElement = element;
                if (old != null) {
                    old.onFocusLost(element);
                }
                focusedElement.onFocus(old);
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
        if (onSelect != null) onSelect.accept(null);
        return false;
    }

    @Override
    public void onRender(float mouseX, float mouseY) {
        // render stuff withing view from code
        // scale canvas to view
        GL11.glPushMatrix();
        GL11.glLineWidth(2.5f);

        //  render bg
        DrawableHelper.rect(x, y, x + width, y + height, 0xFF7F7F7F);

        // render border
        DrawableHelper.rect(x, y, x + 1, y + height, 0xFF000000);
        DrawableHelper.rect(x + width - 1, y, x + width, y + height, 0xFF000000);
        DrawableHelper.rect(x, y, x + width, y + 2, 0xFF000000);
        DrawableHelper.rect(x, y + height - 1, x + width, y + height, 0xFF000000);

        // render code
        GL11.glTranslatef(x, y, 0);
        GL11.glScalef(width / viewportWidth, height / viewportHeight, 1);
        GL11.glTranslatef(-viewportX, -viewportY, 0);

        float scaledMouseX = (mouseX - x) * viewportWidth / width + viewportX;
        float scaledMouseY = (mouseY - y) * viewportHeight / height + viewportY;

        for (BaseElement element : elements) {
            if (element instanceof RenderBlock) {
                element.onRender(mouseX, mouseY);
            }
        }

        GL11.glPopMatrix();

        for (BaseElement element : elements) {
            if (!(element instanceof RenderBlock)) {
                element.onRender(mouseX, mouseY);
            }
        }
    }

    @Override
    public float viewportX() {
        return viewportX;
    }

    @Override
    public float viewportY() {
        return viewportY;
    }

    @Override
    public float viewportWidth() {
        return viewportWidth;
    }

    @Override
    public float viewportHeight() {
        return viewportHeight;
    }

    @Override
    public Set<RenderWire> getWires() {
        return null;
    }

    @Override
    public RenderWire addWireForPort(double x, double y, int blockid, String port) {
        return null;
    }

    @Override
    public void removeBlock(RenderBlock block) {

    }

}
