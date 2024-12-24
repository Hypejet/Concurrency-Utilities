package net.hypejet.concurrency.util.iterator;

import net.hypejet.concurrency.Acquisition;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * Represents {@linkplain Iterator an iterator} wrapper, which ensures that {@linkplain Acquisition an acquisition}
 * is locked and a caller thread has a permission to it during doing any operation.
 *
 * @param <E> a type of entries of the iterator
 * @param <I> a type of the guarded iterator
 * @since 1.0
 * @see Acquisition
 * @see Iterator
 */
public class GuardedIterator<E, I extends Iterator<E>> implements Iterator<E> {

    protected final I delegate;
    protected final Acquisition acquisition;

    /**
     * Constructs the {@linkplain GuardedIterator guarded iterator}.
     *
     * @param delegate the iterator that should be wrapped
     * @param acquisition an acquisition that should guard the iterator
     * @since 1.0
     */
    public GuardedIterator(@NotNull I delegate, @NotNull Acquisition acquisition) {
        this.delegate = Objects.requireNonNull(delegate, "The delegate must not be null");
        this.acquisition = Objects.requireNonNull(acquisition, "The acquisition must not be null");
    }

    @Override
    public final boolean hasNext() {
        this.acquisition.ensurePermittedAndLocked();
        return this.delegate.hasNext();
    }

    @Override
    public final E next() {
        this.acquisition.ensurePermittedAndLocked();
        return this.delegate.next();
    }

    @Override
    public final void remove() {
        this.acquisition.ensurePermittedAndLocked();
        this.delegate.remove();
    }

    @Override
    public final void forEachRemaining(Consumer<? super E> action) {
        this.acquisition.ensurePermittedAndLocked();
        this.delegate.forEachRemaining(action);
    }
}