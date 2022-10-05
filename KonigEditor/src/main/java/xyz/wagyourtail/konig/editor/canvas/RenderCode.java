package xyz.wagyourtail.konig.editor.canvas;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;
import xyz.wagyourtail.konig.structure.code.Code;
import xyz.wagyourtail.konig.structure.code.KonigBlockReference;
import xyz.wagyourtail.konig.structure.code.Wire;
import xyz.wagyourtail.wagyourgui.Font;
import xyz.wagyourtail.wagyourgui.elements.BaseElement;
import xyz.wagyourtail.wagyourgui.elements.DrawableHelper;
import xyz.wagyourtail.wagyourgui.elements.ElementContainer;
import xyz.wagyourtail.wagyourgui.glfw.Cursors;
import xyz.wagyourtail.wagyourgui.glfw.Window;

import java.io.StringReader;
import java.util.*;
import java.util.stream.Collectors;

public class RenderCode extends ElementContainer implements RenderCodeParent, RenderBlockParent {
    public static final boolean SNAP_TO_GRID = true;
    public static final float GRID_SIZE = .1f;
    public static final float SMALL_VALUE = .01f;

    protected final RenderCodeParent parent;
    protected final Code code;
    protected final Font font;
    protected final Window window;

    protected final float x;
    protected final float y;
    protected final float width;
    protected final float height;

    protected float viewportX;
    protected float viewportY;
    protected float viewportWidth;
    protected float viewportHeight;

    protected boolean allowViewportDrag = true;
    protected boolean allowViewportZoom = true;
    protected boolean renderBorder = true;

    protected final Set<RenderWire> compileWires = new HashSet<>();
    protected final Set<RenderBlock> compileBlocks = new HashSet<>();

    public RenderCode(RenderCodeParent parent, float x, float y, float width, float height, Code code, Font font, Window window) {
        this.parent = parent;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;

        this.code = code;
        this.font = font;
        this.window = window;

        this.viewportX = -1;
        this.viewportY = -1;

        // scale view to ratio of width and height
        float ratio = (float) width / (float) height;
        if (ratio > 1) {
            this.viewportWidth = 10;
            this.viewportHeight = 10 / ratio;
        } else {
            this.viewportWidth = 10 * ratio;
            this.viewportHeight = 10;
        }
        compileCode();
    }

    @Override
    public boolean isMouseOver(float x, float y) {
        if (x >= this.x && x <= this.x + this.width && y >= this.y && y <= this.y + this.height) {
            float scaledMouseX = (x - this.x) * viewportWidth / width + viewportX;
            float scaledMouseY = (y - this.y) * viewportHeight / height + viewportY;
            super.isMouseOver(scaledMouseX, scaledMouseY);
            return true;
        }
        return false;
    }

    @Override
    public void onHover(float x, float y) {
        window.setCursor(Cursors.ARROW);
        float scaledMouseX = (x - this.x) * viewportWidth / width + viewportX;
        float scaledMouseY = (y - this.y) * viewportHeight / height + viewportY;
        super.onHover(scaledMouseX, scaledMouseY);
    }

    @Override
    public boolean onClick(float x, float y, int button) {
        float scaledMouseX = (x - this.x) * viewportWidth / width + viewportX;
        float scaledMouseY = (y - this.y) * viewportHeight / height + viewportY;
        if (super.onClick(scaledMouseX, scaledMouseY, button)) return true;
        Optional<RenderBlock> placing = getPlacingBlock();
        placing.ifPresent(this::placeBlock);
        return false;
    }

    public void placeBlock(RenderBlock block) {
        if (block.getBlock().id == -1) {
            Set<Integer> set = code.getBlocks().stream().map(e -> e.id).collect(Collectors.toSet());
            int id = 0;
            while (set.contains(id)) {
                id++;
            }
            code.addBlock(block.getBlock());
            elements.addFirst(block);
            setPlacingBlock(null);
        }
    }

    private float ddX = 0;
    private float ddY = 0;

