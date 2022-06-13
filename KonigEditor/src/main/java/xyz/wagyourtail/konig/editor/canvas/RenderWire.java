package xyz.wagyourtail.konig.editor.canvas;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;
import xyz.wagyourtail.MathHelper;
import xyz.wagyourtail.konig.structure.code.Wire;
import xyz.wagyourtail.wagyourgui.Font;
import xyz.wagyourtail.wagyourgui.GLBuilder;
import xyz.wagyourtail.wagyourgui.elements.BaseElement;
import xyz.wagyourtail.wagyourgui.elements.DrawableHelper;
import xyz.wagyourtail.wagyourgui.elements.ElementContainer;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
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

    protected class RenderWireSegment extends BaseElement {
        protected static final float LINE_WIDTH = 1f;
        protected static final float BRANCH_RADIUS = .025f;
        Wire.WireSegment segment;
        public RenderWireSegment prev;
        public RenderWireSegment next;
        public RenderWireSegment branch;

        protected boolean movingWithMouse = false;
        protected boolean prevSelected = false;

        public RenderWireSegment(Wire.WireSegment segment) {
            this.segment = segment;
        }

        @Override
        public void onRender(float mouseX, float mouseY) {
            if (movingWithMouse) {
                segment.x = mouseX;
                segment.y = mouseY;
            }

            if (prev != null) {

                // check if intersects view box
                AtomicReference<MathHelper.LineSegment> ls = new AtomicReference<>(new MathHelper.LineSegment(
                    (float) prev.segment.x,
                    (float) prev.segment.y,
                    (float) segment.x,
                    (float) segment.y
                ));
                if (!MathHelper.clipLine(ls, new MathHelper.Rectangle(code.viewportX, code.viewportY, code.viewportX + code.viewportWidth, code.viewportY + code.viewportHeight))) {
                    return;
                }

                // draw line
                MathHelper.LineSegment segment1 = ls.get();

                GL11.glLineWidth(LINE_WIDTH * 2);
                GLBuilder.getBuilder().begin(GL11.GL_LINES)
                    .color(0xFF000000)
                    .vertex(segment1.x1(), segment1.y1())
                    .vertex(segment1.x2(), segment1.y2())
                    .end();

                if (branch != null && segment1.x2() == segment.x && segment1.y2() == segment.y) {
                    DrawableHelper.rect(
                        segment1.x2() - BRANCH_RADIUS,
                        segment1.y2() - BRANCH_RADIUS,
                        segment1.x2() + BRANCH_RADIUS,
                        segment1.y2() + BRANCH_RADIUS,
                        0xFF000000
                    );
                }
            }
        }

        @Override
        public boolean onKey(int keycode, int scancode, int action, int mods) {
            if (action == GLFW.GLFW_PRESS) {
                if (keycode == GLFW.GLFW_KEY_ESCAPE && next == null) {
                    wire.removeSegment(segment);
                    if (segment instanceof Wire.WireEndpoint) {
                        //TODO: work with virtual io
                        removeEndpointFromBlock();
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
                        removeEndpointFromBlock();
                        wire.insertSegment(segment, seg1);
                        wire.removeSegment(segment);
                        segment = seg1;
                    }
                } else {
                    if (prev.branch != this) {
                        RenderWireSegment seg = new RenderWireSegment(new Wire.WireSegment(segment.x, segment.y));
                        wire.insertSegment(prev.segment, seg.segment);
                        seg.prev = prev;
                        seg.next = this;
                        this.prev = seg;
                        elements.add(seg);
                    } else {
                        RenderWireSegment seg = new RenderWireSegment(new Wire.WireSegment(segment.x, segment.y));
                        wire.insertSegment(segment, seg.segment);
                        wire.removeSegment(segment);
                        wire.insertSegment(seg.segment, segment);
                        seg.prev = prev;
                        seg.next = this;
                        this.prev = seg;
                        elements.add(seg);
                    }
                }
                prevSelected = true;
                return true;
            } else if (button == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
                if (!prevSelected) {
                    RenderWireSegment seg;
                    if (prev.segment.x == segment.x) {
                        seg = new RenderWireSegment(new Wire.WireBranch(segment.x, y));
                    } else if (prev.segment.y == segment.y) {
                        seg = new RenderWireSegment(new Wire.WireBranch(x, segment.y));
                    } else {
                        seg = new RenderWireSegment(new Wire.WireBranch(x, y));
                    }
                    if (prev.branch != this) {
                        prev.next = seg;
                        seg.prev = prev;
                        seg.next = this;
                        prev = seg;
                        wire.insertSegment(prev.segment, seg.segment);
                    } else {
                        prev.branch = seg;
                        seg.prev = prev;
                        seg.next = this;
                        prev = seg;
                        ((Wire.WireBranch) seg.prev.segment).insertSegment(segment, seg.segment);
                        wire.removeSegment(segment);
                        wire.insertSegment(seg.segment, segment);
                    }
                    RenderWireSegment seg2 = new RenderWireSegment(new Wire.WireSegment(x, y));
                    ((Wire.WireBranch) seg.segment).insertSegment(null, seg2.segment);
                    seg2.prev = seg;
                    seg.branch = seg2;
                    elements.add(seg2);
                    elements.add(seg);
                    focusedElement = seg2;
                    seg2.prevSelected = true;
                    seg2.movingWithMouse = true;
                }
            }
            return false;
        }

        private void removeEndpointFromBlock() {
            code.code.getBlocks().stream().filter(e -> e.id == ((Wire.WireEndpoint) segment).blockid).findFirst().ifPresent(e -> {
                e.io.elementMap.values().stream().filter(f -> f.wireid == wire.id && Objects.equals(f.name, ((Wire.WireEndpoint) segment).port)).findFirst().ifPresent(e.io::remove);
            });
        }


        @Override
        public boolean onDrag(float x, float y, float dx, float dy, int button) {
            segment.x += dx;
            segment.y += dy;
            return true;
        }

        @Override
        public void onFocusLost(BaseElement nextFocus) {
            prevSelected = false;
            movingWithMouse = false;
            // TODO: detect if at angle and insert segment to right-angle
            if (nextFocus instanceof RenderBlock) {
                Wire.WireEndpoint end = new Wire.WireEndpoint(((RenderBlock) nextFocus).block.id, segment.x, segment.y, null);
                wire.insertSegment(segment, end);
                wire.removeSegment(segment);
                segment = end;
            } else if (nextFocus instanceof RenderWire) {
                //TODO, connect wires?
            }
            System.out.println("focus lost");
            super.onFocusLost(nextFocus);
        }

        @Override
        public boolean shouldFocus(float mouseX, float mouseY) {
            if (prev == null) {
                return false;
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
