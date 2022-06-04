package xyz.wagyourtail.wagyourgui;

import org.lwjgl.opengl.GL11;
import org.lwjgl.stb.STBImage;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;

public class Texture implements AutoCloseable {
    private final ByteBuffer imageData;
    int[] w = new int[1];
    int[] h = new int[1];
    int[] comp = new int[1];

    private int texid;

    public Texture(Path path) throws IOException {
        ByteBuffer data = ByteBuffer.wrap(Files.readAllBytes(path));

        if (!STBImage.stbi_info_from_memory(data, w, h, comp)) {
            throw new IOException("Failed to load image");
        }

        imageData = STBImage.stbi_load_from_memory(data, w, h, comp, 4);
        if (imageData == null) {
            throw new IOException("Failed to load image");
        }
    }

    private void upload() {
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        texid = GL11.glGenTextures();

        GL11.glBindTexture(GL11.GL_TEXTURE_2D, texid);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_CLAMP);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_CLAMP);

        int format;
        if (comp[0] == 3) {
            if ((w[0] & 3) != 0) {
                GL11.glPixelStorei(GL11.GL_UNPACK_ALIGNMENT, 2 - (w[0] & 1));
            }
            format = GL11.GL_RGB;
        } else {
            GL11.glEnable(GL11.GL_BLEND);
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

            format = GL11.GL_RGBA;
        }

        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA8, w[0], h[0], 0, format, GL11.GL_UNSIGNED_BYTE, imageData);
    }

    public void bind() {
        if (texid == 0) {
            upload();
        }

        GL11.glBindTexture(GL11.GL_TEXTURE_2D, texid);
    }

    @Override
    public void close() throws Exception {
        GL11.glDeleteTextures(texid);
    }

}
