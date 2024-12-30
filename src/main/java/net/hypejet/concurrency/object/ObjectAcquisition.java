package net.hypejet.concurrency.object;

import net.hypejet.concurrency.Acquisition;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.UnknownNullability;

/**
 * Represents {@linkplain Acquisition an acquisition} that allows getting a guarded {@linkplain O object}.
 *
 * @param <O> a type of the object
 * @since 1.0
 * @see Acquisition
 */
public interface ObjectAcquisition<O> extends Acquisition {
    /**
     * Gets the guarded object.
     *
     * @return a value of the object
     * @since 1.0
     */
    @Contract(pure = true)
    @UnknownNullability O get();
}