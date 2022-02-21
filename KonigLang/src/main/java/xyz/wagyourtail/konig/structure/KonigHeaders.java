package xyz.wagyourtail.konig.structure;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class KonigHeaders implements KonigFile {
    private final Map<String, Map<String, KonigBlock>> customBlockMap = new HashMap<>();
    private String version;

    @Override
    public String getVersion() {
        return version;
    }

    @Override
    public Map<String, Map<String, KonigBlock>> getCustomBlocks() {
        return customBlockMap;
    }

    @Override
    public void parseXML(Node node) throws IOException {
        Node version = node.getAttributes().getNamedItem("version");
        if (version != null) {
            this.version = version.getNodeValue();
        }

        NodeList children = node.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node n = children.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                KonigBlock block;
                if (n.getNodeName().equals("custom")) {
                    block = new KonigCustomBlock();
                } else {
                    block = new KonigBlock();
                }
                block.parseXML(n);
                customBlockMap.computeIfAbsent(
                    block.group,
                    (e) -> new HashMap<>()
                ).put(block.name, block);
            }
        }
    }
}
