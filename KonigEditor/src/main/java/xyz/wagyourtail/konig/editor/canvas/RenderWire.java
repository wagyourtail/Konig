package xyz.wagyourtail.konig.editor.canvas;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;
import xyz.wagyourtail.MathHelper;
import xyz.wagyourtail.konig.structure.code.KonigBlockReference;
import xyz.wagyourtail.konig.structure.code.Wire;
import xyz.wagyourtail.konig.structure.headers.BlockIO;
import xyz.wagyourtail.konig.structure.headers.KonigBlock;
import xyz.wagyourtail.wagyourgui.Font;
import xyz.wagyourtail.wagyourgui.GLBuilder;
import xyz.wagyourtail.wagyourgui.elements.BaseElement;
import xyz.wagyourtail.wagyourgui.elements.DrawableHelper;
import xyz.wagyourtail.wagyourgui.elements.ElementContainer;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

public class RenderWire extends ElementContainer {
    public final RenderCode code;
    public final Wire wire;
    public final Font font;

    protected RenderWire(Wire wire, Font font, RenderCode code) {
        this.font = font;
        this.code = code;
        this.wire = wire;
        compileWire(wire);
    }

    @Override
    public boolean shouldFocus(float mouseX, float mouseY) {
        for (BaseElement element : elements) {
            if (element.shouldFocus(mouseX, mouseY)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean onClick(float x, float y, int button) {
        if (focusedElement != null && focusedElement.shouldFocus(x, y)) {
            return focusedElement.onClick(x, y, button);
        } else {
            return super.onClick(x, y, button);
        }
    }

    public static List<RenderWire> compile(List<Wire> wires, Font font, RenderCode code) {
        List<RenderWire> renderWires = new ArrayList<>();
        for (Wire wire : wires) {
            renderWires.add(new RenderWire(wire, font, code));
        }
        return renderWires;
    }

    public void compileWire(Wire wire) {
        RenderWireSegment prev = null;
        for (Wire.WireSegment segment : wire.getSegments()) {
            RenderWireSegment segRend = new RenderWireSegment(segment);
            if (prev != null) {
                prev.next = segRend;
            }
            segRend.prev = prev;
            if (segment instanceof Wire.WireBranch) {
                compileBranch((Wire.WireBranch) segment, segRend);
                segRend.branch = segRend.next;
            }
            prev = segRend;
            elements.add(segRend);
        }
    }

    public void compileBranch(Wire.WireBranch branch, RenderWireSegment branchSegment) {
        RenderWireSegment prev = branchSegment;
        for (Wire.WireSegment segment : branch.subSegments) {
            RenderWireSegment segRend = new RenderWireSegment(segment);
            if (prev != null) {
                prev.next = segRend;
            }
            segRend.prev = prev;
            if (segment instanceof Wire.WireBranch) {
                compileBranch((Wire.WireBranch) segment, segRend);
                segRend.branch = segRend.next;
            }
            prev = segRend;
            elements.add(segRend);
        }
    }

    public void cancelEndpoint(Wire.WireEndpoint endpoint) {
        for (BaseElement segment : elements) {
            if (segment instanceof RenderWireSegment) {
                RenderWireSegment seg = (RenderWireSegment) segment;
                if (seg.segment == endpoint) {
                    seg.segment = new Wire.WireSegment(endpoint.x, endpoint.y);
                    wire.insertSegment(endpoint, seg.segment);
                    wire.removeSegment(endpoint);
                }
            }
        }
    }

    @Override
    public void onFocusLost(BaseElement nextFocus) {
        bakeCorners();
        super.onFocusLost(nextFocus);
    }

    public void bakeCorners() {
        for (BaseElement segment : List.copyOf(elements)) {
            if (segment instanceof RenderWireSegment) {
                RenderWireSegment seg = (RenderWireSegment) segment;
                if (seg.prevCorner != null) {
                    seg.bakeCorner();
                }
            }
        }
    }

    public class RenderWireSegment extends BaseElement {
        public static final float LINE_WIDTH = 1f;
        protected static final float BRANCH_RADIUS = .025f;
        Wire.WireSegment segment;
        public RenderWireSegment prev;
        public RenderWireSegment next;
        public RenderWireSegment branch;

        protected Wire.WireSegment prevCorner = null;

        protected boolean movingWithMouse = false;
        protected boolean prevSelected = false;

        public RenderWireSegment(Wire.WireSegment segment) {
            this.segment = segment;
        }

        @Override
        public void onRender(float mouseX, float mouseY) {

            if (wire.getEndpoints().isEmpty()) {
                // delete wire
                code.elements.remove(RenderWire.this);
                code.focusedElement = null;
                code.code.removeWire(wire);
            }

            if (movingWithMouse) {
                if (RenderCode.SNAP_TO_GRID) {
                    segment.x = Math.floor(mouseX / RenderCode.GRID_SIZE) * RenderCode.GRID_SIZE;
                    segment.y = Math.floor(mouseY / RenderCode.GRID_SIZE) * RenderCode.GRID_SIZE;
                } else {
                    segment.x = mouseX;
                    segment.y = mouseY;
                }
            }

            if (prev != null && next != null && !isFocused() && !prev.isFocused() && !next.isFocused() && branch == null && (Math.abs(prev.segment.x - next.segment.x) < RenderCode.SMALL_VALUE || Math.abs(prev.segment.y - next.segment.y) < RenderCode.SMALL_VALUE)) {
                // delete current
                wire.removeSegment(segment);
                prev.next = next;
                next.prev = prev;
                elements.remove(this);
                return;
            }

            testCorner();

            Optional<RenderBlock> block = (Optional) code.getHoveredElementsPreTranslatedMouse(mouseX, mouseY)
                .stream()
                .filter(e -> e instanceof RenderBlock)
                .findFirst();
            if (block.isPresent()) {
                RenderBlock b = block.get();
                Optional<BaseElement> plug = b.getHoveredElements(mouseX, mouseY).stream().findFirst();
                if (plug.isPresent() && prevCorner != null) {
                    BaseElement p = plug.get();
                    if (plug.get() instanceof RenderBlock.IOPlug) {
                        RenderBlock.IOPlug plugRend = (RenderBlock.IOPlug) plug.get();
                        // check if allowed to have corner at beginning
                        if (prev != null && !(prev.segment instanceof Wire.WireEndpoint)) {
                            if (plugRend.element.side == BlockIO.Side.LEFT ||
                                plugRend.element.side == BlockIO.Side.RIGHT) {
                                prevCorner.x = prev.segment.x;
                                prevCorner.y = segment.y;
                            } else {
                                prevCorner.x = segment.x;
                                prevCorner.y = prev.segment.y;
                            }
                        }
                    } else if (plug.get() instanceof RenderBlock.VirtualIOPort) {
                        //TODO
                    }
                } else {
                    // check if corner intersects with block
                    if (prevCorner != null) {
                        if (prevCorner.x >= b.block.x && prevCorner.y >= b.block.y &&
                            prevCorner.x <= b.block.x + b.block.scaleX &&
                            prevCorner.y <= b.block.y + b.block.scaleY) {
                            // move corner segment to other end if possible
                            if (prev != null && !(prev.segment instanceof Wire.WireEndpoint)) {
                                if (Math.abs(prevCorner.x - prev.segment.x) < RenderCode.SMALL_VALUE) {
                                    // check if block would still be in bounds
                                    if (segment.x >= b.block.x && prev.segment.y >= b.block.y &&
                                        segment.x <= b.block.x + b.block.scaleX &&
                                        prev.segment.y <= b.block.y + b.block.scaleY) {
                                        // move corner segment to other end
                                        prevCorner.y = prev.segment.y;
                                        prevCorner.x = segment.x;
                                    }
                                } else {
                                    // check if block would still be in bounds
                                    if (segment.y >= b.block.y && prev.segment.x >= b.block.x &&
                                        segment.y <= b.block.y + b.block.scaleY &&
                                        prev.segment.x <= b.block.x + b.block.scaleX) {
                                        // move corner segment to other end
                                        prevCorner.x = prev.segment.x;
                                        prevCorner.y = segment.y;
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (prev != null) {
                AtomicReference<MathHelper.LineSegment>[] renderSegs;
                if (prevCorner != null) {
                    renderSegs = new AtomicReference[] {
                        new AtomicReference(new MathHelper.LineSegment((float) prev.segment.x,
                            (float) prev.segment.y, (float) prevCorner.x, (float) prevCorner.y
                        )),
                        new AtomicReference(new MathHelper.LineSegment((float) prevCorner.x,
                            (float) prevCorner.y, (float) segment.x, (float) segment.y
                        ))
                    };
                } else {
                    renderSegs = new AtomicReference[] {
                        new AtomicReference(new MathHelper.LineSegment((float) prev.segment.x,
                            (float) prev.segment.y, (float) segment.x, (float) segment.y
                        ))
                    };
                }

                for (int i = 0; i < renderSegs.length; i++) {
                    assert renderSegs[i] != null;
                    if (!MathHelper.clipLine(
                        renderSegs[i],
                        new MathHelper.Rectangle(
                            code.viewportX,
                            code.viewportY,
                            code.viewportX + code.viewportWidth,
                            code.viewportY + code.viewportHeight
                        )
                    )) {
                        renderSegs[i] = null;
                    }
                }

                if (isFocused() && !movingWithMouse) {
                    // hilight segment
                    GL11.glLineWidth(LINE_WIDTH * 4);
                    builder.begin(GL11.GL_LINE_STRIP)
                        .color(0xFF00FFFF);
                    if (renderSegs[0] != null) {
                        builder.vertex(renderSegs[0].get().x1(), renderSegs[0].get().y1());
                    }
                    for (int i = 0; i < renderSegs.length; i++) {
                        if (renderSegs[i] != null) {
                            builder.vertex(renderSegs[i].get().x2(), renderSegs[i].get().y2());
                        }
                    }
                    builder.end();
                }

                GL11.glLineWidth(LINE_WIDTH * 2);
                GLBuilder builder = GLBuilder.getBuilder();
                builder.begin(GL11.GL_LINE_STRIP)
                    .color(0xFF000000);
                if (renderSegs[0] != null) {
                    builder.vertex(renderSegs[0].get().x1(), renderSegs[0].get().y1());
                }
                for (int i = 0; i < renderSegs.length; i++) {
                    if (renderSegs[i] != null) {
                        builder.vertex(renderSegs[i].get().x2(), renderSegs[i].get().y2());
                    }
                }
                builder.end();

                if (branch != null && renderSegs[renderSegs.length - 1].get().x2() == (float) segment.x && renderSegs[renderSegs.length - 1].get().y2() == (float) segment.y) {
                    DrawableHelper.rect(
                        renderSegs[renderSegs.length - 1].get().x2() - BRANCH_RADIUS,
                        renderSegs[renderSegs.length - 1].get().y2() - BRANCH_RADIUS,
                        renderSegs[renderSegs.length - 1].get().x2() + BRANCH_RADIUS,
                        renderSegs[renderSegs.length - 1].get().y2() + BRANCH_RADIUS,
                        0xFF000000
                    );
                }
            }
        }

        public synchronized void bakeCorner() {
            if (prevCorner != null) {
                insertBefore(prevCorner);
            }
            prevCorner = null;
        }

        private void insertBefore(Wire.WireSegment segment) {
            if (prev == null) {
                wire.insertSegment(this.segment, segment);
                wire.removeSegment(this.segment);
                wire.insertSegment(segment, this.segment);
                RenderWireSegment newPrev = new RenderWireSegment(segment);
                newPrev.prev = prev;
                newPrev.next = this;
                prev.next = newPrev;
                prev = newPrev;
                elements.add(prev);
            } else if (prev.branch == this) {
                wire.insertSegment(this.segment, segment);
                wire.removeSegment(this.segment);
                wire.insertSegment(segment, this.segment);
                RenderWireSegment newPrev = new RenderWireSegment(segment);
                newPrev.prev = prev;
                newPrev.next = this;
                prev.branch = newPrev;
                prev = newPrev;
                elements.add(prev);
            } else {
                wire.insertSegment(prev.segment, segment);
                RenderWireSegment newPrev = new RenderWireSegment(segment);
                newPrev.prev = prev;
                newPrev.next = this;
                prev.next = newPrev;
                prev = newPrev;
                elements.add(prev);
            }
        }

        @Override
        public boolean onKey(int keycode, int scancode, int action, int mods) {
            if (action == GLFW.GLFW_PRESS && movingWithMouse) {
                if (keycode == GLFW.GLFW_KEY_ESCAPE && next == null) {
                    if (prev.prev == null) {
                        // delete wire
                        code.elements.remove(RenderWire.this);
                        code.focusedElement = null;
                        code.code.removeWire(wire);
                        if (prev.segment instanceof Wire.WireEndpoint) {
                            prev.removeEndpointFromBlock(((Wire.WireEndpoint) prev.segment).blockid);
                        }
                        return true;
                    }
                    wire.removeSegment(segment);
                    if (segment instanceof Wire.WireEndpoint) {
                        removeEndpointFromBlock(((Wire.WireEndpoint) segment).blockid);
                    }
                    if (prev.next == this) {
                        prev.next = null;
                        if (prev.branch != null) {
                            RenderWireSegment branchSeg = prev.branch;
                            prev.next = branchSeg;
                            prev.branch = null;
                            prev.segment = new Wire.WireSegment(prev.segment.x, prev.segment.y);
                            while (branchSeg != null) {
                                if (branchSeg.segment instanceof Wire.WireEndpoint) {
                                    wire.removeSegment(branchSeg.segment);
                                }
                                wire.insertSegment(branchSeg.prev.segment, branchSeg.segment);
                                branchSeg = branchSeg.next;
                            }
                        }
                    }
                    if (prev.branch == this) {
                        prev.branch = null;
                        Wire.WireSegment nobranch = new Wire.WireSegment(prev.segment.x, prev.segment.y);
                        wire.insertSegment(prev.segment, nobranch);
                        wire.removeSegment(prev.segment);
                        prev.segment = nobranch;
                    }
                    elements.remove(this);
                    wire.removeSegment(segment);
                    focusedElement = null;
                    prevCorner = null;
                    onFocusLost(null);
                    return true;
                }
            }
            return false;
        }

        @Override
        public boolean onClick(float x, float y, int button) {
            if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
                if (!prevSelected && next == null && branch == null) {
                    movingWithMouse = true;
                    // convert to not endpoint
                    if (segment instanceof Wire.WireEndpoint) {
                        Wire.WireSegment seg1 = new Wire.WireSegment(segment.x, segment.y);
                        removeEndpointFromBlock(((Wire.WireEndpoint) segment).blockid);
                        wire.insertSegment(segment, seg1);
                        wire.removeSegment(segment);
                        segment = seg1;
                    }
                    prevSelected = true;
                } else if (prevSelected) {
                    bakeCorner();
                    insertBefore(new Wire.WireSegment(segment.x, segment.y));
                }
                return true;
            } else if (button == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
                if (!prevSelected) {
                    Wire.WireSegment seg;
                    if (Math.abs(prev.segment.x - segment.x) < RenderCode.SMALL_VALUE) {
                        seg = new Wire.WireBranch(segment.x, y);
                    } else if (Math.abs(prev.segment.y - segment.y) < RenderCode.SMALL_VALUE) {
                        seg = new Wire.WireBranch(x, segment.y);
                    } else {
                        seg = new Wire.WireBranch(x, y);
                    }
                    insertBefore(seg);
                    x = (float) (Math.floor(x / RenderCode.GRID_SIZE) * RenderCode.GRID_SIZE);
                    y = (float) (Math.floor(y / RenderCode.GRID_SIZE) * RenderCode.GRID_SIZE);
                    RenderWireSegment seg2 = new RenderWireSegment(new Wire.WireSegment(x, y));
                    ((Wire.WireBranch) prev.segment).insertSegment(null, seg2.segment);
                    seg2.prev = prev;
                    prev.branch = seg2;
                    elements.add(seg2);
                    focusedElement = seg2;
                    seg2.onFocus(null);
                    seg2.prevSelected = true;
                    seg2.movingWithMouse = true;
                }
            }
            return false;
        }

        private void removeEndpointFromBlock(int blockid) {
            //TODO: work with virtual io
            Optional.ofNullable(code.code.getBlockMap().get(blockid)).ifPresent(e -> {
                e.io.elementMap.values().stream().filter(f -> f.wireid == wire.id &&
                    Objects.equals(f.name, ((Wire.WireEndpoint) segment).port)).findFirst().ifPresent(e.io::remove);
            });
        }


        @Override
        public boolean onDrag(float x, float y, float dx, float dy, int button) {
            if (!movingWithMouse) {
                if (prev != null) {
                    if (Math.abs(segment.x - prev.segment.x) < RenderCode.SMALL_VALUE) {
                        segment.y += dy;
                        segment.x += dx;
                        if (next != null && !next.isFocused() && !(next.segment instanceof Wire.WireEndpoint)) {
                            next.segment.y += dy;
                        }
                        if (!prev.isFocused() && !(prev.segment instanceof Wire.WireEndpoint)) {
                            prev.segment.x += dx;
                        }
                    } else if (Math.abs(segment.y - prev.segment.y) < RenderCode.SMALL_VALUE) {
                        segment.x += dx;
                        segment.y += dy;
                        if (next != null && !next.isFocused() && !(next.segment instanceof Wire.WireEndpoint)) {
                            next.segment.x += dx;
                        }
                        if (!prev.isFocused() && !(prev.segment instanceof Wire.WireEndpoint)) {
                            prev.segment.y += dy;
                        }
                    }
                    testCorner();
                    bakeCorner();
                } else if (next != null) {
                    if (Math.abs(segment.x - next.segment.x) < RenderCode.SMALL_VALUE) {
                        segment.x += dx;
                        segment.y += dy;
                        if (!next.isFocused() && !(next.segment instanceof Wire.WireEndpoint)) {
                            next.segment.x += dx;
                        }
                    } else if (Math.abs(segment.y - next.segment.y) < RenderCode.SMALL_VALUE) {
                        segment.x += dx;
                        segment.y += dy;
                        if (!next.isFocused() && !(next.segment instanceof Wire.WireEndpoint)) {
                            next.segment.y += dy;
                        }
                    }
                    next.testCorner();
                    next.bakeCorner();
                }
            }
            return true;
        }


        public void testCorner() {
            // check if corner segment is needed and not present
            if (prevCorner == null && prev != null && segment.x != prev.segment.x && segment.y != prev.segment.y) {
                // check if allowed to bend at beginning of current segment
                if (prev.prev != null) {
                    // place initial corner segment at right angle to previous segment
                    if (Math.abs(prev.segment.x - prev.prev.segment.x) < RenderCode.SMALL_VALUE) {
                        prevCorner = new Wire.WireSegment(segment.x, prev.segment.y);
                    } else {
                        prevCorner = new Wire.WireSegment(prev.segment.x, segment.y);
                    }
                } else {
                    // check if prev is endpoint
                    if (prev.segment instanceof Wire.WireEndpoint) {
                        // get which side of block io is on
                        Wire.WireEndpoint endpoint = (Wire.WireEndpoint) prev.segment;
                        KonigBlockReference block = code.code.getBlockMap().get(endpoint.blockid);
                        KonigBlock b = block.attemptToGetBlockSpec();
                        if (block != null && b != null) {
                            BlockIO.IOElement io = b.io.byName.get(endpoint.port);
                            if (io != null) {
                                if (io.side == BlockIO.Side.LEFT || io.side == BlockIO.Side.RIGHT) {
                                    prevCorner = new Wire.WireSegment(prev.segment.x, segment.y);
                                } else {
                                    prevCorner = new Wire.WireSegment(segment.x, prev.segment.y);
                                }
                            } else {
                                prevCorner = new Wire.WireSegment(segment.x, prev.segment.y);
                            }
                        } else {
                            prevCorner = new Wire.WireSegment(segment.x, prev.segment.y);
                        }
                    } else {
                        throw new RuntimeException("prev segment is not endpoint, but prev.prev is null, this is currently not supported");
                    }
                }
            } else if (prevCorner != null) {
                // move corner to match coord
                if (Math.abs(prevCorner.x - prev.segment.x) < RenderCode.SMALL_VALUE) {
                    prevCorner.y = segment.y;
                } else {
                    prevCorner.x = segment.x;
                }
                // check if corner is still needed
                if (Math.abs(segment.x - prev.segment.x) < RenderCode.SMALL_VALUE || Math.abs(segment.y - prev.segment.y) < RenderCode.SMALL_VALUE) {
                    prevCorner = null;
                }
            }
        }


        @Override
        public void onFocusLost(BaseElement nextFocus) {
            prevSelected = false;
            movingWithMouse = false;
            // TODO: detect if at angle and insert segment to right-angle
            if (nextFocus instanceof RenderBlock) {
                Wire.WireEndpoint end = new Wire.WireEndpoint(
                    ((RenderBlock) nextFocus).block.id,
                    segment.x,
                    segment.y,
                    null
                );
                wire.insertSegment(segment, end);
                wire.removeSegment(segment);
                segment = end;
            } else if (nextFocus instanceof RenderWire) {
                //TODO, connect wires?
            }
            super.onFocusLost(nextFocus);
        }

        @Override
        public boolean shouldFocus(float mouseX, float mouseY) {
            if (prev == null) {
                return false;
            }
            if (movingWithMouse) {
                return true;
            }
            // create a tight box around the line
            float x1 = (float) prev.segment.x;
            float y1 = (float) prev.segment.y;
            float x2 = (float) segment.x;
            float y2 = (float) segment.y;
            float x3 = Math.min(x1, x2);
            float y3 = Math.min(y1, y2);
            float x4 = Math.max(x1, x2);
            float y4 = Math.max(y1, y2);
            x3 -= LINE_WIDTH * 2 * code.viewportWidth / code.width;
            y3 -= LINE_WIDTH * 2 * code.viewportHeight / code.height;
            x4 += LINE_WIDTH * 2 * code.viewportWidth / code.width;
            y4 += LINE_WIDTH * 2 * code.viewportHeight / code.height;
            return mouseX >= x3 && mouseX <= x4 && mouseY >= y3 && mouseY <= y4;
        }

    }

}
