package xyz.wagyourtail.konig.structure.code;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import xyz.wagyourtail.XMLBuilder;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ReferenceIO {
    public final Map<String, IOElement> elementMap = new HashMap<>();
    public final Map<String, Input> inputMap = new HashMap<>();
    public final Map<String, Output> outputMap = new HashMap<>();

    public void parseXML(Node node) throws IOException {
        NodeList children = node.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (child.getNodeType() == Node.ELEMENT_NODE) {
                String name = child.getAttributes().getNamedItem("name").getNodeValue();
                int wireid = Integer.parseInt(child.getAttributes().getNamedItem("wireid").getNodeValue());

                if (child.getNodeName().equals("input")) {
                    Input input = new Input(name, wireid);
                    elementMap.put(name, input);
                    inputMap.put(name, input);
                } else if (child.getNodeName().equals("output")) {
                    Output output = new Output(name, wireid);
                    elementMap.put(name, output);
                    outputMap.put(name, output);
                } else {
                    throw new IOException("Unknown element: " + child.getNodeName());
                }
            }
        }
    }

    public XMLBuilder toXML() {
        XMLBuilder builder = new XMLBuilder("io");
        for (IOElement element : elementMap.values()) {
            builder.append(element.toXML());
        }
        return builder;
    }

    public void remove(IOElement element) {
        elementMap.remove(element.name);
        if (element instanceof Input) {
            inputMap.remove(element.name);
        } else if (element instanceof Output) {
            outputMap.remove(element.name);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ReferenceIO)) {
            return false;
        }
        ReferenceIO that = (ReferenceIO) o;
        return Objects.equals(elementMap, that.elementMap) && Objects.equals(inputMap, that.inputMap) &&
            Objects.equals(outputMap, that.outputMap);
    }

    @Override
    public int hashCode() {
        return Objects.hash(elementMap, inputMap, outputMap);
    }

    public static abstract class IOElement {
        public final String name;
        public final int wireid;

        public IOElement(String name, int wireid) {
            this.name = name;
            this.wireid = wireid;
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
            return wireid == ioElement.wireid && Objects.equals(name, ioElement.name);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, wireid);
        }

        public abstract XMLBuilder toXML();
    }

    public static class Input extends IOElement {
        public Input(String name, int wireid) {
            super(name, wireid);
        }

        @Override
        public XMLBuilder toXML() {
            return new XMLBuilder("input").addStringOption("name", name).addStringOption("wireid", Integer.toString(wireid));
        }

    }

    public static class Output extends IOElement {
        public Output(String name, int wireid) {
            super(name, wireid);
        }

        @Override
        public XMLBuilder toXML() {
            return new XMLBuilder("output").addStringOption("name", name).addStringOption("wireid", Integer.toString(wireid));
        }

    }
}
