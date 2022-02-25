package xyz.wagyourtail.konig.lib.stdlib;

import xyz.wagyourtail.konig.lib.Block;
import xyz.wagyourtail.konig.structure.headers.BlockIO;

public class Math {

    @Block(
        name = "add",
        generics = {
            @Block.Generic(name="T")
        },
        inputs = {
            @Block.Input(name="in1", type="T", side = BlockIO.Side.LEFT, justify = BlockIO.Justify.CENTER),
            @Block.Input(name="in2", type="T", side = BlockIO.Side.RIGHT, justify = BlockIO.Justify.CENTER)
        },
        outputs = {
            @Block.Output(name="out", type="T", side = BlockIO.Side.BOTTOM, justify = BlockIO.Justify.CENTER)
        }
    )
    public Object add(Object a, Object b) {
        if (a instanceof Number) {
            return ((Number)a).doubleValue() + ((Number)b).doubleValue();
        } else {
            return a.toString() + b.toString();
        }
    }

}
