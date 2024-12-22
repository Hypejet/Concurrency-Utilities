package net.hypejet.concurrency.map.hashmap;

import net.hypejet.concurrency.map.MapAcquirable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents {@linkplain MapAcquirable a map acquirable}, which uses {@linkplain HashMap a hash map} as
 * an implementation of the {@linkplain Map map}.
 *
 * @param <K> a type of key of the map
 * @param <V> a type of value of the map
 * @since 1.0
 * @see HashMap
 * @see MapAcquirable
 */
public final class HashMapAcquirable<K, V> extends MapAcquirable<K, V> {
    /**
     * Constructs the {@linkplain HashMapAcquirable hash map acquirable} with no initial entries.
     *
     * @since 1.0
     */
    public HashMapAcquirable() {}

    /**
     * Constructs the {@linkplain HashMapAcquirable hash map acquirable}.
     *
     * @param initialEntries a map of entries that should be added to the map during initialization, {@code null} if
     *                       none
     * @since 1.0
     */
    public HashMapAcquirable(@Nullable Map<K, V> initialEntries) {
        super(initialEntries);
    }

    @Override
    protected @NotNull Map<K, V> createMap(@Nullable Map<K, V> initialEntries) {
        if (initialEntries == null)
            return new HashMap<>();
        return new HashMap<>(initialEntries);
    }
}