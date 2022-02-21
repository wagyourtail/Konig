package xyz.wagyourtail.konig.structure;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

public class KonigBlock {
    public String name;
    public String group;
    public Path image;
    public final IO io = new IO();
    //TODO: hollow

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
        return false;
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

    public static class IO {
        public final Map<Side, Map<Justify, List<IOElement>>> elements = new HashMap<>();
        public final List<Input> inputs = new ArrayList<>();
        public final List<Output> outputs = new ArrayList<>();
        public final Map<String, IOElement> byName = new HashMap<>();

        public void parseXML(Node child) throws IOException {
            NodeList children = child.getChildNodes();
            for (int i = 0; i < children.getLength(); i++) {
                Node child2 = children.item(i);
                if (child2.getNodeType() == Node.ELEMENT_NODE) {
                    NamedNodeMap attrs = child2.getAttributes();
                    Side side = Side.valueOf(attrs.getNamedItem("side").getNodeValue().toUpperCase(Locale.ROOT));
                    Justify justify = Justify.valueOf(attrs.getNamedItem("justify").getNodeValue().toUpperCase(Locale.ROOT));
                    String name = attrs.getNamedItem("name").getNodeValue();
                    String type = attrs.getNamedItem("type").getNodeValue();

                    if (child2.getNodeName().equals("input")) {
                        boolean optional = Boolean.parseBoolean(attrs.getNamedItem("optional").getNodeValue());
                        Input input = new Input(side, justify, name, type, optional);
                        inputs.add(input);
                        byName.put(name, input);
                        elements.computeIfAbsent(side, k -> new HashMap<>()).computeIfAbsent(justify, k -> new ArrayList<>()).add(input);
                    } else if (child2.getNodeName().equals("output")) {
                        Output output = new Output(side, justify, name, type);
                        outputs.add(output);
                        byName.put(name, output);
                        elements.computeIfAbsent(side, k -> new HashMap<>()).computeIfAbsent(justify, k -> new ArrayList<>()).add(output);
                    } else {
                        throw new IOException("Unknown child node: " + child2.getNodeName());
                    }
                }
            }
        }

        public static class IOElement {
            public final Side side;
            public final Justify justify;
            public final String name;
            public final String type;

            public IOElement(Side side, Justify justify, String name, String type) {
                this.side = side;
                this.justify = justify;
                this.name = name;
                this.type = type;
            }

        }

        public static class Input extends IOElement {
            public final boolean optional;

            public Input(Side side, Justify justify, String name, String type, boolean optional) {
                super(side, justify, name, type);
                this.optional = optional;
            }

        }

        public static class Output extends IOElement {

            public Output(Side side, Justify justify, String name, String type) {
                super(side, justify, name, type);
            }

        }
    }
}
