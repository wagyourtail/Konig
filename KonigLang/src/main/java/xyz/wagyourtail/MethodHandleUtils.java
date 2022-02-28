package xyz.wagyourtail;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandleProxies;
import java.lang.invoke.MethodType;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class MethodHandleUtils {

    public static Function<Object[], Object> toArgsFunction(MethodHandle handle) {
        MethodType type = handle.type();
        if (type.returnType() == void.class) {
            switch (type.parameterCount()) {
                case 0:
                    Runnable runnable = (Runnable) MethodHandleProxies.asInterfaceInstance(Runnable.class, handle);
                    return args -> {
                        runnable.run();
                        return null;
                    };
                case 1:
                    Consumer<Object> consumer = (Consumer<Object>) MethodHandleProxies.asInterfaceInstance(Consumer.class, handle);
                    return args -> {
                        consumer.accept(args[0]);
                        return null;
                    };
                case 2:
                    BiFunction<Object, Object, Object> biFunction = (BiFunction<Object, Object, Object>) MethodHandleProxies.asInterfaceInstance(BiFunction.class, handle);
                    return args -> biFunction.apply(args[0], args[1]);
                default:
                    throw new IllegalArgumentException("MethodHandle has too many arguments (for now...)");
            }
        } else {
            switch (type.parameterCount()) {
                case 0:
                    Supplier<Object> supplier = MethodHandleProxies.asInterfaceInstance(Supplier.class, handle);
                   return args -> supplier.get();
                case 1:
                    Function<Object, Object> function = MethodHandleProxies.asInterfaceInstance(Function.class, handle);
                    return args -> function.apply(args[0]);
                case 2:
                    BiFunction<Object, Object, Object> function2 = MethodHandleProxies.asInterfaceInstance(BiFunction.class, handle);
                    return args -> function2.apply(args[0], args[1]);
                default:
                    throw new IllegalArgumentException("MethodHandle has too many arguments (for now...)");
            }
        }
    }
}
