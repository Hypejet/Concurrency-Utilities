package net.hypejet.concurrency.map.hashmap;

import net.hypejet.concurrency.map.MapAcquirable;
import net.hypejet.concurrency.map.MapAcquisition;
import net.hypejet.concurrency.util.map.GuardedMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
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
public final class HashMapAcquirable<K, V> extends MapAcquirable<K, V, Map<K, V>> {
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
        return initialEntries == null ? new HashMap<>() : new HashMap<>(initialEntries);
    }

    @Override
    protected @NotNull Map<K, V> createReadOnlyView(@NotNull Map<K, V> map) {
        return Collections.unmodifiableMap(map);
    }

    @Override
    protected @NotNull Map<K, V> createGuardedView(@NotNull Map<K, V> map,
                                                   @NotNull MapAcquisition<K, V, Map<K, V>> acquisition) {
        return new GuardedMap<>(map, acquisition);
    }
}