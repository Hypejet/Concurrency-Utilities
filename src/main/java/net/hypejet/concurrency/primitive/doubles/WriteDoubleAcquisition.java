package net.hypejet.concurrency.primitive.doubles;

/**
 * Represents {@linkplain DoubleAcquisition a double acquisition} that allows for a double, which is guarded to
 * be updated.
 *
 * @since 1.0
 * @see DoubleAcquisition
 */
public interface WriteDoubleAcquisition extends DoubleAcquisition {
    /**
     * Updates the double.
     *
     * @param value a value to replace the double with
     * @since 1.0
     */
    void set(double value);
}