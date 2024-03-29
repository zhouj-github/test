package com.test.finalize;

/**
 * @author zhouj
 * @since 2020-07-09
 */

import java.lang.ref.WeakReference;
import java.util.concurrent.atomic.AtomicInteger;

class Finalizable {
    static AtomicInteger aliveCount = new AtomicInteger(0);

    Finalizable() {
        aliveCount.incrementAndGet();
    }

    @Override
    protected void finalize() throws Throwable {
        Finalizable.aliveCount.decrementAndGet();
    }

    public static void main(String args[]) {
        WeakReference<String> stringWeakReference = new WeakReference<String>("sdds");
        stringWeakReference.get();
        for (int i = 0; ; i++) {
            Finalizable f = new Finalizable();
            if ((i % 100_000) == 0) {
                System.out.format("After creating %d objects, %d are still alive.%n", new Object[]{i, Finalizable.aliveCount.get()});
            }
        }
    }
}