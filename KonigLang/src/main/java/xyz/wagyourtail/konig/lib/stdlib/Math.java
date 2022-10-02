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
            @Block.Input(name="in2", type="T", side = BlockIO.Side.LEFT, justify = BlockIO.Justify.CENTER)
        },
        outputs = {
            @Block.Output(name="out", type="T", side = BlockIO.Side.RIGHT, justify = BlockIO.Justify.CENTER)
        }
    )
    public static Object add(Object a, Object b) {
        if (a instanceof Number) {
            return ((Number)a).doubleValue() + ((Number)b).doubleValue();
        } else {
            return a.toString() + b.toString();
        }
    }

    @Block(
        name = "sub",
        generics = {
            @Block.Generic(name="T")
        },
        inputs = {
            @Block.Input(name="in1", type="T", side = BlockIO.Side.LEFT, justify = BlockIO.Justify.CENTER),
            @Block.Input(name="in2", type="T", side = BlockIO.Side.LEFT, justify = BlockIO.Justify.CENTER)
        },
        outputs = {
            @Block.Output(name="out", type="T", side = BlockIO.Side.RIGHT, justify = BlockIO.Justify.CENTER)
        }
    )
    public static Object sub(Object a, Object b) {
        if (a instanceof Number) {
            return ((Number)a).doubleValue() - ((Number)b).doubleValue();
        } else {
            return a.toString().replace(b.toString(), "");
        }
    }

    @Block(
        name = "mul",
        inputs = {
            @Block.Input(name="in1", type="number", side = BlockIO.Side.LEFT, justify = BlockIO.Justify.CENTER),
            @Block.Input(name="in2", type="number", side = BlockIO.Side.LEFT, justify = BlockIO.Justify.CENTER)
        },
        outputs = {
            @Block.Output(name="out", type="number", side = BlockIO.Side.RIGHT, justify = BlockIO.Justify.CENTER)
        }
    )
    public static Object mul(Object a, Object b) {
        if (a instanceof Number) {
            return ((Number)a).doubleValue() * ((Number)b).doubleValue();
        } else {
            if (b instanceof Number) {
                String s = "";
                for (int i = 0; i < ((Number)b).intValue(); i++) {
                    s += a.toString();
                }
                return s;
            } else {
                throw new IllegalArgumentException("Cannot multiply strings.");
            }
        }
    }

    @Block(
        name = "div",
        inputs = {
            @Block.Input(name="in1", type="number", side = BlockIO.Side.LEFT, justify = BlockIO.Justify.CENTER),
            @Block.Input(name="in2", type="number", side = BlockIO.Side.LEFT, justify = BlockIO.Justify.CENTER)
        },
        outputs = {
            @Block.Output(name="out", type="number", side = BlockIO.Side.RIGHT, justify = BlockIO.Justify.CENTER)
        }
    )
    public static Object div(Object a, Object b) {
        if (a instanceof Number) {
            return ((Number)a).doubleValue() / ((Number)b).doubleValue();
        } else {
            if (b instanceof Number) {
                int size = ((Number)b).intValue();
                int arrSize = a.toString().length() / size;
                if (a.toString().length() % size != 0) arrSize++;
                String[] arr = new String[arrSize];
                for (int i = 0; i < arrSize; i++) {
                    arr[i] = a.toString().substring(i * size, java.lang.Math.min((i + 1) * size, a.toString().length()));
                }
                return arr;
            } else {
                return a.toString().split(b.toString());
            }
        }
    }

    @Block(
        name = "mod",
        inputs = {
            @Block.Input(name="in1", type="number", side = BlockIO.Side.LEFT, justify = BlockIO.Justify.CENTER),
            @Block.Input(name="in2", type="number", side = BlockIO.Side.LEFT, justify = BlockIO.Justify.CENTER)
        },
        outputs = {
            @Block.Output(name="out", type="number", side = BlockIO.Side.RIGHT, justify = BlockIO.Justify.CENTER)
        }
    )
    public static double mod(double a, double b) {
        double r = a % b;
        if (r < 0) {
            r += b;
        }
        return r;
    }

    @Block(
        name = "pow",
        inputs = {
            @Block.Input(name="in1", type="number", side = BlockIO.Side.LEFT, justify = BlockIO.Justify.CENTER),
            @Block.Input(name="in2", type="number", side = BlockIO.Side.LEFT, justify = BlockIO.Justify.CENTER)
        },
        outputs = {
            @Block.Output(name="out", type="number", side = BlockIO.Side.RIGHT, justify = BlockIO.Justify.CENTER)
        }
    )
    public static double pow(double a, double b) {
        return java.lang.Math.pow(a, b);
    }

    @Block(
        name = "sqrt",
        inputs = {
            @Block.Input(name="in", type="number", side = BlockIO.Side.LEFT, justify = BlockIO.Justify.CENTER)
        },
        outputs = {
            @Block.Output(name="out", type="number", side = BlockIO.Side.RIGHT, justify = BlockIO.Justify.CENTER)
        }
    )
    public static double sqrt(double a) {
        return java.lang.Math.sqrt(a);
    }

    @Block(
        name = "abs",
        inputs = {
            @Block.Input(name="in", type="number", side = BlockIO.Side.LEFT, justify = BlockIO.Justify.CENTER)
        },
        outputs = {
            @Block.Output(name="out", type="number", side = BlockIO.Side.RIGHT, justify = BlockIO.Justify.CENTER)
        }
    )
    public static double abs(double a) {
        return java.lang.Math.abs(a);
    }

    @Block(
        name = "sin",
        inputs = {
            @Block.Input(name="in", type="number", side = BlockIO.Side.LEFT, justify = BlockIO.Justify.CENTER)
        },
        outputs = {
            @Block.Output(name="out", type="number", side = BlockIO.Side.RIGHT, justify = BlockIO.Justify.CENTER)
        }
    )
    public static double sin(double a) {
        return java.lang.Math.sin(a);
    }

    @Block(
        name = "cos",
        inputs = {
            @Block.Input(name="in", type="number", side = BlockIO.Side.LEFT, justify = BlockIO.Justify.CENTER)
        },
        outputs = {
            @Block.Output(name="out", type="number", side = BlockIO.Side.RIGHT, justify = BlockIO.Justify.CENTER)
        }
    )
    public static double cos(double a) {
        return java.lang.Math.cos(a);
    }

    @Block(
        name = "tan",
        inputs = {
            @Block.Input(name="in", type="number", side = BlockIO.Side.LEFT, justify = BlockIO.Justify.CENTER)
        },
        outputs = {
            @Block.Output(name="out", type="number", side = BlockIO.Side.RIGHT, justify = BlockIO.Justify.CENTER)
        }
    )
    public static double tan(double a) {
        return java.lang.Math.tan(a);
    }

    @Block(
        name = "asin",
        inputs = {
            @Block.Input(name="in", type="number", side = BlockIO.Side.LEFT, justify = BlockIO.Justify.CENTER)
        },
        outputs = {
            @Block.Output(name="out", type="number", side = BlockIO.Side.RIGHT, justify = BlockIO.Justify.CENTER)
        }
    )
    public static double asin(double a) {
        return java.lang.Math.asin(a);
    }

    @Block(
        name = "acos",
        inputs = {
            @Block.Input(name="in", type="number", side = BlockIO.Side.LEFT, justify = BlockIO.Justify.CENTER)
        },
        outputs = {
            @Block.Output(name="out", type="number", side = BlockIO.Side.RIGHT, justify = BlockIO.Justify.CENTER)
        }
    )
    public static double acos(double a) {
        return java.lang.Math.acos(a);
    }

    @Block(
        name = "atan",
        inputs = {
            @Block.Input(name="in", type="number", side = BlockIO.Side.LEFT, justify = BlockIO.Justify.CENTER)
        },
        outputs = {
            @Block.Output(name="out", type="number", side = BlockIO.Side.RIGHT, justify = BlockIO.Justify.CENTER)
        }
    )
    public static double atan(double a) {
        return java.lang.Math.atan(a);
    }

    @Block(
        name = "atan2",
        inputs = {
            @Block.Input(name = "in1", type = "number", side = BlockIO.Side.LEFT, justify = BlockIO.Justify.CENTER),
            @Block.Input(name = "in2", type = "number", side = BlockIO.Side.LEFT, justify = BlockIO.Justify.CENTER)
        },
        outputs = {
            @Block.Output(name = "out", type = "number", side = BlockIO.Side.RIGHT, justify = BlockIO.Justify.CENTER)
        }
    )
    public static double atan2(double a, double b) {
        return java.lang.Math.atan2(a, b);
    }

    @Block(
        name = "log",
        inputs = {
            @Block.Input(name="in", type="number", side = BlockIO.Side.LEFT, justify = BlockIO.Justify.CENTER)
        },
        outputs = {
            @Block.Output(name="out", type="number", side = BlockIO.Side.RIGHT, justify = BlockIO.Justify.CENTER)
        }
    )
    public static double log(double a) {
        return java.lang.Math.log10(a);
    }

    @Block(
        name = "ln",
        inputs = {
            @Block.Input(name="in", type="number", side = BlockIO.Side.LEFT, justify = BlockIO.Justify.CENTER)
        },
        outputs = {
            @Block.Output(name="out", type="number", side = BlockIO.Side.RIGHT, justify = BlockIO.Justify.CENTER)
        }
    )
    public static double ln(double a) {
        return java.lang.Math.log(a);
    }

    @Block(
        name = "exp",
        inputs = {
            @Block.Input(name="in", type="number", side = BlockIO.Side.LEFT, justify = BlockIO.Justify.CENTER)
        },
        outputs = {
            @Block.Output(name="out", type="number", side = BlockIO.Side.RIGHT, justify = BlockIO.Justify.CENTER)
        }
    )
    public static double exp(double a) {
        return java.lang.Math.exp(a);
    }

    @Block(
        name = "floor",
        inputs = {
            @Block.Input(name="in", type="number", side = BlockIO.Side.LEFT, justify = BlockIO.Justify.CENTER)
        },
        outputs = {
            @Block.Output(name="out", type="number", side = BlockIO.Side.RIGHT, justify = BlockIO.Justify.CENTER)
        }
    )
    public static double floor(double a) {
        return java.lang.Math.floor(a);
    }

    @Block(
        name = "ceil",
        inputs = {
            @Block.Input(name="in", type="number", side = BlockIO.Side.LEFT, justify = BlockIO.Justify.CENTER)
        },
        outputs = {
            @Block.Output(name="out", type="number", side = BlockIO.Side.RIGHT, justify = BlockIO.Justify.CENTER)
        }
    )
    public static double ceil(double a) {
        return java.lang.Math.ceil(a);
    }

    @Block(
        name = "round",
        inputs = {
            @Block.Input(name="in", type="number", side = BlockIO.Side.LEFT, justify = BlockIO.Justify.CENTER)
        },
        outputs = {
            @Block.Output(name="out", type="number", side = BlockIO.Side.RIGHT, justify = BlockIO.Justify.CENTER)
        }
    )
    public static double round(double a) {
        return java.lang.Math.round(a);
    }

    @Block(
        name = "rand",
        inputs = {},
        outputs = {
            @Block.Output(name="out", type="number", side = BlockIO.Side.RIGHT, justify = BlockIO.Justify.CENTER)
        }
    )
    public static double rand() {
        return java.lang.Math.random();
    }

    @Block(
        name = "randInt",
        inputs = {
            @Block.Input(name="in", type="number", side = BlockIO.Side.LEFT, justify = BlockIO.Justify.CENTER)
        },
        outputs = {
            @Block.Output(name="out", type="number", side = BlockIO.Side.RIGHT, justify = BlockIO.Justify.CENTER)
        }
    )
    public static double randInt(double a) {
        return (int) (java.lang.Math.random() * a);
    }

    @Block(
        name = "randrange",
        inputs = {
            @Block.Input(name="in1", type="number", side = BlockIO.Side.LEFT, justify = BlockIO.Justify.CENTER),
            @Block.Input(name="in2", type="number", side = BlockIO.Side.LEFT, justify = BlockIO.Justify.CENTER)
        },
        outputs = {
            @Block.Output(name="out", type="number", side = BlockIO.Side.RIGHT, justify = BlockIO.Justify.CENTER)
        }
    )
    public static double randRange(double a, double b) {
        return (int) (java.lang.Math.random() * (b - a) + a);
    }
}
