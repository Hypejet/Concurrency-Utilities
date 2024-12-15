package net.hypejet.concurrency.primitive.floats;

/**
 * Represents {@linkplain FloatAcquisition a float acquisition} that allows for a float, which is guarded to
 * be updated.
 *
 * @since 1.0
 * @see FloatAcquisition
 */
public interface WriteFloatAcquisition extends FloatAcquisition {
    /**
     * Updates the float.
     *
     * @param value a value to replace the float with
     * @since 1.0
     */
    void set(float value);
}