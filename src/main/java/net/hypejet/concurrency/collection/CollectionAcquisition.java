package net.hypejet.concurrency.collection;

import net.hypejet.concurrency.Acquisition;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

/**
 * Represents {@linkplain Acquisition an acquisition}, which allows getting a guarded
 * {@linkplain Collection collection}.
 *
 * @param <V> a type of value of the collection
 * @param <C> a type of the collection
 * @since 1.0
 * @see Collection
 * @see Acquisition
 */
public interface CollectionAcquisition<V, C extends Collection<V>> extends Acquisition {
    /**
     * Gets the {@linkplain Collection collection}.
     *
     * @return an unmodifiable view of the collection
     * @since 1.0
     */
    @Contract(pure = true)
    @NotNull C collection();
}