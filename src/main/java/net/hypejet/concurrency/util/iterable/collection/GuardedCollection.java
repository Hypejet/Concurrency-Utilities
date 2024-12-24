package net.hypejet.concurrency.util.iterable.collection;

import net.hypejet.concurrency.Acquisition;
import net.hypejet.concurrency.util.iterable.GuardedIterable;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * Represents {@linkplain Collection a collection} wrapper, which ensures that {@linkplain Acquisition an acquisition}
 * is locked and a caller thread has a permission to it during doing any operation.
 *
 * @param <E> a type of value of the collection
 * @param <C> a type of the guarded collection
 * @since 1.0
 * @see Acquisition
 * @see Collection
 */
public class GuardedCollection<E, C extends Collection<E>> extends GuardedIterable<E, C> implements Collection<E> {
    /**
     * Constructs the {@linkplain GuardedCollection guarded collection}.
     *
     * @param collection the collection that should be wrapped
     * @param acquisition an acquisition that should guard the collection
     * @since 1.0
     */
    public GuardedCollection(@NotNull C collection, @NotNull Acquisition acquisition) {
        super(collection, acquisition);
    }

    @Override
    public final int size() {
        this.acquisition.ensurePermittedAndLocked();
        return this.delegate.size();
    }

    @Override
    public final boolean isEmpty() {
        this.acquisition.ensurePermittedAndLocked();
        return this.delegate.isEmpty();
    }

    @Override
    public final boolean contains(Object o) {
        this.acquisition.ensurePermittedAndLocked();
        return this.delegate.contains(o);
    }

    @Override
    public final Object @NotNull [] toArray() {
        this.acquisition.ensurePermittedAndLocked();
        return this.delegate.toArray();
    }

    @Override
    public final <T> T @NotNull [] toArray(T @NotNull [] a) {
        this.acquisition.ensurePermittedAndLocked();
        return this.delegate.toArray(a);
    }

    @Override
    public final <T> T[] toArray(@NotNull IntFunction<T[]> generator) {
        this.acquisition.ensurePermittedAndLocked();
        return this.delegate.toArray(generator);
    }

    @Override
    public final boolean add(E e) {
        this.acquisition.ensurePermittedAndLocked();
        return this.delegate.add(e);
    }

    @Override
    public final boolean remove(Object o) {
        this.acquisition.ensurePermittedAndLocked();
        return this.delegate.remove(o);
    }

    @Override
    public final boolean containsAll(@NotNull Collection<?> c) {
        this.acquisition.ensurePermittedAndLocked();
        return this.delegate.containsAll(c);
    }

    @Override
    public final boolean addAll(@NotNull Collection<? extends E> c) {
        this.acquisition.ensurePermittedAndLocked();
        return this.delegate.addAll(c);
    }

    @Override
    public final boolean retainAll(@NotNull Collection<?> c) {
        this.acquisition.ensurePermittedAndLocked();
        return this.delegate.retainAll(c);
    }

    @Override
    public final boolean removeAll(@NotNull Collection<?> c) {
        this.acquisition.ensurePermittedAndLocked();
        return this.delegate.removeAll(c);
    }

    @Override
    public final boolean removeIf(@NotNull Predicate<? super E> filter) {
        this.acquisition.ensurePermittedAndLocked();
        return this.delegate.removeIf(filter);
    }

    @Override
    public final void clear() {
        this.acquisition.ensurePermittedAndLocked();
        this.delegate.clear();
    }

    @Override
    public final @NotNull Stream<E> stream() {
        this.acquisition.ensurePermittedAndLocked();
        return this.delegate.stream();
    }

    @Override
    public final @NotNull Stream<E> parallelStream() {
        this.acquisition.ensurePermittedAndLocked();
        return this.delegate.parallelStream();
    }
}