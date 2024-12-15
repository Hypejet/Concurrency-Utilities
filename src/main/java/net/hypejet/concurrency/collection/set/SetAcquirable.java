package net.hypejet.concurrency.collection.set;

import net.hypejet.concurrency.collection.CollectionAcquirable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

/**
 * Represents {@linkplain CollectionAcquirable a collection acquirable} of {@linkplain Set a set}.
 *
 * @param <V> a type of value of the set
 * @since 1.0
 * @see Set
 * @see CollectionAcquirable
 */
public abstract class SetAcquirable<V> extends CollectionAcquirable<V, Set<V>> {
    /**
     * Constructs the {@linkplain SetAcquirable set acquirable} with no initial elements.
     *
     * @since 1.0
     */
    public SetAcquirable() {}

    /**
     * Constructs the {@linkplain SetAcquirable set acquirable}.
     *
     * @param initialElements a collection of elements that should be added to the set during initialization
     * @since 1.0
     */
    public SetAcquirable(@Nullable Collection<V> initialElements) {
        // There is no need to check whether the acquirable is null, the superclass will do that for us
        super(initialElements);
    }

    @Override
    protected @NotNull Set<V> createReadOnlyView(@NotNull Set<V> collection) {
        return Collections.unmodifiableSet(collection);
    }
}