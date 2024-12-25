package net.hypejet.concurrency.map;

import net.hypejet.concurrency.Acquirable;
import net.hypejet.concurrency.Acquisition;
import net.hypejet.concurrency.util.guard.map.GuardedMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

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
public abstract class MapAcquirable<K, V, M extends Map<K, V>>
        extends Acquirable<MapAcquisition<K, V, M>> {
    
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
     * @param initialEntries a map of entries that should be added to the map during initialization, {@code null} if
     *                       none
     * @since 1.0
     */
    public MapAcquirable(@Nullable Map<K, V> initialEntries) {
        this.map = this.createMap(initialEntries);
        this.readOnlyView = this.createReadOnlyView(this.map);
    }

    /**
     * Creates {@linkplain MapAcquisition a map acquisition} of a map held by this
     * {@linkplain MapAcquirable map acquirable} that supports read-only operations.
     *
     * <p>If the caller thread has already created an acquisition a special implementation is used, which reuses it,
     * does nothing when {@link MapAcquisition#close()} is called and always returns {@code true} when
     * {@link MapAcquisition#isUnlocked()} is called.</p>
     *
     * <p>If the acquisition needs to be unlocked the already existing acquisition needs to be used to do that.</p>
     *
     * @return the acquisition
     * @since 1.0
     */
    @Override
    public final @NotNull MapAcquisition<K, V, M> acquireRead() {
        MapAcquisition<K, V, M> foundAcquisition = this.findAcquisition();
        if (foundAcquisition != null)
            return new ReusedMapAcquisition<>(foundAcquisition);
        return new MapAcquisitionImpl<>(this, Acquisition.AcquisitionType.READ);
    }

    /**
     * Creates {@linkplain MapAcquisition a map acquisition} of this {@linkplain MapAcquirable map acquirable} that
     * supports write operations.
     *
     * <p>If the caller thread has already created a write acquisition a special implementation is used, which
     * reuses it, does nothing when {@link MapAcquisition#close()} is called and always returns {@code true}
     * when {@link MapAcquisition#isUnlocked()} is called.</p>
     *
     * <p>If the acquisition needs to be unlocked the already existing acquisition needs to be used to do that.</p>
     *
     * @return the acquisition
     * @since 1.0
     * @throws IllegalArgumentException if the caller thread has already created an acquisition, but it is not a write
     *                                  acquisition
     */
    @Override
    public final @NotNull MapAcquisition<K, V, M> acquireWrite() {
        MapAcquisition<K, V, M> foundAcquisition = this.findAcquisition();
        if (foundAcquisition == null)
            return new MapAcquisitionImpl<>(this, Acquisition.AcquisitionType.WRITE);

        if (foundAcquisition.acquisitionType() != Acquisition.AcquisitionType.WRITE) {
            throw new IllegalArgumentException("The caller thread has already created an acquisition, but it is not" +
                    "a write acquisition");
        }
        return new ReusedMapAcquisition<>(foundAcquisition);
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
     * Creates a new view of a map, which is guarded by an acquisition specified. This means that the map
     * returned should do checks using the acquisition specified with {@link Acquisition#ensurePermittedAndLocked()}.
     *
     * <p>{@link GuardedMap} is recommended as an implementation of the guarded
     * map.</p>
     *
     * @param map a view of the map - read-only or normal, depending on the acquisition - to create the guarded view
     *            with
     * @param acquisition an acquisition that guards the map
     * @return the guarded view
     * @since 1.0
     */
    protected abstract @NotNull M createGuardedView(@NotNull M map, @NotNull MapAcquisition<K, V, M> acquisition);

    /**
     * Represents an implementation of {@linkplain AbstractAcquisition an acquisition} and
     * {@linkplain MapAcquisition a map acquisition}.
     *
     * @param <K> a type of key of the held map
     * @param <V> a type of value of the held map
     * @param <M> a type of the held map
     * @param <AE> a type of acquirable that the map acquisition should be registered in
     * @since 1.0
     * @see MapAcquisition
     * @see AbstractAcquisition
     */
    private static final class MapAcquisitionImpl
            <K, V, M extends Map<K, V>, AE extends MapAcquirable<K, V, M>>
            extends AbstractAcquisition<MapAcquisition<K, V, M>, AE> implements MapAcquisition<K, V, M> {

        private final M guardedView;

        /**
         * Constructs the {@linkplain MapAcquisitionImpl map acquisition implementation}.
         *
         * @param acquirable an acquirable whose map is guarded by the lock
         * @param type a type, of which the acquisition should be
         * @since 1.0
         */
        private MapAcquisitionImpl(@NotNull AE acquirable, @NotNull AcquisitionType type) {
            super(acquirable, type);

            // Java requires a cast here for some reason
            MapAcquirable<K, V, M> castAcquirable = acquirable;
            this.guardedView = castAcquirable.createGuardedView(switch (this.acquisitionType()) {
                case READ -> castAcquirable.readOnlyView;
                case WRITE -> castAcquirable.map;
            }, this);
        }

        @Override
        public @NotNull M map() {
            this.ensurePermittedAndLocked();
            return this.guardedView;
        }

        @Override
        protected @NotNull MapAcquisition<K, V, M> cast() {
            return this;
        }
    }

    /**
     * Represents an implementation of {@linkplain ReusedAcquisition a reused acquisition} and
     * {@linkplain MapAcquisition a map acquisition}.
     *
     * @param <K> a type of key of the following map
     * @param <V> a type of value of the following map
     * @param <M> a type of graded map of the map acquisition that is being reused
     * @param <A> a type of the acquisition that is being reused
     * @since 1.0
     * @see MapAcquisition
     * @see ReusedAcquisition
     */
    private final static class ReusedMapAcquisition<K, V, M extends Map<K, V>, A extends MapAcquisition<K, V, M>>
            extends ReusedAcquisition<A> implements MapAcquisition<K, V, M> {
        /**
         * Constructs the {@linkplain ReusedMapAcquisition reused map acquisition}.
         *
         * @param originalAcquisition an original acquisition to create the reused acquisition with
         * @since 1.0
         */
        private ReusedMapAcquisition(@NotNull A originalAcquisition) {
            // There is no need to do nullability checks, the superclass will do that for us
            super(originalAcquisition);
        }

        @Override
        public @NotNull M map() {
            return this.originalAcquisition.map();
        }
    }
}