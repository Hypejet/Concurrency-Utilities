package net.hypejet.concurrency.util.iterable;

import net.hypejet.concurrency.Acquisition;
import net.hypejet.concurrency.util.iterator.GuardedIterator;
import net.hypejet.concurrency.util.iterator.GuardedSpliterator;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.Objects;
import java.util.Spliterator;
import java.util.function.Consumer;

/**
 * Represents {@linkplain Iterable a iterable} wrapper, which ensures that {@linkplain Acquisition an acquisition}
 * is locked and a caller thread has a permission to it during doing any operation.
 *
 * @param <T> a type of elements returned by the iterable
 * @param <I> a type of the guarded iterable
 * @since 1.0
 * @see Acquisition
 * @see Iterable
 */
public class GuardedIterable<T, I extends Iterable<T>> implements Iterable<T> {

    protected final I iterable;
    protected final Acquisition acquisition;

    /**
     * Constructs the {@linkplain GuardedIterable guarded iterable}.
     *
     * @param iterable the iterable that should be wrapped
     * @param acquisition an acquisition that should guard the iterable
     * @since 1.0
     */
    public GuardedIterable(@NotNull I iterable, @NotNull Acquisition acquisition) {
        this.iterable = Objects.requireNonNull(iterable, "The iterable must not be null");
        this.acquisition = Objects.requireNonNull(acquisition, "The acquisition must not be null");
    }

    @Override
    public final @NotNull Iterator<T> iterator() {
        this.acquisition.ensurePermittedAndLocked();
        return new GuardedIterator<>(this.iterable.iterator(), this.acquisition);
    }

    @Override
    public final void forEach(Consumer<? super T> action) {
        this.acquisition.ensurePermittedAndLocked();
        this.iterable.forEach(action);
    }

    @Override
    public final @NotNull Spliterator<T> spliterator() {
        this.acquisition.ensurePermittedAndLocked();
        return new GuardedSpliterator<>(this.iterable.spliterator(), this.acquisition);
    }
}