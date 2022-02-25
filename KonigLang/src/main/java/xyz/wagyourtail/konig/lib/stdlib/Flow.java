package xyz.wagyourtail.konig.lib.stdlib;

import xyz.wagyourtail.konig.lib.Block;
import xyz.wagyourtail.konig.structure.headers.BlockIO;

import java.util.function.Function;

public class Flow {
    @Block(
        name = "loop",
        inputs = {},
        outputs = {},
        hollows = {
            @Block.Hollow(name = "inner",
                inputs = {},
                outputs = {
                    @Block.Output(name = "continue", type = "boolean", side = BlockIO.Side.RIGHT, justify = BlockIO.Justify.CENTER)
                }
            )
        }
    )
    public static void loop(Function<Object[], Object> o) {
        while ((boolean) o.apply(new Object[]{}));
    }

    @Block(
        name = "if",
        inputs = {
            @Block.Input(name = "condition", type = "boolean", side = BlockIO.Side.LEFT, justify = BlockIO.Justify.CENTER)
        },
        outputs = {},
        hollows = {
            @Block.Hollow(name = "true",
                inputs = {},
                outputs = {}
            ),
            @Block.Hollow(name = "false",
                inputs = {},
                outputs = {}
            )
        }
    )
    public static void ifS(boolean condition, Function<Object[], Object> tr, Function<Object[], Object> fl) {
        if (condition) {
            tr.apply(new Object[]{});
        } else {
            fl.apply(new Object[]{});
        }
    }

    @Block(
        name = "stackedif",
        inputs = {
            @Block.Input(name = "condition", type = "boolean", side = BlockIO.Side.LEFT, justify = BlockIO.Justify.CENTER)
        },
        outputs = {},
        hollows = {
            @Block.Hollow(name = "true", group = "stack",
                inputs = {},
                outputs = {}
            ),
            @Block.Hollow(name = "false", group = "stack",
                inputs = {},
                outputs = {}
            )
        }
    )
    public static void ifS2(boolean condition, Function<Object[], Object> tr, Function<Object[], Object> fl) {
        if (condition) {
            tr.apply(new Object[]{});
        } else {
            fl.apply(new Object[]{});
        }
    }
}
