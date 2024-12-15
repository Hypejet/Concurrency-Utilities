package net.hypejet.concurrency.primitive.integer;

import net.hypejet.concurrency.Acquisition;
import org.jetbrains.annotations.Contract;

/**
 * Represents {@linkplain Acquisition an acquisition} that allows getting a guarded integer.
 *
 * @since 1.0
 * @see Acquisition
 */
public interface IntegerAcquisition extends Acquisition {
    /**
     * Gets the guarded integer.
     *
     * @return a value of the integer
     * @since 1.0
     */
    @Contract(pure = true)
    int get();
}