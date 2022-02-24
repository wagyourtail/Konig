package xyz.wagyourtail.konig.structure.headers;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import xyz.wagyourtail.konig.Konig;
import xyz.wagyourtail.konig.structure.KonigFile;
import xyz.wagyourtail.konig.structure.code.Code;
import xyz.wagyourtail.konig.structure.code.KonigProgram;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class KonigHeaders implements KonigFile, Code.CodeParent {
    private final Path path;
    private final Map<String, Map<String, KonigBlock>> blockMap = new HashMap<>();
    private String version;

    public KonigHeaders(Path path) {
        this.path = path;
    }

    @Override
    public Path getPath() {
        return path;
    }

    @Override
    public String getVersion() {
        return version;
    }

    @Override
    public Map<String, Map<String, KonigBlock>> getBlocks() {
        return blockMap;
    }

    @Override
    public void parseXML(Node node) throws IOException, ParserConfigurationException, SAXException {
        Node version = node.getAttributes().getNamedItem("version");
        if (version != null) {
            this.version = version.getNodeValue();
        }

        NodeList children = node.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node n = children.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                switch (n.getNodeName()) {
                    case "custom" -> {
                        KonigBlock block;
                        block = new KonigCustomBlock(this);
                        block.parseXML(n);
                        blockMap.computeIfAbsent(
                            block.group,
                            (e) -> new HashMap<>()
                        ).put(block.name, block);
                    }
                    case "block" -> {
//                        KonigBlock block;
//                        block = new KonigBlock();
//                        block.parseXML(n);
//                        blockMap.computeIfAbsent(
//                            block.group,
//                            (e) -> new HashMap<>()
//                        ).put(block.name, block);
                    }
                    case "include" -> {
                        NamedNodeMap attrs = n.getAttributes();
                        if (attrs.getNamedItem("src") != null) {
                            KonigFile file = Konig.deserialize(path.resolve(attrs.getNamedItem("src").getNodeValue()));
                            if (file instanceof KonigHeaders) {
                                KonigHeaders headers = (KonigHeaders) file;
                                for (Map.Entry<String, Map<String, KonigBlock>> entry : headers.getBlocks()
                                    .entrySet()) {
                                    blockMap.computeIfAbsent(
                                        entry.getKey(),
                                        (e) -> new HashMap<>()
                                    ).putAll(entry.getValue());
                                }
                            } else if (file instanceof KonigProgram) {
                                KonigProgram program = (KonigProgram) file;
                                for (Map.Entry<String, Map<String, KonigBlock>> entry : program.getBlocks()
                                    .entrySet()) {
                                    blockMap.computeIfAbsent(
                                        entry.getKey(),
                                        (e) -> new HashMap<>()
                                    ).putAll(entry.getValue());
                                }
                            }
                        } else if (attrs.getNamedItem("intern") != null) {
                            String intern = attrs.getNamedItem("intern").getNodeValue();
                            KonigHeaders headers = Konig.getInternHeaders(intern);
                            if (headers != null) {
                                for (Map.Entry<String, Map<String, KonigBlock>> entry : headers.getBlocks()
                                    .entrySet()) {
                                    blockMap.computeIfAbsent(
                                        entry.getKey(),
                                        (e) -> new HashMap<>()
                                    ).putAll(entry.getValue());
                                }
                            } else {
                                throw new IOException("Unable to find intern headers for " + intern);
                            }
                        } else {
                            throw new IOException("Invalid include statement");
                        }
                    }
                    default -> {
                        throw new IOException("Unknown tag: " + n.getNodeName());
                    }
                }

            }
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(blockMap, version);
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
        return Objects.equals(blockMap, that.blockMap) && Objects.equals(
            version,
            that.version
        );
    }

    @Override
    public KonigBlock getBlockByName(String name) {
        for (Map<String, KonigBlock> map : blockMap.values()) {
            if (map.containsKey(name)) {
                return map.get(name);
            }
        }
        return null;
    }

}
