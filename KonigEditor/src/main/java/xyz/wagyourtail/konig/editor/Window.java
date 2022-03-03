package xyz.wagyourtail.konig.editor;

import org.lwjgl.system.MemoryUtil;

import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;

public class Window {
    public final long handle;

    private final List<KeyListener> keyListeners = new ArrayList<>();
    private final List<Consumer<Character>> charListeners = new ArrayList<>();

    private boolean visible = false;
    private int width;
    private int height;

    public Window(String title, int width, int height) {
        this.handle = glfwCreateWindow(800, 600, "Konig Editor", MemoryUtil.NULL, MemoryUtil.NULL);
        if (handle == MemoryUtil.NULL) {
            throw new RuntimeException("Failed to create the GLFW window");
        }

        this.width = width;
        this.height = height;

        glfwSetWindowSizeCallback(handle, (window, width1, height1) -> {
            this.width = width1;
            this.height = height1;
        });
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

    public interface KeyListener {
        void onKey(int key, int scancode, int action, int mods);
    }
}
