package xyz.wagyourtail.konig.editor.canvas.blocks;

import org.lwjgl.opengl.GL11;
import xyz.wagyourtail.MathHelper;
import xyz.wagyourtail.konig.structure.headers.KonigBlock;
import xyz.wagyourtail.wagyourgui.Font;
import xyz.wagyourtail.wagyourgui.GLBuilder;
import xyz.wagyourtail.wagyourgui.Texture;
import xyz.wagyourtail.wagyourgui.elements.DrawableHelper;
import xyz.wagyourtail.konig.structure.code.KonigBlockReference;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

@FunctionalInterface
public interface BlockRenderer {
    Map<String, BlockRenderer> byBlockName = new HashMap<>();

    //TODO: make this a cache of some kind?
    Map<KonigBlock, Texture> blockTextures = new HashMap<>();

    static BlockRenderer register(String name, BlockRenderer renderer) {
        byBlockName.put(name, renderer);
        return renderer;
    }

    BlockRenderer DEFAULT = (block, rect, viewport, mouseX, mouseY, font) -> {

        // render block
        KonigBlock blockSpec = block.attemptToGetBlockSpec();
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
                // render in yellow
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

    BlockRenderer COSNT = register("const", new ConstBlock());

    static void render(KonigBlockReference block, MathHelper.Rectangle viewport, float mouseX, float mouseY, Font font) {
        BlockRenderer renderer = byBlockName.getOrDefault(block.name, DEFAULT);

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

        renderer.render(block, r.get(), viewport, mouseX, mouseY, font);
    }

    void render(KonigBlockReference block, MathHelper.Rectangle clippedBlock, MathHelper.Rectangle viewport, float mouseX, float mouseY, Font font);
}
