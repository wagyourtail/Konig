package xyz.wagyourtail.konig.structure.code;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import xyz.wagyourtail.konig.structure.headers.BlockIO;
import xyz.wagyourtail.konig.structure.headers.KonigBlock;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

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

    public Function<Map<String, Object>, CompletableFuture<Map<String, Object>>> compile() {
        // map all the blocks
        Map<KonigBlockReference, BlockWires> blockMap = new HashMap<>();
        for (Wire wire : wireMap.values()) {
            CompiledWire cw = compileWire(wire);
            blockMap.computeIfAbsent(cw.input.reference, BlockWires::new).outputs.add(cw);
            for (BlockPort output : cw.outputs) {
                blockMap.computeIfAbsent(output.reference, BlockWires::new).inputs.add(cw);
            }
        }
        return (inputs) -> {
            Map<String, Object> outputs = new HashMap<>();
            return CompletableFuture.supplyAsync(() -> {
                //TODO: finish impl

                return outputs;
            });
        };
    }

    public CompiledWire compileWire(Wire wire) {
        BlockPort input = null;
        List<BlockPort> output = new ArrayList<>();
        String type = null;
        for (Wire.WireEndpoint endpoint : wire.getEndpoints()) {
            KonigBlockReference br = blockMap.get(endpoint.blockid);
            ReferenceIO.IOElement ioe = br.io.elementMap.get(endpoint.port);
            if (ioe.wireid != wire.id) {
                throw new IllegalStateException("Wire " + wire.id + " has endpoint " + endpoint.blockid + ":" + endpoint.port + " which the block does not back-reference to the wire");
            }
            KonigBlock block = parent.getBlockByName(br.name);
            BlockIO.IOElement port = block.io.byName.get(endpoint.port);
            if (port == null) {
                if (endpoint.port.startsWith("virtual")) {
                    String[] parts = endpoint.port.split("\\$");
                    if (parts.length != 4) {
                        throw new IllegalStateException("Invalid virtual port name: " + endpoint.port);
                    }
                    VirtualIO.Port p;
                    if (parts[1].equals("forName")) {
                        p = br.virtualIONameMap.get(parts[2]).portMap.get(Integer.parseInt(parts[3]));
                    } else if (parts[1].equals("forGroup")) {
                        p = br.virtualIOGroupsMap.get(parts[2]).portMap.get(Integer.parseInt(parts[3]));
                    } else {
                        throw new IllegalStateException("Invalid virtual port name: " + endpoint.port);
                    }
                    if (p == null) {
                        throw new IllegalStateException("Invalid virtual port name: " + endpoint.port);
                    }
                    if (Objects.equals(p.direction, "out")) {
                        if (input != null) {
                            throw new IllegalStateException("Multiple inputs for wire " + wire.id);
                        }
                        input = new BlockPort(br, endpoint.port);
                    } else {
                        output.add(new BlockPort(br, endpoint.port));
                    }
                } else {
                    throw new IllegalStateException("Unknown port: " + endpoint.port);
                }
            } else {
                if (port instanceof BlockIO.Output) {
                    if (input != null) {
                        throw new IllegalStateException("Wire " + wire.id + " has multiple inputs");
                    }
                    input = new BlockPort(br, endpoint.port);
                    if (block.generics.get(port.type) == null) {
                        type = port.type;
                    }
                } else if (port instanceof BlockIO.Input) {
                    output.add(new BlockPort(br, endpoint.port));
                    if (block.generics.get(port.type) == null) {
                        type = port.type;
                    }
                } else {
                    throw new IllegalStateException("Unknown port type: " + port.getClass());
                }
            }

        }

        return new CompiledWire(type, input, output);
    }

    public static class CompiledWire {
        public String type;
        public final BlockPort input;
        public final List<BlockPort> outputs = new ArrayList<>();

        public CompiledWire(String type, BlockPort input, List<BlockPort> outputs) {
            this.type = type;
            this.input = input;
            this.outputs.addAll(outputs);
        }
    }

    public static class BlockPort {
        public final KonigBlockReference reference;
        public final String portName;

        public BlockPort(KonigBlockReference reference, String portName) {
            this.reference = reference;
            this.portName = portName;
        }
    }

    public static class BlockWires {
        public final KonigBlockReference reference;
        public final List<CompiledWire> inputs = new ArrayList<>();
        public final List<CompiledWire> outputs = new ArrayList<>();

        public BlockWires(KonigBlockReference reference) {
            this.reference = reference;
        }
    }
}
