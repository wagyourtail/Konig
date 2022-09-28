package xyz.wagyourtail.konig.structure.code;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import xyz.wagyourtail.XMLBuilder;
import xyz.wagyourtail.konig.structure.headers.BlockIO;
import xyz.wagyourtail.konig.structure.headers.KonigBlock;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class KonigBlockReference {
    public Code parent;
    public int id;
    public String name;

    public float x;
    public float y;
    public float scaleX;
    public float scaleY;
    public int rotation;
    public boolean flipH;
    public boolean flipV;

    public final ReferenceIO io = new ReferenceIO();
    public final Map<String, VirtualIO> virtualIOGroupsMap = new HashMap<>();
    public final Map<String, VirtualIO> virtualIONameMap = new HashMap<>();
    public final Map<String, InnerCode> innerCodeMap = new HashMap<>();

    public String value = "";

    public KonigBlockReference(Code parent) {
        this.parent = parent;
    }

    public void parseXML(Node node) throws IOException {
        id = Integer.parseInt(node.getAttributes().getNamedItem("blockid").getNodeValue());
        name = node.getNodeName();

        x = Float.parseFloat(node.getAttributes().getNamedItem("x").getNodeValue());
        y = Float.parseFloat(node.getAttributes().getNamedItem("y").getNodeValue());
        scaleX = Float.parseFloat(node.getAttributes().getNamedItem("scaleX").getNodeValue());
        scaleY = Float.parseFloat(node.getAttributes().getNamedItem("scaleY").getNodeValue());
        rotation = Integer.parseInt(node.getAttributes().getNamedItem("rotate").getNodeValue()) % 4;
        flipH = Boolean.parseBoolean(node.getAttributes().getNamedItem("flipH").getNodeValue());
        flipV = Boolean.parseBoolean(node.getAttributes().getNamedItem("flipV").getNodeValue());

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

    public XMLBuilder toXML() {
        XMLBuilder builder = new XMLBuilder(name);
        builder.addStringOption("blockid", Integer.toString(id));
        builder.addStringOption("x", Float.toString(x));
        builder.addStringOption("y", Float.toString(y));
        builder.addStringOption("scaleX", Float.toString(scaleX));
        builder.addStringOption("scaleY", Float.toString(scaleY));
        builder.addStringOption("rotate", Integer.toString(rotation));
        builder.addStringOption("flipH", Boolean.toString(flipH));
        builder.addStringOption("flipV", Boolean.toString(flipV));
        builder.append(io.toXML());
        for (Map.Entry<String, VirtualIO> entry : virtualIOGroupsMap.entrySet()) {
            builder.append(entry.getValue().toXML(false, entry.getKey()));
        }
        for (Map.Entry<String, VirtualIO> entry : virtualIONameMap.entrySet()) {
            builder.append(entry.getValue().toXML(true, entry.getKey()));
        }
        for (Map.Entry<String, InnerCode> entry : innerCodeMap.entrySet()) {
            builder.append(entry.getValue().toXML(entry.getKey()));
        }
        if (value != null) {
            builder.append(new XMLBuilder("value", XMLBuilder.INLINE | XMLBuilder.START_NEW_LINE).append(value));
        }
        return builder;
    }

    public KonigBlockReference copy(Code parent) {
        KonigBlockReference clone = new KonigBlockReference(parent);
        clone.id = id;
        clone.name = name;
        clone.x = x;
        clone.y = y;
        clone.scaleX = scaleX;
        clone.scaleY = scaleY;
        clone.rotation = rotation;
        clone.flipH = flipH;
        clone.flipV = flipV;
        clone.value = value;
        return clone;
    }

    public KonigBlock attemptToGetBlockSpec() {
        return parent.parent.getBlockByName(name);
    }
}
