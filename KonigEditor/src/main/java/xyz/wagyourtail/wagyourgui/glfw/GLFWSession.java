package xyz.wagyourtail.wagyourgui.glfw;

import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;
import org.lwjgl.system.MemoryStack;
import xyz.wagyourtail.wagyourgui.Font;
import xyz.wagyourtail.wagyourgui.elements.DrawableHelper;
import xyz.wagyourtail.wagyourgui.screens.BaseScreen;
import xyz.wagyourtail.wagyourgui.screens.EditorMainScreen;

import java.io.IOException;
import java.nio.IntBuffer;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL11C.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL11C.glClearColor;

public class GLFWSession implements ResizeListener, MouseListener, KeyListener {
    public Window window;
    public Font font;
    public long fps;

    private BaseScreen screen = new EditorMainScreen(this);

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
        window.addResizeListener(this);
        window.addKeyListener(this);
        window.addMouseListener(this);

        glfwMakeContextCurrent(window.handle);
        GL.createCapabilities();

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

        font = new Font("demo/UbuntuMono-R.ttf");
        window.setVisible(true);
    }

    public void setScreen(BaseScreen screen) {
        this.screen = screen;
        screen.onWindowResize(window);
    }

    public void loop() {
        GL.createCapabilities();

        glClearColor(1.0f, 0.0f, 0.0f, 0.0f);
        onWindowResize(window);

        window.setupFramebuffer();
        long timeNanos = System.nanoTime();
        int frameCount = 0;

        while (!glfwWindowShouldClose(window.handle)) {
            ++frameCount;
            if (frameCount % 10 == 0) {
                fps = frameCount * 1000000000L / (System.nanoTime() - timeNanos);
                timeNanos = System.nanoTime();
                frameCount = 0;
            }
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
            GL14.glBlendFuncSeparate(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, GL_ONE, GL_ZERO);

            glPushMatrix();

            GL11.glDisable(GL_TEXTURE_2D);
            DrawableHelper.rect(-1, -1, 1, 1, 0xFF2b2b2b);

            glTranslatef(-1, 1, 0f);
            glScalef(2f / window.getWidth(), -2f / window.getHeight(), 1f);

//            glEnable(GL_BLEND);
//            glEnable(GL_POLYGON_SMOOTH);
//            glEnable(GL_LINE_SMOOTH);
//            glEnable(GL_POINT_SMOOTH);
//            glDisable(GL_TEXTURE_2D);
//            glColor3f(1f, 0f, 0f);
//            GL11.glBegin(GL_QUADS);
//            GL11.glVertex2f(0, 0);
//            GL11.glVertex2f(0, 100);
//            GL11.glVertex2f(100, 100);
//            GL11.glVertex2f(100, 0);
//            GL11.glEnd();
//
//            glColor3f(1f, 1f, 0f);
//            glEnable(GL_TEXTURE_2D);
//            f.drawString("Hello, world!", 0, 25);

            double[] cursorX = new double[1];
            double[] cursorY = new double[1];
            glfwGetCursorPos(window.handle, cursorX, cursorY);

            screen.onRender((float) cursorX[0], (float) cursorY[0]);

            glPopMatrix();

            glfwSwapBuffers(window.handle);
            glfwPollEvents();
        }
    }

    @Override
    public void onWindowResize(Window window) {
        screen.onWindowResize(window);
    }

    @Override
    public void onKey(int key, int scancode, int action, int mods) {
        screen.onKey(key, scancode, action, mods);
    }

    @Override
    public void onChar(int codepoint) {
        screen.onChar(codepoint);
    }

    @Override
    public void onMouseButton(int button, int action, int mods) {
        double[] cursorX = new double[1];
        double[] cursorY = new double[1];
        glfwGetCursorPos(window.handle, cursorX, cursorY);
        screen.onMouseButton((float) cursorX[0], (float) cursorY[0], button, action, mods);
    }

    @Override
    public void onMousePos(double x, double y) {
        for (int i = 0; i < 6; ++i) {
            if (glfwGetMouseButton(window.handle, i) == GLFW_PRESS) {
                screen.onMouseDrag((float) x, (float) y, i);
            }
        }
        screen.onMousePos((float) x, (float) y);
    }

    @Override
    public void onScroll(double dx, double dy) {
        double[] cursorX = new double[1];
        double[] cursorY = new double[1];
        glfwGetCursorPos(window.handle, cursorX, cursorY);
        screen.onScroll((float) cursorX[0], (float) cursorY[0], (float) dx, (float) dy);
    }
}
