package net.hypejet.concurrency.util.wrapping.iterable.collection;

import org.jetbrains.annotations.NotNull;

import java.util.Set;
import java.util.function.UnaryOperator;

/**
 * Represents {@linkplain Set a set} wrapper, which wraps elements from the delegating set and returns them.
 *
 * @param <E> a type of elements of both sets
 * @param <S> a type of the wrapped set
 * @since 1.0
 * @see Set
 */
public class ElementWrappingSet<E, S extends Set<E>> extends ElementWrappingCollection<E, S> implements Set<E> {
    /**
     * Constructs the {@linkplain ElementWrappingSet element-wrapping set}.
     *
     * @param delegate a set that should be wrapped
     * @param elementWrapper a unary operator that should wrap the elements
     * @since 1.0
     */
    public ElementWrappingSet(@NotNull S delegate, @NotNull UnaryOperator<E> elementWrapper) {
        super(delegate, elementWrapper);
    }
}