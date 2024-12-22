package net.hypejet.concurrency;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * Represents an acquisition of a {@linkplain java.util.concurrent.locks.Lock lock}. Acquisitions allow getting
 * states guarded by their locks safely in multithreaded environments.
 *
 * <p>{@linkplain #close() A close method} should be called when the state was consumed and the work using it
 * has finished.</p>
 *
 * @since 1.0
 */
public interface Acquisition extends AutoCloseable {
    /**
     * Gets whether the {@linkplain Acquisition acquisition} has been unlocked.
     *
     * @return {@code true} if the acquisition has been unlocked, {@code false} otherwise
     * @since 1.0
     * @throws IllegalArgumentException if the caller thread does not own the acquisition
     */
    @Contract(pure = true)
    boolean isUnlocked();

    /**
     * Unlocks the {@linkplain Acquisition acquisition}, does nothing if the acquisition has been already unlocked.
     *
     * @since 1.0
     * @throws IllegalArgumentException if the caller thread does not own the acquisition
     */
    @Override
    void close();

    /**
     * Ensures whether the caller thread is permitted to do operations using this acquisition and the acquisition
     * has not been unlocked.
     *
     * @since 1.0
     * @throws IllegalArgumentException if the caller thread is not permitted to do operations using this acquisition
     * @throws IllegalStateException if the acquisition has been unlocked
     */
    void ensurePermittedAndLocked();

    /**
     * Gets {@linkplain AcquisitionType an acquisition type} of this acquisition.
     *
     * @return the acquisition type
     * @since 1.0
     */
    @Contract(pure = true)
    @NotNull AcquisitionType acquisitionType();

    /**
     * Represents a type of {@linkplain Acquisition an acquisition}.
     *
     * @since 1.0
     * @see Acquisition
     */
    enum AcquisitionType {
        /**
         * {@linkplain AcquisitionType An acquisition type} indicating that the acquisition supports read-only
         * operations.
         *
         * @since 1.0
         */
        READ,
        /**
         * {@linkplain AcquisitionType An acquisition type} indicating that the acquisition supports all kinds
         * of operations.
         *
         * @since 1.0
         */
        WRITE
    }
}