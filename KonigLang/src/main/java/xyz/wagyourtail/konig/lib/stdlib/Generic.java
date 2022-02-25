package xyz.wagyourtail.konig.lib.stdlib;

import xyz.wagyourtail.konig.lib.Block;
import xyz.wagyourtail.konig.structure.headers.BlockIO;

import java.util.function.Function;

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

    @Block(
        name = "tostring",
        inputs = {
            @Block.Input(name = "in", type = "any", side = BlockIO.Side.LEFT, justify = BlockIO.Justify.CENTER)
        },
        outputs = {
            @Block.Output(name = "out", type = "string", side = BlockIO.Side.RIGHT, justify = BlockIO.Justify.CENTER)
        }
    )
    public static String toString(Object o) {
        return o.toString();
    }

    @Block(
        name = "tonumber",
        inputs = {
            @Block.Input(name = "in", type = "any", side = BlockIO.Side.LEFT, justify = BlockIO.Justify.CENTER)
        },
        outputs = {
            @Block.Output(name = "out", type = "number", side = BlockIO.Side.RIGHT, justify = BlockIO.Justify.CENTER)
        }
    )
    public static Number toNumber(Object o) {
        return Double.parseDouble(o.toString());
    }

    @Block(
        name = "toboolean",
        inputs = {
            @Block.Input(name = "in", type = "any", side = BlockIO.Side.LEFT, justify = BlockIO.Justify.CENTER)
        },
        outputs = {
            @Block.Output(name = "out", type = "boolean", side = BlockIO.Side.RIGHT, justify = BlockIO.Justify.CENTER)
        }
    )
    public static boolean toBoolean(Object o) {
        if (o instanceof Boolean) {
            return (boolean) o;
        } else if (o instanceof Number) {
            return ((Number) o).doubleValue() != 0;
        } else {
            return o.toString().length() > 0 && !o.toString().equals("0") && !o.toString().equals("false");
        }
    }

}
