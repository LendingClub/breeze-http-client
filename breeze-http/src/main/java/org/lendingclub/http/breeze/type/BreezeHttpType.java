package org.lendingclub.http.breeze.type;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * Shamelessly stolen from Spring's ParameterizedTypeReference. Accomplishes
 * the same thing as that class, Gson's TypeToken and JAX-RS GenericType: the
 * ability to create class references to Java generics, based on the fact that
 * while Java generics lose type information at runtime, that type information
 * is preserved for subclasses.
 *
 * This class allows BreezeHttp to use type-safe generics across multiple
 * implementations that support JSON via Java reflection's Type class, without
 * forcing you to include a new external dependency that changes when you
 * change BreezeHttp implementations.
 */
public class BreezeHttpType<T> {
    private final Type type;

    protected BreezeHttpType() {
        Class<?> subclass = findGenericSubclass(getClass());
        this.type = firstTypeArgument(subclass.getGenericSuperclass());
    }

    public BreezeHttpType(Type type) {
        this.type = type;
    }

    public Type type() {
        return type;
    }

    private static Class<?> findGenericSubclass(Class<?> child) {
        Class<?> parent = child.getSuperclass();
        if (Object.class == parent) {
            throw new IllegalStateException("expected BreezeHttpType superclass");
        } else if (BreezeHttpType.class == parent) {
            return child;
        } else {
            return findGenericSubclass(parent);
        }
    }

    public static Type firstTypeArgument(Type type) {
        if (type instanceof ParameterizedType) {
            return ((ParameterizedType) type).getActualTypeArguments()[0];
        } else {
            return null;
        }
    }

    public static Type rawType(Type type) {
        if (type instanceof ParameterizedType) {
            return ((ParameterizedType) type).getRawType();
        } else {
            return type;
        }
    }

    public static boolean isSubclass(Type parent, Type child) {
        Type rawParent = rawType(parent);
        Type rawChild = rawType(child);
        if (rawParent instanceof Class && rawChild instanceof Class) {
            return ((Class<?>) rawParent).isAssignableFrom((Class<?>) rawChild);
        } else {
            return parent == child || rawParent == rawChild;
        }
    }
}
