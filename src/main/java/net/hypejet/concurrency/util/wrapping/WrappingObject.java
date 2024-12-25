package net.hypejet.concurrency.util.wrapping;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * Represents {@linkplain Object an object} wrapper, which forwards all default object method calls to the wrapped
 * object.
 *
 * @param <O> a type of the both objects
 * @since 1.0
 * @see Object
 */
public class WrappingObject<O> {

    protected final O delegate;

    /**
     * Constructs the {@linkplain WrappingObject wrapping object}.
     *
     * @param delegate the object that should be wrapped
     * @since 1.0
     */
    public WrappingObject(@NotNull O delegate) {
        this.delegate = Objects.requireNonNull(delegate, "The delegate must not be null");
    }

    @Override
    public final int hashCode() {
        return this.delegate.hashCode();
    }

    @Override
    public final boolean equals(Object obj) {
        return this.delegate.equals(obj);
    }

    @Override
    public final String toString() {
        return this.delegate.toString();
    }
}