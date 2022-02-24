package xyz.wagyourtail.konig.structure;

import org.w3c.dom.Node;
import org.xml.sax.SAXException;
import xyz.wagyourtail.konig.structure.headers.KonigBlock;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;

public interface KonigFile {
    Path getPath();
    String getVersion();

    Map<String, Map<String, KonigBlock>> getBlocks();

    void parseXML(Node node) throws IOException, ParserConfigurationException, SAXException;

}
