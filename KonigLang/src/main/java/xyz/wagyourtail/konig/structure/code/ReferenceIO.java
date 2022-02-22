package xyz.wagyourtail.konig.structure.code;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

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

    public static class IOElement {
        public final String name;
        public final int wireid;

        public IOElement(String name, int wireid) {
            this.name = name;
            this.wireid = wireid;
        }
    }

    public static class Input extends IOElement {
        public Input(String name, int wireid) {
            super(name, wireid);
        }
    }

    public static class Output extends IOElement {
        public Output(String name, int wireid) {
            super(name, wireid);
        }
    }
}
