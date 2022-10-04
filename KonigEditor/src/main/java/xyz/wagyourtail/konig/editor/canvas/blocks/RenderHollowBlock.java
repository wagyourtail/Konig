package xyz.wagyourtail.konig.editor.canvas.blocks;

import xyz.wagyourtail.konig.editor.canvas.RenderBlock;
import xyz.wagyourtail.konig.editor.canvas.RenderBlockParent;
import xyz.wagyourtail.konig.editor.canvas.RenderCode;
import xyz.wagyourtail.konig.editor.canvas.RenderCodeParent;
import xyz.wagyourtail.konig.structure.code.Code;
import xyz.wagyourtail.konig.structure.code.InnerCode;
import xyz.wagyourtail.konig.structure.code.KonigBlockReference;
import xyz.wagyourtail.konig.structure.headers.BlockIO;
import xyz.wagyourtail.konig.structure.headers.Hollow;
import xyz.wagyourtail.wagyourgui.Font;
import xyz.wagyourtail.wagyourgui.elements.BaseElement;
import xyz.wagyourtail.wagyourgui.elements.DrawableHelper;
import xyz.wagyourtail.wagyourgui.elements.ElementContainer;
import xyz.wagyourtail.wagyourgui.elements.Selector;
import xyz.wagyourtail.wagyourgui.glfw.Cursors;
import xyz.wagyourtail.wagyourgui.glfw.Window;

import java.util.*;

public class RenderHollowBlock<T extends RenderBlockParent & RenderCodeParent> extends RenderBlock {

    public RenderHollowBlock(KonigBlockReference block, Font font, T code) {
        super(block, font, code);
    }

    @Override
    protected void initIO() {
        super.initIO();
        initInnerCode();
    }

    public void initInnerCode() {
        float size = block.scaleY / blockSpec.hollowsByGroupName.size();
        float y0 = 0;
        for (Map.Entry<String, Map<String, Hollow>> hollow : blockSpec.hollowsByGroupName.entrySet()) {
            elements.add(new HollowContainer(hollow.getKey(), hollow.getValue(), y0, size));
            y0 += size;
        }
    }

    public class HollowContainer extends ElementContainer implements RenderCodeParent {
        final String groupName;
        final Map<String, Hollow> hollowsForGroup;
        final float y0;
        final float height;
        Selector selector;
        RenderInnerCode ric;

        Hollow currentHollow;

        public HollowContainer(String groupName, Map<String, Hollow> hollowsForGroup, float y0, float height) {
            this.groupName = groupName;
            this.hollowsForGroup = hollowsForGroup;
            this.y0 = y0;
            this.height = height;

            boolean ungrouped = groupName.startsWith("$ungrouped$");
            Hollow h;
            if (ungrouped) {
                h = hollowsForGroup.get(groupName.substring(11));
            } else {
                h = hollowsForGroup.get(hollowsForGroup.keySet().stream().findFirst().orElseThrow());
                Set<String> options = hollowsForGroup.keySet();
                selector = new Selector(
                    block.scaleX / 2 - .4f,
                    (float) (y0 + h.paddingTop / 2),
                    .8f,
                    height,
                    .2f,
                    font,
                    h.name,
                    0xFF000000,
                    0xFF4F4F4F,
                    0xFFFFFFFF,
                    (s) -> select(s.getSelected())
                );
                selector.setOptions(new ArrayList<>(options));
            }

            select(h.name);
        }

        @Override
        public Optional<RenderBlock> getPlacingBlock() {
            Optional<RenderBlock> rb = ((T) code).getPlacingBlock();
            if (rb.isPresent() && rb.get().equals(RenderHollowBlock.this)) {
                return Optional.empty();
            }
            return rb;
        }

