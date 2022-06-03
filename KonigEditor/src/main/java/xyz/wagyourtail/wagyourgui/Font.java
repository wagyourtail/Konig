package xyz.wagyourtail.wagyourgui;

import org.lwjgl.BufferUtils;
import org.lwjgl.stb.STBTTAlignedQuad;
import org.lwjgl.stb.STBTTBakedChar;
import org.lwjgl.stb.STBTTFontinfo;
import org.lwjgl.system.MemoryStack;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.lwjgl.BufferUtils.createByteBuffer;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.stb.STBTruetype.*;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.system.MemoryUtil.memSlice;

public class Font {
    public final ByteBuffer fontBuffer;
    public final STBTTFontinfo fontInfo;

    private final float scale;

    private final int ascent;
    private final int descent;
    private final int lineGap;
    public final int FONT_HEIGHT = 16;

    private STBTTBakedChar.Buffer cdata;

    public Font(String fontPath) throws IOException {
        this.fontBuffer = ioResourceToByteBuffer(fontPath, 512 * 1024);
        fontInfo = STBTTFontinfo.create();
        if (!stbtt_InitFont(fontInfo, fontBuffer)) {
            throw new IOException("Failed to load font: " + fontPath);
        }

        try (MemoryStack stack = stackPush()) {
            IntBuffer pAscent  = stack.mallocInt(1);
            IntBuffer pDescent = stack.mallocInt(1);
            IntBuffer pLineGap = stack.mallocInt(1);

            stbtt_GetFontVMetrics(fontInfo, pAscent, pDescent, pLineGap);

            ascent = pAscent.get(0);
            descent = pDescent.get(0);
            lineGap = pLineGap.get(0);
        }

        scale = stbtt_ScaleForPixelHeight(fontInfo, FONT_HEIGHT);
    }

