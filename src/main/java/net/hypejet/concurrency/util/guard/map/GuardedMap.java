package net.hypejet.concurrency.util.guard.map;

import net.hypejet.concurrency.Acquisition;
import net.hypejet.concurrency.util.guard.GuardedObject;
import net.hypejet.concurrency.util.guard.iterable.collection.GuardedCollection;
import net.hypejet.concurrency.util.guard.iterable.collection.GuardedSet;
import net.hypejet.concurrency.util.wrapping.iterable.collection.ElementWrappingSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Represents {@linkplain Map a map} wrapper, which ensures that {@linkplain Acquisition an acquisition}
 * is locked and a caller thread has a permission to it during doing any operation.
 *
 * @param <K> a type of keys of the map
 * @param <V> a type of values of the map
 * @param <M> a type of the guarded map
 * @since 1.0
 * @see Acquisition
 * @see Map
 */
public class GuardedMap<K, V, M extends Map<K, V>> extends GuardedObject<M> implements Map<K, V> {
    /**
     * Constructs the {@linkplain GuardedMap guarded map}.
     *
     * @param delegate the map that should be wrapped
     * @param acquisition an acquisition that should guard the map
     * @since 1.0
     */
    public GuardedMap(@NotNull M delegate, @NotNull Acquisition acquisition) {
        super(delegate, acquisition);
    }

    @Override
    public final int size() {
        this.acquisition.ensurePermittedAndLocked();
        return this.delegate.size();
    }

    @Override
    public final boolean isEmpty() {
        this.acquisition.ensurePermittedAndLocked();
        return this.delegate.isEmpty();
    }

    @Override
    public final boolean containsKey(Object key) {
        this.acquisition.ensurePermittedAndLocked();
        return this.delegate.containsKey(key);
    }

    @Override
    public final boolean containsValue(Object value) {
        this.acquisition.ensurePermittedAndLocked();
        return this.delegate.containsValue(value);
    }

    @Override
    public final V get(Object key) {
        this.acquisition.ensurePermittedAndLocked();
        return this.delegate.get(key);
    }

    @Override
    public final @Nullable V put(K key, V value) {
        this.acquisition.ensurePermittedAndLocked();
        return this.delegate.put(key, value);
    }

    @Override
    public final V remove(Object key) {
        this.acquisition.ensurePermittedAndLocked();
        return this.delegate.remove(key);
    }

    @Override
    public final void putAll(@NotNull Map<? extends K, ? extends V> m) {
        this.acquisition.ensurePermittedAndLocked();
        this.delegate.putAll(m);
    }

    @Override
    public final void clear() {
        this.acquisition.ensurePermittedAndLocked();
        this.delegate.clear();
    }

    @Override
    public final @NotNull Set<K> keySet() {
        this.acquisition.ensurePermittedAndLocked();
        return new GuardedSet<>(this.delegate.keySet(), this.acquisition);
    }

    @Override
    public final @NotNull Collection<V> values() {
        this.acquisition.ensurePermittedAndLocked();
        return new GuardedCollection<>(this.delegate.values(), this.acquisition);
    }

    @Override
    public final @NotNull Set<Entry<K, V>> entrySet() {
        this.acquisition.ensurePermittedAndLocked();
        return new GuardedSet<>(
                new ElementWrappingSet<>(
                        this.delegate.entrySet(),
                        entry -> new GuardedMapEntry<>(entry, this.acquisition)
                ),
                this.acquisition
        );
    }

    @Override
    public final V getOrDefault(Object key, V defaultValue) {
        this.acquisition.ensurePermittedAndLocked();
        return this.delegate.getOrDefault(key, defaultValue);
    }

    @Override
    public final void forEach(BiConsumer<? super K, ? super V> action) {
        this.acquisition.ensurePermittedAndLocked();
        this.delegate.forEach(action);
    }

    @Override
    public final void replaceAll(BiFunction<? super K, ? super V, ? extends V> function) {
        this.acquisition.ensurePermittedAndLocked();
        this.delegate.replaceAll(function);
    }

    @Override
    public final @Nullable V putIfAbsent(K key, V value) {
        this.acquisition.ensurePermittedAndLocked();
        return this.delegate.putIfAbsent(key, value);
    }

    @Override
    public final boolean remove(Object key, Object value) {
        this.acquisition.ensurePermittedAndLocked();
        return this.delegate.remove(key, value);
    }

    @Override
    public final boolean replace(K key, V oldValue, V newValue) {
        this.acquisition.ensurePermittedAndLocked();
        return this.delegate.replace(key, oldValue, newValue);
    }

    @Override
    public final @Nullable V replace(K key, V value) {
        this.acquisition.ensurePermittedAndLocked();
        return this.delegate.replace(key, value);
    }

    @Override
    public final V computeIfAbsent(K key, @NotNull Function<? super K, ? extends V> mappingFunction) {
        this.acquisition.ensurePermittedAndLocked();
        return this.delegate.computeIfAbsent(key, mappingFunction);
    }

    @Override
    public final V computeIfPresent(K key, @NotNull BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        this.acquisition.ensurePermittedAndLocked();
        return this.delegate.computeIfPresent(key, remappingFunction);
    }

    @Override
    public final V compute(K key, @NotNull BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        this.acquisition.ensurePermittedAndLocked();
        return this.delegate.compute(key, remappingFunction);
    }

    @Override
    public final V merge(K key, @NotNull V value,
                         @NotNull BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
        this.acquisition.ensurePermittedAndLocked();
        return this.delegate.merge(key, value, remappingFunction);
    }
}