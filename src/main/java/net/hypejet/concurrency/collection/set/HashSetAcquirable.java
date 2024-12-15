package net.hypejet.concurrency.collection.set;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Represents {@linkplain SetAcquirable a set acquirable}, which uses {@linkplain HashSet an hash set}
 * as an implementation of the {@linkplain Set set}.
 *
 * @param <V> a type of value of the set
 * @since 1.0
 * @see HashSet
 * @see SetAcquirable
 */
public final class HashSetAcquirable<V> extends SetAcquirable<V> {
    /**
     * Constructs the {@linkplain HashSetAcquirable hash set acquirable} with no initial elements.
     *
     * @since 1.0
     */
    public HashSetAcquirable() {}

    /**
     * Constructs the {@linkplain HashSetAcquirable hash set acquirable}.
     *
     * @param initialElements a collection of elements that should be added to the set during initialization
     * @since 1.0
     */
    public HashSetAcquirable(@Nullable Collection<V> initialElements) {
        // There is no need to check whether the acquirable is null, the superclass will do that for us
        super(initialElements);
    }

    @Override
    protected @NotNull Set<V> createCollection() {
        return new HashSet<>();
    }
}