package xyz.wagyourtail.konig.lib.stdlib;

import xyz.wagyourtail.konig.lib.Block;
import xyz.wagyourtail.konig.structure.headers.BlockIO;

public class Boolean {

    @Block(
        name = "lt",
        inputs = {
            @Block.Input(name = "in1", type = "any", side = BlockIO.Side.LEFT, justify = BlockIO.Justify.CENTER),
            @Block.Input(name = "in2", type = "any", side = BlockIO.Side.LEFT, justify = BlockIO.Justify.CENTER)
        },
        outputs = {
            @Block.Output(name = "out", type = "boolean", side = BlockIO.Side.RIGHT, justify = BlockIO.Justify.CENTER)
        }
    )
    public static boolean lessThan(Object inp1, Object inp2) {
        if (inp1 instanceof Number && inp2 instanceof Number) {
            return ((Number) inp1).doubleValue() < ((Number) inp2).doubleValue();
        }
        if (inp1 instanceof Comparable<?> && inp2 instanceof Comparable<?>) {
            if (inp1.getClass().isAssignableFrom(inp2.getClass())) {
                return ((Comparable) inp1).compareTo(inp2) < 0;
            } else if (inp2.getClass().isAssignableFrom(inp1.getClass())) {
                return ((Comparable) inp2).compareTo(inp1) > 0;
            }
        }
        return inp1.toString().compareTo(inp2.toString()) < 0;
    }

    @Block(
        name = "leq",
        inputs = {
            @Block.Input(name = "in1", type = "any", side = BlockIO.Side.LEFT, justify = BlockIO.Justify.CENTER),
            @Block.Input(name = "in2", type = "any", side = BlockIO.Side.LEFT, justify = BlockIO.Justify.CENTER)
        },
        outputs = {
            @Block.Output(name = "out", type = "boolean", side = BlockIO.Side.RIGHT, justify = BlockIO.Justify.CENTER)
        }
    )
    public static boolean lessThanOrEqual(Object inp1, Object inp2) {
        if (inp1 instanceof Number && inp2 instanceof Number) {
            return ((Number) inp1).doubleValue() <= ((Number) inp2).doubleValue();
        }
        if (inp1 instanceof Comparable<?> && inp2 instanceof Comparable<?>) {
            if (inp1.getClass().isAssignableFrom(inp2.getClass())) {
                return ((Comparable) inp1).compareTo(inp2) <= 0;
            } else if (inp2.getClass().isAssignableFrom(inp1.getClass())) {
                return ((Comparable) inp2).compareTo(inp1) >= 0;
            }
        }
        return inp1.toString().compareTo(inp2.toString()) <= 0;
    }

    @Block(
        name = "gt",
        inputs = {
            @Block.Input(name = "in1", type = "any", side = BlockIO.Side.LEFT, justify = BlockIO.Justify.CENTER),
            @Block.Input(name = "in2", type = "any", side = BlockIO.Side.LEFT, justify = BlockIO.Justify.CENTER)
        },
        outputs = {
            @Block.Output(name = "out", type = "boolean", side = BlockIO.Side.RIGHT, justify = BlockIO.Justify.CENTER)
        }
    )
    public static boolean greaterThan(Object inp1, Object inp2) {
        if (inp1 instanceof Number && inp2 instanceof Number) {
            return ((Number) inp1).doubleValue() > ((Number) inp2).doubleValue();
        }
        if (inp1 instanceof Comparable<?> && inp2 instanceof Comparable<?>) {
            if (inp1.getClass().isAssignableFrom(inp2.getClass())) {
                return ((Comparable) inp1).compareTo(inp2) > 0;
            } else if (inp2.getClass().isAssignableFrom(inp1.getClass())) {
                return ((Comparable) inp2).compareTo(inp1) < 0;
            }
        }
        return inp1.toString().compareTo(inp2.toString()) > 0;
    }

    @Block(
        name = "geq",
        inputs = {
            @Block.Input(name = "in1", type = "any", side = BlockIO.Side.LEFT, justify = BlockIO.Justify.CENTER),
            @Block.Input(name = "in2", type = "any", side = BlockIO.Side.LEFT, justify = BlockIO.Justify.CENTER)
        },
        outputs = {
            @Block.Output(name = "out", type = "boolean", side = BlockIO.Side.RIGHT, justify = BlockIO.Justify.CENTER)
        }
    )
    public static boolean greaterThanOrEqual(Object inp1, Object inp2) {
        if (inp1 instanceof Number && inp2 instanceof Number) {
            return ((Number) inp1).doubleValue() >= ((Number) inp2).doubleValue();
        }
        if (inp1 instanceof Comparable<?> && inp2 instanceof Comparable<?>) {
            if (inp1.getClass().isAssignableFrom(inp2.getClass())) {
                return ((Comparable) inp1).compareTo(inp2) >= 0;
            } else if (inp2.getClass().isAssignableFrom(inp1.getClass())) {
                return ((Comparable) inp2).compareTo(inp1) <= 0;
            }
        }
        return inp1.toString().compareTo(inp2.toString()) >= 0;
    }

