package xyz.wagyourtail.konig.editor.canvas;

import xyz.wagyourtail.wagyourgui.elements.BaseElement;

import java.util.Optional;

public interface RenderCodeParent {

    Optional<RenderBlock> getPlacingBlock();

    void setPlacingBlock(RenderBlock block);

    void focusCode(BaseElement code);
}
