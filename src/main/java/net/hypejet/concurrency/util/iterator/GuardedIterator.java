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
 * @since 1.0
 * @see Acquisition
 * @see Iterator
 */
public final class GuardedIterator<E> implements Iterator<E> {

    private final Iterator<E> iterator;
    private final Acquisition acquisition;

    /**
     * Constructs the {@linkplain GuardedIterator guarded iterator}.
     *
     * @param iterator the iterator that should be wrapped
     * @param acquisition an acquisition that should guard the iterator
     * @since 1.0
     */
    public GuardedIterator(@NotNull Iterator<E> iterator, @NotNull Acquisition acquisition) {
        this.iterator = Objects.requireNonNull(iterator, "The iterator must not be null");
        this.acquisition = Objects.requireNonNull(acquisition, "The acquisition must not be null");
    }

    @Override
    public boolean hasNext() {
        this.acquisition.ensurePermittedAndLocked();
        return this.iterator.hasNext();
    }

    @Override
    public E next() {
        this.acquisition.ensurePermittedAndLocked();
        return this.iterator.next();
    }

    @Override
    public void remove() {
        this.acquisition.ensurePermittedAndLocked();
        this.iterator.remove();
    }

    @Override
    public void forEachRemaining(Consumer<? super E> action) {
        this.acquisition.ensurePermittedAndLocked();
        this.iterator.forEachRemaining(action);
    }
}