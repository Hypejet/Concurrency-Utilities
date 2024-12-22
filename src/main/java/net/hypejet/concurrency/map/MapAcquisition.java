package net.hypejet.concurrency.map;

import net.hypejet.concurrency.Acquisition;

import java.util.Map;

/**
 * Represents {@linkplain Acquisition an acquisition}, which allows getting a guarded {@linkplain java.util.Map map}.
 *
 * @param <K> a type of key of the map
 * @param <V> a type of value of the map
 * @since 1.0
 * @see Acquisition
 */
public interface MapAcquisition<K, V> extends Acquisition, Map<K, V> {}