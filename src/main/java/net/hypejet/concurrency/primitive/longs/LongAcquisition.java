package net.hypejet.concurrency.primitive.longs;

import net.hypejet.concurrency.Acquisition;
import org.jetbrains.annotations.Contract;

/**
 * Represents {@linkplain Acquisition an acquisition} that allows getting a guarded long.
 *
 * @since 1.0
 * @see Acquisition
 */
public interface LongAcquisition extends Acquisition {
    /**
     * Gets the guarded long.
     *
     * @return a value of the long
     * @since 1.0
     */
    @Contract(pure = true)
    long get();
}