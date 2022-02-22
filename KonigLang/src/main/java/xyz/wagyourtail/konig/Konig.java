package xyz.wagyourtail.konig;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;
import xyz.wagyourtail.konig.structure.KonigFile;
import xyz.wagyourtail.konig.structure.code.KonigProgram;
import xyz.wagyourtail.konig.structure.headers.KonigBlock;
import xyz.wagyourtail.konig.structure.headers.KonigHeaders;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class Konig {
    public static final Map<String, Map<String, KonigBlock>> blocks = new HashMap<>();

    public static KonigFile deserialize(Path file) throws ParserConfigurationException, IOException, SAXException {
        if (!Files.isRegularFile(file)) {
            throw new IllegalArgumentException("File is not a regular file");
        }
        if (!Files.isReadable(file)) {
            throw new IllegalArgumentException("File is not readable");
        }
        return deserialize(Files.newInputStream(file));
    }

    public static KonigFile deserialize(InputStream stream) throws ParserConfigurationException, IOException, SAXException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(stream);
        doc.getDocumentElement().normalize();

        Node root = doc.getDocumentElement();
        if (root.getNodeName().equals("code")) {
            KonigProgram program = new KonigProgram();
            program.parseXML(root);
            return program;
        } else if (root.getNodeName().equals("headers")) {
            KonigHeaders header = new KonigHeaders();
            header.parseXML(root);
            return header;
        }
        throw new IllegalArgumentException("File is not a valid Konig file");
    }

    public static KonigFile deserializeString(String code) throws ParserConfigurationException, IOException, SAXException {
        // string to input stream
        InputStream stream = new ByteArrayInputStream(code.getBytes());
        return deserialize(stream);
    }

}
