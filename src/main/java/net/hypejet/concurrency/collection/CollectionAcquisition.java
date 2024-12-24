package net.hypejet.concurrency.collection;

import net.hypejet.concurrency.Acquisition;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

/**
 * Represents {@linkplain Acquisition an acquisition}, which allows getting a guarded
 * {@linkplain Collection collection}.
 *
 * @param <E> a type of elements of the collection
 * @param <C> a type of the guarded collection
 * @since 1.0
 * @see Collection
 * @see Acquisition
 */
public interface CollectionAcquisition<E, C extends Collection<E>> extends Acquisition {
    /**
     * Gets a guarded view of the collection. That means that before any operation is done with that view,
     * {@linkplain Acquisition#ensurePermittedAndLocked() an acquisition permission and lock check} is also being
     * done.
     *
     * <p>The guarded view might allow mutable operations, depending on
     * {@linkplain AcquisitionType an acquisition type} of this acquisition.</p>
     *
     * @return the guarded view
     * @since 1.0
     */
    @NotNull C collection();
}