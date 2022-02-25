package xyz.wagyourtail.konig.lib.stdlib;

import xyz.wagyourtail.konig.lib.Block;
import xyz.wagyourtail.konig.structure.headers.BlockIO;

public class Generic {

    @Block(
        name = "print",
        inputs = {
            @Block.Input(name = "in", type = "any", side = BlockIO.Side.LEFT, justify = BlockIO.Justify.CENTER)
        },
        outputs = {}
    )
    public static void print(Object o) {
        System.out.println(o);
    }
}
