package net.hypejet.concurrency.map;

import net.hypejet.concurrency.Acquisition;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * Represents {@linkplain Acquisition an acquisition}, which allows getting a guarded {@linkplain java.util.Map map}.
 *
 * @param <K> a type of key of the map
 * @param <V> a type of value of the map
 * @param <M> a type of the guarded map
 * @since 1.0
 * @see Acquisition
 */
public interface MapAcquisition<K, V, M extends Map<K, V>> extends Acquisition {
    /**
     * Gets a guarded view of the map. That means that before any operation is done with that view,
     * {@linkplain Acquisition#ensurePermittedAndLocked() an acquisition permission and lock check} is also being
     * done.
     *
     * <p>The guarded view might allow mutable operations, depending on
     * {@linkplain AcquisitionType an acquisition type} of this acquisition.</p>
     *
     * @return the guarded view
     * @since 1.0
     */
    @NotNull M map();
}