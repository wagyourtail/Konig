package xyz.wagyourtail.konig.lib;

import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;
import xyz.wagyourtail.konig.structure.code.Wire;
import xyz.wagyourtail.konig.structure.headers.BlockIO;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.*;

class InternBlockTest {

    @Test
    void parseMethod() throws ParserConfigurationException, IOException, SAXException, NoSuchMethodException {
        Wire w = new Wire();
        String xml = """
            <block name="acos" group="math">
                <io>
                    <input side="left" justify="center" name="in" type="number"/>
                    <output side="right" justify="center" name="out" type="number"/>
                </io>
            </block>
        """;
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        InputStream stream = new ByteArrayInputStream(xml.getBytes());
        Document doc = builder.parse(stream);
        doc.getDocumentElement().normalize();

        Node root = doc.getDocumentElement();
        InternBlock b1 = new InternBlock();
        b1.parseXML(root);

        InternBlock b2 = new InternBlock();
        b2.parseMethod(getClass().getDeclaredMethod("acos", double.class), "math");

        assertEquals(b1, b2);
    }

    @Block(
            name = "acos",
            group = "math",
            inputs = {
                    @Block.Input(name = "in", type = "number", side = BlockIO.Side.LEFT, justify = BlockIO.Justify.CENTER)
            },
            outputs = {
                    @Block.Output(name = "out", type = "number", side = BlockIO.Side.RIGHT, justify = BlockIO.Justify.CENTER)
            }
    )
    public void acos(double input) {}

}