package xyz.wagyourtail.wagyourgui.glfw;

import static org.lwjgl.glfw.GLFW.*;

public class Cursors {
    public static final long NORMAL = glfwCreateStandardCursor(GLFW_CURSOR_NORMAL);
    public static final long HIDDEN = glfwCreateStandardCursor(GLFW_CURSOR_HIDDEN);
    public static final long DISABLED = glfwCreateStandardCursor(GLFW_CURSOR_DISABLED);

    public static final long ARROW = glfwCreateStandardCursor(GLFW_ARROW_CURSOR);
    public static final long IBEAM = glfwCreateStandardCursor(GLFW_IBEAM_CURSOR);
    public static final long CROSSHAIR = glfwCreateStandardCursor(GLFW_CROSSHAIR_CURSOR);
    public static final long HAND = glfwCreateStandardCursor(GLFW_HAND_CURSOR);
    public static final long HRESIZE = glfwCreateStandardCursor(GLFW_HRESIZE_CURSOR);
    public static final long VRESIZE = glfwCreateStandardCursor(GLFW_VRESIZE_CURSOR);

    // these are not in lwjgl
    public static final long RESIZE_NWSE = glfwCreateStandardCursor(GLFW_VRESIZE_CURSOR + 1);
    public static final long RESIZE_NESW = glfwCreateStandardCursor(GLFW_VRESIZE_CURSOR + 2);

    public static final long RESIZE_ALL = glfwCreateStandardCursor(GLFW_VRESIZE_CURSOR + 3);
}