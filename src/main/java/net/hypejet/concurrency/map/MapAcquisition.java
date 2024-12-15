package net.hypejet.concurrency.map;

import net.hypejet.concurrency.Acquisition;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * Represents {@linkplain Acquisition an acquisition}, which allows getting a guarded {@linkplain Map a map}.
 *
 * @param <K> a type of key of the map
 * @param <V> a type of value of the map
 * @param <M> a type of the map
 * @since 1.0
 * @see Acquisition
 */
public interface MapAcquisition<K, V, M extends Map<K, V>> extends Acquisition {
    /**
     * Gets the {@linkplain Map map}.
     *
     * @return an unmodifiable view of the map
     * @since 1.0
     */
    @Contract(pure = true)
    @NotNull M map();
}