package net.hypejet.concurrency.primitive.shorts;

/**
 * Represents {@linkplain ShortAcquisition a short acquisition} that allows for a short, which is guarded to be updated.
 *
 * @since 1.0
 * @see ShortAcquisition
 */
public interface WriteShortAcquisition extends ShortAcquisition {
    /**
     * Updates the short.
     *
     * @param value a value to replace the short with
     * @since 1.0
     */
    void set(short value);
}