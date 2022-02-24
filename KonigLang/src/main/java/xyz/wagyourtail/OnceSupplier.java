package xyz.wagyourtail;

import java.util.function.Supplier;

public class OnceSupplier<T> implements Supplier<T> {
    Supplier<T> supplier;
    volatile boolean ran = false;
    T value;

    public OnceSupplier(Supplier<T> supplier) {
        this.supplier = supplier;
    }

    @Override
    public T get() {
        if (!ran) {
            synchronized (this) {
                if (!ran) {
                    value = supplier.get();
                    ran = true;
                }
            }
        }
        return value;
    }

}
