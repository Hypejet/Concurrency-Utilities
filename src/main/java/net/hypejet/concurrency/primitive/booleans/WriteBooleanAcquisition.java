package net.hypejet.concurrency.primitive.booleans;

/**
 * Represents {@linkplain BooleanAcquisition a boolean acquisition} that allows for a boolean, which is guarded to
 * be updated.
 *
 * @since 1.0
 * @see BooleanAcquisition
 */
public interface WriteBooleanAcquisition extends BooleanAcquisition {
    /**
     * Updates the boolean.
     *
     * @param value a value to replace the boolean with
     * @since 1.0
     */
    void set(boolean value);
}