package net.hypejet.concurrency.util.guard.iterator;

import net.hypejet.concurrency.Acquisition;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.ListIterator;

/**
 * Represents {@linkplain ListIterator a list iterator} wrapper, which ensures that
 * {@linkplain Acquisition an acquisition} is locked and a caller thread has a permission to it during doing any
 * operation.
 *
 * @param <E> a type of entries of the iterator
 * @param <L> a type of the guarded list iterator
 * @since 1.0
 * @see Acquisition
 * @see Iterator
 */
public class GuardedListIterator<E, L extends ListIterator<E>> extends GuardedIterator<E, L>
        implements ListIterator<E> {
    /**
     * Constructs the {@linkplain GuardedListIterator guarded list iterator}.
     *
     * @param delegate the list iterator that should be wrapped
     * @param acquisition an acquisition that should guard the iterator
     * @since 1.0
     */
    public GuardedListIterator(@NotNull L delegate, @NotNull Acquisition acquisition) {
        super(delegate, acquisition);
    }

    @Override
    public final boolean hasPrevious() {
        this.acquisition.ensurePermittedAndLocked();
        return this.delegate.hasPrevious();
    }

    @Override
    public final E previous() {
        this.acquisition.ensurePermittedAndLocked();
        return this.delegate.previous();
    }

    @Override
    public final int nextIndex() {
        this.acquisition.ensurePermittedAndLocked();
        return this.delegate.nextIndex();
    }

    @Override
    public final int previousIndex() {
        this.acquisition.ensurePermittedAndLocked();
        return this.delegate.previousIndex();
    }

    @Override
    public final void set(E e) {
        this.acquisition.ensurePermittedAndLocked();
        this.delegate.set(e);
    }

    @Override
    public final void add(E e) {
        this.acquisition.ensurePermittedAndLocked();
        this.delegate.add(e);
    }
}