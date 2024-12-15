package net.hypejet.concurrency.collection.list;

import net.hypejet.concurrency.collection.CollectionAcquirable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Represents {@linkplain CollectionAcquirable a collection acquirable} of {@linkplain List a list}.
 *
 * @param <V> a type of value of the list
 * @since 1.0
 * @see List
 * @see CollectionAcquirable
 */
public abstract class ListAcquirable<V> extends CollectionAcquirable<V, List<V>> {
    /**
     * Constructs the {@linkplain ListAcquirable list acquirable} with no initial elements.
     *
     * @since 1.0
     */
    public ListAcquirable() {}

    /**
     * Constructs the {@linkplain ListAcquirable list acquirable}.
     *
     * @param initialElements a collection of elements that should be added to the list during initialization
     * @since 1.0
     */
    public ListAcquirable(@Nullable Collection<V> initialElements) {
        // There is no need to check whether the acquirable is null, the superclass will do that for us
        super(initialElements);
    }

    @Override
    protected @NotNull List<V> createReadOnlyView(@NotNull List<V> collection) {
        return Collections.unmodifiableList(collection);
    }
}