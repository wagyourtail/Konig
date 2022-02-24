package xyz.wagyourtail.konig.structure.headers;

import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;

class KonigCustomBlockTest {

    @Test
    void parseXML1() throws ParserConfigurationException, IOException, SAXException {
        KonigBlock block = new KonigCustomBlock(null);
        String xml = """
            <block name="tonumber" group="generic">
                <io>
                    <input side="left" justify="center" name="in" type="string"/>
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
        block.parseXML(root);

        KonigBlock block2 = new KonigCustomBlock(null);
        block2.name = "tonumber";
        block2.group = "generic";
        BlockIO io = block2.io;

        BlockIO.Input in = new BlockIO.Input(BlockIO.Side.LEFT, BlockIO.Justify.CENTER, "in", "string", false);
        io.elements.computeIfAbsent(BlockIO.Side.LEFT, k -> new HashMap<>()).computeIfAbsent(BlockIO.Justify.CENTER, k -> new ArrayList<>()).add(in);
        io.inputs.add(in);
        io.byName.put("in", in);

        BlockIO.Output out = new BlockIO.Output(BlockIO.Side.RIGHT, BlockIO.Justify.CENTER, "out", "number");
        io.elements.computeIfAbsent(BlockIO.Side.RIGHT, k -> new HashMap<>()).computeIfAbsent(BlockIO.Justify.CENTER, k -> new ArrayList<>()).add(out);
        io.outputs.add(out);
        io.byName.put("out", out);

        assertEquals(block2, block);
    }

    @Test
    void parseXML2() throws ParserConfigurationException, IOException, SAXException {
        KonigBlock block = new KonigCustomBlock(null);
        String xml = """
                <block name="stackedif" group="flow">
                    <io>
                        <input side="left" justify="center" name="condition" type="boolean"/>
                    </io>
                    <hollow name="true" paddingTop=".2" paddingLeft=".2" paddingRight=".2" paddingBottom=".2" group="stack">
            
                    </hollow>
                    <hollow name="false" paddingTop=".2" paddingLeft=".2" paddingRight=".2" paddingBottom=".2" group="stack">
            
                    </hollow>
                </block>
            """;
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        InputStream stream = new ByteArrayInputStream(xml.getBytes());
        Document doc = builder.parse(stream);
        doc.getDocumentElement().normalize();

        Node root = doc.getDocumentElement();
        block.parseXML(root);

        KonigBlock block2 = new KonigCustomBlock(null);
        block2.name = "stackedif";
        block2.group = "flow";
        BlockIO io = block2.io;

        BlockIO.Input in = new BlockIO.Input(BlockIO.Side.LEFT, BlockIO.Justify.CENTER, "condition", "boolean", false);
        io.elements.computeIfAbsent(BlockIO.Side.LEFT, k -> new HashMap<>()).computeIfAbsent(BlockIO.Justify.CENTER, k -> new ArrayList<>()).add(in);
        io.inputs.add(in);
        io.byName.put("condition", in);

        Hollow hollow = new Hollow();
        hollow.name = "true";
        hollow.group = "stack";
        hollow.paddingTop = .2;
        hollow.paddingLeft = .2;
        hollow.paddingRight = .2;
        hollow.paddingBottom = .2;

        block2.hollowsByName.put("true", hollow);
        block2.hollowsByGroupName.computeIfAbsent("stack", k -> new HashMap<>()).put("true", hollow);

        hollow = new Hollow();
        hollow.name = "false";
        hollow.group = "stack";
        hollow.paddingTop = .2;
        hollow.paddingLeft = .2;
        hollow.paddingRight = .2;
        hollow.paddingBottom = .2;

        block2.hollowsByName.put("false", hollow);
        block2.hollowsByGroupName.computeIfAbsent("stack", k -> new HashMap<>()).put("false", hollow);

        assertEquals(block2, block);
    }

}