package xyz.wagyourtail.konig.structure;

import org.w3c.dom.Node;
import xyz.wagyourtail.konig.structure.headers.KonigBlock;

import java.io.IOException;
import java.util.Map;

public interface KonigFile {
    String getVersion();

    Map<String, Map<String, KonigBlock>> getCustomBlocks();

    void parseXML(Node node) throws IOException;

}
