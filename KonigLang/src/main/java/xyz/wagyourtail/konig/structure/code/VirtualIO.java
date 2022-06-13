package xyz.wagyourtail.konig.structure.code;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import xyz.wagyourtail.XMLBuilder;

import javax.sound.sampled.Port;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class VirtualIO {
    public String forGroup;
    public String forName;
    public final Map<Integer, Port> portMap = new HashMap<>();

    public void parseXML(Node node) throws IOException {
        NamedNodeMap attrs = node.getAttributes();
        if (attrs.getNamedItem("forGroup") != null) {
            forGroup = attrs.getNamedItem("forGroup").getNodeValue();
        }
        if (attrs.getNamedItem("forName") != null) {
            forName = attrs.getNamedItem("forName").getNodeValue();
        }
        if (forGroup != null && forName != null) {
            throw new IOException("VirtualIO cannot have both forGroup and forName");
        }
        if (forGroup == null && forName == null) {
            throw new IOException("VirtualIO must have either forGroup or forName");
        }
        NodeList children = node.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (child.getNodeType() == Node.ELEMENT_NODE) {
                if (child.getNodeName().equals("port")) {
                    String direction = child.getAttributes().getNamedItem("direction").getNodeValue();
                    int id = Integer.parseInt(child.getAttributes().getNamedItem("id").getNodeValue());
                    Port port = new Port(direction, id);
                    port.parseXML(child);
                    portMap.put(id, port);
                } else {
                    throw new IOException("Unknown VirtualIO child node: " + child.getNodeName());
                }
            }
        }
    }

    public XMLBuilder toXML(boolean forName, String name) {
        XMLBuilder builder = new XMLBuilder("virtualio");
        if (forName) {
            builder.addStringOption("forName", name);
        } else {
            builder.addStringOption("forGroup", name);
        }
        for (Port port : portMap.values()) {
            builder.append(port.toXML());
        }
        return builder;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof VirtualIO)) {
            return false;
        }
        VirtualIO virtualIO = (VirtualIO) o;
        return Objects.equals(forGroup, virtualIO.forGroup) && Objects.equals(
            forName,
            virtualIO.forName
        ) && portMap.equals(virtualIO.portMap);
    }

    @Override
    public int hashCode() {
        return Objects.hash(forGroup, forName, portMap);
    }

    public static class Port {
        public final String direction;
        public final int id;

        public Outer outer;
        public Inner inner;
        public Loopback loopback;


        public Port(String direction, int id) {
            this.direction = direction;
            this.id = id;
        }


        public void parseXML(Node node) throws IOException {
            NodeList children = node.getChildNodes();
            for (int i = 0; i < children.getLength(); i++) {
                Node child = children.item(i);
                if (child.getNodeType() == Node.ELEMENT_NODE) {
                    int wireid = Integer.parseInt(child.getAttributes().getNamedItem("wireid").getNodeValue());
                    String side = child.getAttributes().getNamedItem("side").getNodeValue();
                    double offset = Double.parseDouble(child.getAttributes().getNamedItem("offset").getNodeValue());
                    switch (child.getNodeName()) {
                        case "outer" -> {
                            if (outer != null) {
                                throw new IOException("Multiple outer ports");
                            }
                            outer = new Outer(wireid, side, offset);
                        }
                        case "inner" -> {
                            if (inner != null) {
                                throw new IOException("Multiple inner ports");
                            }
                            inner = new Inner(wireid, side, offset);
                        }
                        case "loopback" -> {
                            if (loopback != null) {
                                throw new IOException("Multiple loopback ports");
                            }
                            if (direction.equals("out")) {
                                throw new IOException("Loopback port cannot be an output");
                            }
                            loopback = new Loopback(wireid, side, offset);
                        }
                        default -> throw new IOException("Unknown port type: " + child.getNodeName());
                    }
                }
            }
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof Port)) {
                return false;
            }
            Port port = (Port) o;
            return id == port.id && Objects.equals(direction, port.direction) && Objects.equals(
                outer,
                port.outer
            ) && Objects.equals(inner, port.inner) && Objects.equals(loopback, port.loopback);
        }

        @Override
        public int hashCode() {
            return Objects.hash(direction, id, outer, inner, loopback);
        }

        public XMLBuilder toXML() {
            XMLBuilder builder = new XMLBuilder("port");
            builder.addStringOption("direction", direction);
            builder.addStringOption("id", Integer.toString(id));
            if (outer != null) {
                builder.append(outer.toXML());
            }
            if (inner != null) {
                builder.append(inner.toXML());
            }
            if (loopback != null) {
                builder.append(loopback.toXML());
            }
            return builder;
        }
    }

    public static abstract class PortElement {
        public final int wireid;
        public final String side;
        public final double offset;

        public PortElement(int wireid, String side, double offset) {
            this.wireid = wireid;
            this.side = side;
            this.offset = offset;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof PortElement)) {
                return false;
            }
            PortElement that = (PortElement) o;
            return wireid == that.wireid && Double.compare(that.offset, offset) == 0 && Objects.equals(
                side,
                that.side
            );
        }

        @Override
        public int hashCode() {
            return Objects.hash(wireid, side, offset);
        }

        public abstract XMLBuilder toXML();

    }

    public static class Outer extends PortElement {
        public Outer(int wireid, String side, double offset) {
            super(wireid, side, offset);
        }

        @Override
        public XMLBuilder toXML() {
            return new XMLBuilder("outer", XMLBuilder.SELF_CLOSING)
                .addStringOption("wireid", Integer.toString(wireid))
                .addStringOption("side", side)
                .addStringOption("offset", Double.toString(offset));
        }

    }

    public static class Inner extends PortElement {
        public Inner(int wireid, String side, double offset) {
            super(wireid, side, offset);
        }

        @Override
        public XMLBuilder toXML() {
            return new XMLBuilder("inner", XMLBuilder.SELF_CLOSING)
                .addStringOption("wireid", Integer.toString(wireid))
                .addStringOption("side", side)
                .addStringOption("offset", Double.toString(offset));
        }
    }

    public static class Loopback extends PortElement {
        public Loopback(int wireid, String side, double offset) {
            super(wireid, side, offset);
        }

        @Override
        public XMLBuilder toXML() {
            return new XMLBuilder("loopback", XMLBuilder.SELF_CLOSING)
                .addStringOption("wireid", Integer.toString(wireid))
                .addStringOption("side", side)
                .addStringOption("offset", Double.toString(offset));
        }
    }
}
