package net.hypejet.concurrency.object.nullable;

import net.hypejet.concurrency.object.WriteObjectAcquisition;
import org.jetbrains.annotations.Nullable;

/**
 * Represents a combination of {@linkplain NullableObjectAcquisition a nullable object acquisition} and
 * {@linkplain WriteObjectAcquisition a write object acquisition}.
 *
 * @param <O> a type of the object
 * @since 1.0
 * @see WriteObjectAcquisition
 * @see NullableObjectAcquisition
 */
public interface WriteNullableObjectAcquisition<O> extends NullableObjectAcquisition<O>, WriteObjectAcquisition<O> {
    /**
     * {@inheritDoc}
     *
     * @since 1.0
     */
    @Override
    void set(@Nullable O value);
}