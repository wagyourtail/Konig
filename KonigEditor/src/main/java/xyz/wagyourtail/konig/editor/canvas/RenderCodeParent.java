package xyz.wagyourtail.konig.editor.canvas;

import java.util.Optional;

public interface RenderCodeParent {

    Optional<RenderBlock> getPlacingBlock();

    void setPlacingBlock(RenderBlock block);
}
