package xyz.wagyourtail.wagyourgui.screens;

import org.xml.sax.SAXException;
import xyz.wagyourtail.konig.Konig;
import xyz.wagyourtail.konig.editor.blockselect.BlockSelector;
import xyz.wagyourtail.konig.editor.canvas.RenderBlock;
import xyz.wagyourtail.konig.editor.canvas.RenderCode;
import xyz.wagyourtail.konig.editor.canvas.RenderCodeParent;
import xyz.wagyourtail.wagyourgui.elements.BaseElement;
import xyz.wagyourtail.wagyourgui.elements.Button;
import xyz.wagyourtail.wagyourgui.elements.DrawableHelper;
import xyz.wagyourtail.wagyourgui.elements.HorizontalScrollBar;
import xyz.wagyourtail.wagyourgui.glfw.GLFWSession;
import xyz.wagyourtail.wagyourgui.glfw.Window;
import xyz.wagyourtail.konig.structure.code.KonigProgram;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;

public class EditorMainScreen extends BaseScreen implements RenderCodeParent {

    private RenderBlock placeBlock;

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


        Path hello_world = Path.of("software-engineering-bs/language-spec-revisions/1.0/example/helloworld.konig");
        long start = System.nanoTime();
        try {
            KonigProgram f = (KonigProgram) Konig.deserialize(hello_world);
            elements.add(new RenderCode(
                this,
                0, 20, window.getWidth(), window.getHeight() - 200, f.code, session.font));


            elements.add(new Button(0, 0, 100, 20, session.font, "Run", 0, 0x7FFFFFFF, 0xFFFFFFFF, 0xFF000000, (btn) -> {
                f.jitCompile(true).apply(Map.of());
            }));

            elements.add(new Button(100, 0, 100, 20, session.font, "Write", 0, 0x7FFFFFFF, 0xFFFFFFFF, 0xFF000000, (btn) -> {
                System.out.println(f.toXML());
            }));

            elements.add(new BlockSelector(0, window.getHeight() - 200, window.getWidth(), 200, session.font, (block) -> {
                if (block != null) {
                    setPlacingBlock(RenderBlock.compile(block, session.font, null));
                } else {
                    setPlacingBlock(null);
                }
            }));
        } catch (ParserConfigurationException | IOException | SAXException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Optional<RenderBlock> getPlacingBlock() {
        return Optional.ofNullable(placeBlock);
    }

    @Override
    public void setPlacingBlock(RenderBlock block) {
        this.placeBlock = block;
    }

    @Override
    public void focusCode(BaseElement code) {
        if (elements.contains(code) && focusedElement != code) {
            BaseElement prevFocus = focusedElement;
            if (focusedElement != null) {
                focusedElement.onFocusLost(code);
            }
            focusedElement = code;
            focusedElement.onFocus(prevFocus);
        }
    }

}
