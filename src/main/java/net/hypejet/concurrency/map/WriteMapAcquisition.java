package net.hypejet.concurrency.map;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Represents {@linkplain MapAcquisition a map acquisition}, which allows modifying the guarded {@linkplain Map map}.
 *
 * @param <K> a type of key of the map
 * @param <V> a type of value of the map
 * @param <M> a type of the map
 * @since 1.0
 * @see MapAcquisition
 */
public interface WriteMapAcquisition<K, V, M extends Map<K, V>> extends MapAcquisition<K, V, M> {
    /**
     * Puts a mapping containing a key and value specified to the map.
     *
     * @param key the key
     * @param value the value
     * @return a value of the previous mapping, {@code null} if the previous mapping does not exist
     * @since 1.0
     * @see Map#put(Object, Object)
     */
    @Nullable V put(@NotNull K key, @NotNull V value);

    /**
     * Removes a mapping containing a key specified.
     *
     * @param key the key
     * @return a value of the mapping, {@code null} if the mapping does not exist
     * @since 1.0
     * @see Map#remove(Object)
     */
    @Nullable V remove(@NotNull K key);

    /**
     * Removes a mapping containing a key specified only if a value of it is the value specified.
     *
     * @param key the key
     * @param value the value
     * @return {@code true} if the mapping was removed, {@code false} otherwise
     * @since 1.0
     * @see Map#remove(Object, Object)
     */
    boolean remove(@NotNull K key, @NotNull V value);

    /**
     * Replaces value of a mapping containing a key specified with a new value only if the previous value specified
     * matches the current value.
     *
     * @param key the key
     * @param oldValue the previous value
     * @param newValue the new value
     * @return {@code true} if the value was replaced, {@code false} otherwise
     * @since 1.0
     * @see Map#replace(Object, Object, Object)
     */
    boolean replace(@NotNull K key, @Nullable V oldValue, @NotNull V newValue);

    /**
     * Replaces a mapping containing a key specified only if it is mapped to some value.
     *
     * @param key the key
     * @param value the new value
     * @return the value replaced, {@code null} if no value was attached to the mapping
     * @since 1.0
     * @see Map#replace(Object, Object)
     */
    @Nullable V replace(@NotNull K key, @NotNull V value);

    /**
     * Puts all mappings to the map from a map specified.
     *
     * @param map the map to get the mappings from
     * @since 1.0
     * @see Map#putAll(Map)
     */
    void putAll(@NotNull Map<? extends K, ? extends V> map);

    /**
     * Puts a mapping containing a key and value specified only if the key is currently not mapped to any value.
     *
     * @param key the key
     * @param value the value
     * @return a value of the previous mapping containing the key, {@code null} if none
     * @since 1.0
     * @see Map#putIfAbsent(Object, Object)
     */
    @Nullable V putIfAbsent(@NotNull K key, @NotNull V value);

    /**
     * Clears all mapping from the map.
     *
     * @since 1.0
     * @see Map#clear()
     */
    void clear();

    /**
     * Maps a mapping containing a key specified to a value specified if the key is not mapped to any value. Otherwise,
     * replaces the value attached to the mapping with a result of the remapping function, or removes it if the result
     * is {@code null}.
     *
     * @param key the key
     * @param value the value
     * @param remappingFunction the remapping function, which provides a previous value and a value specified in this
     *                          method
     * @since 1.0
     * @see Map#merge(Object, Object, BiFunction)
     */
    void merge(@NotNull K key, @NotNull V value, @NotNull BiFunction<V, V, ? extends V> remappingFunction);

    /**
     * Replaces values of all mappings using a function specified, which is applied to all mapping specified.
     *
     * @param function the function, whose result is a new value
     * @since 1.0
     * @see Map#replaceAll(BiFunction)
     */
    void replaceAll(@NotNull BiFunction<K, V, ? extends V> function);

    /**
     * Attempts to compute a value using a function specified for a mapping of a key specified if the mapping
     * is not already attached to any value.
     *
     * @param key the key
     * @param mappingFunction the function
     * @return the current (existing or computed) value, {@code null} if the computed value is null
     * @since 1.0
     * @see Map#computeIfAbsent(Object, Function)
     */
    @Nullable V computeIfAbsent(@NotNull K key, @NotNull Function<K, ? extends V> mappingFunction);

    /**
     * Attempts to compute a value using a function specified for a mapping of a key specified if the mapping
     * is already attached to some value.
     *
     * @param key the key
     * @param remappingFunction the function
     * @return the new value attached to the mapping, {@code null} if none
     * @since 1.0
     * @see Map#computeIfPresent(Object, BiFunction)
     */
    @Nullable V computeIfPresent(@NotNull K key, @NotNull BiFunction<K, V, ? extends V> remappingFunction);

    /**
     * Attempts to compute a value for a key and value attached to a mapping containing the key specified using
     * a function specified. The {@code null} value is provided for the function if not value is attached to the
     * mapping.
     *
     * @param key the key
     * @param remappingFunction the function
     * @return the new value attached to the mapping, {@code null} if none
     * @since 1.0
     * @see Map#compute(Object, BiFunction)
     */
    @Nullable V compute(@NotNull K key, @NotNull BiFunction<K, V, ? extends V> remappingFunction);
}