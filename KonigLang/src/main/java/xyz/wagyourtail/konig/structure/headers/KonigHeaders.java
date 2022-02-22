package xyz.wagyourtail.konig.structure.headers;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import xyz.wagyourtail.konig.structure.KonigFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

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

    @Override
    public int hashCode() {
        return Objects.hash(customBlockMap, version);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof KonigHeaders)) {
            return false;
        }
        KonigHeaders that = (KonigHeaders) o;
        return Objects.equals(customBlockMap, that.customBlockMap) && Objects.equals(
            version,
            that.version
        );
    }

}
