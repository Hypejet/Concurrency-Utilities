package net.hypejet.concurrency.primitive.bytes;

import net.hypejet.concurrency.Acquisition;
import org.jetbrains.annotations.Contract;

/**
 * Represents {@linkplain Acquisition an acquisition} that allows getting a guarded byte.
 *
 * @since 1.0
 * @see Acquisition
 */
public interface ByteAcquisition extends Acquisition {
    /**
     * Gets the guarded byte.
     *
     * @return a value of the byte
     * @since 1.0
     */
    @Contract(pure = true)
    byte get();
}