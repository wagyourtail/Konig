package xyz.wagyourtail.konig.lib;

import xyz.wagyourtail.konig.structure.code.KonigBlockReference;
import xyz.wagyourtail.konig.structure.headers.BlockIO;
import xyz.wagyourtail.konig.structure.headers.Hollow;
import xyz.wagyourtail.konig.structure.headers.KonigBlock;

import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.function.Supplier;

public class InternBlock extends KonigBlock {
    Method method;
    Block block;

    public void parseMethod(Method method, String defaultGroup) {
        block = method.getAnnotation(Block.class);
        if (block == null) throw new IllegalArgumentException("Method must be annotated with @Block");
        this.method = method;
        this.name = block.name();
        this.group = block.group().equals("") ? defaultGroup : block.group();
        this.image = Path.of(block.image());
        for (Block.Generic generic : block.generics()) {
            BlockIO.Generic g = new BlockIO.Generic(generic.name(), generic.extend(), generic.supers());
            generics.put(g.name, g);
        }
        parseIO(block.inputs(), block.outputs(), io);
        for (Block.Hollow hollow : block.hollows()) {
            Hollow h = new Hollow();
            h.name = hollow.name();
            h.group = hollow.group().equals("") ? "$ungrouped$" + hollow.name() : hollow.group();
            h.paddingTop = hollow.paddingTop();
            h.paddingBottom = hollow.paddingBottom();
            h.paddingLeft = hollow.paddingLeft();
            h.paddingRight = hollow.paddingRight();
            parseIO(hollow.inputs(), hollow.outputs(), h);
            hollowsByGroupName.computeIfAbsent(h.group, k -> new HashMap<>()).put(h.name, h);
            hollowsByName.put(h.name, h);
        }
    }

    public void parseIO(Block.Input[] inputs, Block.Output[] outputs, BlockIO io) {
        for (Block.Input input : inputs) {
            BlockIO.Input inp = new BlockIO.Input(input.side(), input.justify(), input.name(), input.type(), false);
            io.inputs.add(inp);
            io.byName.put(name, inp);
            io.elements.computeIfAbsent(inp.side, k -> new HashMap<>()).computeIfAbsent(inp.justify,
                k -> new ArrayList<>()
            ).add(inp);
        }
        for (Block.Output output : outputs) {
            BlockIO.Output out = new BlockIO.Output(output.side(), output.justify(), output.name(), output.type());
            io.outputs.add(out);
            io.byName.put(name, out);
            io.elements.computeIfAbsent(out.side, k -> new HashMap<>()).computeIfAbsent(out.justify,
                k -> new ArrayList<>()
            ).add(out);
        }
    }

    @Override
    public Function<Map<String, CompletableFuture<Object>>, Map<String, CompletableFuture<Object>>> compile(KonigBlockReference self) {
        return null;
    }
}
