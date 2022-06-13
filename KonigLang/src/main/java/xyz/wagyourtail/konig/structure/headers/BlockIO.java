package xyz.wagyourtail.konig.structure.headers;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import xyz.wagyourtail.XMLBuilder;

import java.io.IOException;
import java.util.*;

public class BlockIO {
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
                Side side = Side.valueOf(attrs.getNamedItem("side")
                    .getNodeValue()
                    .toUpperCase(Locale.ROOT));
                Justify justify = Justify.valueOf(attrs.getNamedItem("justify")
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

    public XMLBuilder toXML() {
        XMLBuilder builder = new XMLBuilder("io");
        for (Map<Justify, List<IOElement>> side : elements.values()) {
            for (List<IOElement> justify : side.values()) {
                for (IOElement element : justify) {
                    builder.append(element.toXML());
                }
            }
        }
        return builder;
    }

    public void copyTo(BlockIO io) {
        io.elements.putAll(elements);
        io.inputs.addAll(inputs);
        io.outputs.addAll(outputs);
        io.byName.putAll(byName);
    }

    public void copyInputsTo(BlockIO io) {
        io.inputs.addAll(inputs);
        for (Input input : inputs) {
            io.elements.computeIfAbsent(input.side, k -> new HashMap<>()).computeIfAbsent(input.justify, k -> new ArrayList<>()).add(input);
            io.byName.put(input.name, input);
        }
    }

    public void copyOutputsTo(BlockIO io) {
        io.outputs.addAll(outputs);
        for (Output output : outputs) {
            io.elements.computeIfAbsent(output.side, k -> new HashMap<>()).computeIfAbsent(output.justify, k -> new ArrayList<>()).add(output);
            io.byName.put(output.name, output);
        }
    }

    public void copyReverseTo(BlockIO io) {
        for (Input input : inputs) {
            Output output = new Output(input.side, input.justify, input.name, input.type);
            io.elements.computeIfAbsent(output.side, k -> new HashMap<>()).computeIfAbsent(output.justify, k -> new ArrayList<>()).add(output);
            io.outputs.add(output);
            io.byName.put(output.name, output);
        }
        for (Output output : outputs) {
            Input input = new Input(output.side, output.justify, output.name, output.type, false);
            io.elements.computeIfAbsent(input.side, k -> new HashMap<>()).computeIfAbsent(input.justify, k -> new ArrayList<>()).add(input);
            io.inputs.add(input);
            io.byName.put(input.name, input);
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

    public static abstract class IOElement {
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

        protected void buildXML(XMLBuilder builder) {
            builder.addStringOption("side", side.name().toLowerCase(Locale.ROOT))
                .addStringOption("justify", justify.name().toLowerCase(Locale.ROOT))
                .addStringOption("name", name)
                .addStringOption("type", type);
        }

        public abstract XMLBuilder toXML();
    }

    public static class Input extends IOElement {
        public final boolean optional;

        public Input(Side side, Justify justify, String name, String type, boolean optional) {
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
        public XMLBuilder toXML() {
            XMLBuilder builder = new XMLBuilder("input");
            buildXML(builder);
            builder.addStringOption("optional", optional ? "true" : "false");
            return builder;
        }

        @Override
        public int hashCode() {
            return Objects.hash(super.hashCode(), optional);
        }

    }

    public static class Output extends IOElement {

        public Output(Side side, Justify justify, String name, String type) {
            super(side, justify, name, type);
        }

        @Override
        public XMLBuilder toXML() {
            XMLBuilder builder = new XMLBuilder("output");
            buildXML(builder);
            return builder;
        }

    }

    public static class Generic {
        public final String name;
        public final String extend;
        public final String supers;

        public Generic(String name, String extend, String supers) {
            this.name = name;
            this.extend = extend;
            this.supers = supers;
        }

        public XMLBuilder toXML() {
            XMLBuilder builder = new XMLBuilder("generic");
            builder.addStringOption("name", name);
            if (extend != null) {
                builder.addStringOption("extends", extend);
            }
            if (supers != null) {
                builder.addStringOption("supers", supers);
            }
            return builder;
        }
    }

}
