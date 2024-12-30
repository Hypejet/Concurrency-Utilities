package net.hypejet.concurrency.object.notnull;

import net.hypejet.concurrency.object.WriteObjectAcquisition;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a combination of {@linkplain NotNullObjectAcquisition a not-null object acquisition} and
 * {@linkplain WriteObjectAcquisition a write object acquisition}.
 *
 * @param <O> a type of the object
 * @since 1.0
 * @see WriteObjectAcquisition
 * @see NotNullObjectAcquisition
 */
public interface WriteNotNullObjectAcquisition<O> extends NotNullObjectAcquisition<O>, WriteObjectAcquisition<O> {
    /**
     * {@inheritDoc}
     *
     * @since 1.0
     */
    @Override
    void set(@NotNull O value);
}