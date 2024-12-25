package net.hypejet.concurrency.util.wrapping.iterable.collection;

import net.hypejet.concurrency.util.wrapping.iterable.ElementWrappingIterable;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

/**
 * Represents {@linkplain Collection a collection} wrapper, which wraps elements from the delegating collection and
 * returns them.
 *
 * @param <E> a type of elements of both collections
 * @param <C> a type of the wrapped collection
 * @since 1.0
 * @see Collection
 */
public class ElementWrappingCollection<E, C extends Collection<E>> extends ElementWrappingIterable<E, C>
        implements Collection<E> {
    /**
     * Constructs the {@linkplain ElementWrappingCollection element-wrapping collection}.
     *
     * @param delegate a collection that should be wrapped
     * @param elementWrapper a unary operator that should wrap the elements
     * @since 1.0
     */
    public ElementWrappingCollection(@NotNull C delegate, @NotNull UnaryOperator<E> elementWrapper) {
        super(delegate, elementWrapper);
    }

    @Override
    public final int size() {
        return this.delegate.size();
    }

    @Override
    public final boolean isEmpty() {
        return this.delegate.isEmpty();
    }

    @Override
    public final boolean contains(Object o) {
        return this.delegate.contains(o);
    }

    @Override
    public final Object @NotNull [] toArray() {
        Object[] result = this.delegate.toArray();
        this.wrapArray(result);
        return result;
    }

    @Override
    public final <T> T @NotNull [] toArray(T @NotNull [] a) {
        T[] parameterArrayResult = a.clone();
        T[] returnedArrayResult = this.delegate.toArray(parameterArrayResult);

        int copyLength = Math.min(parameterArrayResult.length, a.length);
        boolean[] changes = new boolean[copyLength];

        for (int index = 0; index < copyLength; index++)
            changes[index] = a[index] != parameterArrayResult[index];

        this.wrapArray(parameterArrayResult);
        this.wrapArray(returnedArrayResult);

        for (int index = 0; index < changes.length; index++) {
            if (!changes[index]) continue;
            a[index] = parameterArrayResult[index];
        }

        return returnedArrayResult;
    }

    @Override
    public final <T> T[] toArray(@NotNull IntFunction<T[]> generator) {
        T[] result = this.delegate.toArray(generator);
        this.wrapArray(result);
        return result;
    }

    @Override
    public final boolean add(E e) {
        return this.delegate.add(e);
    }

    @Override
    public final boolean remove(Object o) {
        return this.delegate.remove(o);
    }

    @Override
    public final boolean containsAll(@NotNull Collection<?> c) {
        return this.delegate.containsAll(c);
    }

    @Override
    public final boolean addAll(@NotNull Collection<? extends E> c) {
        return this.delegate.addAll(c);
    }

    @Override
    public final boolean removeAll(@NotNull Collection<?> c) {
        return this.delegate.removeAll(c);
    }

    @Override
    public final boolean removeIf(@NotNull Predicate<? super E> filter) {
        return this.delegate.removeIf(element -> filter.test(this.elementWrapper.apply(element)));
    }

    @Override
    public final boolean retainAll(@NotNull Collection<?> c) {
        return this.delegate.retainAll(c);
    }

    @Override
    public final void clear() {
        this.delegate.clear();
    }

    @Override
    public final @NotNull Stream<E> stream() {
        return this.delegate.stream().map(this.elementWrapper);
    }

    @Override
    public final @NotNull Stream<E> parallelStream() {
        return this.delegate.parallelStream().map(this.elementWrapper);
    }

    private <T> void wrapArray(T[] array) {
        if (array == null) return;
        for (int index = 0; index < array.length; index++) {
            T element = array[index];
            if (element == null) continue;
            array[index] = (T) this.elementWrapper.apply((E) element); // Java generics are limiting
        }
    }
}