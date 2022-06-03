package xyz.wagyourtail.konig.editor;

import xyz.wagyourtail.wagyourgui.glfw.GLFWSession;

import java.io.IOException;

public class Main {

    public static void main(String[] args) throws IOException {
        GLFWSession editor = new GLFWSession();
        editor.start();
    }
}
