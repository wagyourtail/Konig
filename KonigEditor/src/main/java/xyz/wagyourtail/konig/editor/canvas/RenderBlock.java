package xyz.wagyourtail.konig.editor.canvas;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;
import xyz.wagyourtail.MathHelper;
import xyz.wagyourtail.konig.editor.canvas.blocks.RenderConstBlock;
import xyz.wagyourtail.konig.structure.code.KonigBlockReference;
import xyz.wagyourtail.konig.structure.code.ReferenceIO;
import xyz.wagyourtail.konig.structure.code.VirtualIO;
import xyz.wagyourtail.konig.structure.code.Wire;
import xyz.wagyourtail.konig.structure.headers.BlockIO;
import xyz.wagyourtail.konig.structure.headers.KonigBlock;
import xyz.wagyourtail.wagyourgui.Font;
import xyz.wagyourtail.wagyourgui.GLBuilder;
import xyz.wagyourtail.wagyourgui.Texture;
import xyz.wagyourtail.wagyourgui.elements.BaseElement;
import xyz.wagyourtail.wagyourgui.elements.DrawableHelper;
import xyz.wagyourtail.wagyourgui.elements.ElementContainer;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

public class RenderBlock extends ElementContainer {

    public static final Map<String, RenderBlockCreator> special_cases = Map.of(
        "const", RenderConstBlock::new
    );

    //TODO: make this a cache of some kind?
    Map<KonigBlock, Texture> blockTextures = new HashMap<>();

    protected BaseElement prev;

    protected final RenderBlockParent code;
    protected final Font font;

    protected final KonigBlockReference block;
    protected final KonigBlock blockSpec;


    public static List<RenderBlock> compile(List<KonigBlockReference> blocks, Font font, RenderBlockParent code) {
        List<RenderBlock> renderBlocks = new ArrayList<>();
        for (KonigBlockReference block : blocks) {
            renderBlocks.add(special_cases.getOrDefault(block.name, RenderBlock::new).create(block, font, code));
        }
        return renderBlocks;
    }

    public RenderBlock(KonigBlockReference block, Font font, RenderBlockParent code) {
        this.block = block;
        this.font = font;
        this.code = code;
        this.blockSpec = block.attemptToGetBlockSpec();
        initIO();
    }

