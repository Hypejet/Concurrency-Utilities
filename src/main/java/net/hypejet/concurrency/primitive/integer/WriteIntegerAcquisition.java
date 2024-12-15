package net.hypejet.concurrency.primitive.integer;

/**
 * Represents {@linkplain IntegerAcquisition an integer acquisition} that allows for an integer, which is guarded to
 * be updated.
 *
 * @since 1.0
 * @see IntegerAcquisition
 */
public interface WriteIntegerAcquisition extends IntegerAcquisition {
    /**
     * Updates the integer.
     *
     * @param value a value to replace the integer with
     * @since 1.0
     */
    void set(int value);
}