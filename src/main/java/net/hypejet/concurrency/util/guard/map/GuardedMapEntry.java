package net.hypejet.concurrency.util.guard.map;

import net.hypejet.concurrency.Acquisition;
import net.hypejet.concurrency.util.guard.GuardedObject;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * Represents {@linkplain Map.Entry a map entry} wrapper, which ensures that {@linkplain Acquisition an acquisition}
 * is locked and a caller thread has a permission to it during doing any operation.
 *
 * @param <K> a type of key of the map entry
 * @param <V> a type of value of the map entry
 * @param <E> a type of the guarded map entry
 * @since 1.0
 * @see Acquisition
 * @see Map.Entry
 */
public class GuardedMapEntry<K, V, E extends Map.Entry<K, V>> extends GuardedObject<E> implements Map.Entry<K, V> {
    /**
     * Constructs the {@linkplain GuardedMapEntry guarded map entry}.
     *
     * @param delegate the map entry that should be wrapped
     * @param acquisition an acquisition that should guard the map entry
     */
    public GuardedMapEntry(@NotNull E delegate, @NotNull Acquisition acquisition) {
        super(delegate, acquisition);
    }

    @Override
    public final K getKey() {
        this.acquisition.ensurePermittedAndLocked();
        return this.delegate.getKey();
    }

    @Override
    public final V getValue() {
        this.acquisition.ensurePermittedAndLocked();
        return this.delegate.getValue();
    }

    @Override
    public final V setValue(V value) {
        this.acquisition.ensurePermittedAndLocked();
        return this.delegate.setValue(value);
    }
}