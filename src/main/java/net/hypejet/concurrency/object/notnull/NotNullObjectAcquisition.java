package net.hypejet.concurrency.object.notnull;

import net.hypejet.concurrency.object.ObjectAcquisition;
import org.jetbrains.annotations.NotNull;

/**
 * Represents {@linkplain ObjectAcquisition an object acquisition}, whose object is never {@code null}.
 *
 * @param <O> a type of the object
 * @since 1.0
 * @see ObjectAcquisition
 */
public interface NotNullObjectAcquisition<O> extends ObjectAcquisition<O> {
    /**
     * {@inheritDoc}
     *
     * @since 1.0
     */
    @Override
    @NotNull O get();
}