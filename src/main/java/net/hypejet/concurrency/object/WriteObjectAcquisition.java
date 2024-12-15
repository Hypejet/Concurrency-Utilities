package net.hypejet.concurrency.object;

import org.jetbrains.annotations.NotNull;

/**
 * Represents {@linkplain ObjectAcquisition an object acquisition} that allows for {@linkplain O an object}, which
 * is guarded to be updated.
 *
 * @param <O> a type of the object
 * @since 1.0
 * @see ObjectAcquisition
 */
public interface WriteObjectAcquisition<O> extends ObjectAcquisition<O> {
    /**
     * Updates the object.
     *
     * @param value a value to replace the object with
     * @since 1.0
     */
    void set(@NotNull O value);
}