package net.hypejet.concurrency.map;

import net.hypejet.concurrency.Acquirable;
import net.hypejet.concurrency.collection.CollectionAcquirable;
import net.hypejet.concurrency.collection.CollectionAcquisition;
import net.hypejet.concurrency.collection.WriteCollectionAcquisition;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Represents {@linkplain Acquirable an acquirable}, which guards {@linkplain Map a map}.
 *
 * @param <K> a type of key of the map
 * @param <V> a type of value of the map
 * @param <M> a type of the map
 * @since 1.0
 * @see Map
 * @see Acquirable
 */
public abstract class MapAcquirable<K, V, M extends Map<K, V>> extends Acquirable<MapAcquisition<K, V, M>> {
    
    private final @NotNull M map;
    private final @NotNull M readOnlyView;

    /**
     * Constructs the {@linkplain MapAcquirable map acquirable} with no initial entries.
     *
     * @since 1.0
     */
    public MapAcquirable() {
        this(null);
    }

    /**
     * Constructs the {@linkplain MapAcquirable map acquirable}.
     *
     * @param initialEntries a map of entries that should be added to the map during initialization
     * @since 1.0
     */
    public MapAcquirable(@Nullable Map<K, V> initialEntries) {
        this.map = this.createMap();
        if (initialEntries != null)
            this.map.putAll(initialEntries);
        this.readOnlyView = this.createReadOnlyView(this.map);
    }

    @Override
    public final @NotNull MapAcquisition<K, V, M> acquireRead() {
        MapAcquisition<K, V, M> foundAcquisition = this.findAcquisition();
        if (foundAcquisition != null)
            return new ReusedMapAcquisition<>(foundAcquisition);
        return new MapAcquisitionImpl<>(this);
    }

    @Override
    public final @NotNull WriteMapAcquisition<K, V, M> acquireWrite() {
        MapAcquisition<K, V, M> foundAcquisition = this.findAcquisition();
        if (foundAcquisition == null)
            return new WriteMapAcquisitionImpl<>(this);

        if (!(foundAcquisition instanceof WriteMapAcquisition<K, V, M> writeAcquisition)) {
            throw new IllegalArgumentException("The caller thread has already created an acquisition," +
                    " but it is not a write acquisition");
        }
        return new ReusedWriteMapAcquisition<>(writeAcquisition);
    }

    /**
     * Creates a new mutable instance of the map.
     *
     * @return the created instance
     * @since 1.0
     */
    protected abstract @NotNull M createMap();

    /**
     * Creates a new read-only view of a map specified.
     *
     * @param map the map to create the read-only view with
     * @return the read-only view created
     * @since 1.0
     */
    protected abstract @NotNull M createReadOnlyView(@NotNull M map);

    /**
     * Represents an implementation of {@linkplain AbstractMapAcquisition an abstract map acquisition}.
     *
     * @param <K> a type of key of the held map
     * @param <V> a type of value of the held map
     * @param <M> a type of the held map
     * @since 1.0
     * @see AbstractMapAcquisition
     */
    private static final class MapAcquisitionImpl<K, V, M extends Map<K, V>> extends AbstractMapAcquisition<K, V, M> {
        /**
         * Constructs the {@linkplain MapAcquisitionImpl map acquisition implementation}.
         *
         * @param acquirable an acquirable whose state is guarded by the lock
         * @since 1.0
         */
        private MapAcquisitionImpl(@NotNull MapAcquirable<K, V, M> acquirable) {
            // There is no need to do nullability checks, the superclass will do that for us
            super(acquirable, AcquisitionType.READ);
        }
    }