    @Block(
        name = "eq",
        inputs = {
            @Block.Input(name = "in1", type = "any", side = BlockIO.Side.LEFT, justify = BlockIO.Justify.CENTER),
            @Block.Input(name = "in2", type = "any", side = BlockIO.Side.LEFT, justify = BlockIO.Justify.CENTER)
        },
        outputs = {
            @Block.Output(name = "out", type = "boolean", side = BlockIO.Side.RIGHT, justify = BlockIO.Justify.CENTER)
        }
    )
    public static boolean equals(Object inp1, Object inp2) {
        return inp1.equals(inp2);
    }

    @Block(
        name = "neq",
        inputs = {
            @Block.Input(name = "in1", type = "any", side = BlockIO.Side.LEFT, justify = BlockIO.Justify.CENTER),
            @Block.Input(name = "in2", type = "any", side = BlockIO.Side.LEFT, justify = BlockIO.Justify.CENTER)
        },
        outputs = {
            @Block.Output(name = "out", type = "boolean", side = BlockIO.Side.RIGHT, justify = BlockIO.Justify.CENTER)
        }
    )
    public static boolean notEquals(Object inp1, Object inp2) {
        return !inp1.equals(inp2);
    }

    @Block(
        name = "and",
        inputs = {
            @Block.Input(name = "in1", type = "boolean", side = BlockIO.Side.LEFT, justify = BlockIO.Justify.CENTER),
            @Block.Input(name = "in2", type = "boolean", side = BlockIO.Side.LEFT, justify = BlockIO.Justify.CENTER)
        },
        outputs = {
            @Block.Output(name = "out", type = "boolean", side = BlockIO.Side.RIGHT, justify = BlockIO.Justify.CENTER)
        }
    )
    public static boolean and(boolean inp1, boolean inp2) {
        return inp1 && inp2;
    }

    @Block(
        name = "or",
        inputs = {
            @Block.Input(name = "in1", type = "boolean", side = BlockIO.Side.LEFT, justify = BlockIO.Justify.CENTER),
            @Block.Input(name = "in2", type = "boolean", side = BlockIO.Side.LEFT, justify = BlockIO.Justify.CENTER)
        },
        outputs = {
            @Block.Output(name = "out", type = "boolean", side = BlockIO.Side.RIGHT, justify = BlockIO.Justify.CENTER)
        }
    )
    public static boolean or(boolean inp1, boolean inp2) {
        return inp1 || inp2;
    }

    @Block(
        name = "not",
        inputs = {
            @Block.Input(name = "in", type = "boolean", side = BlockIO.Side.LEFT, justify = BlockIO.Justify.CENTER)
        },
        outputs = {
            @Block.Output(name = "out", type = "boolean", side = BlockIO.Side.RIGHT, justify = BlockIO.Justify.CENTER)
        }
    )
    public static boolean not(boolean inp) {
        return !inp;
    }

    @Block(
        name = "xor",
        inputs = {
            @Block.Input(name = "in1", type = "boolean", side = BlockIO.Side.LEFT, justify = BlockIO.Justify.CENTER),
            @Block.Input(name = "in2", type = "boolean", side = BlockIO.Side.LEFT, justify = BlockIO.Justify.CENTER)
        },
        outputs = {
            @Block.Output(name = "out", type = "boolean", side = BlockIO.Side.RIGHT, justify = BlockIO.Justify.CENTER)
        }
    )
    public static boolean xor(boolean inp1, boolean inp2) {
        return inp1 ^ inp2;
    }

    @Block(
        name = "iif",
        generics = {
            @Block.Generic(name = "T")
        },
        inputs = {
            @Block.Input(name = "condition", type = "boolean", side = BlockIO.Side.LEFT, justify = BlockIO.Justify.CENTER),
            @Block.Input(name = "then", type = "T", side = BlockIO.Side.LEFT, justify = BlockIO.Justify.CENTER),
            @Block.Input(name = "else", type = "T", side = BlockIO.Side.LEFT, justify = BlockIO.Justify.CENTER)
        },
        outputs = {
            @Block.Output(name = "out", type = "T", side = BlockIO.Side.RIGHT, justify = BlockIO.Justify.CENTER)
        }
    )
    public static Object ifThenElse(boolean condition, Object then, Object else_) {
        return condition ? then : else_;
    }
}
