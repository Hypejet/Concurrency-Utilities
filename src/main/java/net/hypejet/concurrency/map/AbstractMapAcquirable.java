package net.hypejet.concurrency.map;

import net.hypejet.concurrency.Acquirable;
import net.hypejet.concurrency.collection.CollectionAcquisition;
import net.hypejet.concurrency.collection.WriteCollectionAcquisition;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Objects;
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
public abstract class AbstractMapAcquirable<K, V, M extends Map<K, V>, A extends MapAcquisition<K, V, M>>
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
     * Represents an implementation of {@linkplain AbstractMapAcquisition an abstract map acquisition}.
     *
     * @param <K> a type of key of the held map
     * @param <V> a type of value of the held map
     * @param <M> a type of the held map
     * @param <AN> a type of map acquisition of the following map acquirable
     * @param <AE> a type of acquirable that the map acquisition should be registered in
     * @since 1.0
     * @see AbstractMapAcquisition
     */
    protected static abstract class AbstractReadMapAcquisition
            <K, V, M extends Map<K, V>,
                    AN extends MapAcquisition<K, V, M>,
                    AE extends AbstractMapAcquirable<K, V, M, AN>>
            extends AbstractMapAcquisition<K, V, M, AN, AE> {
        /**
         * Constructs the {@linkplain AbstractReadMapAcquisition abstract read map acquisition}.
         *
         * @param acquirable an acquirable whose map is guarded by the lock
         * @since 1.0
         */
        protected AbstractReadMapAcquisition(@NotNull AE acquirable) {
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
     * @param <AN> a type of map acquisition of the following map acquirable
     * @param <AE> a type of acquirable that the map acquisition should be registered in
     * @since 1.0
     * @see WriteCollectionAcquisition
     * @see AbstractMapAcquisition
     */
    protected static abstract class AbstractWriteMapAcquisition
            <K, V, M extends Map<K, V>,
                    AN extends MapAcquisition<K, V, M>,
                    AE extends AbstractMapAcquirable<K, V, M, AN>>
            extends AbstractMapAcquisition<K, V, M, AN, AE> implements WriteMapAcquisition<K, V, M> {
        /**
         * Constructs the {@linkplain AbstractWriteMapAcquisition abstract write map acquisition}.
         *
         * @param acquirable an acquirable whose map is guarded by the lock
         * @since 1.0
         */
        protected AbstractWriteMapAcquisition(@NotNull AE acquirable) {
            // There is no need to do nullability checks, the superclass will do that for us
            super(acquirable, AcquisitionType.WRITE);
        }

        @Override
        public @Nullable V put(@NotNull K key, @NotNull V value) {
            this.runChecks();

            Objects.requireNonNull(key, "The key must not be null");
            Objects.requireNonNull(value, "The value must not be null");

            return this.writableMap().put(key, value);
        }

        @Override
        public @Nullable V remove(@NotNull K key) {
            this.runChecks();
            Objects.requireNonNull(key, "The key must not be null");
            return this.writableMap().remove(key);
        }

        @Override
        public boolean remove(@NotNull K key, @NotNull V value) {
            this.runChecks();

            Objects.requireNonNull(key, "The key must not be null");
            Objects.requireNonNull(value, "The value must not be null");

            return this.writableMap().remove(key, value);
        }

        @Override
        public boolean replace(@NotNull K key, @Nullable V oldValue, @NotNull V newValue) {
            this.runChecks();

            Objects.requireNonNull(key, "The key must not be null");
            Objects.requireNonNull(oldValue, "The old value must not be null");
            Objects.requireNonNull(oldValue, "The new value must not be null");

            return this.writableMap().replace(key, oldValue, newValue);
        }

        @Override
        public @Nullable V replace(@NotNull K key, @NotNull V value) {
            this.runChecks();

            Objects.requireNonNull(key, "The key must not be null");
            Objects.requireNonNull(value, "The value must not be null");

            return this.writableMap().replace(key, value);
        }

        @Override
        public void putAll(@NotNull Map<? extends K, ? extends V> map) {
            this.runChecks();
            Objects.requireNonNull(map, "The map must not be null");
            this.writableMap().putAll(map);
        }

        @Override
        public @Nullable V putIfAbsent(@NotNull K key, @NotNull V value) {
            this.runChecks();

            Objects.requireNonNull(key, "The key must not be null");
            Objects.requireNonNull(value, "The value must not be null");

            return this.writableMap().putIfAbsent(key, value);
        }

        @Override
        public void clear() {
            this.runChecks();
            this.writableMap().clear();
        }

        @Override
        public void merge(@NotNull K key, @NotNull V value, @NotNull BiFunction<V, V, ? extends V> remappingFunction) {
            this.runChecks();

            Objects.requireNonNull(key, "The key must not be null");
            Objects.requireNonNull(value, "The value must not be null");
            Objects.requireNonNull(remappingFunction, "The remapping function must not be null");

            this.writableMap().merge(key, value, remappingFunction);
        }

        @Override
        public void replaceAll(@NotNull BiFunction<K, V, ? extends V> function) {
            this.runChecks();
            Objects.requireNonNull(function, "The function not be null");
            this.writableMap().replaceAll(function);
        }

        @Override
        public @Nullable V computeIfAbsent(@NotNull K key, @NotNull Function<K, ? extends V> mappingFunction) {
            this.runChecks();

            Objects.requireNonNull(key, "The key must not be null");
            Objects.requireNonNull(mappingFunction, "The mapping function must not be null");

            return this.writableMap().computeIfAbsent(key, mappingFunction);
        }

        @Override
        public @Nullable V computeIfPresent(@NotNull K key, @NotNull BiFunction<K, V, ? extends V> remappingFunction) {
            this.runChecks();

            Objects.requireNonNull(key, "The key must not be null");
            Objects.requireNonNull(remappingFunction, "The remapping function must not be null");

            return this.writableMap().computeIfPresent(key, remappingFunction);
        }

        @Override
        public @Nullable V compute(@NotNull K key, @NotNull BiFunction<K, V, ? extends V> remappingFunction) {
            this.runChecks();

            Objects.requireNonNull(key, "The key must not be null");
            Objects.requireNonNull(remappingFunction, "The remapping function must not be null");

            return this.writableMap().compute(key, remappingFunction);
        }

        /**
         * Accesses and returns a map of the acquirable that owns this acquisition.
         *
         * <p>Note that it is unsafe to be called outside a locked thread and the acquired lock. It is up to
         * an implementation to keep it safe.</p>
         *
         * @return the map
         * @since 1.0
         */
        protected final @NotNull M writableMap() {
            this.runChecks();
            // Java for some reason needs a cast of the acquirable to access private methods and fields
            AbstractMapAcquirable<K, V, M, AN> castAcquirable = this.acquirable;
            return castAcquirable.map;
        }
    }

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
    private static abstract class AbstractMapAcquisition
            <K, V, M extends Map<K, V>,
                    AN extends MapAcquisition<K, V, M>,
                    AE extends AbstractMapAcquirable<K, V, M, AN>>
            extends AbstractAcquisition<AN, AE> implements MapAcquisition<K, V, M> {
        /**
         * Constructs the {@linkplain AbstractMapAcquisition abstract map acquisition}.
         *
         * @param acquirable an acquirable whose map is guarded by the lock
         * @param type a type, of which the acquisition should be
         * @since 1.0
         */
        protected AbstractMapAcquisition(@NotNull AE acquirable, @NotNull AcquisitionType type) {
            // There is no need to do nullability checks, the superclass will do that for us
            super(acquirable, type);
        }

        @Override
        public final @NotNull M map() {
            this.runChecks();
            // Java for some reason needs a cast of the acquirable to access private methods and fields
            AbstractMapAcquirable<K, V, M, AN> castAcquirable = this.acquirable;
            return castAcquirable.readOnlyView;
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
    protected static class ReusedWriteMapAcquisition<K, V, M extends Map<K, V>>
            extends ReusedMapAcquisition<K, V, M, WriteMapAcquisition<K, V, M>>
            implements WriteMapAcquisition<K, V, M> {
        /**
         * Constructs the {@linkplain ReusedWriteMapAcquisition reused write map acquisition}.
         *
         * @param originalAcquisition an original acquisition to create the reused acquisition with
         * @since 1.0
         */
        protected ReusedWriteMapAcquisition(@NotNull WriteMapAcquisition<K, V, M> originalAcquisition) {
            // There is no need to do nullability checks, the superclass will do that for us
            super(originalAcquisition);
        }

        @Override
        public final @Nullable V put(@NotNull K key, @NotNull V value) {
            // There is no need for a nullability check, the method will do that for us
            return this.originalAcquisition.put(key, value);
        }

        @Override
        public final @Nullable V remove(@NotNull K key) {
            // There is no need for a nullability check, the method will do that for us
            return this.originalAcquisition.remove(key);
        }

        @Override
        public final boolean remove(@NotNull K key, @NotNull V value) {
            // There is no need for a nullability check, the method will do that for us
            return this.originalAcquisition.remove(key, value);
        }

        @Override
        public final boolean replace(@NotNull K key, @Nullable V oldValue, @NotNull V newValue) {
            // There is no need for a nullability check, the method will do that for us
            return this.originalAcquisition.replace(key, oldValue, newValue);
        }

        @Override
        public final @Nullable V replace(@NotNull K key, @NotNull V value) {
            // There is no need for a nullability check, the method will do that for us
            return this.originalAcquisition.replace(key, value);
        }

        @Override
        public final void putAll(@NotNull Map<? extends K, ? extends V> map) {
            // There is no need for a nullability check, the method will do that for us
            this.originalAcquisition.putAll(map);
        }

        @Override
        public final @Nullable V putIfAbsent(@NotNull K key, @NotNull V value) {
            // There is no need for a nullability check, the method will do that for us
            return this.originalAcquisition.putIfAbsent(key, value);
        }

        @Override
        public final void clear() {
            this.originalAcquisition.clear();
        }

        @Override
        public final void merge(@NotNull K key, @NotNull V value,
                                @NotNull BiFunction<V, V, ? extends V> remappingFunction) {
            // There is no need for a nullability check, the method will do that for us
            this.originalAcquisition.merge(key, value, remappingFunction);
        }

        @Override
        public final void replaceAll(@NotNull BiFunction<K, V, ? extends V> function) {
            // There is no need for a nullability check, the method will do that for us
            this.originalAcquisition.replaceAll(function);
        }

        @Override
        public final @Nullable V computeIfAbsent(@NotNull K key, @NotNull Function<K, ? extends V> mappingFunction) {
            // There is no need for a nullability check, the method will do that for us
            return this.originalAcquisition.computeIfAbsent(key, mappingFunction);
        }

        @Override
        public final @Nullable V computeIfPresent(@NotNull K key,
                                                  @NotNull BiFunction<K, V, ? extends V> remappingFunction) {
            // There is no need for a nullability check, the method will do that for us
            return this.originalAcquisition.computeIfPresent(key, remappingFunction);
        }

        @Override
        public final @Nullable V compute(@NotNull K key, @NotNull BiFunction<K, V, ? extends V> remappingFunction) {
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
    protected static class ReusedMapAcquisition<K, V, M extends Map<K, V>, A extends MapAcquisition<K, V, M>>
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