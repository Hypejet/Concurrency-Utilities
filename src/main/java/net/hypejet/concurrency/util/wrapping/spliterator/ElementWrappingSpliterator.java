package net.hypejet.concurrency.util.wrapping.spliterator;

import net.hypejet.concurrency.util.wrapping.WrappingObject;
import org.jetbrains.annotations.NotNull;

import java.util.Comparator;
import java.util.Objects;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;

/**
 * Represents {@linkplain Spliterator a spliterator} wrapper, which wraps elements from the delegating spliterator and
 * returns them.
 *
 * @param <T> a type of elements of both spliterators
 * @param <S> a type of the wrapped spliterator
 * @since 1.0
 * @see Spliterator
 */
public class ElementWrappingSpliterator<T, S extends Spliterator<T>> extends WrappingObject<S>
        implements Spliterator<T> {

    protected final UnaryOperator<T> elementWrapper;


    /**
     * Constructs the {@linkplain ElementWrappingSpliterator element-wrapping spliterator}.
     *
     * @param delegate a spliterator that should be wrapped
     * @param elementWrapper a unary operator that should wrap the elements
     * @since 1.0
     */
    public ElementWrappingSpliterator(@NotNull S delegate, @NotNull UnaryOperator<T> elementWrapper) {
        super(delegate);
        this.elementWrapper = Objects.requireNonNull(elementWrapper, "The element wrapper must not be null");
    }

    @Override
    public final boolean tryAdvance(Consumer<? super T> action) {
        return this.delegate.tryAdvance(element -> action.accept(this.elementWrapper.apply(element)));
    }

    @Override
    public final void forEachRemaining(Consumer<? super T> action) {
        this.delegate.forEachRemaining(element -> action.accept(this.elementWrapper.apply(element)));
    }

    @Override
    public final Spliterator<T> trySplit() {
        return new ElementWrappingSpliterator<>(this.delegate.trySplit(), this.elementWrapper);
    }

    @Override
    public final long estimateSize() {
        return this.delegate.estimateSize();
    }

    @Override
    public final long getExactSizeIfKnown() {
        return this.delegate.getExactSizeIfKnown();
    }

    @Override
    public final int characteristics() {
        return this.delegate.characteristics();
    }

    @Override
    public final boolean hasCharacteristics(int characteristics) {
        return this.delegate.hasCharacteristics(characteristics);
    }

    @Override
    public final Comparator<? super T> getComparator() {
        return this.delegate.getComparator();
    }
}