package net.hypejet.concurrency.util.spliterator;

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
 * @param <S> a type of the guarded spliterator
 * @since 1.0
 * @see Acquisition
 * @see Spliterator
 */
public class GuardedSpliterator<T, S extends Spliterator<T>> implements Spliterator<T> {

    protected final S delegate;
    protected final Acquisition acquisition;

    /**
     * Constructs the {@linkplain GuardedSpliterator guarded spliterator}.
     *
     * @param delegate the spliterator that should be wrapped
     * @param acquisition an acquisition that should guard the spliterator
     * @since 1.0
     */
    public GuardedSpliterator(@NotNull S delegate, @NotNull Acquisition acquisition) {
        this.delegate = Objects.requireNonNull(delegate, "The delegate must not be null");
        this.acquisition = Objects.requireNonNull(acquisition, "The acquisition must not be null");
    }

    @Override
    public final boolean tryAdvance(Consumer<? super T> action) {
        this.acquisition.ensurePermittedAndLocked();
        return this.delegate.tryAdvance(action);
    }

    @Override
    public final void forEachRemaining(Consumer<? super T> action) {
        this.acquisition.ensurePermittedAndLocked();
        this.delegate.forEachRemaining(action);
    }

    @Override
    public final Spliterator<T> trySplit() {
        this.acquisition.ensurePermittedAndLocked();
        return this.delegate.trySplit();
    }

    @Override
    public final long estimateSize() {
        this.acquisition.ensurePermittedAndLocked();
        return this.delegate.estimateSize();
    }

    @Override
    public final long getExactSizeIfKnown() {
        this.acquisition.ensurePermittedAndLocked();
        return this.delegate.getExactSizeIfKnown();
    }

    @Override
    public final int characteristics() {
        this.acquisition.ensurePermittedAndLocked();
        return this.delegate.characteristics();
    }

    @Override
    public final boolean hasCharacteristics(int characteristics) {
        this.acquisition.ensurePermittedAndLocked();
        return this.delegate.hasCharacteristics(characteristics);
    }

    @Override
    public final Comparator<? super T> getComparator() {
        this.acquisition.ensurePermittedAndLocked();
        return this.delegate.getComparator();
    }
}