    @Override
    public boolean onDrag(float x, float y, float dx, float dy, int button) {
        float scaledMouseX = (x - this.x) * viewportWidth / width + viewportX;
        float scaledMouseY = (y - this.y) * viewportHeight / height + viewportY;
        float scaledMouseDX = dx * viewportWidth / width;
        float scaledMouseDY = dy * viewportHeight / height;
        ddX += scaledMouseDX;
        ddY += scaledMouseDY;
        if (SNAP_TO_GRID) {
            // snap to grid
            if (Math.abs(ddX) >= GRID_SIZE || Math.abs(ddY) >= GRID_SIZE) {
                float nDDx;
                float nDDy;
                if (Math.abs(ddX) >= GRID_SIZE) {
                    nDDx = (float) (Math.signum(ddX) * Math.floor(Math.abs(ddX) / GRID_SIZE) * GRID_SIZE);
                    ddX = ddX % GRID_SIZE;
                } else {
                    nDDx = 0;
                }
                if (Math.abs(ddY) >= GRID_SIZE) {
                    nDDy = (float) (Math.signum(ddY) * (Math.floor(Math.abs(ddY) / GRID_SIZE) * GRID_SIZE));
                    ddY = ddY % GRID_SIZE;
                } else {
                    nDDy = 0;
                }
                if (super.onDrag(scaledMouseX, scaledMouseY, nDDx, nDDy, button)) {
                    return true;
                }
            }
        } else if (super.onDrag(scaledMouseX, scaledMouseY, scaledMouseDX, scaledMouseDY, button)) {
            return true;
        }
        if (focusedElement == null && allowViewportDrag) {
            // drag viewport
            if (button == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
                viewportX -= scaledMouseDX;
                viewportY -= scaledMouseDY;
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean onRelease(float x, float y, int button) {
        ddX = 0;
        ddY = 0;
        float scaledMouseX = (x - this.x) * viewportWidth / width + viewportX;
        float scaledMouseY = (y - this.y) * viewportHeight / height + viewportY;
        return super.onRelease(scaledMouseX, scaledMouseY, button);
    }

    @Override
    public boolean onScroll(float x, float y, float dx, float dy) {
        float scaledMouseX = (x - this.x) * viewportWidth / width + viewportX;
        float scaledMouseY = (y - this.y) * viewportHeight / height + viewportY;
        if (super.onScroll(scaledMouseX, scaledMouseY, dx, dy)) {
            return true;
        }
        // zoom viewport, about mouse position
        if (allowViewportZoom) {
            float viewportHeightBefore = viewportHeight;
            float viewportWidthBefore = viewportWidth;
            if (dy > 0) {
                viewportHeight *= 1.1f;
                viewportWidth *= 1.1f;
            } else {
                viewportHeight *= 0.9f;
                viewportWidth *= 0.9f;
            }
            float viewportHeightDiff = viewportHeight - viewportHeightBefore;
            float viewportWidthDiff = viewportWidth - viewportWidthBefore;
            float mouseRatioX = (scaledMouseX - viewportX) / viewportWidthBefore;
            float mouseRatioY = (scaledMouseY - viewportY) / viewportHeightBefore;
            if (dy > 0) {
                viewportX -= viewportWidthDiff / 2 * (1 - mouseRatioX);
                viewportY -= viewportHeightDiff / 2 * (1 - mouseRatioY);
            } else {
                viewportX -= viewportWidthDiff / 2 * mouseRatioX;
                viewportY -= viewportHeightDiff / 2 * mouseRatioY;
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean shouldFocus(float mouseX, float mouseY) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }

    public void compileCode() {
        compileWires.clear();
        compileBlocks.clear();
        elements.clear();
        compileWires.addAll(RenderWire.compile(code.getWires(), font, this));
        compileBlocks.addAll(RenderBlock.compile(code.getBlocks(), font, this));
        elements.addAll(compileBlocks);
        elements.addAll(compileWires);
    }

    @Override
    public void onFocus(BaseElement prevFocus) {
        super.onFocus(prevFocus);
        Optional<RenderBlock> placeBlock = getPlacingBlock();
        if (placeBlock.isPresent()) {
            if (placeBlock.get().code != this) {
                if (focusedElement instanceof RenderBlock) {
                    if (!((RenderBlock) focusedElement).getBlockSpec().hollow()) {
                        setPlacingBlock(RenderBlock.compile(placeBlock.get().getBlock(), font, this));
//                        System.out.println("Placing block: " + placeBlock.get().getBlock() + " " + this.getClass().getName());
                    } else {
//                        System.out.println("focused block is hollow");
                    }
                } else {
                    setPlacingBlock(RenderBlock.compile(placeBlock.get().getBlock(), font, this));
//                    System.out.println("Placing block: " + placeBlock.get().getBlock() + " " + this.getClass().getName());
                }
            } else {
//                System.out.println("Code is already this");
            }
        }
    }

    public List<BaseElement> getHoveredElementsPreTranslatedMouse(float mouseX, float mouseY) {
        List<BaseElement> hoveredElements = new ArrayList<>();
        for (BaseElement element : elements) {
            if (element.shouldFocus(mouseX, mouseY)) {
                hoveredElements.add(element);
            }
        }
        return hoveredElements;
    }

    public List<BaseElement> getHoveredElements(float mouseX, float mouseY) {
        mouseX = (mouseX - x) * viewportWidth / width + viewportX;
        mouseY = (mouseY - y) * viewportHeight / height + viewportY;
        List<BaseElement> hoveredElements = new ArrayList<>();
        for (BaseElement element : elements) {
            if (element.shouldFocus(mouseX, mouseY)) {
                hoveredElements.add(element);
            }
        }
        return hoveredElements;
    }

    @Override
    public float getWireWidth() {
        return 1f;
    }

    public float translateMouseX(float mouseX) {
        return (mouseX - x) * viewportWidth / width + viewportX;
    }

    public float translateMouseY(float mouseY) {
        return (mouseY - y) * viewportHeight / height + viewportY;
    }

    @Override
    public void onRender(float mouseX, float mouseY) {
        // render stuff withing view from code
        // scale canvas to view
        GL11.glPushMatrix();
        GL11.glLineWidth(2.5f);

        // test if mouse hovering
        if (mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height) {
            if (getPlacingBlock().isPresent()) {
                parent.focusCode(this);
            }
        }

        //  render bg
        DrawableHelper.rect(x, y, x + width, y + height, 0xFFFFFFFF);

        // render border
        if (renderBorder) {
            DrawableHelper.rect(x, y, x + 1, y + height, 0xFF000000);
            DrawableHelper.rect(x + width - 1, y, x + width, y + height, 0xFF000000);
            DrawableHelper.rect(x, y, x + width, y + 1, 0xFF000000);
            DrawableHelper.rect(x, y + height - 1, x + width, y + height, 0xFF000000);
        }

        float scaledMouseX = (mouseX - x) * viewportWidth / width + viewportX;
        float scaledMouseY = (mouseY - y) * viewportHeight / height + viewportY;

        // render viewport text
        String viewportText = "";
        if (allowViewportDrag) {
            viewportText += String.format(" %.1f %.1f", scaledMouseX, scaledMouseY);
        }
        if (allowViewportZoom) {
            viewportText += String.format(" %.0f%%", width / viewportWidth);
        }
        viewportText += " ";
        float viewportTextWidth = DrawableHelper.getScaledWidth(font, viewportText, 8);
        if (viewportTextWidth < width / 2) {
            DrawableHelper.drawStringAtScale(font, viewportText, x + width - viewportTextWidth, y + height -
                12, 8, 0xFF000000);
        }

        // render code
        GL11.glTranslatef(x, y, 0);
        GL11.glScalef(width / viewportWidth, height / viewportHeight, 1);
        GL11.glTranslatef(-viewportX, -viewportY, 0);

        super.onRender(scaledMouseX, scaledMouseY);

        Optional<RenderBlock> placeBlock = getPlacingBlock();
        if (placeBlock.isPresent()) {
            RenderBlock block = placeBlock.get();
            if (block.code == this) {
                block.block.x = scaledMouseX - block.block.scaleX / 2;
                block.block.y = scaledMouseY - block.block.scaleY / 2;
                if (SNAP_TO_GRID) {
                    block.block.x = (float) (Math.floor(block.block.x / GRID_SIZE) * GRID_SIZE);
                    block.block.y = (float) (Math.floor(block.block.y / GRID_SIZE) * GRID_SIZE);
                }
                block.onRender(scaledMouseX, scaledMouseY);
            }
        }

        GL11.glPopMatrix();
    }

    @Override
    public Optional<RenderBlock> getPlacingBlock() {
        return parent.getPlacingBlock();
    }

    @Override
    public void setPlacingBlock(RenderBlock block) {
        parent.setPlacingBlock(block);
    }

    @Override
    public void focusCode(BaseElement code) {
        if (elements.contains(code) && focusedElement != code) {
            BaseElement prevFocus = focusedElement;
            if (focusedElement != null) {
                focusedElement.onFocusLost(code);
            }
            focusedElement = code;
            focusedElement.onFocus(prevFocus);
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
    public float width() {
        return width;
    }

    @Override
    public float height() {
        return height;
    }

    @Override
    public Set<RenderWire> getWires() {
        return compileWires;
    }

    @Override
    public RenderWire addWireForPort(double x, double y, int blockid, String port) {
        // create new wire
        Wire wire = new Wire();
        wire.id = -1;
        wire.addSegment(new Wire.WireEndpoint(blockid, x, y, port));
        wire.addSegment(new Wire.WireSegment(x, y));
        // add wire
        code.addWire(wire);
        RenderWire w = RenderWire.compile(List.of(wire), font, this).get(0);
        compileWires.add(w);
        elements.addLast(w);
        focusedElement.onFocusLost(null);
        focusedElement = w;
        focusedElement.onFocus(null);

        Iterator<BaseElement> it = w.elements.iterator();
        it.next();
        BaseElement wc = it.next();

        w.focusedElement = wc;
        w.focusedElement.onFocus(null);
        wc.onClick(0, 0, GLFW.GLFW_MOUSE_BUTTON_LEFT);
        return w;
    }

    @Override
    public void removeBlock(RenderBlock block) {
        if (focusedElement == block) {
            focusedElement = null;
        }
        elements.remove(block);
    }

    @Override
    public Window getWindow() {
        return window;
    }

}
