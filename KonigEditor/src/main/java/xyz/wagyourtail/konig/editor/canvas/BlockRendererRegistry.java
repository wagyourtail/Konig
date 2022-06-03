package xyz.wagyourtail.konig.editor.canvas;

import org.lwjgl.opengl.GL11;
import xyz.wagyourtail.MathHelper;
import xyz.wagyourtail.wagyourgui.Font;
import xyz.wagyourtail.wagyourgui.elements.DrawableHelper;
import xyz.wagyourtail.konig.structure.code.KonigBlockReference;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

public class BlockRendererRegistry {
    private static Map<String, BlockRenderer> byBlockName = new HashMap<>();


    public static BlockRenderer register(String name, BlockRenderer renderer) {
        byBlockName.put(name, renderer);
        return renderer;
    }

    private static final BlockRenderer DEFAULT = (block, viewport, mouseX, mouseY, font) -> {
        // check if intersects view box
        AtomicReference<MathHelper.Rectangle> r = new AtomicReference<>(new MathHelper.Rectangle(
            block.x,
            block.y,
            block.x + block.scaleX,
            block.y + block.scaleY
        ));

        if (!MathHelper.clipRect(r, viewport)) {
            return;
        }

        MathHelper.Rectangle rect = r.get();

        // render block
        DrawableHelper.rect(
            rect.topLeftX(),
            rect.topLeftY(),
            rect.bottomRightX(),
            rect.bottomRightY(),
            0xFF7F7F7F
        );

        // check if rect is trimmed
        if (rect.topLeftX() != block.x || rect.topLeftY() != block.y || rect.bottomRightX() != block.x + block.scaleX || rect.bottomRightY() != block.y + block.scaleY) {
            return;
        }

        // draw name on top, scaled to fill
        float width = font.getWidth(block.name) * 2;
        GL11.glPushMatrix();
        if (width > block.scaleX) {
            GL11.glTranslatef(block.x + block.scaleX / 2, block.y + block.scaleY / 2, 0);
            GL11.glScalef(block.scaleX / width, block.scaleY / width, 1);
        }
        DrawableHelper.drawCenteredString(font, block.name, 0, 0, 0xFF000000);
        GL11.glPopMatrix();
    };

    public static void render(KonigBlockReference ref, MathHelper.Rectangle viewport, float mouseX, float mouseY, Font font) {
        BlockRenderer renderer = byBlockName.getOrDefault(ref.name, DEFAULT);
        renderer.render(ref, viewport, mouseX, mouseY, font);
    }

    @FunctionalInterface
    public interface BlockRenderer {
        void render(KonigBlockReference block, MathHelper.Rectangle viewport, float mouseX, float mouseY, Font font);
    }
}
