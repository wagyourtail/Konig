package xyz.wagyourtail.konig.editor.screens;

import xyz.wagyourtail.konig.editor.elements.Button;
import xyz.wagyourtail.konig.editor.elements.DrawableHelper;
import xyz.wagyourtail.konig.editor.glfw.GLFWSession;
import xyz.wagyourtail.konig.editor.glfw.Window;

public class EditorMainScreen extends BaseScreen {

    public EditorMainScreen(GLFWSession session) {
        super(session);
    }

    @Override
    public void onRender(float mouseX, float mouseY) {
        super.onRender(mouseX, mouseY);

        DrawableHelper.drawString(session.font, Long.toString(session.fps), 0, session.window.getHeight() - session.font.FONT_HEIGHT, 0xFFFFFFFF);
    }

    @Override
    public void init(Window window) {
        elements.add(new Button(0, 0, 200, 200, session.font, "New Project", 0, 0x7FFFFFFF, 0xFFFFFFFF, 0xFF000000, (btn) -> {
            System.out.println("clicked!");
        }));
    }

}
