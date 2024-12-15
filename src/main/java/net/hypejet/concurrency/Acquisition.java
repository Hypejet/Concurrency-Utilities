package net.hypejet.concurrency;

import org.jetbrains.annotations.Contract;

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
}