package xyz.wagyourtail.konig.structure.code;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import xyz.wagyourtail.konig.structure.headers.KonigBlock;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Code {
    public final CodeParent parent;
    Map<Integer, Wire> wireMap = new HashMap<>();
    Map<Integer, KonigBlockReference> blockMap = new HashMap<>();

    public Code(CodeParent parent) {
        this.parent = parent;
    }


    public void parseXML(Node node) throws IOException {
        NodeList children = node.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (child.getNodeType() == Node.ELEMENT_NODE) {
                if (!parseChild(child)) {
                    throw new IOException("Unknown child node: " + child.getNodeName());
                }
            }
        }
    }

    public boolean parseChild(Node node) throws IOException {
        if (node.getNodeName().equals("wires")) {
            parseWires(node);
            return true;
        }
        if (node.getNodeName().equals("blocks")) {
            parseBlocks(node);
            return true;
        }
        return false;
    }

    public void parseWires(Node wires) throws IOException {
        NodeList children = wires.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (child.getNodeType() == Node.ELEMENT_NODE) {
                Wire wire = new Wire();
                wire.parseXML(child);
                wireMap.put(wire.id, wire);
            }
        }
    }

    public void parseBlocks(Node blocks) throws IOException {
        NodeList children = blocks.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (child.getNodeType() == Node.ELEMENT_NODE) {
                KonigBlockReference block = new KonigBlockReference(this);
                block.parseXML(child);
                blockMap.put(block.id, block);
            }
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Code)) {
            return false;
        }
        Code code = (Code) o;
        return Objects.equals(wireMap, code.wireMap) && Objects.equals(blockMap, code.blockMap);
    }

    @Override
    public int hashCode() {
        return Objects.hash(wireMap, blockMap);
    }

    public interface CodeParent {
        KonigBlock getBlockByName(String name);
    }
}
