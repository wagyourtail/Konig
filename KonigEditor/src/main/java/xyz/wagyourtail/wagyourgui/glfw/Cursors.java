package xyz.wagyourtail.wagyourgui.glfw;

import static org.lwjgl.glfw.GLFW.*;

public class Cursors {
    public static final long ARROW = glfwCreateStandardCursor(GLFW_ARROW_CURSOR);
    public static final long IBEAM = glfwCreateStandardCursor(GLFW_IBEAM_CURSOR);
    public static final long CROSSHAIR = glfwCreateStandardCursor(GLFW_CROSSHAIR_CURSOR);
    public static final long HAND = glfwCreateStandardCursor(GLFW_HAND_CURSOR);

    public static final long HRESIZE = glfwCreateStandardCursor(GLFW_HRESIZE_CURSOR);
    public static final long VRESIZE = glfwCreateStandardCursor(GLFW_VRESIZE_CURSOR);
    public static final long RESIZE_NWSE = glfwCreateStandardCursor(GLFW_RESIZE_NWSE_CURSOR);
    public static final long RESIZE_NESW = glfwCreateStandardCursor(GLFW_RESIZE_NESW_CURSOR);

    public static final long RESIZE_ALL = glfwCreateStandardCursor(GLFW_RESIZE_ALL_CURSOR);

    public static final long NOT_ALLOWED = glfwCreateStandardCursor(GLFW_NOT_ALLOWED_CURSOR);
}