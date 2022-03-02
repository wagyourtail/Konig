package xyz.wagyourtail.konig.editor;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.awt.AWTGLCanvas;

import javax.swing.*;
import java.awt.event.*;

public class GLComponent extends AWTGLCanvas {
    @Override
    public void initGL() {
        if (!GLFW.glfwInit())
            throw new IllegalStateException("Failed to init GLFW");
        GL.createCapabilities();
        GL11.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);

        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glEnable(GL11.GL_TEXTURE_2D);

        System.out.println("OpenGL version: " + effective.majorVersion + "." + effective.minorVersion + " (Profile: " + effective.profile + ")");

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                System.out.println(e);
            }

            @Override
            public void componentMoved(ComponentEvent e) {
                System.out.println(e);
            }

            @Override
            public void componentShown(ComponentEvent e) {
                System.out.println(e);
            }

            @Override
            public void componentHidden(ComponentEvent e) {
                System.out.println(e);
            }
        });
        addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                System.out.println(e);
            }

            @Override
            public void focusLost(FocusEvent e) {
                System.out.println(e);
            }
        });
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                System.out.println(e);
            }

            @Override
            public void keyTyped(KeyEvent e) {
                System.out.println(e);
            }

            @Override
            public void keyReleased(KeyEvent e) {
                System.out.println(e);
            }
        });
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                System.out.println(e);
            }

            @Override
            public void mousePressed(MouseEvent e) {
                System.out.println(e);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                System.out.println(e);
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                System.out.println(e);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                System.out.println(e);
            }

            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                System.out.println(e);
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                System.out.println(e);
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                System.out.println(e);
            }
        });
        addMouseMotionListener(new MouseMotionListener() {
            @Override
            public void mouseDragged(MouseEvent e) {
                System.out.println(e);
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                System.out.println(e);
            }
        });
        addMouseWheelListener(System.out::println);
    }

    @Override
    public void paintGL() {
        int w = getWidth();
        int h = getHeight();
        GL11.glViewport(0, 0, w, h);
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
        GL11.glPushMatrix();
        GL11.glTranslatef(-1, -1, 0);
        GL11.glScalef(1/(float)w, 1/(float)h, 1);
        doDraw();
        swapBuffers();
        GL11.glPopMatrix();
        GLFW.glfwPollEvents();
    }

    public void doDraw() {
        GL11.glBegin(GL11.GL_TRIANGLES);
        GL11.glColor4f(1.0f, 0, 0, 1.0f);
        GL11.glVertex2f(0f, 0f);
        GL11.glVertex2f(10f, 0);
        GL11.glVertex2f(10f, 10f);
        GL11.glEnd();
    }
    public void startRenderLoop() {
        Runnable renderLoop = new Runnable() {
            @Override
            public void run() {
                try {
                    render();
                } catch (Throwable t) {
                }
                SwingUtilities.invokeLater(this);
            }
        };
        SwingUtilities.invokeLater(renderLoop);
    }
}