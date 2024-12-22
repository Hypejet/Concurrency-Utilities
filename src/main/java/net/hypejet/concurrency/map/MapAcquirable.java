package net.hypejet.concurrency.map;

import net.hypejet.concurrency.Acquisition;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Map;

/**
 * Represents an implementation of {@linkplain AbstractMapAcquirable an abstract map acquirable}, which guards
 * {@linkplain Map a clean map}
 *
 * @param <K> a type of key of the map
 * @param <V> a type of value of the map
 * @since 1.0
 * @see AbstractMapAcquirable
 */
public abstract class MapAcquirable<K, V> extends AbstractMapAcquirable<K, V, Map<K, V>, MapAcquisition<K, V>> {
    /**
     * Constructs the {@linkplain MapAcquirable map acquirable} with no initial entries.
     *
     * @since 1.0
     */
    public MapAcquirable() {}

    /**
     * Constructs the {@linkplain MapAcquirable map acquirable}.
     *
     * @param initialEntries a map of entries that should be added to the map during initialization, {@code null} if
     *                       none
     * @since 1.0
     */
    public MapAcquirable(@Nullable Map<K, V> initialEntries) {
        super(initialEntries);
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
    public final @NotNull MapAcquisition<K, V> acquireRead() {
        MapAcquisition<K, V> foundAcquisition = this.findAcquisition();
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
    public final @NotNull MapAcquisition<K, V> acquireWrite() {
        MapAcquisition<K, V> foundAcquisition = this.findAcquisition();
        if (foundAcquisition == null)
            return new MapAcquisitionImpl<>(this, Acquisition.AcquisitionType.WRITE);

        if (foundAcquisition.acquisitionType() != Acquisition.AcquisitionType.WRITE) {
            throw new IllegalArgumentException("The caller thread has already created an acquisition, but it is not" +
                    "a write acquisition");
        }
        return new ReusedMapAcquisition<>(foundAcquisition);
    }

    @Override
    protected final @NotNull Map<K, V> createReadOnlyView(@NotNull Map<K, V> map) {
        return Collections.unmodifiableMap(map);
    }

    /**
     * Represents an implementation of {@linkplain AbstractMapAcquisition an abstract map acquisition}.
     *
     * @param <K> a type of key of the map
     * @param <V> a type of value of the map
     * @since 1.0
     * @see AbstractMapAcquisition
     */
    private static final class MapAcquisitionImpl<K, V>
            extends AbstractMapAcquisition<K, V, Map<K, V>, MapAcquisition<K, V>, MapAcquirable<K, V>> {
        /**
         * Constructs the {@linkplain MapAcquisitionImpl read map acquisition implementation}.
         *
         * @param acquirable an acquirable whose map is guarded by the lock
         * @param type a type, of which the acquisition should be
         * @since 1.0
         */
        private MapAcquisitionImpl(@NotNull MapAcquirable<K, V> acquirable, @NotNull AcquisitionType type) {
            super(acquirable, type);
        }

        @Override
        protected @NotNull MapAcquisition<K, V> cast() {
            return this;
        }
    }
}