package net.hypejet.concurrency.collection.list;

import net.hypejet.concurrency.collection.CollectionAcquirable;
import net.hypejet.concurrency.collection.CollectionAcquisition;
import net.hypejet.concurrency.util.guard.iterable.collection.GuardedList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Represents {@linkplain CollectionAcquirable an abstract collection acquirable} of {@linkplain List a list}.
 *
 * @param <E> a type of elements of the list
 * @since 1.0
 * @see List
 * @see CollectionAcquirable
 */
public abstract class ListAcquirable<E> extends CollectionAcquirable<E, List<E>> {
    /**
     * Constructs the {@linkplain ListAcquirable list acquirable} with no initial elements.
     *
     * @since 1.0
     */
    public ListAcquirable() {}

    /**
     * Constructs the {@linkplain ListAcquirable list acquirable}.
     *
     * @param initialElements a collection of elements that should be added to the list during initialization,
     *                        {@code null} if none
     * @since 1.0
     */
    public ListAcquirable(@Nullable Collection<E> initialElements) {
        super(initialElements);
    }

    @Override
    protected final @NotNull List<E> createReadOnlyView(@NotNull List<E> collection) {
        return Collections.unmodifiableList(collection);
    }

    @Override
    protected final @NotNull List<E> createGuardedView(@NotNull List<E> collection,
                                                       @NotNull CollectionAcquisition<E, List<E>> acquisition) {
        return new GuardedList<>(collection, acquisition);
    }
}