package xyz.wagyourtail.konig.editor.canvas.blocks;

import xyz.wagyourtail.konig.editor.canvas.RenderBlock;
import xyz.wagyourtail.konig.editor.canvas.RenderBlockParent;
import xyz.wagyourtail.konig.editor.canvas.RenderCode;
import xyz.wagyourtail.konig.editor.canvas.RenderCodeParent;
import xyz.wagyourtail.konig.structure.code.Code;
import xyz.wagyourtail.konig.structure.code.InnerCode;
import xyz.wagyourtail.konig.structure.code.KonigBlockReference;
import xyz.wagyourtail.konig.structure.headers.Hollow;
import xyz.wagyourtail.wagyourgui.Font;
import xyz.wagyourtail.wagyourgui.elements.BaseElement;
import xyz.wagyourtail.wagyourgui.elements.Selector;
import xyz.wagyourtail.wagyourgui.glfw.Window;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

public class RenderHollowBlock<T extends RenderBlockParent & RenderCodeParent> extends RenderBlock implements RenderCodeParent {

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
            boolean ungrouped = hollow.getKey().startsWith("$ungrouped$");
            Hollow h;
            if (ungrouped) {
                h = hollow.getValue().get(hollow.getKey().substring(11));
            } else {
                h = hollow.getValue().get(hollow.getValue().keySet().stream().findFirst().orElseThrow());
            }
            InnerCode c = block.innerCodeMap.get(h.name);
            if (c == null) {
                c = new InnerCode(block);
                c.name = h.name;
                ((InnerCode.InnerParent) c.parent).applyBlock(c.outer, c.name);
                block.innerCodeMap.put(h.name, c);
            }
            if (code == null) return;
            RenderInnerCode ric = new RenderInnerCode(
                (float) h.paddingLeft,
                (float) (y0 + h.paddingTop),
                (float) (block.scaleX - h.paddingRight - h.paddingLeft),
                (float) (size - h.paddingBottom - h.paddingTop),
                c,
                font,
                code.getWindow()
            );
            if (!ungrouped) {
                Set<String> options = hollow.getValue().keySet();
                AtomicReference<RenderInnerCode> r = new AtomicReference<>(ric);
                float finalY = y0;
                Selector s = new Selector(
                    block.scaleX / 2 - .4f,
                    (float) (y0 + h.paddingTop / 2),
                    .8f,
                    size,
                    .2f,
                    font,
                    h.name,
                    0xFF000000,
                    0xFF4F4F4F,
                    0xFFFFFFFF,
                    (s2) -> {
                        Hollow h2 = hollow.getValue().get(s2.getSelected());
                        InnerCode c2 = block.innerCodeMap.get(h2.name);
                        if (c2 == null) {
                            c2 = new InnerCode(block);
                            c2.name = h2.name;
                            ((InnerCode.InnerParent) c2.parent).applyBlock(c2.outer, c2.name);
                            block.innerCodeMap.put(s2.getSelected(), c2);
                        }
                        RenderInnerCode ric2 = new RenderInnerCode(
                            (float) h2.paddingLeft,
                            (float) (finalY + h2.paddingTop),
                            (float) (block.scaleX - h2.paddingRight - h2.paddingLeft),
                            (float) (size - h2.paddingBottom - h2.paddingTop),
                            c2,
                            font,
                            code.getWindow()
                        );
                        Deque<BaseElement> stack = new ArrayDeque<>();
                        BaseElement el = elements.pollLast();
                        while (el != r.get()) {
                            stack.add(el);
                            el = elements.pollLast();
                        }
                        r.set(ric2);
                        elements.add(ric2);
                        elements.addAll(stack);
                    }
                );
                s.setOptions(new ArrayList<>(options));
                elements.add(s);
            }
            elements.add(ric);
            y0 += size;
        }
    }

    @Override
    public Optional<RenderBlock> getPlacingBlock() {
        Optional<RenderBlock> rb = ((T) code).getPlacingBlock();
        if (rb.isPresent() && rb.get().equals(this)) {
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
                return;
            }
            focusedElement = code;
            focusedElement.onFocus(prevFocus);
        }
    }

    public class RenderInnerCode extends RenderCode {

        public RenderInnerCode(float x, float y, float width, float height, Code code, Font font, Window window) {
            super(RenderHollowBlock.this, x, y, width, height, code, font, window);
            allowViewportDrag = false;
            allowViewportZoom = false;
            viewportWidth = width;
            viewportHeight = height;
            renderBorder = false;
        }

    }
}
