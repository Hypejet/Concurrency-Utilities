package net.hypejet.concurrency.map;

import net.hypejet.concurrency.Acquirable;
import net.hypejet.concurrency.collection.CollectionAcquisition;
import net.hypejet.concurrency.util.collection.GuardedCollection;
import net.hypejet.concurrency.util.collection.GuardedSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Represents an abstract {@linkplain Acquirable acquirable}, which guards {@linkplain Map a map}.
 *
 * @param <K> a type of key of the map
 * @param <V> a type of value of the map
 * @param <M> a type of the map
 * @param <A> a type of acquisition of the map
 * @since 1.0
 * @see Map
 * @see Acquirable
 */
public abstract class AbstractMapAcquirable<K, V, M extends Map<K, V>, A extends MapAcquisition<K, V>>
        extends Acquirable<A> {
    
    private final @NotNull M map;
    private final @NotNull M readOnlyView;

    /**
     * Constructs the {@linkplain AbstractMapAcquirable abstract map acquirable} with no initial entries.
     *
     * @since 1.0
     */
    public AbstractMapAcquirable() {
        this(null);
    }

    /**
     * Constructs the {@linkplain AbstractMapAcquirable abstract map acquirable}.
     *
     * @param initialEntries a map of entries that should be added to the map during initialization, {@code null} if
     *                       none
     * @since 1.0
     */
    public AbstractMapAcquirable(@Nullable Map<K, V> initialEntries) {
        this.map = this.createMap(initialEntries);
        this.readOnlyView = this.createReadOnlyView(this.map);
    }

    /**
     * Creates a new mutable instance of the map.
     *
     * @param initialEntries a map of entries that should be added to the map during initialization, {@code null} if
     *                       none
     * @return the created instance
     * @since 1.0
     */
    protected abstract @NotNull M createMap(@Nullable Map<K, V> initialEntries);

    /**
     * Creates a new read-only view of a map specified.
     *
     * @param map the map to create the read-only view with
     * @return the read-only view created
     * @since 1.0
     */
    protected abstract @NotNull M createReadOnlyView(@NotNull M map);

    /**
     * Represents a common implementation of {@linkplain AbstractAcquisition an abstract acquisition}
     * and {@linkplain MapAcquisition a map acquisition}.
     *
     * @param <K> a type of key of the held map
     * @param <V> a type of value of the held map
     * @param <M> a type of the held map
     * @param <AN> a type of map acquisition of the following map acquirable
     * @param <AE> a type of acquirable that the map acquisition should be registered in
     * @since 1.0
     * @see CollectionAcquisition
     * @see AbstractAcquisition
     */
    protected static abstract class AbstractMapAcquisition
            <K, V, M extends Map<K, V>, AN extends MapAcquisition<K, V>, AE extends AbstractMapAcquirable<K, V, M, AN>>
            extends AbstractAcquisition<AN, AE> implements MapAcquisition<K, V> {
        /**
         * Constructs the {@linkplain AbstractMapAcquisition abstract map acquisition}.
         *
         * @param acquirable an acquirable whose map is guarded by the lock
         * @param type a type, of which the acquisition should be
         * @since 1.0
         */
        protected AbstractMapAcquisition(@NotNull AE acquirable, @NotNull AcquisitionType type) {
            super(acquirable, type);
        }

        @Override
        public final int size() {
            return this.map().size();
        }

        @Override
        public final boolean isEmpty() {
            return this.map().isEmpty();
        }

        @Override
        public final boolean containsKey(Object key) {
            return this.map().containsKey(key);
        }

        @Override
        public final boolean containsValue(Object value) {
            return this.map().containsValue(value);
        }

        @Override
        public final V get(Object key) {
            return this.map().get(key);
        }

        @Override
        public final @Nullable V put(K key, V value) {
            return this.map().put(key, value);
        }

        @Override
        public final V remove(Object key) {
            return this.map().remove(key);
        }

        @Override
        public final void putAll(@NotNull Map<? extends K, ? extends V> m) {
            this.map().putAll(Objects.requireNonNull(m, "The map must not be null"));
        }

        @Override
        public final void clear() {
            this.map().clear();
        }

        @Override
        public final @NotNull Set<K> keySet() {
            return new GuardedSet<>(this.map().keySet(), this);
        }

        @Override
        public final @NotNull Collection<V> values() {
            return new GuardedCollection<>(this.map().values(), this);
        }

        @Override
        public final @NotNull Set<Entry<K, V>> entrySet() {
            return new GuardedSet<>(this.map().entrySet(), this);
        }

        @Override
        public final V getOrDefault(Object key, V defaultValue) {
            return this.map().getOrDefault(key, defaultValue);
        }

        @Override
        public final void forEach(BiConsumer<? super K, ? super V> action) {
            this.map().forEach(action);
        }

        @Override
        public final void replaceAll(BiFunction<? super K, ? super V, ? extends V> function) {
            this.map().replaceAll(function);
        }

        @Override
        public final @Nullable V putIfAbsent(K key, V value) {
            return this.map().putIfAbsent(key, value);
        }

        @Override
        public final boolean remove(Object key, Object value) {
            return this.map().remove(key, value);
        }

        @Override
        public final boolean replace(K key, V oldValue, V newValue) {
            return this.map().replace(key, oldValue, newValue);
        }

        @Override
        public final @Nullable V replace(K key, V value) {
            return this.map().replace(key, value);
        }

        @Override
        public final V computeIfAbsent(K key, @NotNull Function<? super K, ? extends V> mappingFunction) {
            Objects.requireNonNull(mappingFunction, "The mapping function must not be null");
            return this.map().computeIfAbsent(key, mappingFunction);
        }

        @Override
        public final V computeIfPresent(K key,
                                        @NotNull BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
            Objects.requireNonNull(remappingFunction, "The remapping function must not be null");
            return this.map().computeIfPresent(key, remappingFunction);
        }

        @Override
        public final V compute(K key,
                               @NotNull BiFunction<? super K, ? super @Nullable V, ? extends V> remappingFunction) {
            Objects.requireNonNull(remappingFunction, "The remapping function must not be null");
            return this.map().compute(key, remappingFunction);
        }

        @Override
        public final V merge(K key, @NotNull V value,
                             @NotNull BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
            Objects.requireNonNull(value, "The value must not be null");
            Objects.requireNonNull(remappingFunction, "The remapping function must not be null");
            return this.map().merge(key, value, remappingFunction);
        }

        /**
         * Accesses and returns a view - read-only or normal, depending on the acquisition - of a map of the acquirable
         * that owns this acquisition.
         *
         * <p>Note that the map should be used only for the acquisition implementation purposes. It must not be used
         * anywhere else.</p>
         *
         * @return the view of the map
         * @since 1.0
         */
        protected final @NotNull M map() {
            this.ensurePermittedAndLocked();
            // Java requires a cast here for some reason
            AbstractMapAcquirable<K, V, M, AN> castAcquirable = acquirable;
            return switch (this.acquisitionType()) {
                case READ -> castAcquirable.readOnlyView;
                case WRITE -> castAcquirable.map;
            };
        }
    }

    /**
     * Represents an implementation of {@linkplain ReusedAcquisition a reused acquisition} and
     * {@linkplain MapAcquisition a map acquisition}.
     *
     * @param <K> a type of key of the following map
     * @param <V> a type of value of the following map
     * @param <A> a type of the acquisition that is being reused
     * @since 1.0
     * @see MapAcquisition
     * @see ReusedAcquisition
     */
    protected static class ReusedMapAcquisition<K, V, A extends MapAcquisition<K, V>> extends ReusedAcquisition<A>
            implements MapAcquisition<K, V> {
        /**
         * Constructs the {@linkplain ReusedMapAcquisition reused map acquisition}.
         *
         * @param originalAcquisition an original acquisition to create the reused acquisition with
         * @since 1.0
         */
        protected ReusedMapAcquisition(@NotNull A originalAcquisition) {
            // There is no need to do nullability checks, the superclass will do that for us
            super(originalAcquisition);
        }

        @Override
        public final int size() {
            return this.originalAcquisition.size();
        }

        @Override
        public final boolean isEmpty() {
            return this.originalAcquisition.isEmpty();
        }

        @Override
        public final boolean containsKey(Object key) {
            return this.originalAcquisition.containsKey(key);
        }

        @Override
        public final boolean containsValue(Object value) {
            return this.originalAcquisition.containsValue(value);
        }

        @Override
        public final V get(Object key) {
            return this.originalAcquisition.get(key);
        }

        @Override
        public final @Nullable V put(K key, V value) {
            return this.originalAcquisition.put(key, value);
        }

        @Override
        public final V remove(Object key) {
            return this.originalAcquisition.remove(key);
        }

        @Override
        public final void putAll(@NotNull Map<? extends K, ? extends V> m) {
            this.originalAcquisition.putAll(m);
        }

        @Override
        public final void clear() {
            this.originalAcquisition.clear();
        }

        @Override
        public final @NotNull Set<K> keySet() {
            return this.originalAcquisition.keySet();
        }

        @Override
        public final @NotNull Collection<V> values() {
            return this.originalAcquisition.values();
        }

        @Override
        public final @NotNull Set<Entry<K, V>> entrySet() {
            return this.originalAcquisition.entrySet();
        }

        @Override
        public final V getOrDefault(Object key, V defaultValue) {
            return this.originalAcquisition.getOrDefault(key, defaultValue);
        }

        @Override
        public final void forEach(BiConsumer<? super K, ? super V> action) {
            this.originalAcquisition.forEach(action);
        }

        @Override
        public final void replaceAll(BiFunction<? super K, ? super V, ? extends V> function) {
            this.originalAcquisition.replaceAll(function);
        }

        @Override
        public final @Nullable V putIfAbsent(K key, V value) {
            return this.originalAcquisition.putIfAbsent(key, value);
        }

        @Override
        public final boolean remove(Object key, Object value) {
            return this.originalAcquisition.remove(key, value);
        }

        @Override
        public final boolean replace(K key, V oldValue, V newValue) {
            return this.originalAcquisition.replace(key, oldValue, newValue);
        }

        @Override
        public final @Nullable V replace(K key, V value) {
            return this.originalAcquisition.replace(key, value);
        }

        @Override
        public final V computeIfAbsent(K key, @NotNull Function<? super K, ? extends V> mappingFunction) {
            return this.originalAcquisition.computeIfAbsent(key, mappingFunction);
        }

        @Override
        public final V computeIfPresent(K key,
                                        @NotNull BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
            return this.originalAcquisition.computeIfPresent(key, remappingFunction);
        }

        @Override
        public final V compute(K key,
                               @NotNull BiFunction<? super K, ? super @Nullable V, ? extends V> remappingFunction) {
            return this.originalAcquisition.computeIfPresent(key, remappingFunction);
        }

        @Override
        public final V merge(K key, @NotNull V value,
                             @NotNull BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
            return this.originalAcquisition.merge(key, value, remappingFunction);
        }
    }
}