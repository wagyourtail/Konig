package xyz.wagyourtail.konig.structure.headers;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class KonigBlock {
    public final BlockIO io = new BlockIO();
    public final Map<String, Map<String, Hollow>> hollowsByGroupName = new HashMap<>();
    public final Map<String, Hollow> hollowsByName = new HashMap<>();
    public String name;
    public String group;
    public Path image;

    public void parseXML(Node node) throws IOException {
        name = node.getAttributes().getNamedItem("name").getNodeValue();
        group = node.getAttributes().getNamedItem("group").getNodeValue();

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

    protected boolean parseChild(Node child) throws IOException {
        if (child.getNodeName().equals("io")) {
            io.parseXML(child);
            return true;
        }
        if (child.getNodeName().equals("image")) {
            image = Path.of(child.getAttributes().getNamedItem("src").getNodeValue());
            return true;
        }
        if (child.getNodeName().equals("hollow")) {
            Hollow io = new Hollow();
            io.parseXML(child);

            hollowsByGroupName.computeIfAbsent(io.group, k -> new HashMap<>()).put(io.name, io);
            hollowsByName.put(io.name, io);
            return true;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, group, image, io, hollowsByGroupName, hollowsByName);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof KonigBlock)) {
            return false;
        }
        KonigBlock that = (KonigBlock) o;
        return Objects.equals(name, that.name) && Objects.equals(group, that.group) &&
            Objects.equals(image, that.image) && Objects.equals(io, that.io) &&
            Objects.equals(hollowsByGroupName, that.hollowsByGroupName) && Objects.equals(
            hollowsByName,
            that.hollowsByName
        );
    }

    public enum Side {
        TOP,
        BOTTOM,
        LEFT,
        RIGHT
    }

    public enum Justify {
        LEFT,
        CENTER,
        RIGHT
    }

    public static class Hollow extends BlockIO {
        public double paddingTop, paddingLeft, paddingBottom, paddingRight;
        public String name;
        public String group;

        @Override
        public void parseXML(Node child) throws IOException {
            paddingBottom = Double.parseDouble(child.getAttributes().getNamedItem("paddingBottom").getNodeValue());
            paddingLeft = Double.parseDouble(child.getAttributes().getNamedItem("paddingLeft").getNodeValue());
            paddingRight = Double.parseDouble(child.getAttributes().getNamedItem("paddingRight").getNodeValue());
            paddingTop = Double.parseDouble(child.getAttributes().getNamedItem("paddingTop").getNodeValue());
            name = child.getAttributes().getNamedItem("name").getNodeValue();
            if (child.getAttributes().getNamedItem("group") != null) {
                group = child.getAttributes().getNamedItem("group").getNodeValue();
            } else {
                group = "$ungrouped$" + name;
            }
            super.parseXML(child);
        }

        @Override
        public int hashCode() {
            return Objects.hash(super.hashCode(), paddingTop, paddingLeft, paddingBottom, paddingRight, name, group);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof Hollow)) {
                return false;
            }
            if (!super.equals(o)) {
                return false;
            }
            Hollow hollow = (Hollow) o;
            return Double.compare(hollow.paddingTop, paddingTop) == 0 && Double.compare(
                hollow.paddingLeft,
                paddingLeft
            ) == 0 && Double.compare(hollow.paddingBottom, paddingBottom) == 0 &&
                Double.compare(hollow.paddingRight, paddingRight) == 0 && Objects.equals(
                name,
                hollow.name
            ) && Objects.equals(group, hollow.group);
        }

    }

}
