package net.hypejet.concurrency.util.iterator;

import net.hypejet.concurrency.Acquisition;
import org.jetbrains.annotations.NotNull;

import java.util.Comparator;
import java.util.Objects;
import java.util.Spliterator;
import java.util.function.Consumer;

/**
 * Represents {@linkplain Spliterator a spliterator} wrapper, which ensures that
 * {@linkplain Acquisition an acquisition} is locked and a caller thread has a permission to it during doing any
 * operation.
 *
 * @param <T> a type of entries of the spliterator
 * @since 1.0
 * @see Acquisition
 * @see Spliterator
 */
public final class GuardedSpliterator<T> implements Spliterator<T> {

    private final Spliterator<T> spliterator;
    private final Acquisition acquisition;

    /**
     * Constructs the {@linkplain GuardedSpliterator guarded spliterator}.
     *
     * @param spliterator the spliterator that should be wrapped
     * @param acquisition an acquisition that should guard the spliterator
     * @since 1.0
     */
    public GuardedSpliterator(@NotNull Spliterator<T> spliterator, @NotNull Acquisition acquisition) {
        this.spliterator = Objects.requireNonNull(spliterator, "The spliterator must not be null");
        this.acquisition = Objects.requireNonNull(acquisition, "The acquisition must not be null");
    }

    @Override
    public boolean tryAdvance(Consumer<? super T> action) {
        this.acquisition.ensurePermittedAndLocked();
        return this.spliterator.tryAdvance(action);
    }

    @Override
    public void forEachRemaining(Consumer<? super T> action) {
        this.acquisition.ensurePermittedAndLocked();
        this.spliterator.forEachRemaining(action);
    }

    @Override
    public Spliterator<T> trySplit() {
        this.acquisition.ensurePermittedAndLocked();
        return this.spliterator.trySplit();
    }

    @Override
    public long estimateSize() {
        this.acquisition.ensurePermittedAndLocked();
        return this.spliterator.estimateSize();
    }

    @Override
    public long getExactSizeIfKnown() {
        this.acquisition.ensurePermittedAndLocked();
        return this.spliterator.getExactSizeIfKnown();
    }

    @Override
    public int characteristics() {
        this.acquisition.ensurePermittedAndLocked();
        return this.spliterator.characteristics();
    }

    @Override
    public boolean hasCharacteristics(int characteristics) {
        this.acquisition.ensurePermittedAndLocked();
        return this.spliterator.hasCharacteristics(characteristics);
    }

    @Override
    public Comparator<? super T> getComparator() {
        this.acquisition.ensurePermittedAndLocked();
        return this.spliterator.getComparator();
    }
}
