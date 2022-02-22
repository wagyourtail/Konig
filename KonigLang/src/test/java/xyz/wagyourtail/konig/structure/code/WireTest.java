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

class WireTest {

    @Test
    void parseXML() throws ParserConfigurationException, IOException, SAXException {
        Wire w = new Wire();
        String xml = """
            <wire wireid="0">
                <end x="0" y="0" block="0" port="out1"/>
                <branch x=".5" y=".5">
                    <end x="0" y="1" block="3" port="in1"/>
                </branch>
                <end x="1" y="1" block="2" port="in2"/>
            </wire>
        """;
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        InputStream stream = new ByteArrayInputStream(xml.getBytes());
        Document doc = builder.parse(stream);
        doc.getDocumentElement().normalize();

        Node root = doc.getDocumentElement();
        w.parseXML(root);

        Wire w2 = new Wire();
        Wire.WireEndpoint e1 = new Wire.WireEndpoint(0, 0, 0, "out1");
        w2.getEndpoints().add(e1);
        w2.getSegments().add(e1);

        Wire.WireBranch b = new Wire.WireBranch(0.5, 0.5);
        Wire.WireEndpoint e2 = new Wire.WireEndpoint(3, 0, 1, "in1");
        b.endpoint = e2;
        b.subSegments.add(e2);
        w2.getSegments().add(b);
        w2.getEndpoints().add(e2);

        Wire.WireEndpoint e3 = new Wire.WireEndpoint(2, 1, 1, "in2");
        w2.getEndpoints().add(e3);
        w2.getSegments().add(e3);

        assertEquals(w, w2);
    }

}