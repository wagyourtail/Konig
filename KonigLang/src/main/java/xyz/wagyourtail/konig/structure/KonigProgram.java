package xyz.wagyourtail.konig.structure;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class KonigProgram implements KonigFile {
    private final KonigHeaders headers = new KonigHeaders();
    public final Code code = new Code();
    public String version;

    @Override
    public String getVersion() {
        return version;
    }

    @Override
    public Map<String, KonigBlock> getCustomBlocks() {
        return headers.getCustomBlocks();
    }

    @Override
    public void parseXML(Node node) throws IOException {
        Node version = node.getAttributes().getNamedItem("version");
        if (version != null) {
            this.version = version.getNodeValue();
        }

        NodeList nodes = node.getChildNodes();
        Set<String> found = new HashSet<>();

        for (int i = 0; i < nodes.getLength(); i++) {
            Node n = nodes.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                if (n.getNodeName().equals("headers")) {
                    if (!found.add("headers")) throw new IOException("Duplicate headers");
                    headers.parseXML(n);
                } else if (n.getNodeName().equals("code")) {
                    if (!found.add("code")) throw new IOException("Duplicate code");
                    code.parseXML(n);
                } else {
                    throw new IOException("Unknown node: " + n.getNodeName());
                }
            }
        }
    }

}
