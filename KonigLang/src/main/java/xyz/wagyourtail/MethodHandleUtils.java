package xyz.wagyourtail;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandleProxies;
import java.lang.invoke.MethodType;
import java.util.function.*;

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
                    BiConsumer<Object, Object> biConsumer = (BiConsumer<Object, Object>) MethodHandleProxies.asInterfaceInstance(BiConsumer.class, handle);
                    return args -> {
                        biConsumer.accept(args[0], args[1]);
                        return null;
                    };
                case 3:
                    TriConsumer triConsumer = MethodHandleProxies.asInterfaceInstance(TriConsumer.class, handle);
                    return args -> {
                        triConsumer.accept(args[0], args[1], args[2]);
                        return null;
                    };
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
                case 3:
                    TriFunction function3 = MethodHandleProxies.asInterfaceInstance(TriFunction.class, handle);
                    return args -> function3.apply(args[0], args[1], args[2]);
                default:
                    throw new IllegalArgumentException("MethodHandle has too many arguments (for now...)");
            }
        }
    }

    @FunctionalInterface
    public interface TriConsumer {
        void accept(Object a, Object b, Object c);
    }

    @FunctionalInterface
    public interface TriFunction {
        Object apply(Object t, Object u, Object v);
    }
}
