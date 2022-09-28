package xyz.wagyourtail.konig.lib;

import xyz.wagyourtail.konig.structure.headers.BlockIO;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface Block {
    String name();
    String group() default "";
    String image() default "";
    Generic[] generics() default {};
    Input[] inputs();
    Output[] outputs();
    Hollow[] hollows() default {};

    @interface Input {
        int index() default -1;
        String name();
        String type();
        BlockIO.Side side();
        BlockIO.Justify justify();
    }

    @interface Output {
        String name();
        String type();
        BlockIO.Side side();
        BlockIO.Justify justify();
    }

    @interface Hollow {
        int index() default -1;
        String name();
        String group() default "";
        double paddingTop() default .1;
        double paddingBottom() default .1;
        double paddingLeft() default .1;
        double paddingRight() default .1;
        Input[] inputs();
        Output[] outputs();
    }

    @interface Generic {
        String name();
        String extend() default "";
        String supers() default "";
    }
}
