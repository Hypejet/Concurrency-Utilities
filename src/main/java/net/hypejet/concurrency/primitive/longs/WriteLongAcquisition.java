package net.hypejet.concurrency.primitive.longs;

/**
 * Represents {@linkplain LongAcquisition a long acquisition} that allows for a long, which is guarded to be updated.
 *
 * @since 1.0
 * @see LongAcquisition
 */
public interface WriteLongAcquisition extends LongAcquisition {
    /**
     * Updates the long.
     *
     * @param value a value to replace the long with
     * @since 1.0
     */
    void set(long value);
}