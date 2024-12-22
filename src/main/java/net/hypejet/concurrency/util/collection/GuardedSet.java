package net.hypejet.concurrency.util.collection;

import net.hypejet.concurrency.Acquisition;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

/**
 * Represents {@linkplain Set a set} wrapper, which ensures that {@linkplain Acquisition an acquisition} is locked
 * and a caller thread has a permission to it during doing any operation.
 *
 * @param <E> a type of value of the set
 * @since 1.0
 * @see Acquisition
 * @see Set
 */
public final class GuardedSet<E> extends GuardedCollection<E, Set<E>> implements Set<E> {
    /**
     * Constructs the {@linkplain GuardedSet guarded set}.
     *
     * @param set the set that should be wrapped
     * @param acquisition an acquisition that should guard the set
     * @since 1.0
     */
    public GuardedSet(@NotNull Set<E> set, @NotNull Acquisition acquisition) {
        super(set, acquisition);
    }
}