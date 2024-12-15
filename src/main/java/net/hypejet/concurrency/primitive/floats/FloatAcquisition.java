package net.hypejet.concurrency.primitive.floats;

import net.hypejet.concurrency.Acquisition;
import org.jetbrains.annotations.Contract;

/**
 * Represents {@linkplain Acquisition an acquisition} that allows getting a guarded float.
 *
 * @since 1.0
 * @see Acquisition
 */
public interface FloatAcquisition extends Acquisition {
    /**
     * Gets the guarded float.
     *
     * @return a value of the float
     * @since 1.0
     */
    @Contract(pure = true)
    float get();
}