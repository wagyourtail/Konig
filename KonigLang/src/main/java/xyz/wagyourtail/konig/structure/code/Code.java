package xyz.wagyourtail.konig.structure.code;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import xyz.wagyourtail.XMLBuilder;
import xyz.wagyourtail.konig.structure.headers.BlockIO;
import xyz.wagyourtail.konig.structure.headers.KonigBlock;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.BiFunction;
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

    public List<Wire> getWires() {
        return List.copyOf(wireMap.values());
    }

    public List<KonigBlockReference> getBlocks() {
        return List.copyOf(blockMap.values());
    }

    public void addBlock(KonigBlockReference block) {
        if (block.id == -1 || blockMap.containsKey(block.id)) {
            int id = 0;
            while (blockMap.keySet().contains(id)) {
                id++;
            }
            block.id = id;
        }
        block.parent = this;
        blockMap.put(block.id, block);
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

    public XMLBuilder toXML() {
        XMLBuilder builder = new XMLBuilder("main");
        XMLBuilder wires = new XMLBuilder("wires");
        for (Wire wire : wireMap.values()) {
            wires.append(wire.toXML());
        }
        builder.append(wires);
        XMLBuilder blocks = new XMLBuilder("blocks");
        for (KonigBlockReference block : blockMap.values()) {
            blocks.append(block.toXML());
        }
        builder.append(blocks);
        return builder;
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

    public Function<Map<String, Object>, CompletableFuture<Map<String, Object>>> jitCompile(boolean async) {
        BiFunction<ForkJoinPool, Map<String, Object>, CompletableFuture<Map<String, Object>>> compiled = jitCompile();
        return (inputs) -> {
            ForkJoinPool pool = async ? new ForkJoinPool(Runtime.getRuntime().availableProcessors()) : null;
            CompletableFuture<Map<String, Object>> result = compiled.apply(pool, inputs);
            // kill the pool when done
            if (pool != null) result.thenRun(pool::shutdown);
            return result;
        };
    }

    public BiFunction<ForkJoinPool, Map<String, Object>, CompletableFuture<Map<String, Object>>> jitCompile() {
        int i = 0;
        for (KonigBlockReference block : this.blockMap.values()) {
            i = Math.max(i, block.id);
        }
        BlockWires[] blockMap = new BlockWires[i+3];

        for (Wire wire : wireMap.values()) {
            CompiledWire cw = compileWire(wire);
            if (blockMap[cw.input.reference.id + 2] == null) {
                blockMap[cw.input.reference.id + 2] = new BlockWires(cw.input.reference);
            }
            blockMap[cw.input.reference.id + 2].outputs.add(cw);
            for (BlockPort output : cw.outputs.values()) {
                if (blockMap[output.reference.id + 2] == null) {
                    blockMap[output.reference.id + 2] = new BlockWires(output.reference);
                }
                blockMap[output.reference.id + 2].inputs.add(cw);
            }
        }

        // check for missing blocks
        for (KonigBlockReference block : this.blockMap.values()) {
            if (blockMap[block.id + 2] == null) {
                blockMap[block.id + 2] = new BlockWires(block);
            }
        }

        // check for circular references
        Arrays.stream(blockMap)
            .filter(Objects::nonNull)
            .filter(bw -> bw.outputs.size() == 0)
            .forEach(e -> e.checkCircular(blockMap, new HashSet<>(this.blockMap.values())));


        // compile the blocks
        for (BlockWires bw : blockMap) {
            if (bw != null) {
                bw.jitCompile(parent);
            }
        }

        // generate input block list
        Set<BlockWires> inputBlocks = new HashSet<>();
        for (BlockWires bw : blockMap) {
            if (bw != null) {
                if (bw.inputs.size() == 0) {
                    inputBlocks.add(bw);
                }
            }
        }

        // gen remaining execution order
        Set<BlockWires> executionOrder = new LinkedHashSet<>();
        for (BlockWires bw : inputBlocks) {
            for (CompiledWire output : bw.outputs) {
                for (KonigBlockReference outputRef : output.outputs.keySet()) {
                    genExecutionOrder(blockMap[outputRef.id + 2], blockMap, executionOrder, inputBlocks);
                }
            }
        }

        // generate output block list
        Set<Integer> outputBlockIds = new HashSet<>();
        for (BlockWires bw : blockMap) {
            if (bw != null) {
                if (bw.outputs.size() == 0) {
                    outputBlockIds.add(bw.reference.id);
                }
            }
        }

        return (pool, inputs) -> {
            Map<String, CompletableFuture<Object>>[] runBlocks = new Map[blockMap.length];
            Map<String, Object> outputs = new ConcurrentHashMap<>();
            Map<String, CompletableFuture<Object>> runInputs = new HashMap<>();
            for (Map.Entry<String, Object> entry : inputs.entrySet()) {
                runInputs.put(entry.getKey(), CompletableFuture.completedFuture(entry.getValue()));
            }
            //            Arrays.stream(blockMap).filter(Objects::nonNull).filter(e -> e.inputs.size() == 0).forEach(e -> {
            //                runBlock(e, runInputs, runBlocks, blockMap);
            //            });

            for (BlockWires bw : inputBlocks) {
                runBlock(pool, bw, runInputs, runBlocks);
            }

            for (BlockWires bw : executionOrder) {
                Map<String, CompletableFuture<Object>> nextInputs = new HashMap<>();
                for (CompiledWire input : bw.inputs) {
                    nextInputs.put(input.outputs.get(bw.reference).portName, runBlocks[input.input.reference.id + 2].get(input.input.portName));
                }
                runBlock(pool, bw, nextInputs, runBlocks);
            }

            List<Map.Entry<String, CompletableFuture<Object>>> entries = outputBlockIds.stream().map(e -> runBlocks[e + 2]).flatMap((b) -> b.entrySet().stream()).collect(Collectors.toList());
            return CompletableFuture.allOf(entries.stream().map(Map.Entry::getValue).toArray(CompletableFuture[]::new)).thenApply(v -> {
                entries.forEach(e -> {
                    if (e.getValue().join() != null) {
                        outputs.put(e.getKey(), e.getValue().join());
                    }
                });
                outputs.remove("$void");
                return outputs;
            });
        };
    }

    private void genExecutionOrder(BlockWires current, BlockWires[] blockMap, Set<BlockWires> executionOrder, Set<BlockWires> inputsBlocks) {
        for (CompiledWire input : current.inputs) {
            BlockWires inputBlock = blockMap[input.input.reference.id + 2];
            if (!executionOrder.contains(inputBlock) && !inputsBlocks.contains(inputBlock)) {
                genExecutionOrder(inputBlock, blockMap, executionOrder, inputsBlocks);
            }
        }
        executionOrder.add(current);
        for (CompiledWire output : current.outputs) {
            for (KonigBlockReference outputRef : output.outputs.keySet()) {
                genExecutionOrder(blockMap[outputRef.id + 2], blockMap, executionOrder, inputsBlocks);
            }
        }
    }

    private void runBlock(ForkJoinPool async, BlockWires bw, Map<String, CompletableFuture<Object>> runInputs, Map<String, CompletableFuture<Object>>[] runBlocks) {
        Map<String, CompletableFuture<Object>> outputs = bw.compiled.apply(async, runInputs);
        runBlocks[bw.reference.id + 2] = outputs;
    }

    public CompiledWire compileWire(Wire wire) {
        BlockPort input = null;
        List<BlockPort> output = new ArrayList<>();
        String type = null;
        for (Wire.WireEndpoint endpoint : wire.getEndpoints()) {
            KonigBlockReference br = blockMap.get(endpoint.blockid);
            ReferenceIO.IOElement ioe = br.io.elementMap.get(endpoint.port);
            if (ioe == null) {
                // find if virtual
                String[] parts = endpoint.port.split("\\$");
                if (parts.length != 4) {
                    if (parts.length != 5 || !parts[4].equals("loopback")) {
                        throw new IllegalStateException("Invalid virtual port name: " + endpoint.port);
                    }
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
                if (p.outer.wireid != wire.id && br.id > -1) {
                    throw new IllegalStateException(
                        "Wire " + wire.id + " has endpoint " + endpoint.blockid + ":" + endpoint.port +
                            " which the block does not back-reference to the wire");
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
                // skip this check for "virtual" blocks from innercode
                // TODO: redifine how innercode connects to the outer block to not skip this?
                if (ioe.wireid != wire.id && br.id > -1) {
                    throw new IllegalStateException(
                        "Wire " + wire.id + " has endpoint " + endpoint.blockid + ":" + endpoint.port +
                            " which the block does not back-reference to the wire");
                }
                KonigBlock block = parent.getBlockByName(br.name);
                if (block == null) {
                    throw new IllegalStateException("Block " + br.name + " does not exist");
                }
                BlockIO.IOElement port = block.io.byName.get(endpoint.port);
                if (port == null) {
                    throw new IllegalStateException("Port " + endpoint.port + " does not exist in block " + br.name);
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

        public BiFunction<ForkJoinPool, Map<String, CompletableFuture<Object>>, Map<String, CompletableFuture<Object>>> compiled;

        public BlockWires(KonigBlockReference reference) {
            this.reference = reference;
        }

        public void checkCircular(BlockWires[] wireMap, Set<KonigBlockReference> remaining) {
            if (remaining.contains(reference)) {
                HashSet<KonigBlockReference> visited = new HashSet<>(remaining);
                visited.remove(reference);
                for (CompiledWire input : inputs) {
                    wireMap[input.input.reference.id + 2].checkCircular(wireMap, visited);
                }
            } else {
                throw new IllegalStateException("Circular wire into: " + reference);
            }
        }

        public void jitCompile(CodeParent parent) {
            KonigBlock kb = parent.getBlockByName(reference.name);
            compiled = kb.jitCompile(reference);
        }

        public synchronized boolean areExpectedWiresPresent(Map<String, CompletableFuture<Object>>[] bw) {
            for (CompiledWire input : inputs) {
                if (bw[input.input.reference.id + 2] == null) {
                    return false;
                }
            }
            return true;
        }
    }
}
