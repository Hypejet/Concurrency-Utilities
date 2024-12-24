package net.hypejet.concurrency.util.iterable.collection;

import net.hypejet.concurrency.Acquisition;
import net.hypejet.concurrency.util.iterator.GuardedListIterator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.ListIterator;
import java.util.function.UnaryOperator;

/**
 * Represents {@linkplain List a list} wrapper, which ensures that {@linkplain Acquisition an acquisition}
 * is locked and a caller thread has a permission to it during doing any operation.
 *
 * @param <E> a type of value of the collection
 * @param <L> a type of the guarded list
 * @since 1.0
 * @see Acquisition
 * @see Collection
 */
public class GuardedList<E, L extends List<E>> extends GuardedCollection<E, L> implements List<E> {
    /**
     * Constructs the {@linkplain GuardedCollection guarded collection}.
     *
     * @param delegate the delegate that should be wrapped
     * @param acquisition an acquisition that should guard the collection
     * @since 1.0
     */
    public GuardedList(@NotNull L delegate, @NotNull Acquisition acquisition) {
        super(delegate, acquisition);
    }

    @Override
    public final boolean addAll(int index, @NotNull Collection<? extends E> c) {
        this.acquisition.ensurePermittedAndLocked();
        return this.delegate.addAll(index, c);
    }

    @Override
    public final void replaceAll(@NotNull UnaryOperator<E> operator) {
        this.acquisition.ensurePermittedAndLocked();
        this.delegate.replaceAll(operator);
    }

    @Override
    public final void sort(@Nullable Comparator<? super E> c) {
        this.acquisition.ensurePermittedAndLocked();
        this.delegate.sort(c);
    }

    @Override
    public final E get(int index) {
        this.acquisition.ensurePermittedAndLocked();
        return this.delegate.get(index);
    }

    @Override
    public final E set(int index, E element) {
        this.acquisition.ensurePermittedAndLocked();
        return this.delegate.set(index, element);
    }

    @Override
    public final void add(int index, E element) {
        this.acquisition.ensurePermittedAndLocked();
        this.delegate.add(index, element);
    }

    @Override
    public final E remove(int index) {
        this.acquisition.ensurePermittedAndLocked();
        return this.delegate.remove(index);
    }

    @Override
    public final int indexOf(Object o) {
        this.acquisition.ensurePermittedAndLocked();
        return this.delegate.indexOf(o);
    }

    @Override
    public final int lastIndexOf(Object o) {
        this.acquisition.ensurePermittedAndLocked();
        return this.delegate.lastIndexOf(o);
    }

    @Override
    public final @NotNull ListIterator<E> listIterator() {
        this.acquisition.ensurePermittedAndLocked();
        return new GuardedListIterator<>(this.delegate.listIterator(), this.acquisition);
    }

    @Override
    public final @NotNull ListIterator<E> listIterator(int index) {
        this.acquisition.ensurePermittedAndLocked();
        return new GuardedListIterator<>(this.delegate.listIterator(index), this.acquisition);
    }

    @Override
    public final @NotNull List<E> subList(int fromIndex, int toIndex) {
        this.acquisition.ensurePermittedAndLocked();
        return new GuardedList<>(this.delegate.subList(fromIndex, toIndex), this.acquisition);
    }

    @Override
    public final void addFirst(E e) {
        this.acquisition.ensurePermittedAndLocked();
        this.delegate.addFirst(e);
    }

    @Override
    public final void addLast(E e) {
        this.acquisition.ensurePermittedAndLocked();
        this.delegate.addLast(e);
    }

    @Override
    public final E getFirst() {
        this.acquisition.ensurePermittedAndLocked();
        return this.delegate.getFirst();
    }

    @Override
    public final E getLast() {
        this.acquisition.ensurePermittedAndLocked();
        return this.delegate.getLast();
    }

    @Override
    public final E removeFirst() {
        this.acquisition.ensurePermittedAndLocked();
        return this.delegate.removeFirst();
    }

    @Override
    public final E removeLast() {
        this.acquisition.ensurePermittedAndLocked();
        return this.delegate.removeLast();
    }

    @Override
    public final List<E> reversed() {
        this.acquisition.ensurePermittedAndLocked();
        return new GuardedList<>(this.delegate.reversed(), this.acquisition);
    }
}