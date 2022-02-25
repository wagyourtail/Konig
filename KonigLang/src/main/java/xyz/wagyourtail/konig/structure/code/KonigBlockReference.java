package xyz.wagyourtail.konig.structure.code;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class KonigBlockReference {
    public final Code parent;
    public int id;
    public String name;
    public final ReferenceIO io = new ReferenceIO();
    public final Map<String, VirtualIO> virtualIOGroupsMap = new HashMap<>();
    public final Map<String, VirtualIO> virtualIONameMap = new HashMap<>();
    public final Map<String, InnerCode> innerCodeMap = new HashMap<>();

    public String value;

    public KonigBlockReference(Code parent) {
        this.parent = parent;
    }

    public void parseXML(Node node) throws IOException {
        id = Integer.parseInt(node.getAttributes().getNamedItem("blockid").getNodeValue());
        name = node.getNodeName();

        NodeList children = node.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (child.getNodeName().equals("io")) {
                io.parseXML(child);
            } else if (child.getNodeName().equals("virtual")) {
                VirtualIO virtualIO = new VirtualIO();
                virtualIO.parseXML(child);
                if (virtualIO.forGroup != null) {
                    virtualIOGroupsMap.put(virtualIO.forGroup, virtualIO);
                } else if (virtualIO.forName != null) {
                    virtualIONameMap.put(virtualIO.forName, virtualIO);
                } else {
                    throw new IOException("VirtualIO has no forGroup or forName");
                }
            } else if (child.getNodeName().equals("innercode")) {
                InnerCode innerCode = new InnerCode(this);
                innerCode.parseXML(child);
                innerCodeMap.put(innerCode.name, innerCode);
            } else if (child.getNodeName().equals("value")) {
                value = child.getTextContent();
            } else if (!child.getNodeName().equals("#text")) {
                throw new IOException("Unknown child node: " + child.getNodeName());
            }
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof KonigBlockReference)) {
            return false;
        }
        KonigBlockReference that = (KonigBlockReference) o;
        return id == that.id && Objects.equals(io, that.io) && Objects.equals(
            virtualIOGroupsMap,
            that.virtualIOGroupsMap
        ) && Objects.equals(virtualIONameMap, that.virtualIONameMap) && Objects.equals(
            innerCodeMap,
            that.innerCodeMap
        );
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, io, virtualIOGroupsMap, virtualIONameMap, innerCodeMap);
    }

}
