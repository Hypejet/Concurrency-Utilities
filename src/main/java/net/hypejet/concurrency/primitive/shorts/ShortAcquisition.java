package net.hypejet.concurrency.primitive.shorts;

import net.hypejet.concurrency.Acquisition;
import org.jetbrains.annotations.Contract;

/**
 * Represents {@linkplain Acquisition an acquisition} that allows getting a guarded short.
 *
 * @since 1.0
 * @see Acquisition
 */
public interface ShortAcquisition extends Acquisition {
    /**
     * Gets the guarded short.
     *
     * @return a value of the short
     * @since 1.0
     */
    @Contract(pure = true)
    short get();
}