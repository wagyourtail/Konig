package xyz.wagyourtail.wagyourgui.glfw;

import org.lwjgl.system.MemoryUtil;

import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.glViewport;

public class Window {
    public final long handle;
    private boolean visible = false;
    private int width;
    private int height;

    private final Set<ResizeListener> resizeListeners = Collections.newSetFromMap(new WeakHashMap<>());
    private final Set<MouseListener> mouseListeners = Collections.newSetFromMap(new WeakHashMap<>());
    private final Set<KeyListener> keyListeners = Collections.newSetFromMap(new WeakHashMap<>());

    public Window(String title, int width, int height) {
        this.handle = glfwCreateWindow(800, 600, "Konig Editor", MemoryUtil.NULL, MemoryUtil.NULL);
        if (handle == MemoryUtil.NULL) {
            throw new RuntimeException("Failed to create the GLFW window");
        }

        glfwSetWindowSizeCallback(handle, (window, width1, height1) -> {
            this.width = width1;
            this.height = height1;
            glViewport(0, 0, width1, height1);

            for (ResizeListener listener : resizeListeners) {
                listener.onWindowResize(this);
            }
        });

        glfwSetMouseButtonCallback(handle, (window, button, action, mods) -> {
            for (MouseListener listener : mouseListeners) {
                listener.onMouseButton(button, action, mods);
            }
        });

        glfwSetCursorPosCallback(handle, (window, x, y) -> {
            for (MouseListener listener : mouseListeners) {
                listener.onMousePos(x, y);
            }
        });

        glfwSetCharCallback(handle, (window, codepoint) -> {
            for (KeyListener listener : keyListeners) {
                listener.onChar(codepoint);
            }
        });

        glfwSetKeyCallback(handle, (window, key, scancode, action, mods) -> {
            for (KeyListener listener : keyListeners) {
                listener.onKey(key, scancode, action, mods);
            }
        });

        glfwSetScrollCallback(handle, (window, xoffset, yoffset) -> {
            for (MouseListener listener : mouseListeners) {
                listener.onScroll(xoffset, yoffset);
            }
        });
    }

    public void addResizeListener(ResizeListener listener) {
        resizeListeners.add(listener);
    }

    public void addMouseListener(MouseListener listener) {
        mouseListeners.add(listener);
    }

    public void addKeyListener(KeyListener listener) {
        keyListeners.add(listener);
    }

    public void setupFramebuffer() {
        int[] widthBuffer = new int[1];
        int[] heightBuffer = new int[1];
        glfwGetFramebufferSize(this.handle, widthBuffer, heightBuffer);
        this.width = widthBuffer[0];
        this.height = heightBuffer[0];
        glViewport(0, 0, this.width, this.height);

        for (ResizeListener listener : resizeListeners) {
            listener.onWindowResize(this);
        }
    }

    public void shutdown() {
        glfwFreeCallbacks(handle);
        glfwDestroyWindow(handle);
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public void setVisible(boolean visible) {
        if (visible) {
            glfwShowWindow(handle);
        } else {
            glfwHideWindow(handle);
        }
        this.visible = visible;
    }

    public boolean getVisible() {
        return visible;
    }

    public long getHandle() {
        return handle;
    }
}
