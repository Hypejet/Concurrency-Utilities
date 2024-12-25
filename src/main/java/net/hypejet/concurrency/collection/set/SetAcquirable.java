package net.hypejet.concurrency.collection.set;

import net.hypejet.concurrency.collection.CollectionAcquirable;
import net.hypejet.concurrency.collection.CollectionAcquisition;
import net.hypejet.concurrency.util.guard.iterable.collection.GuardedSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

/**
 * Represents {@linkplain CollectionAcquirable a collection acquirable} of {@linkplain Set a set}.
 *
 * @param <E> a type of elements of the set
 * @since 1.0
 * @see Set
 * @see CollectionAcquirable
 */
public abstract class SetAcquirable<E> extends CollectionAcquirable<E, Set<E>> {
    /**
     * Constructs the {@linkplain SetAcquirable set acquirable} with no initial elements.
     *
     * @since 1.0
     */
    public SetAcquirable() {}

    /**
     * Constructs the {@linkplain SetAcquirable set acquirable}.
     *
     * @param initialElements a collection of elements that should be added to the set during initialization,
     *                        {@code null} if none
     * @since 1.0
     */
    public SetAcquirable(@Nullable Collection<E> initialElements) {
        super(initialElements);
    }

    @Override
    protected final @NotNull Set<E> createReadOnlyView(@NotNull Set<E> collection) {
        return Collections.unmodifiableSet(collection);
    }

    @Override
    protected final @NotNull Set<E> createGuardedView(@NotNull Set<E> collection,
                                                      @NotNull CollectionAcquisition<E, Set<E>> acquisition) {
        return new GuardedSet<>(collection, acquisition);
    }
}