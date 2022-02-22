package xyz.wagyourtail.konig.structure.headers;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.IOException;
import java.util.*;

public class BlockIO {
    public final Map<KonigBlock.Side, Map<KonigBlock.Justify, List<IOElement>>> elements = new HashMap<>();
    public final List<Input> inputs = new ArrayList<>();
    public final List<Output> outputs = new ArrayList<>();
    public final Map<String, IOElement> byName = new HashMap<>();

    public void parseXML(Node child) throws IOException {
        NodeList children = child.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child2 = children.item(i);
            if (child2.getNodeType() == Node.ELEMENT_NODE) {
                NamedNodeMap attrs = child2.getAttributes();
                KonigBlock.Side side = KonigBlock.Side.valueOf(attrs.getNamedItem("side")
                    .getNodeValue()
                    .toUpperCase(Locale.ROOT));
                KonigBlock.Justify justify = KonigBlock.Justify.valueOf(attrs.getNamedItem("justify")
                    .getNodeValue()
                    .toUpperCase(Locale.ROOT));
                String name = attrs.getNamedItem("name").getNodeValue();
                String type = attrs.getNamedItem("type").getNodeValue();

                if (child2.getNodeName().equals("input")) {
                    boolean optional =
                        attrs.getNamedItem("optional") != null && Boolean.parseBoolean(attrs.getNamedItem("optional")
                            .getNodeValue());
                    Input input = new Input(side, justify, name, type, optional);
                    inputs.add(input);
                    byName.put(name, input);
                    elements.computeIfAbsent(side, k -> new HashMap<>()).computeIfAbsent(justify,
                        k -> new ArrayList<>()
                    ).add(input);
                } else if (child2.getNodeName().equals("output")) {
                    Output output = new Output(side, justify, name, type);
                    outputs.add(output);
                    byName.put(name, output);
                    elements.computeIfAbsent(side, k -> new HashMap<>()).computeIfAbsent(justify,
                        k -> new ArrayList<>()
                    ).add(output);
                } else {
                    throw new IOException("Unknown child node: " + child2.getNodeName());
                }
            }
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(elements, inputs, outputs, byName);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof BlockIO)) {
            return false;
        }
        BlockIO io = (BlockIO) o;
        return Objects.equals(elements, io.elements) && Objects.equals(inputs, io.inputs) && Objects.equals(outputs,
            io.outputs
        ) && Objects.equals(byName, io.byName);
    }

    public static class IOElement {
        public final KonigBlock.Side side;
        public final KonigBlock.Justify justify;
        public final String name;
        public final String type;

        public IOElement(KonigBlock.Side side, KonigBlock.Justify justify, String name, String type) {
            this.side = side;
            this.justify = justify;
            this.name = name;
            this.type = type;
        }

        @Override
        public int hashCode() {
            return Objects.hash(side, justify, name, type);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof IOElement)) {
                return false;
            }
            IOElement ioElement = (IOElement) o;
            return side == ioElement.side && justify == ioElement.justify && Objects.equals(name, ioElement.name) &&
                Objects.equals(type, ioElement.type);
        }

    }

    public static class Input extends IOElement {
        public final boolean optional;

        public Input(KonigBlock.Side side, KonigBlock.Justify justify, String name, String type, boolean optional) {
            super(side, justify, name, type);
            this.optional = optional;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof Input)) {
                return false;
            }
            if (!super.equals(o)) {
                return false;
            }
            Input input = (Input) o;
            return optional == input.optional;
        }

        @Override
        public int hashCode() {
            return Objects.hash(super.hashCode(), optional);
        }

    }

    public static class Output extends IOElement {

        public Output(KonigBlock.Side side, KonigBlock.Justify justify, String name, String type) {
            super(side, justify, name, type);
        }

    }

}
