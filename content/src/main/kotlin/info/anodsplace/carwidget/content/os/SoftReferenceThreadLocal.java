package info.anodsplace.carwidget.content.os;

import java.lang.ref.SoftReference;

public abstract class SoftReferenceThreadLocal<T> {
    private final ThreadLocal<SoftReference<T>> mThreadLocal;

    public SoftReferenceThreadLocal() {
        mThreadLocal = new ThreadLocal<>();
    }

    protected abstract T initialValue();

    public void set(T t) {
        mThreadLocal.set(new SoftReference<T>(t));
    }

    public T get() {
        SoftReference<T> reference = mThreadLocal.get();
        T obj;
        if (reference == null) {
            obj = initialValue();
            mThreadLocal.set(new SoftReference<T>(obj));
        } else {
            obj = reference.get();
            if (obj == null) {
                obj = initialValue();
                mThreadLocal.set(new SoftReference<T>(obj));
            }
        }
        return obj;
    }
}