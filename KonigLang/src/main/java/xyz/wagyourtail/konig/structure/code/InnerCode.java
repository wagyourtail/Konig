package xyz.wagyourtail.konig.structure.code;

import org.w3c.dom.Node;
import xyz.wagyourtail.konig.structure.headers.BlockIO;
import xyz.wagyourtail.konig.structure.headers.KonigBlock;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ForkJoinPool;
import java.util.function.BiFunction;
import java.util.function.Function;

public class InnerCode extends Code {
    public String name;
    public KonigBlockReference outer;
    public InnerCode(KonigBlockReference block) {
        super(new InnerParent());
        outer = block;
    }

    @Override
    public void parseXML(Node node) throws IOException {
        name = node.getAttributes().getNamedItem("name").getNodeValue();
        super.parseXML(node);

        ((InnerParent) parent).applyBlock(outer, name);
    }

    @Override
    public BiFunction<ForkJoinPool, Map<String, Object>, CompletableFuture<Map<String, Object>>> jitCompile() {
        KonigBlock parentBlock = parent.getBlockByName(outer.name);
        KonigBlock childBlock = parent.getBlockByName("$hollowinner");

        // create "virtual" hollow blocks to connect with outer block
        KonigBlockReference input = new KonigBlockReference(this);
        input.id = -2;
        input.name = "$hollowinner";
        for (VirtualIO value : outer.virtualIONameMap.values()) {
            VirtualIO newVal = new VirtualIO();
            newVal.forName = value.forName;
            input.virtualIONameMap.put(value.forName, newVal);
            for (Map.Entry<Integer, VirtualIO.Port> integerPortEntry : value.portMap.entrySet()) {
                if (integerPortEntry.getValue().direction.equals("in")) {
                    VirtualIO.Port port = new VirtualIO.Port("out", integerPortEntry.getKey());
                    VirtualIO.Inner inner = integerPortEntry.getValue().inner;
                    port.outer = new VirtualIO.Outer(inner.wireid, inner.side, inner.offset);
                    newVal.portMap.put(port.id, port);
                }
            }
        }
        for (VirtualIO value : outer.virtualIOGroupsMap.values()) {
            VirtualIO newVal = new VirtualIO();
            newVal.forGroup = value.forGroup;
            input.virtualIOGroupsMap.put(value.forGroup, newVal);
            for (Map.Entry<Integer, VirtualIO.Port> integerPortEntry : value.portMap.entrySet()) {
                if (integerPortEntry.getValue().direction.equals("in")) {
                    VirtualIO.Port port = new VirtualIO.Port("out", integerPortEntry.getKey());
                    VirtualIO.Inner inner = integerPortEntry.getValue().inner;
                    port.outer = new VirtualIO.Outer(inner.wireid, inner.side, inner.offset);
                    newVal.portMap.put(port.id, port);
                }
            }
        }
        // block io to reference io
        for (BlockIO.IOElement value : childBlock.io.byName.values()) {
            input.io.elementMap.put(value.name, new ReferenceIO.IOElement(value.name, -42));
        }
        blockMap.put(-2, input);

        KonigBlockReference output = new KonigBlockReference(this);
        output.id = -1;
        output.name = "$hollowinner";
        for (VirtualIO value : outer.virtualIONameMap.values()) {
            VirtualIO newVal = new VirtualIO();
            newVal.forName = value.forName;
            output.virtualIONameMap.put(value.forName, newVal);
            for (Map.Entry<Integer, VirtualIO.Port> integerPortEntry : value.portMap.entrySet()) {
                    VirtualIO.Port port = new VirtualIO.Port("in", integerPortEntry.getKey());
                    VirtualIO.Inner inner = integerPortEntry.getValue().inner;
                    port.outer = new VirtualIO.Outer(inner.wireid, inner.side, inner.offset);
                    newVal.portMap.put(port.id, port);
            }
        }
        for (VirtualIO value : outer.virtualIOGroupsMap.values()) {
            VirtualIO newVal = new VirtualIO();
            newVal.forGroup = value.forGroup;
            output.virtualIOGroupsMap.put(value.forGroup, newVal);
            for (Map.Entry<Integer, VirtualIO.Port> integerPortEntry : value.portMap.entrySet()) {
                    VirtualIO.Port port = new VirtualIO.Port("in", integerPortEntry.getKey());
                    VirtualIO.Inner inner = integerPortEntry.getValue().inner;
                    port.outer = new VirtualIO.Outer(inner.wireid, inner.side, inner.offset);
                    newVal.portMap.put(port.id, port);
            }
        }
        for (BlockIO.IOElement value : childBlock.io.byName.values()) {
            output.io.elementMap.put(value.name, new ReferenceIO.IOElement(value.name, -42));
        }
        blockMap.put(-1, output);
        return super.jitCompile();
    }

    public static class InnerParent implements CodeParent {
        public CodeParent parent;
        public HollowBlock hb;

        public InnerParent() {
        }

        public void applyBlock(KonigBlockReference outer, String hollowname) {
            this.parent = outer.parent.parent;
            this.hb = new HollowBlock(outer, parent, hollowname);
        }

        @Override
        public KonigBlock getBlockByName(String name) {
            if (name.equals("$hollowinner")) {
                return hb;
            }
            return parent.getBlockByName(name);
        }

    }


    public static class HollowBlock extends KonigBlock {

        public HollowBlock(KonigBlockReference outer, CodeParent parent, String hollowname) {
            KonigBlock parentBlock = parent.getBlockByName(outer.name);
            if (parentBlock == null) {
                throw new RuntimeException("Parent block not found");
            }
            parentBlock.hollowsByName.get(hollowname).copyReverseTo(io);
        }

        @Override
        public BiFunction<ForkJoinPool, Map<String, CompletableFuture<Object>>, Map<String, CompletableFuture<Object>>> jitCompile(KonigBlockReference self) {
            return (a, b) -> b;
        }

    }
}
