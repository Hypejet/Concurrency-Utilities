package net.hypejet.concurrency.primitive.doubles;

import net.hypejet.concurrency.Acquisition;
import org.jetbrains.annotations.Contract;

/**
 * Represents {@linkplain Acquisition an acquisition} that allows getting a guarded double.
 *
 * @since 1.0
 * @see Acquisition
 */
public interface DoubleAcquisition extends Acquisition {
    /**
     * Gets the guarded double.
     *
     * @return a value of the double
     * @since 1.0
     */
    @Contract(pure = true)
    double get();
}