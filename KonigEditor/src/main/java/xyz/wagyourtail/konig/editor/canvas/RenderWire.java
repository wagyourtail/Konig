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
import java.util.concurrent.atomic.AtomicReference;

public class RenderWire extends ElementContainer {
    public final RenderCode code;
    public final Wire wire;
    public final Font font;

    private RenderWireSegment hoverXSegment;
    private RenderWireSegment hoverYSegment;
    protected RenderWire(Wire wire, Font font, RenderCode code) {
        this.font = font;
        this.code = code;
        this.wire = wire;
        compileWire(wire);
    }

    @Override
    public boolean shouldFocus(float mouseX, float mouseY) {
        for (BaseElement element : elements) {
            if (element == hoverXSegment || element == hoverYSegment) {
                continue;
            }
            if (element.shouldFocus(mouseX, mouseY)) {
                return true;
            }
        }
        return false;
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
    public void onRender(float mouseX, float mouseY) {
        if (hoverYSegment != hoverXSegment) {
            if (hoverXSegment != null) {
                hoverXSegment.segment.x = mouseX;
                //            System.out.println("X: " + mouseX);
            }
            if (hoverYSegment != null) {
                hoverYSegment.segment.y = mouseY;
                //            System.out.println("Y: " + mouseY);
            }
        } else if (hoverYSegment != null) {
            // detect disallowed direction
            if (hoverYSegment.prev.prev != null && hoverYSegment.prev.prev.segment.x == hoverYSegment.prev.segment.x) {
                // check if negative or positive is ok
                if (hoverYSegment.prev.prev.segment.y < hoverYSegment.prev.segment.y) {
                    if (mouseY > hoverYSegment.prev.segment.y) {
                        if (Math.abs(mouseY - hoverYSegment.prev.segment.y) > Math.abs(mouseX - hoverYSegment.prev.segment.x)) {
                            hoverXSegment.segment.x = hoverXSegment.prev.segment.x;
                            hoverYSegment.segment.y = mouseY;
                        } else {
                            hoverYSegment.segment.x = mouseX;
                            hoverXSegment.segment.y = hoverYSegment.prev.segment.y;

                        }
                    } else {
                        hoverYSegment.segment.x = mouseX;
                        hoverYSegment.segment.y = hoverYSegment.prev.segment.y;
                    }
                } else {
                    if (mouseY < hoverYSegment.prev.segment.y) {
                        if (Math.abs(mouseY - hoverYSegment.prev.segment.y) > Math.abs(mouseX - hoverYSegment.prev.segment.x)) {
                            hoverXSegment.segment.x = hoverXSegment.prev.segment.x;
                            hoverYSegment.segment.y = mouseY;
                        } else {
                            hoverYSegment.segment.x = mouseX;
                            hoverXSegment.segment.y = hoverYSegment.prev.segment.y;

                        }
                    } else {
                        hoverYSegment.segment.x = mouseX;
                        hoverYSegment.segment.y = hoverYSegment.prev.segment.y;
                    }
                }
            } else if (hoverXSegment.prev.prev != null && hoverXSegment.prev.prev.segment.y == hoverXSegment.prev.segment.y) {
                // check if negative or positive is ok
                if (hoverXSegment.prev.prev.segment.x < hoverXSegment.prev.segment.x) {
                    if (mouseX > hoverXSegment.prev.segment.x) {
                        if (Math.abs(mouseX - hoverXSegment.prev.segment.x) > Math.abs(mouseY - hoverXSegment.prev.segment.y)) {
                            hoverXSegment.segment.x = mouseX;
                            hoverYSegment.segment.y = hoverYSegment.prev.segment.y;
                        } else {
                            hoverYSegment.segment.x = hoverYSegment.prev.segment.x;
                            hoverXSegment.segment.y = mouseY;
                        }
                    } else {
                        hoverXSegment.segment.x = hoverXSegment.prev.segment.x;
                        hoverXSegment.segment.y = mouseY;
                    }
                } else {
                    if (mouseX < hoverXSegment.prev.segment.x) {
                        if (Math.abs(mouseX - hoverXSegment.prev.segment.x) > Math.abs(mouseY - hoverXSegment.prev.segment.y)) {
                            hoverXSegment.segment.x = mouseX;
                            hoverYSegment.segment.y = hoverYSegment.prev.segment.y;
                        } else {
                            hoverYSegment.segment.x = hoverYSegment.prev.segment.x;
                            hoverXSegment.segment.y = mouseY;
                        }
                    } else {
                        hoverXSegment.segment.x = hoverXSegment.prev.segment.x;
                        hoverXSegment.segment.y = mouseY;
                    }
                }
            } else {
                if (Math.abs(mouseX - hoverXSegment.prev.segment.x) > Math.abs(mouseY - hoverXSegment.prev.segment.y)) {
                    hoverXSegment.segment.x = mouseX;
                    hoverYSegment.segment.y = hoverYSegment.prev.segment.y;
                } else {
                    hoverYSegment.segment.x = hoverYSegment.prev.segment.x;
                    hoverXSegment.segment.y = mouseY;
                }
            }
        }
        super.onRender(mouseX, mouseY);
    }

    @Override
    public boolean onKey(int keycode, int scancode, int action, int mods) {
        if (action != 1) {
            return false;
        }
        if (keycode == GLFW.GLFW_KEY_ESCAPE) {
            if (hoverYSegment != null) {
                if (hoverYSegment.prev.segment instanceof Wire.WireBranch) {
                    if (hoverYSegment.prev.branch == hoverYSegment) {
                        if (hoverYSegment.prev.prev.segment.x == hoverYSegment.prev.next.segment.x ||
                            hoverYSegment.prev.prev.segment.y == hoverYSegment.prev.next.segment.y) {
                            // delete branch & segment
                            RenderWireSegment prev = hoverYSegment.prev.prev;
                            RenderWireSegment next = hoverYSegment.prev.next;
                            prev.next = next;
                            next.prev = prev;
                            elements.remove(hoverYSegment.prev);
                            elements.remove(hoverYSegment);
                            wire.removeSegment(hoverYSegment.prev.segment);
                            wire.removeSegment(hoverYSegment.segment);
                        } else {
                            // replace branch with segment
                            RenderWireSegment prev = hoverYSegment.prev;
                            RenderWireSegment next = hoverYSegment.next;
                            Wire.WireSegment newSeg = new Wire.WireSegment(
                                hoverXSegment.segment.x,
                                hoverYSegment.segment.y
                            );
                            RenderWireSegment newSegRend = new RenderWireSegment(newSeg);
                            prev.next = newSegRend;
                            newSegRend.prev = prev;
                            newSegRend.next = next;
                            next.prev = newSegRend;
                            wire.insertSegment(hoverYSegment.segment, newSeg);
                            elements.add(newSegRend);
                            wire.removeSegment(hoverYSegment.segment);
                            elements.remove(hoverYSegment);
                        }
                    } else {
                        // replace branch with segment
                        RenderWireSegment prev = hoverYSegment.prev.prev;
                        RenderWireSegment next = hoverYSegment.prev.branch;
                        wire.removeSegment(hoverYSegment.segment);
                        wire.removeSegment(hoverYSegment.prev.segment);
                        Wire.WireSegment newSeg = new Wire.WireSegment(
                            hoverYSegment.prev.segment.x,
                            hoverYSegment.prev.segment.y
                        );
                        wire.insertSegment(prev.segment, newSeg);
                        RenderWireSegment newSegRend = new RenderWireSegment(newSeg);
                        prev.next = newSegRend;
                        newSegRend.prev = prev;
                        newSegRend.next = next;
                        next.prev = newSegRend;
                        Wire.WireSegment prevSeg = newSeg;
                        for (Wire.WireSegment seg : ((Wire.WireBranch) hoverYSegment.prev.segment).subSegments) {
                            wire.insertSegment(prevSeg, seg);
                            prevSeg = seg;
                        }
                        elements.remove(hoverYSegment.prev);
                        elements.remove(hoverYSegment);
                        elements.add(newSegRend);
                    }
                } else {
                    // delete segment
                    hoverYSegment.prev.next = null;
                    elements.remove(hoverYSegment);
                    wire.removeSegment(hoverYSegment.segment);
                }
                hoverXSegment = null;
                hoverYSegment = null;
                return true;
            } else if (hoverXSegment != null) {
                if (hoverXSegment.prev.segment instanceof Wire.WireBranch) {
                    if (hoverXSegment.prev.branch == hoverXSegment) {
                        if (hoverXSegment.prev.prev.segment.x == hoverXSegment.prev.next.segment.x ||
                            hoverXSegment.prev.prev.segment.y == hoverXSegment.prev.next.segment.y) {
                            // delete branch & segment
                            RenderWireSegment prev = hoverXSegment.prev.prev;
                            RenderWireSegment next = hoverXSegment.prev.next;
                            prev.next = next;
                            next.prev = prev;
                            elements.remove(hoverXSegment.prev);
                            elements.remove(hoverXSegment);
                            wire.removeSegment(hoverXSegment.prev.segment);
                            wire.removeSegment(hoverXSegment.segment);
                        } else {
                            // replace branch with segment
                            RenderWireSegment prev = hoverXSegment.prev;
                            RenderWireSegment next = hoverXSegment.next;
                            Wire.WireSegment newSeg = new Wire.WireSegment(
                                hoverXSegment.prev.segment.x,
                                hoverXSegment.prev.segment.y
                            );
                            RenderWireSegment newSegRend = new RenderWireSegment(newSeg);
                            prev.next = newSegRend;
                            newSegRend.prev = prev;
                            newSegRend.next = next;
                            next.prev = newSegRend;
                            wire.insertSegment(hoverXSegment.segment, newSeg);
                            elements.add(newSegRend);
                            wire.removeSegment(hoverXSegment.segment);
                            elements.remove(hoverXSegment);
                        }
                    } else {
                        // replace branch with segment
                        RenderWireSegment prev = hoverXSegment.prev.prev;
                        RenderWireSegment next = hoverXSegment.prev.branch;
                        wire.removeSegment(hoverXSegment.segment);
                        wire.removeSegment(hoverXSegment.prev.segment);
                        Wire.WireSegment newSeg = new Wire.WireSegment(
                            hoverXSegment.prev.segment.x,
                            hoverXSegment.prev.segment.y
                        );
                        wire.insertSegment(prev.segment, newSeg);
                        RenderWireSegment newSegRend = new RenderWireSegment(newSeg);
                        prev.next = newSegRend;
                        newSegRend.prev = prev;
                        newSegRend.next = next;
                        next.prev = newSegRend;
                        Wire.WireSegment prevSeg = newSeg;
                        for (Wire.WireSegment seg : ((Wire.WireBranch) hoverXSegment.prev.segment).subSegments) {
                            wire.insertSegment(prevSeg, seg);
                            prevSeg = seg;
                        }
                        elements.remove(hoverXSegment.prev);
                        elements.remove(hoverXSegment);
                        elements.add(newSegRend);
                    }
                } else {
                    // delete segment
                    hoverXSegment.prev.next = null;
                    elements.remove(hoverXSegment);
                    wire.removeSegment(hoverXSegment.segment);
                }
                hoverXSegment = null;
                hoverYSegment = null;
                return true;
            }
        }
        return super.onKey(keycode, scancode, action, mods);
    }

    @Override
    public void onFocusLost(BaseElement nextFocus) {
        super.onFocusLost(nextFocus);
//        System.out.println("Focus lost");
        if (nextFocus instanceof RenderCode) {
            if (hoverXSegment != null) {
                Wire.WireSegment next = new Wire.WireSegment(hoverXSegment.segment.x, hoverXSegment.segment.y);
                wire.insertSegment(hoverXSegment.segment, next);
                hoverYSegment = new RenderWireSegment(next);
                hoverYSegment.prev = hoverXSegment;
                hoverXSegment.next = hoverYSegment;
                elements.add(hoverYSegment);
                hoverXSegment = hoverYSegment;
                ((RenderCode) nextFocus).focusedElement = this;
            } else if (hoverYSegment != null) {
                Wire.WireSegment next = new Wire.WireSegment(hoverYSegment.segment.x, hoverYSegment.segment.y);
                wire.insertSegment(hoverYSegment.segment, next);
                hoverXSegment = new RenderWireSegment(next);
                hoverXSegment.prev = hoverYSegment;
                hoverYSegment.next = hoverXSegment;
                elements.add(hoverXSegment);
                hoverYSegment = hoverXSegment;
                ((RenderCode) nextFocus).focusedElement = this;
            }
        } else if (nextFocus instanceof RenderBlock) {
            // replace segment with endpoint
            if (hoverXSegment != null) {
                Wire.WireEndpoint end = new Wire.WireEndpoint(((RenderBlock) nextFocus).block.id, hoverXSegment.segment.x, hoverXSegment.segment.y);
                wire.insertSegment(hoverXSegment.segment, end);
                wire.removeSegment(hoverXSegment.segment);
                hoverXSegment.segment = end;
                hoverXSegment = null;
                hoverYSegment = null;
            } else if (hoverYSegment != null) {
                Wire.WireEndpoint end = new Wire.WireEndpoint(((RenderBlock) nextFocus).block.id, hoverYSegment.segment.x, hoverYSegment.segment.y);
                wire.insertSegment(hoverYSegment.segment, end);
                wire.removeSegment(hoverYSegment.segment);
                hoverYSegment.segment = end;
                hoverXSegment = null;
                hoverYSegment = null;
            }
        } else {
            hoverXSegment = null;
            hoverYSegment = null;
        }
    }

    protected class RenderWireSegment extends BaseElement {
        protected static final float LINE_WIDTH = 1f;
        protected static final float BRANCH_RADIUS = .025f;
        Wire.WireSegment segment;
        public RenderWireSegment prev;
        public RenderWireSegment next;
        public RenderWireSegment branch;

        public RenderWireSegment(Wire.WireSegment segment) {
            this.segment = segment;
        }

        @Override
        public void onRender(float mouseX, float mouseY) {
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

        @Override
        public void onFocus(BaseElement prevFocus) {
        }

        @Override
        public boolean onClick(float x, float y, int button) {
            if (this == hoverXSegment || this == hoverYSegment) {
                return false;
            }
            if (button == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
                hoverXSegment = null;
                hoverYSegment = null;

                // insert a branch
                // snap to wire
                Wire.WireBranch branch;
                Wire.WireSegment next;
                if (prev.segment.x == segment.x) {
                     branch = new Wire.WireBranch(segment.x, y);
                    next = new Wire.WireSegment(branch.x, branch.y);
                } else if (prev.segment.y == segment.y) {
                    branch = new Wire.WireBranch(x, segment.y);
                    next = new Wire.WireSegment(branch.x, branch.y);
                } else {
                    return false;
                }
                RenderWireSegment segRend = new RenderWireSegment(branch);
                RenderWireSegment nextSeg = new RenderWireSegment(next);
                if (prev.segment.x == segment.x) {
                    hoverXSegment = nextSeg;
                } else if (prev.segment.y == segment.y) {
                    hoverYSegment = nextSeg;
                }
                segRend.prev = prev;
                segRend.next = this;
                this.prev.next = segRend;
                this.prev = segRend;
                segRend.branch = nextSeg;
                nextSeg.prev = segRend;
                elements.add(segRend);
                elements.add(nextSeg);
                wire.insertSegment(prev.segment, branch);
                branch.insertSegment(null, next);
            } else if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
                hoverXSegment = null;
                hoverYSegment = null;
                if (next != null) return true;
                if (segment instanceof Wire.WireEndpoint) {
                    // replace endpoint with segment
                    Wire.WireSegment ns = new Wire.WireSegment(segment.x, segment.y);
                    wire.insertSegment(segment, ns);
                    wire.removeSegment(segment);
                    segment = ns;
                }
                if (prev.segment.x == segment.x) {
                    hoverXSegment = this;
                    hoverYSegment = this;
                } else if (prev.segment.y == segment.y) {
                    hoverXSegment = this;
                    hoverYSegment = this;
                }
            }
            return true;
        }

    }

}
