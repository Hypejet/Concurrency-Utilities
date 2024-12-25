package net.hypejet.concurrency.util.wrapping.iterator;

import net.hypejet.concurrency.util.wrapping.WrappingObject;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;

/**
 * Represents {@linkplain Iterator an iterator} wrapper, which wraps elements from the delegating iterator and returns
 * them.
 *
 * @param <E> a type of elements of both iterators
 * @param <I> a type of the wrapped iterator
 * @since 1.0
 * @see Iterator
 */
public class ElementWrappingIterator<E, I extends Iterator<E>> extends WrappingObject<I> implements Iterator<E> {

    protected final UnaryOperator<E> elementWrapper;

    /**
     * Constructs the {@linkplain ElementWrappingIterator element-wrapping iterator}.
     *
     * @param delegate an iterator that should be wrapped
     * @param elementWrapper a unary operator that should wrap the elements
     * @since 1.0
     */
    public ElementWrappingIterator(@NotNull I delegate, @NotNull UnaryOperator<E> elementWrapper) {
        super(delegate);
        this.elementWrapper = Objects.requireNonNull(elementWrapper, "The element wrapper must not be null");
    }

    @Override
    public final boolean hasNext() {
        return this.delegate.hasNext();
    }

    @Override
    public final E next() {
        return this.elementWrapper.apply(this.delegate.next());
    }

    @Override
    public final void remove() {
        this.delegate.remove();
    }

    @Override
    public final void forEachRemaining(Consumer<? super E> action) {
        this.delegate.forEachRemaining(element -> action.accept(this.elementWrapper.apply(element)));
    }
}