    private STBTTBakedChar.Buffer init(int BITMAP_W, int BITMAP_H) {
        int                   texID = glGenTextures();
        STBTTBakedChar.Buffer cdata = STBTTBakedChar.malloc(96);

        ByteBuffer bitmap = createByteBuffer(BITMAP_W * BITMAP_H);
        stbtt_BakeFontBitmap(fontBuffer, 32, bitmap, BITMAP_W, BITMAP_H, 32, cdata);

        glBindTexture(GL_TEXTURE_2D, texID);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_ALPHA, BITMAP_W, BITMAP_H, 0, GL_ALPHA, GL_UNSIGNED_BYTE, bitmap);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);

        glClearColor(43f / 255f, 43f / 255f, 43f / 255f, 0f); // BG color
        glColor3f(169f / 255f, 183f / 255f, 198f / 255f); // Text color

        glEnable(GL_TEXTURE_2D);
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        return cdata;
    }

    public float drawString(String text, float x, float y) {
        // move y to top instead of bottom
        y += FONT_HEIGHT;

        if (cdata == null) {
            cdata = init(1024, 1024);
        }

        try (MemoryStack stack = stackPush()) {
            IntBuffer pCodePoint = stack.mallocInt(1);

            FloatBuffer xP = stack.floats(0f);
            FloatBuffer yP = stack.floats(0f);

            STBTTAlignedQuad q = STBTTAlignedQuad.mallocStack(stack);

            int lineStart = 0;
            float lineY = 0;

            glBegin(GL_QUADS);
            for (int i = 0, to = text.length(); i < to;) {
                i += getCP(text, to, i, pCodePoint);
                int cp = pCodePoint.get(0);
                if (cp == '\n') {
                    throw new IllegalStateException("Newline not supported");
                }
                float cpX = xP.get(0);
                float cpY = yP.get(0);
                stbtt_GetBakedQuad(cdata, 1024, 1024, cp - 32, xP, yP, q, true);
                xP.put(0, scale(cpX, xP.get(0), 1));
                // kerning
                if (i < to) {
                    getCP(text, to, i, pCodePoint);
                    xP.put(0, xP.get(0) + stbtt_GetCodepointKernAdvance(fontInfo, cp, pCodePoint.get(0)) * 1f);
                }
                float x0 = scale(cpX, q.x0(), 1f);
                float x1 = scale(cpX, q.x1(), 1f);
                float y0 = scale(cpY, q.y0(), 1f);
                float y1 = scale(cpY, q.y1(), 1f);

                glTexCoord2f(q.s0(), q.t0());
                glVertex2f(x0 + x, y0 + y);

                glTexCoord2f(q.s1(), q.t0());
                glVertex2f(x1 + x, y0 + y);

                glTexCoord2f(q.s1(), q.t1());
                glVertex2f(x1 + x, y1 + y);

                glTexCoord2f(q.s0(), q.t1());
                glVertex2f(x0 + x, y1 + y);
            }
            glEnd();
            return q.x1();
        }
    }

    public float getWidth(String text) {
        int width = 0;

        try (MemoryStack stack = stackPush()) {
            IntBuffer pCodePoint       = stack.mallocInt(1);
            IntBuffer pAdvancedWidth   = stack.mallocInt(1);
            IntBuffer pLeftSideBearing = stack.mallocInt(1);

            int i = 0;
            while (i < text.length()) {
                i += getCP(text, text.length(), i, pCodePoint);
                int cp = pCodePoint.get(0);

                stbtt_GetCodepointHMetrics(fontInfo, cp, pAdvancedWidth, pLeftSideBearing);
                width += pAdvancedWidth.get(0);

                if (i < text.length()) {
                    getCP(text, text.length(), i, pCodePoint);
                    width += stbtt_GetCodepointKernAdvance(fontInfo, cp, pCodePoint.get(0));
                }
            }
        }

        return width * scale * 2;
    }

    public float drawTrimmed(String text, float x, float y, float width) {
        if (cdata == null) {
            cdata = init(1024, 1024);
        }

        try (MemoryStack stack = stackPush()) {
            IntBuffer pCodePoint = stack.mallocInt(1);

            FloatBuffer xP = stack.floats(0f);
            FloatBuffer yP = stack.floats(0f);

            STBTTAlignedQuad q = STBTTAlignedQuad.mallocStack(stack);

            int lineStart = 0;
            float lineY = 0;

            glBegin(GL_QUADS);
            for (int i = 0, to = text.length(); i < to;) {
                i += getCP(text, to, i, pCodePoint);
                int cp = pCodePoint.get(0);
                if (cp == '\n') {
                    throw new IllegalStateException("Newline not supported");
                }
                float cpX = xP.get(0);
                float cpY = yP.get(0);
                stbtt_GetBakedQuad(cdata, 1024, 1024, cp - 32, xP, yP, q, true);
                xP.put(0, scale(cpX, xP.get(0), 1));
                // kerning
                if (i < to) {
                    getCP(text, to, i, pCodePoint);
                    xP.put(0, xP.get(0) + stbtt_GetCodepointKernAdvance(fontInfo, cp, pCodePoint.get(0)) * 1f);
                }
                float x0 = scale(cpX, q.x0(), 1f);
                float x1 = scale(cpX, q.x1(), 1f);
                float y0 = scale(cpY, q.y0(), 1f);
                float y1 = scale(cpY, q.y1(), 1f);

                if (q.x1() - x > width) {
                    glEnd();
                    return q.x0();
                }

                glTexCoord2f(q.s0(), q.t0());
                glVertex2f(x0 + x, y0 + y);

                glTexCoord2f(q.s1(), q.t0());
                glVertex2f(x1 + x, y0 + y);

                glTexCoord2f(q.s1(), q.t1());
                glVertex2f(x1 + x, y1 + y);

                glTexCoord2f(q.s0(), q.t1());
                glVertex2f(x0 + x, y1 + y);
            }
            glEnd();
            return q.x1();
        }
    }

    private static int getCP(String text, int to, int i, IntBuffer cpOut) {
        char c1 = text.charAt(i);
        if (Character.isHighSurrogate(c1) && i + 1 < to) {
            char c2 = text.charAt(i + 1);
            if (Character.isLowSurrogate(c2)) {
                cpOut.put(0, Character.toCodePoint(c1, c2));
                return 2;
            }
        }
        cpOut.put(0, c1);
        return 1;
    }

    private static float scale(float center, float offset, float factor) {
        return (offset - center) * factor + center;
    }

    public void free() {
        if (cdata != null) {
            cdata.free();
        }
    }

    private static ByteBuffer resizeBuffer(ByteBuffer buffer, int newCapacity) {
        ByteBuffer newBuffer = BufferUtils.createByteBuffer(newCapacity);
        buffer.flip();
        newBuffer.put(buffer);
        return newBuffer;
    }


    public static ByteBuffer ioResourceToByteBuffer(String resource, int bufferSize) throws IOException {
        ByteBuffer buffer;

        Path path = Paths.get(resource);
        if (Files.isReadable(path)) {
            try (SeekableByteChannel fc = Files.newByteChannel(path)) {
                buffer = BufferUtils.createByteBuffer((int)fc.size() + 1);
                while (fc.read(buffer) != -1) {
                    ;
                }
            }
        } else {
            try (
                InputStream source = Font.class.getClassLoader().getResourceAsStream(resource);
                ReadableByteChannel rbc = Channels.newChannel(source)
            ) {
                buffer = createByteBuffer(bufferSize);

                while (true) {
                    int bytes = rbc.read(buffer);
                    if (bytes == -1) {
                        break;
                    }
                    if (buffer.remaining() == 0) {
                        buffer = resizeBuffer(buffer, buffer.capacity() * 3 / 2); // 50%
                    }
                }
            }
        }

        buffer.flip();
        return memSlice(buffer);
    }
}
