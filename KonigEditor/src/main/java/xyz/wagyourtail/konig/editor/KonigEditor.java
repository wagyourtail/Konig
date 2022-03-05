package xyz.wagyourtail.konig.editor;

import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;
import org.lwjgl.system.MemoryStack;

import java.io.IOException;
import java.nio.IntBuffer;
import java.nio.file.Path;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL11C.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL11C.glClearColor;

public class KonigEditor {
    private Window window;
    private Font f;

    public void start() throws IOException {
        init();
        loop();

        window.shutdown();
        glfwTerminate();
        glfwSetErrorCallback(null).free();
    }

    public void init() throws IOException {
        GLFWErrorCallback.createPrint(System.err).set();

        if (!glfwInit()) {
            throw new IllegalStateException("Unable to initialize GLFW");
        }

        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);
        glfwWindowHint(GLFW_OPENGL_DEBUG_CONTEXT, GLFW_TRUE);

        window = new Window("Konig Editor", 800, 600);

        glfwMakeContextCurrent(window.handle);
        GL.createCapabilities();

        window.setupFramebuffer();

        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer pWidth = stack.mallocInt(1);
            IntBuffer pHeight = stack.mallocInt(1);

            glfwGetWindowSize(window.handle, pWidth, pHeight);

            GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());

            glfwSetWindowPos(
                window.handle,
                (vidmode.width() - pWidth.get(0)) / 2,
                (vidmode.height() - pHeight.get(0)) / 2
            );
        }


        // make the OpenGL context current
        glfwMakeContextCurrent(window.handle);

        // Enable v-sync
        glfwSwapInterval(1);

        f = new Font("demo/UbuntuMono-R.ttf");
        window.setVisible(true);
    }

    public void loop() {
        GL.createCapabilities();

        glClearColor(1.0f, 0.0f, 0.0f, 0.0f);

        while (!glfwWindowShouldClose(window.handle)) {
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
            GL14.glBlendFuncSeparate(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, GL_ONE, GL_ZERO);

            glPushMatrix();

            glTranslatef(-1, 1, 0f);
            glScalef(2f / window.getWidth(), -2f / window.getHeight(), 1f);

            glEnable(GL_BLEND);
            glEnable(GL_POLYGON_SMOOTH);
            glEnable(GL_LINE_SMOOTH);
            glEnable(GL_POINT_SMOOTH);
            glDisable(GL_TEXTURE_2D);
            glColor3f(1f, 0f, 0f);
            GL11.glBegin(GL_QUADS);
            GL11.glVertex2f(0, 0);
            GL11.glVertex2f(0, 100);
            GL11.glVertex2f(100, 100);
            GL11.glVertex2f(100, 0);
            GL11.glEnd();

            glColor3f(1f, 1f, 0f);
            glEnable(GL_TEXTURE_2D);
            f.drawString("Hello, world!", 0, 25);

            glPopMatrix();

            glfwSwapBuffers(window.handle);
            glfwPollEvents();
        }
    }
}