    /**
     * Represents an implementation of {@linkplain AbstractMapAcquisition an abstract map acquisition}
     * and {@linkplain WriteMapAcquisition a write map acquisition}.
     *
     * @param <K> a type of key of the held map
     * @param <V> a type of value of the held map
     * @param <M> a type of the held map
     * @since 1.0
     * @see WriteCollectionAcquisition
     * @see AbstractMapAcquisition
     */
    private static final class WriteMapAcquisitionImpl<K, V, M extends Map<K, V>>
            extends AbstractMapAcquisition<K, V, M> implements WriteMapAcquisition<K, V, M> {
        /**
         * Constructs the {@linkplain WriteMapAcquisitionImpl write map acquisition implementation}.
         *
         * @param acquirable an acquirable whose state is guarded by the lock
         * @since 1.0
         */
        private WriteMapAcquisitionImpl(@NotNull MapAcquirable<K, V, M> acquirable) {
            // There is no need to do nullability checks, the superclass will do that for us
            super(acquirable, AcquisitionType.WRITE);
        }

        @Override
        public @Nullable V put(@NotNull K key, @NotNull V value) {
            this.runChecks();
            Objects.requireNonNull(key, "The key must not be null");
            Objects.requireNonNull(value, "The value must not be null");
            return this.acquirable.map.put(key, value);
        }

        @Override
        public @Nullable V remove(@NotNull K key) {
            this.runChecks();
            Objects.requireNonNull(key, "The key must not be null");
            return this.acquirable.map.remove(key);
        }

        @Override
        public boolean remove(@NotNull K key, @NotNull V value) {
            this.runChecks();
            Objects.requireNonNull(key, "The key must not be null");
            Objects.requireNonNull(value, "The value must not be null");
            return this.acquirable.map.remove(key, value);
        }

        @Override
        public boolean replace(@NotNull K key, @Nullable V oldValue, @NotNull V newValue) {
            this.runChecks();
            Objects.requireNonNull(key, "The key must not be null");
            Objects.requireNonNull(oldValue, "The old value must not be null");
            Objects.requireNonNull(oldValue, "The new value must not be null");
            return this.acquirable.map.replace(key, oldValue, newValue);
        }

        @Override
        public @Nullable V replace(@NotNull K key, @NotNull V value) {
            this.runChecks();
            Objects.requireNonNull(key, "The key must not be null");
            Objects.requireNonNull(value, "The value must not be null");
            return this.acquirable.map.replace(key, value);
        }

        @Override
        public void putAll(@NotNull Map<? extends K, ? extends V> map) {
            this.runChecks();
            Objects.requireNonNull(map, "The map must not be null");
            this.acquirable.map.putAll(map);
        }

        @Override
        public @Nullable V putIfAbsent(@NotNull K key, @NotNull V value) {
            this.runChecks();
            Objects.requireNonNull(key, "The key must not be null");
            Objects.requireNonNull(value, "The value must not be null");
            return this.acquirable.map.putIfAbsent(key, value);
        }

        @Override
        public void clear() {
            this.runChecks();
            this.acquirable.map.clear();
        }

        @Override
        public void merge(@NotNull K key, @NotNull V value, @NotNull BiFunction<V, V, ? extends V> remappingFunction) {
            this.runChecks();
            Objects.requireNonNull(key, "The key must not be null");
            Objects.requireNonNull(value, "The value must not be null");
            Objects.requireNonNull(remappingFunction, "The remapping function must not be null");
            this.acquirable.map.merge(key, value, remappingFunction);
        }

        @Override
        public void replaceAll(@NotNull BiFunction<K, V, ? extends V> function) {
            this.runChecks();
            Objects.requireNonNull(function, "The function not be null");
            this.acquirable.map.replaceAll(function);
        }

        @Override
        public @Nullable V computeIfAbsent(@NotNull K key, @NotNull Function<K, ? extends V> mappingFunction) {
            this.runChecks();
            Objects.requireNonNull(key, "The key must not be null");
            Objects.requireNonNull(mappingFunction, "The mapping function must not be null");
            return this.acquirable.map.computeIfAbsent(key, mappingFunction);
        }

        @Override
        public @Nullable V computeIfPresent(@NotNull K key, @NotNull BiFunction<K, V, ? extends V> remappingFunction) {
            this.runChecks();
            Objects.requireNonNull(key, "The key must not be null");
            Objects.requireNonNull(remappingFunction, "The remapping function must not be null");
            return this.acquirable.map.computeIfPresent(key, remappingFunction);
        }

        @Override
        public @Nullable V compute(@NotNull K key, @NotNull BiFunction<K, V, ? extends V> remappingFunction) {
            this.runChecks();
            Objects.requireNonNull(key, "The key must not be null");
            Objects.requireNonNull(remappingFunction, "The remapping function must not be null");
            return this.acquirable.map.compute(key, remappingFunction);
        }
    }

    /**
     * Represents a common implementation of {@linkplain AbstractAcquisition an abstract acquisition}
     * and {@linkplain MapAcquisition a map acquisition}.
     *
     * @param <K> a type of key of the held map
     * @param <V> a type of value of the held map
     * @param <M> a type of the held map
     * @since 1.0
     * @see CollectionAcquisition
     * @see AbstractAcquisition
     */
    private static abstract class AbstractMapAcquisition<K, V, M extends Map<K, V>>
            extends AbstractAcquisition<MapAcquisition<K, V, M>, MapAcquirable<K, V, M>>
            implements MapAcquisition<K, V, M> {
        /**
         * Constructs the {@linkplain AbstractMapAcquisition abstract map acquisition}.
         *
         * @param acquirable an acquirable whose state is guarded by the lock
         * @param type a type, of which the acquisition should be
         * @since 1.0
         */
        protected AbstractMapAcquisition(@NotNull MapAcquirable<K, V, M> acquirable, @NotNull AcquisitionType type) {
            // There is no need to do nullability checks, the superclass will do that for us
            super(acquirable, type);
        }

        @Override
        public final @NotNull M map() {
            this.runChecks();
            return this.acquirable.readOnlyView;
        }

        @Override
        protected final @NotNull MapAcquisition<K, V, M> cast() {
            return this;
        }
    }

