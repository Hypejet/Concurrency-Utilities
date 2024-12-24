package net.hypejet.concurrency.collection.list;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Represents {@linkplain ListAcquirable a list acquirable}, which uses {@linkplain ArrayList an array list}
 * as an implementation of the {@linkplain List list}.
 *
 * @param <V> a type of value of the list
 * @since 1.0
 * @see ArrayList
 * @see ListAcquirable
 */
public final class ArrayListAcquirable<V> extends ListAcquirable<V> {
    /**
     * Constructs the {@linkplain ArrayListAcquirable array list acquirable} with no initial elements.
     *
     * @since 1.0
     */
    public ArrayListAcquirable() {}

    /**
     * Constructs the {@linkplain ArrayListAcquirable array list acquirable}.
     *
     * @param initialElements a collection of elements that should be added to the list during initialization
     * @since 1.0
     */
    public ArrayListAcquirable(@Nullable Collection<V> initialElements) {
        super(initialElements);
    }

    @Override
    protected @NotNull List<V> createCollection(@Nullable Collection<V> initialElements) {
        return initialElements == null ? new ArrayList<>() : new ArrayList<>(initialElements);
    }
}