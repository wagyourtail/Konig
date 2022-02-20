package xyz.wagyourtail.konig;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;
import xyz.wagyourtail.konig.structure.KonigFile;
import xyz.wagyourtail.konig.structure.KonigHeaders;
import xyz.wagyourtail.konig.structure.KonigProgram;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class Konig {
    private static final Map<String, Class<KonigBlock>> blocks = new HashMap<>();

    public static KonigFile deserialize(Path file) throws ParserConfigurationException, IOException, SAXException {
        if (!Files.isRegularFile(file)) throw new IllegalArgumentException("File is not a regular file");
        if (!Files.isReadable(file)) throw new IllegalArgumentException("File is not readable");
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(file.toFile());
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
}