    /**
     * Represents {@linkplain ReusedMapAcquisition a reused map acquisition}, which reused an already
     * existing {@linkplain WriteMapAcquisition write map acquisition}.
     *
     * @param <K> a type of key of the following map
     * @param <V> a type of value of the following map
     * @param <M> a type of the map of the map acquisition that is being reused
     * @since 1.0
     * @see WriteMapAcquisition
     * @see ReusedMapAcquisition
     */
    private static final class ReusedWriteMapAcquisition<K, V, M extends Map<K, V>>
            extends ReusedMapAcquisition<K, V, M, WriteMapAcquisition<K, V, M>>
            implements WriteMapAcquisition<K, V, M> {
        /**
         * Constructs the {@linkplain ReusedWriteMapAcquisition reused write map acquisition}.
         *
         * @param originalAcquisition an original acquisition to create the reused acquisition with
         * @since 1.0
         */
        private ReusedWriteMapAcquisition(@NotNull WriteMapAcquisition<K, V, M> originalAcquisition) {
            // There is no need to do nullability checks, the superclass will do that for us
            super(originalAcquisition);
        }

        @Override
        public @Nullable V put(@NotNull K key, @NotNull V value) {
            // There is no need for a nullability check, the method will do that for us
            return this.originalAcquisition.put(key, value);
        }

        @Override
        public @Nullable V remove(@NotNull K key) {
            // There is no need for a nullability check, the method will do that for us
            return this.originalAcquisition.remove(key);
        }

        @Override
        public boolean remove(@NotNull K key, @NotNull V value) {
            // There is no need for a nullability check, the method will do that for us
            return this.originalAcquisition.remove(key, value);
        }

        @Override
        public boolean replace(@NotNull K key, @Nullable V oldValue, @NotNull V newValue) {
            // There is no need for a nullability check, the method will do that for us
            return this.originalAcquisition.replace(key, oldValue, newValue);
        }

        @Override
        public @Nullable V replace(@NotNull K key, @NotNull V value) {
            // There is no need for a nullability check, the method will do that for us
            return this.originalAcquisition.replace(key, value);
        }

        @Override
        public void putAll(@NotNull Map<? extends K, ? extends V> map) {
            // There is no need for a nullability check, the method will do that for us
            this.originalAcquisition.putAll(map);
        }

        @Override
        public @Nullable V putIfAbsent(@NotNull K key, @NotNull V value) {
            // There is no need for a nullability check, the method will do that for us
            return this.originalAcquisition.putIfAbsent(key, value);
        }

        @Override
        public void clear() {
            this.originalAcquisition.clear();
        }

        @Override
        public void merge(@NotNull K key, @NotNull V value, @NotNull BiFunction<V, V, ? extends V> remappingFunction) {
            // There is no need for a nullability check, the method will do that for us
            this.originalAcquisition.merge(key, value, remappingFunction);
        }

        @Override
        public void replaceAll(@NotNull BiFunction<K, V, ? extends V> function) {
            // There is no need for a nullability check, the method will do that for us
            this.originalAcquisition.replaceAll(function);
        }

        @Override
        public @Nullable V computeIfAbsent(@NotNull K key, @NotNull Function<K, ? extends V> mappingFunction) {
            // There is no need for a nullability check, the method will do that for us
            return this.originalAcquisition.computeIfAbsent(key, mappingFunction);
        }

        @Override
        public @Nullable V computeIfPresent(@NotNull K key, @NotNull BiFunction<K, V, ? extends V> remappingFunction) {
            // There is no need for a nullability check, the method will do that for us
            return this.originalAcquisition.computeIfPresent(key, remappingFunction);
        }

        @Override
        public @Nullable V compute(@NotNull K key, @NotNull BiFunction<K, V, ? extends V> remappingFunction) {
            // There is no need for a nullability check, the method will do that for us
            return this.originalAcquisition.compute(key, remappingFunction);
        }
    }

    /**
     * Represents an implementation of {@linkplain ReusedAcquisition a reused acquisition} and
     * {@linkplain MapAcquisition a map acquisition}.
     *
     * @param <K> a type of key of the following map
     * @param <V> a type of value of the following map
     * @param <M> a type of the map of the map acquisition that is being reused
     * @param <A> a type of the acquisition that is being reused
     * @since 1.0
     * @see MapAcquisition
     * @see ReusedAcquisition
     */
    private static class ReusedMapAcquisition<K, V, M extends Map<K, V>, A extends MapAcquisition<K, V, M>>
            extends ReusedAcquisition<A> implements MapAcquisition<K, V, M> {
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
        public final @NotNull M map() {
            return this.originalAcquisition.map();
        }
    }
}