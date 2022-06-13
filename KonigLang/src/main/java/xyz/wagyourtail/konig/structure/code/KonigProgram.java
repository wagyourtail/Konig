package xyz.wagyourtail.konig.structure.code;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import xyz.wagyourtail.XMLBuilder;
import xyz.wagyourtail.konig.structure.KonigFile;
import xyz.wagyourtail.konig.structure.headers.KonigBlock;
import xyz.wagyourtail.konig.structure.headers.KonigHeaders;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public class KonigProgram implements KonigFile, Code.CodeParent {
    private final Path path;
    public final KonigHeaders headers;
    public final Code code = new Code(this);
    public String name;
    public String version;

    public KonigProgram(Path path) {
        this.headers = new KonigHeaders(path);
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
        return headers.getBlocks();
    }

    @Override
    public void parseXML(Node node) throws IOException, ParserConfigurationException, SAXException {
        Node version = node.getAttributes().getNamedItem("version");
        if (version != null) {
            this.version = version.getNodeValue();
        }
        Node name = node.getAttributes().getNamedItem("name");
        if (name != null) {
            this.name = name.getNodeValue();
        }

        NodeList nodes = node.getChildNodes();
        Set<String> found = new HashSet<>();

        for (int i = 0; i < nodes.getLength(); i++) {
            Node n = nodes.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                if (n.getNodeName().equals("headers")) {
                    if (!found.add("headers")) {
                        throw new IOException("Duplicate headers");
                    }
                    headers.parseXML(n);
                } else if (n.getNodeName().equals("code") || n.getNodeName().equals("main")) {
                    if (!found.add("code")) {
                        throw new IOException("Duplicate code");
                    }
                    code.parseXML(n);
                } else {
                    throw new IOException("Unknown node: " + n.getNodeName());
                }
            }
        }
    }

    @Override
    public XMLBuilder toXML() {
        XMLBuilder builder = new XMLBuilder("code");
        builder.addStringOption("name", name == null ? path.getFileName().toString().replace(".konig", "") : name);
        if (version != null) {
            builder.addStringOption("version", version);
        }
        builder.append(headers.toXML());
        builder.append(code.toXML());
        return builder;
    }

    @Override
    public KonigBlock getBlockByName(String name) {
        //TODO: stdlib and stuff gets
        return headers.getBlockByName(name);
    }

    public Function<Map<String, Object>, CompletableFuture<Map<String, Object>>> jitCompile(boolean async) {
        return code.jitCompile(async);
    }

}