    @Override
    public boolean onDrag(float x, float y, float dx, float dy, int button) {
        if (focusedElement instanceof RenderCode) return super.onDrag(x, y, dx, dy, button);
        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            // move block, resolve wire endpoints, move them too, adding a right angle if necessary
            for (RenderWire wire : code.getWires()) {

            }
        }
        return true;
    }

    public KonigBlock getBlockSpec() {
        return blockSpec;
    }

    public KonigBlockReference getBlock() {
        return block;
    }

    protected void initIO() {
        if (blockSpec == null) {
            return;
        }
        for (Map.Entry<BlockIO.Side, Map<BlockIO.Justify, List<BlockIO.IOElement>>> side : blockSpec.io.elements.entrySet()) {
            for (Map.Entry<BlockIO.Justify, List<BlockIO.IOElement>> justify : side.getValue().entrySet()) {
                for (int i = 0; i < justify.getValue().size(); i++) {
                    BlockIO.IOElement io = justify.getValue().get(i);
                    addIO(io, i, justify.getValue().size());
                }
            }
        }
        for (VirtualIO virtualIO : block.virtualIOGroupsMap.values()) {
            for (VirtualIO.Port port : virtualIO.portMap.values()) {
                //TODO
            }
        }
        for (VirtualIO virtualIO : block.virtualIONameMap.values()) {
            for (VirtualIO.Port port : virtualIO.portMap.values()) {
                //TODO
            }
        }
    }

    protected void addIO(BlockIO.IOElement io, int index, int countOnJustify) {
        // determine x y relative to block code
        float x = 0;
        float y = 0;
        switch (io.side) {
            case LEFT:
                y = getJustifyPos(io.justify, index, countOnJustify, block.scaleY);
                break;
            case RIGHT:
                x = block.scaleX;
                y = getJustifyPos(io.justify, index, countOnJustify, block.scaleY);
                break;
            case TOP:
                x = getJustifyPos(io.justify, index, countOnJustify, block.scaleX);
                break;
            case BOTTOM:
                y = block.scaleY;
                x = getJustifyPos(io.justify, index, countOnJustify, block.scaleX);
                break;
        }

        elements.add(new IOPlug(x, y, io));
    }

    private float getJustifyPos(BlockIO.Justify justify, int index, int countOnJustify, float blockScale) {
        switch (justify) {
            case LEFT:
                return  .1f + index * .2f;
            case CENTER:
                if (countOnJustify % 2 == 0) {
                    return blockScale / 2 - countOnJustify * .1f + .1f + index * .2f;
                } else {
                    return blockScale / 2 - (countOnJustify - 1) * .1f + index * .2f;
                }
            case RIGHT:
                return blockScale - .1f - (countOnJustify - 1) * .2f + index * .2f;
        }
        return 0;
    }

    protected MathHelper.Rectangle clip() {
        // check if intersects view box
        AtomicReference<MathHelper.Rectangle> r = new AtomicReference<>(new MathHelper.Rectangle(
            block.x,
            block.y,
            block.x + block.scaleX,
            block.y + block.scaleY
        ));

        if (!MathHelper.clipRect(r, new MathHelper.Rectangle(code.viewportX(), code.viewportY(), code.viewportX() + code.viewportWidth(), code.viewportY() + code.viewportHeight()))) {
            return null;
        }

        return r.get();
    }

    @Override
    public boolean shouldFocus(float mouseX, float mouseY) {
        for (BaseElement element : elements) {
            if (element.shouldFocus(mouseX, mouseY)) {
                return true;
            }
        }
        return mouseX >= block.x && mouseX <= block.x + block.scaleX && mouseY >= block.y && mouseY <= block.y + block.scaleY;
    }

    public void renderSubElements(float mouseX, float mouseY) {
        // translate to block position
        GL11.glPushMatrix();
        GL11.glTranslatef(block.x, block.y, 0);
        super.onRender(mouseX, mouseY);
        GL11.glPopMatrix();
    }

    @Override
    public void onFocus(BaseElement prevFocus) {
        super.onFocus(prevFocus);
        prev = prevFocus;
    }

    @Override
    public void onRender(float mouseX, float mouseY) {

        MathHelper.Rectangle rect = clip();

        if (rect == null) {
            return;
        }

        if (blockSpec != null) {
            if (blockSpec.image != null) {
                // clip image to match
                float x = (rect.x1() - block.x) / block.scaleX;
                float y = (rect.y1() - block.y) / block.scaleY;
                float x2 = 1 - (block.x + block.scaleX - rect.x2()) / block.scaleX;
                float y2 = 1 - (block.y + block.scaleY - rect.y2()) / block.scaleY;

                Optional<Texture> tex = Optional.ofNullable(blockTextures.computeIfAbsent(blockSpec, (bk) -> {
                    try {
                        return new Texture(bk.image);
                    } catch (IOException e) {
                        e.printStackTrace();
                        return null;
                    }
                }));
                if (tex.isPresent()) {
                    tex.get().bind();
                    GL11.glEnable(GL11.GL_TEXTURE_2D);
                    GLBuilder.getBuilder().begin(GL11.GL_TRIANGLE_STRIP)
                        .color(0xFFFFFFFF)
                        .vertex(rect.x1(), rect.y1()).uv(x, y)
                        .vertex(rect.x2(), rect.y2()).uv(x2, y2)
                        .vertex(rect.x1(), rect.y2()).uv(x, y2)
                        .vertex(rect.x2(), rect.y1()).uv(x2, y)
                        .end();
                    GL11.glDisable(GL11.GL_TEXTURE_2D);
                } else {
                    // render in yellow
                    DrawableHelper.rect(
                        rect.x1(),
                        rect.y1(),
                        rect.x2(),
                        rect.y2(),
                        0xFFFF00FF
                    );
                }

            } else {
                // render in gray
                DrawableHelper.rect(
                    rect.x1(),
                    rect.y1(),
                    rect.x2(),
                    rect.y2(),
                    0xFF777777
                );
            }
        } else {
            // render in red
            DrawableHelper.rect(
                rect.x1(),
                rect.y1(),
                rect.x2(),
                rect.y2(),
                0xFFFF0000
            );
        }

        // check if rect is trimmed
        if (rect.x1() != block.x || rect.y1() != block.y || rect.x2() != block.x + block.scaleX || rect.y2() != block.y + block.scaleY) {
            renderSubElements(mouseX, mouseY);
            return;
        }

        // draw name on top, scaled to fill
        float width = font.getWidth(block.name) * 2;
        GL11.glPushMatrix();
        if (width > block.scaleX) {
            GL11.glTranslatef(block.x + block.scaleX / 2, block.y + block.scaleY / 2, 0);
            GL11.glScalef(block.scaleX / width, block.scaleX / width, 1);
        }
        //get scaled font height
        DrawableHelper.drawCenteredString(font, block.name, 0, -font.FONT_HEIGHT / 2f, 0xFF000000);
        GL11.glPopMatrix();

        renderSubElements(mouseX, mouseY);
    }

    @Override
    public boolean onClick(float x, float y, int button) {
        super.onClick(x - block.x, y - block.y, button);
        if (prev instanceof RenderWire) {
            RenderWire p = (RenderWire) prev;
            for (Wire.WireEndpoint end : p.wire.getEndpoints()) {
                if (end.blockid == block.id) {
                    if (end.port == null) {
                        p.cancelEndpoint(end);
                    }
                }
            }
        }
        return true;
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
        mouseX -= block.x;
        mouseY -= block.y;
        List<BaseElement> hoveredElements = new ArrayList<>();
        for (BaseElement element : elements) {
            if (element.shouldFocus(mouseX, mouseY)) {
                hoveredElements.add(element);
            }
        }
        return hoveredElements;
    }


    @FunctionalInterface
    public interface RenderBlockCreator {
        RenderBlock create(KonigBlockReference block, Font font, RenderBlockParent code);
    }

    public class IOPlug extends BaseElement {
        private static final float PORT_RADIUS = .05f;
        public final BlockIO.IOElement element;

        private final float x;
        private final float y;

        public IOPlug(float x, float y, BlockIO.IOElement element) {
            this.element = element;
            this.x = x;
            this.y = y;
        }

        @Override
        public boolean shouldFocus(float mouseX, float mouseY) {
            return mouseX >= x - PORT_RADIUS && mouseX <= x + PORT_RADIUS && mouseY >= y - PORT_RADIUS && mouseY <= y + PORT_RADIUS;
        }

        @Override
        public void onFocus(BaseElement prevFocus) {
            super.onFocus(prevFocus);
            // check if already plugged in
            if (block.io.elementMap.get(element.name) != null) {
                return;
            }
            if (prev instanceof RenderWire) {
                RenderWire p = (RenderWire) prev;
                for (Wire.WireEndpoint end : p.wire.getEndpoints()) {
                    if (end.blockid == block.id) {
                        if (end.port == null) {
                            // check position in hitbox
                            if (end.x >= block.x + x - PORT_RADIUS && end.x <= block.x + x + PORT_RADIUS && end.y >= block.y + y - PORT_RADIUS && end.y <= block.y + y + PORT_RADIUS) {
                                ReferenceIO.IOElement io = null;
                                if (element instanceof BlockIO.Input) {
                                    end.port = element.name;
                                    io = new ReferenceIO.Input(element.name, p.wire.id);
                                    block.io.inputMap.put(element.name, (ReferenceIO.Input) io);
                                } else if (element instanceof BlockIO.Output) {
                                    end.port = element.name;
                                    io = new ReferenceIO.Output(element.name, p.wire.id);
                                    block.io.outputMap.put(element.name, (ReferenceIO.Output) io);
                                }
                                if (io != null) {
                                    block.io.elementMap.put(element.name, io);
                                }
                                return;
                            }
                        }
                    }
                }
            }
        }

        @Override
        public boolean onClick(float x, float y, int button) {
            System.out.println("clicked on " + element);
            return true;
        }

        @Override
        public void onRender(float mouseX, float mouseY) {
            // check if plugged in
            if (block.io.elementMap.get(element.name) != null) {
                // triangle
                GL11.glDisable(GL11.GL_TEXTURE_2D);
                GL11.glEnable(GL11.GL_COLOR);
                GLBuilder builder = GLBuilder.getBuilder().begin(GL11.GL_TRIANGLES).color(0xFF000000);
                switch (element.side) {
                    case TOP:
                        builder.vertex(x, y - PORT_RADIUS)
                            .vertex(x + PORT_RADIUS, y - PORT_RADIUS)
                            .vertex(x - PORT_RADIUS, y - PORT_RADIUS)
                            .end();
                        break;
                    case BOTTOM:
                        builder.vertex(x, y - PORT_RADIUS)
                            .vertex(x + PORT_RADIUS, y + PORT_RADIUS)
                            .vertex(x - PORT_RADIUS, y + PORT_RADIUS)
                            .end();
                        break;
                    case LEFT:
                        builder.vertex(x - PORT_RADIUS, y)
                            .vertex(x + PORT_RADIUS, y - PORT_RADIUS)
                            .vertex(x + PORT_RADIUS, y + PORT_RADIUS)
                            .end();
                        break;
                    case RIGHT:
                        builder.vertex(x + PORT_RADIUS, y)
                            .vertex(x - PORT_RADIUS, y - PORT_RADIUS)
                            .vertex(x - PORT_RADIUS, y + PORT_RADIUS)
                            .end();
                        break;
                }
            } else {
                DrawableHelper.rect(
                    x - PORT_RADIUS,
                    y - PORT_RADIUS,
                    x + PORT_RADIUS,
                    y + PORT_RADIUS,
                    0xFF000000
                );
            }
        }

    }

    public class VirtualIOPort extends ElementContainer {
        private static final float PORT_RADIUS = .05f;
        private VirtualIO.Port port;

        private float x;
        private float y;

        public VirtualIOPort(float x, float y, VirtualIO.Port port) {
            this.port = port;
            this.x = x;
            this.y = y;
        }

        public class VirtualIOPlug extends BaseElement {
            private VirtualIO.PortElement element;

            private float x;
            private float y;

            public VirtualIOPlug(float x, float y, VirtualIO.PortElement element) {
                this.element = element;
                this.x = x;
                this.y = y;
            }

            @Override
            public boolean shouldFocus(float mouseX, float mouseY) {
                return mouseX >= x - PORT_RADIUS && mouseX <= x + PORT_RADIUS && mouseY >= y - PORT_RADIUS && mouseY <= y + PORT_RADIUS;
            }

            @Override
            public void onRender(float mouseX, float mouseY) {
                DrawableHelper.rect(
                    x - PORT_RADIUS,
                    y - PORT_RADIUS,
                    x + PORT_RADIUS,
                    y + PORT_RADIUS,
                    0xFF000000
                );
            }
        }
    }


}
