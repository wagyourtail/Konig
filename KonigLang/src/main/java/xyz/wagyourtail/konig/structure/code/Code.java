package xyz.wagyourtail.konig.structure.code;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import xyz.wagyourtail.konig.structure.headers.BlockIO;
import xyz.wagyourtail.konig.structure.headers.KonigBlock;
import xyz.wagyourtail.konig.structure.headers.blocks.GlobalInput;
import xyz.wagyourtail.konig.structure.headers.blocks.GlobalOutput;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

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
            for (BlockPort output : cw.outputs.values()) {
                blockMap.computeIfAbsent(output.reference, BlockWires::new).inputs.add(cw);
            }
        }

        // check for missing blocks
        for (KonigBlockReference block : this.blockMap.values()) {
            if (!blockMap.containsKey(block)) {
                blockMap.put(block, new BlockWires(block));
            }
        }

        // check for circular references
        blockMap.values()
            .parallelStream()
            .filter(bw -> bw.outputs.size() == 0)
            .forEach(e -> e.checkCircular(blockMap, blockMap.keySet()));

        // compile the blocks
        blockMap.values().parallelStream().forEach(bw -> bw.precompile(parent));


        return (inputs) -> {
            Map<KonigBlockReference, Map<String, CompletableFuture<Object>>> runBlocks = new ConcurrentHashMap<>();
            Map<String, Object> outputs = new HashMap<>();
            Map<String, CompletableFuture<Object>> runInputs = new HashMap<>();
            for (Map.Entry<String, Object> entry : inputs.entrySet()) {
                runInputs.put(entry.getKey(), CompletableFuture.completedFuture(entry.getValue()));
            }
            return CompletableFuture.supplyAsync(() -> {
                blockMap.values().parallelStream().filter(e -> e.inputs.size() == 0).forEach(e -> {
                    runBlock(e, runInputs, runBlocks, blockMap);
                });
                return blockMap.values().parallelStream().filter(e -> e.outputs.size() == 0).filter(e -> e.reference.name.equals("globaloutput")).map(e -> runBlocks.get(e.reference)).reduce(new HashMap(), (a, b) -> {
                    b.forEach((k, v) -> a.put(k, (CompletableFuture<Object>) v.join()));
                    return a;
                });
            });
        };
    }

    private void runBlock(BlockWires bw, Map<String, CompletableFuture<Object>> runInputs, Map<KonigBlockReference, Map<String, CompletableFuture<Object>>> runBlocks, Map<KonigBlockReference, BlockWires> blockMap) {
        synchronized (runBlocks) {
            if (runBlocks.containsKey(bw.reference)) {
                return;
            }
            Map<String, CompletableFuture<Object>> outputs = bw.compiled.apply(runInputs);
            runBlocks.put(bw.reference, outputs);
        }
        for (CompiledWire output : bw.outputs) {
            for (KonigBlockReference nextBlockReference : output.outputs.keySet()) {
                BlockWires nextBlock = blockMap.get(nextBlockReference);
                if (nextBlock.areExpectedWiresPresent(runBlocks.keySet())) {
                    Map<String, CompletableFuture<Object>> nextInputs = new HashMap<>();
                    for (CompiledWire input : nextBlock.inputs) {
                        nextInputs.put(input.outputs.get(nextBlockReference).portName, runBlocks.get(input.input.reference).get(input.input.portName));
                    }
                    runBlock(nextBlock, nextInputs, runBlocks, blockMap);
                }
            }
        }
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
            if (block == null) {
                throw new IllegalStateException("Block " + br.name + " does not exist");
            }
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
                        if (type != null) {
                            if (!type.equals(port.type) && !port.type.equals("any")) {
                                throw new IllegalStateException("Wire " + wire.id + " has multiple types");
                            }
                        }
                        type = port.type;
                    }
                } else if (port instanceof BlockIO.Input) {
                    output.add(new BlockPort(br, endpoint.port));
                    if (block.generics.get(port.type) == null) {
                        if (type != null) {
                            if (!type.equals(port.type) && !port.type.equals("any")) {
                                throw new IllegalStateException("Wire " + wire.id + " has multiple types");
                            }
                        }
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
        public final Map<KonigBlockReference, BlockPort> outputs = new HashMap<>();

        public CompiledWire(String type, BlockPort input, List<BlockPort> outputs) {
            this.type = type;
            this.input = input;
            for (BlockPort output : outputs) {
                this.outputs.put(output.reference, output);
            }
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
        public final Set<CompiledWire> inputs = new HashSet<>();
        public final Set<CompiledWire> outputs = new HashSet<>();

        public Function<Map<String, CompletableFuture<Object>>, Map<String, CompletableFuture<Object>>> compiled;

        public BlockWires(KonigBlockReference reference) {
            this.reference = reference;
        }

        public void checkCircular(Map<KonigBlockReference, BlockWires> wireMap, Set<KonigBlockReference> remaining) {
            if (remaining.contains(reference)) {
                HashSet<KonigBlockReference> visited = new HashSet<>(remaining);
                visited.remove(reference);
                for (CompiledWire input : inputs) {
                    wireMap.get(input.input.reference).checkCircular(wireMap, visited);
                }
            } else {
                throw new IllegalStateException("Circular wire into: " + reference);
            }
        }

        public void precompile(CodeParent parent) {
            KonigBlock kb = parent.getBlockByName(reference.name);
            compiled = kb.compile(reference);
        }

        public synchronized boolean areExpectedWiresPresent(Set<KonigBlockReference> bw) {
            for (CompiledWire input : inputs) {
                if (!bw.contains(input.input.reference)) {
                    return false;
                }
            }
            return true;
        }
    }
}
