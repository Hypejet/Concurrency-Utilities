package net.hypejet.concurrency.object.nullable;

import net.hypejet.concurrency.object.ObjectAcquisition;
import org.jetbrains.annotations.Nullable;

/**
 * Represents {@linkplain ObjectAcquisition an object acquisition}, whose object is allowed to be {@code null}.
 *
 * @param <O> a type of the object
 * @since 1.0
 * @see ObjectAcquisition
 */
public interface NullableObjectAcquisition<O> extends ObjectAcquisition<O> {
    /**
     * {@inheritDoc}
     *
     * @since 1.0
     */
    @Override
    @Nullable O get();
}