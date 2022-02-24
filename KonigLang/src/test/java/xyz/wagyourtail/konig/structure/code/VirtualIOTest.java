package xyz.wagyourtail.konig.structure.code;

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

import static org.junit.jupiter.api.Assertions.*;

class VirtualIOTest {

    @Test
    void parseXML() throws ParserConfigurationException, IOException, SAXException {
        VirtualIO io = new VirtualIO();
        String xml = """
            <virtual forName="example">
                <port direction="in" id="0">
                    <outer wireid="0" side="top" offset=".3"/>
                    <inner wireid="0" side="top" offset=".2"/>
                </port>
            </virtual>
        """;
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        InputStream stream = new ByteArrayInputStream(xml.getBytes());
        Document doc = builder.parse(stream);
        doc.getDocumentElement().normalize();

        Node root = doc.getDocumentElement();
        io.parseXML(root);

        VirtualIO io2 = new VirtualIO();
        io2.forName = "example";
        VirtualIO.Port port = new VirtualIO.Port("in", 0);
        port.outer = new VirtualIO.Outer(0, "top", .3);
        port.inner = new VirtualIO.Inner(0, "top", .2);
        io2.portMap.put(0, port);
        assertEquals(io, io2);
    }

}