package net.hypejet.concurrency.util.wrapping.iterable;

import net.hypejet.concurrency.util.wrapping.WrappingObject;
import net.hypejet.concurrency.util.wrapping.iterator.ElementWrappingIterator;
import net.hypejet.concurrency.util.wrapping.spliterator.ElementWrappingSpliterator;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.Objects;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;

/**
 * Represents {@linkplain Iterable an iterable} wrapper, which wraps elements from the delegating iterable and returns
 * them.
 *
 * @param <T> a type of elements of both iterables
 * @param <I> a type of the wrapped iterable
 * @since 1.0
 * @see Iterable
 */
public class ElementWrappingIterable<T, I extends Iterable<T>> extends WrappingObject<I> implements Iterable<T> {

    protected final UnaryOperator<T> elementWrapper;

    /**
     * Constructs the {@linkplain ElementWrappingIterable element-wrapping iterable}.
     *
     * @param delegate an iterable that should be wrapped
     * @param elementWrapper a unary operator that should wrap the elements
     * @since 1.0
     */
    public ElementWrappingIterable(@NotNull I delegate, @NotNull UnaryOperator<T> elementWrapper) {
        super(delegate);
        this.elementWrapper = Objects.requireNonNull(elementWrapper, "The element wrapper must not be null");
    }

    @Override
    public final @NotNull Iterator<T> iterator() {
        return new ElementWrappingIterator<>(this.delegate.iterator(), this.elementWrapper);
    }

    @Override
    public final void forEach(Consumer<? super T> action) {
        this.delegate.forEach(element -> action.accept(this.elementWrapper.apply(element)));
    }

    @Override
    public final Spliterator<T> spliterator() {
        return new ElementWrappingSpliterator<>(this.delegate.spliterator(), this.elementWrapper);
    }
}