        @Override
        public void setPlacingBlock(RenderBlock block) {
            ((T) code).setPlacingBlock(block);
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

        public void select(String name) {
            currentHollow = hollowsForGroup.get(name);
            elements.clear();
            if (selector != null)
                elements.add(selector);

            InnerCode c = block.innerCodeMap.get(currentHollow.name);
            if (c == null) {
                c = new InnerCode(block);
                c.name = currentHollow.name;
                ((InnerCode.InnerParent) c.parent).applyBlock(c.outer, c.name);
                block.innerCodeMap.put(currentHollow.name, c);
            }
            if (code == null)
                return;
            ric = new RenderInnerCode(
                (float) currentHollow.paddingLeft,
                (float) (y0 + currentHollow.paddingTop),
                (float) (block.scaleX - currentHollow.paddingRight - currentHollow.paddingLeft),
                (float) (height - currentHollow.paddingBottom - currentHollow.paddingTop),
                c,
                font,
                code.getWindow()
            );
            addInnerIOFor();
            elements.add(ric);
        }

        public void addInnerIOFor() {
            for (Map.Entry<BlockIO.Side, Map<BlockIO.Justify, List<BlockIO.IOElement>>> side : currentHollow.elements.entrySet()) {
                for (Map.Entry<BlockIO.Justify, List<BlockIO.IOElement>> justify : side.getValue().entrySet()) {
                    for (int i = 0; i < justify.getValue().size(); i++) {
                        BlockIO.IOElement io = justify.getValue().get(i);
                        addInnerIO(io, i, justify.getValue().size());
                    }
                }
            }
        }

        public void addInnerIO(BlockIO.IOElement io, int index, int countOnJustify) {
            float x = (float) currentHollow.paddingLeft;
            float y = y0 + (float) currentHollow.paddingTop;
            float w = (float) (block.scaleX - currentHollow.paddingRight - currentHollow.paddingLeft);
            float h = (float) (height - currentHollow.paddingBottom - currentHollow.paddingTop);
            switch (io.side) {
                case LEFT:
                    y += getJustifyPos(io.justify, index, countOnJustify, h);
                    break;
                case RIGHT:
                    x += w;
                    y += getJustifyPos(io.justify, index, countOnJustify, h);
                    break;
                case TOP:
                    x += getJustifyPos(io.justify, index, countOnJustify, w);
                    break;
                case BOTTOM:
                    x += getJustifyPos(io.justify, index, countOnJustify, w);
                    y += h;
                    break;
            }
            elements.add(new InnerIOPlug(x, y, io));
        }

        public class InnerIOPlug extends BaseElement {
            private static final float PORT_RADIUS = .05f;
            public final BlockIO.IOElement element;

            private final float x;
            private final float y;

            public InnerIOPlug(float x, float y, BlockIO.IOElement element) {
                this.element = element;
                this.x = x;
                this.y = y;
            }

            @Override
            public void onHover(float x, float y) {
                code.getWindow().setCursor(Cursors.CROSSHAIR);
            }

            @Override
            public void onHoverLost() {
                code.getWindow().setCursor(Cursors.ARROW);
            }

            @Override
            public boolean isMouseOver(float x, float y) {
                return shouldFocus(x, y);
            }

            @Override
            public boolean shouldFocus(float mouseX, float mouseY) {
                return mouseX >= x - PORT_RADIUS && mouseX <= x + PORT_RADIUS && mouseY >= y - PORT_RADIUS &&
                    mouseY <= y + PORT_RADIUS;
            }

            @Override
            public void onFocus(BaseElement prevFocus) {
                super.onFocus(prevFocus);
            }

            @Override
            public boolean onClick(float x, float y, int button) {
                return super.onClick(x, y, button);
                // todo
            }

            @Override
            public void onRender(float mouseX, float mouseY) {
                // TODO: check if plugged in

                DrawableHelper.rect(
                    x - PORT_RADIUS,
                    y - PORT_RADIUS,
                    x + PORT_RADIUS,
                    y + PORT_RADIUS,
                    0xFF000000
                );
            }

        }

        public class RenderInnerCode extends RenderCode {

            public RenderInnerCode(float x, float y, float width, float height, Code code, Font font, Window window) {
                super(HollowContainer.this, x, y, width, height, code, font, window);
                allowViewportDrag = false;
                allowViewportZoom = false;
                viewportWidth = width;
                viewportHeight = height;
                renderBorder = false;
            }

            @Override
            public float getWireWidth() {
                return RenderHollowBlock.this.code.getWireWidth() * RenderHollowBlock.this.code.viewportWidth() / RenderHollowBlock.this.code.width();
            }

        }

    }
}
