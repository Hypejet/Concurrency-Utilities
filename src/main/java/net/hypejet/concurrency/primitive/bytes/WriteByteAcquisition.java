package net.hypejet.concurrency.primitive.bytes;

/**
 * Represents {@linkplain ByteAcquisition a byte acquisition} that allows for a byte, which is guarded to be updated.
 *
 * @since 1.0
 * @see ByteAcquisition
 */
public interface WriteByteAcquisition extends ByteAcquisition {
    /**
     * Updates the byte.
     *
     * @param value a value to replace the byte with
     * @since 1.0
     */
    void set(byte value);
}