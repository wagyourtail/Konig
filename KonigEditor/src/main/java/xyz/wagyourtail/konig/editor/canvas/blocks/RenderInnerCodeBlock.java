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
import xyz.wagyourtail.wagyourgui.glfw.Window;

import java.util.Map;
import java.util.Optional;

public class RenderInnerCodeBlock<T extends RenderBlockParent & RenderCodeParent> extends RenderBlock implements RenderCodeParent {

    public RenderInnerCodeBlock(KonigBlockReference block, Font font, T code) {
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
            if (hollow.getKey().startsWith("$ungrouped$")) {
                Hollow h = hollow.getValue().get(hollow.getKey().substring(11));
                InnerCode c = block.innerCodeMap.get(h.name);
                if (c == null) {
                    c = new InnerCode(block);
                    c.name = h.name;
                    block.innerCodeMap.put(h.name, c);
                }
                if (code != null)
                    elements.add(new RenderInnerCode(
                        (float) h.paddingLeft,
                        (float) (y0 + h.paddingTop),
                        (float) (block.scaleX - h.paddingRight - h.paddingLeft),
                        (float) (size - h.paddingBottom - h.paddingTop),
                        c,
                        font,
                        code.getWindow()
                    ));
                y0 += size;
            } else {

            }
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
                focusedElement.onFocusLost(code);
            }
            focusedElement = code;
            focusedElement.onFocus(prevFocus);
        }
    }

    public class RenderInnerCode extends RenderCode {

        public RenderInnerCode(float x, float y, float width, float height, Code code, Font font, Window window) {
            super(RenderInnerCodeBlock.this, x, y, width, height, code, font, window);
            allowViewportDrag = false;
            allowViewportZoom = false;
            viewportWidth = width;
            viewportHeight = height;
            renderBorder = false;
        }

    }
}
