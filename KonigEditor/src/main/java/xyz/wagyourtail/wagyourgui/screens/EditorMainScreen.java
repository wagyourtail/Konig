package xyz.wagyourtail.wagyourgui.screens;

import org.xml.sax.SAXException;
import xyz.wagyourtail.konig.Konig;
import xyz.wagyourtail.konig.editor.canvas.RenderCode;
import xyz.wagyourtail.wagyourgui.elements.Button;
import xyz.wagyourtail.wagyourgui.elements.DrawableHelper;
import xyz.wagyourtail.wagyourgui.glfw.GLFWSession;
import xyz.wagyourtail.wagyourgui.glfw.Window;
import xyz.wagyourtail.konig.structure.code.KonigProgram;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;

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


        Path hello_world = Path.of("software-enginnering-bs/language-spec-revisions/1.0/example/helloworld.konig");
        long start = System.nanoTime();
        try {
            KonigProgram f = (KonigProgram) Konig.deserialize(hello_world);
            elements.add(new RenderCode(200, 200, window.getWidth() - 200, window.getHeight() - 200, f.code, session.font));


            elements.add(new Button(0, 0, 200, 200, session.font, "New Project", 0, 0x7FFFFFFF, 0xFFFFFFFF, 0xFF000000, (btn) -> {
                f.jitCompile(true).apply(Map.of());
            }));
        } catch (ParserConfigurationException | IOException | SAXException e) {
            throw new RuntimeException(e);
        }
    }

}
