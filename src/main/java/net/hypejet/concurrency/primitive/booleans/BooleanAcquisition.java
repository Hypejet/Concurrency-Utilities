package net.hypejet.concurrency.primitive.booleans;

import net.hypejet.concurrency.Acquisition;
import org.jetbrains.annotations.Contract;

/**
 * Represents {@linkplain Acquisition an acquisition} that allows getting a guarded boolean.
 *
 * @since 1.0
 * @see Acquisition
 */
public interface BooleanAcquisition extends Acquisition {
    /**
     * Gets the guarded boolean.
     *
     * @return a value of the boolean
     * @since 1.0
     */
    @Contract(pure = true)
    boolean get();
}