package net.hypejet.concurrency;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Represents something that protects a state using {@linkplain Lock a lock} that requires to be acquired to read
 * or write the state.
 *
 * @param <A> a type of acquisition that the acquirable creates
 * @since 1.0
 * @see Lock
 */
public abstract class Acquirable<A extends Acquisition> {

    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    private final Map<Thread, A> acquisitions = new IdentityHashMap<>();
    private final ReentrantLock acquisitionsLock = new ReentrantLock();

    /**
     * Gets {@linkplain Acquisition an acquisition} of this acquirable that was created by the caller thread.
     *
     * <p>Using the found acquisition is safe, since it belongs to the caller thread, and it is impossible for the
     * acquisition to be closed by another thread.</p>
     *
     * @return the acquisition, {@code null} if caller thread did not create an acquisition of this acquirable
     * @since 1.0
     */
    @Contract(pure = true)
    public final @Nullable A findAcquisition() {
        try {
            this.acquisitionsLock.lock();
            return this.acquisitions.get(Thread.currentThread());
        } finally {
            this.acquisitionsLock.unlock();
        }
    }

    /**
     * Creates a new {@linkplain Condition condition} of a write lock of this {@linkplain Acquirable acquirable}.
     *
     * @return the condition
     * @since 1.0
     */
    @Contract(pure = true)
    public final @NotNull Condition newCondition() {
        return this.lock.writeLock().newCondition();
    }

    /**
     * Creates {@linkplain Acquisition an acquisition} of a state held by this {@linkplain Acquirable acquirable}
     * that supports read-only operations.
     *
     * @return the acquisition
     * @since 1.0
     */
    public abstract @NotNull A acquireRead();

    /**
     * Creates {@linkplain Acquisition an acquisition} of a state held by this {@linkplain Acquirable acquirable}
     * that supports write operations.
     *
     * @return the acquisition
     * @since 1.0
     */
    public abstract @NotNull A acquireWrite();

    /**
     * Registers an acquisition.
     *
     * @param acquisition the acquisition
     * @since 1.0
     * @throws IllegalArgumentException if an acquisition for current thread has been already registered
     */
    private void registerAcquisition(@NotNull A acquisition) {
        try {
            this.acquisitionsLock.lock();
            // There is no need for a nullability check, the validate method will do that for us
            AbstractAcquisition<?, ?> validatedAcquisition = this.validate(acquisition);

            Thread owner = validatedAcquisition.owner;
            if (this.acquisitions.containsKey(owner))
                throw new IllegalArgumentException("An acquisition for current thread has been already registered");
            this.acquisitions.put(owner, acquisition);
        } finally {
            this.acquisitionsLock.unlock();
        }
    }

    /**
     * Unregisters an acquisition.
     *
     * @param acquisition the acquisition
     * @since 1.0
     */
    private void unregisterAcquisition(@NotNull A acquisition) {
        try {
            this.acquisitionsLock.lock();

            // There is no need for a nullability check, the validate method will do that for us
            AbstractAcquisition<?, ?> validatedAcquisition = this.validate(acquisition);
            this.acquisitions.remove(validatedAcquisition.owner, acquisition);
        } finally {
            this.acquisitionsLock.unlock();
        }
    }

    /**
     * Validates whether the acquisition specified can belong to this acquirable and casts it
     * to {@linkplain AbstractAcquisition an abstract acquisition}.
     *
     * @param acquisition the acquisition to validate
     * @return the abstract acquisition
     * @since 1.0
     */
    private @NotNull AbstractAcquisition<?, ?> validate(@NotNull A acquisition) {
        Objects.requireNonNull(acquisition, "the acquisition must not be null");

        if (!(acquisition instanceof Acquirable.AbstractAcquisition<?,?> abstractAcquisition))
            throw new IllegalArgumentException("The acquisition specified must extend an abstract acquisition");
        if (abstractAcquisition.acquirable != this)
            throw new IllegalArgumentException("The acquisition specified belongs to another acquirable");

        return abstractAcquisition;
    }

    /**
     * Represents an abstract implementation of {@linkplain Acquisition acquisition}.
     *
     * @param <AN> a type of acquisition that the following acquirable
     * @param <AE> a type of acquirable that the acquisition should be registered in
     * @since 1.0
     * @see Acquisition
     */
    protected static abstract class AbstractAcquisition<AN extends Acquisition, AE extends Acquirable<AN>>
            implements Acquisition {

        protected final AE acquirable;

        private final AcquisitionType acquisitionType;
        private final Lock lock;
        private final Thread owner;

        private boolean unlocked;

        /**
         * Constructs the {@linkplain AbstractAcquisition abstract acquisition}.
         *
         * @param acquirable an acquirable whose state is guarded by the lock
         * @param type a type, of which the acquisition should be
         * @since 1.0
         */
        protected AbstractAcquisition(@NotNull AE acquirable, @NotNull AcquisitionType type) {
            this.acquirable = Objects.requireNonNull(acquirable, "The acquirable must not be null");
            this.acquisitionType = Objects.requireNonNull(type, "The type must not be null");

            // Java for some reason needs a cast of the acquirable to access private methods and fields
            Acquirable<AN> castAcquirable = acquirable;

            ReentrantReadWriteLock acquirableLock = castAcquirable.lock;
            this.lock = switch (type) {
                case READ -> acquirableLock.readLock();
                case WRITE -> acquirableLock.writeLock();
            };

            this.owner = Thread.currentThread();
            castAcquirable.registerAcquisition(this.safeCast());

            this.lock.lock();
        }

        @Override
        public final void close() {
            this.ensurePermittedAndLocked();

            if (this.unlocked) return;
            this.unlocked = true;

            // Java for some reason needs a cast of the acquirable
            Acquirable<AN> castAcquirable = this.acquirable;
            castAcquirable.unregisterAcquisition(this.safeCast());

            this.lock.unlock();
        }

        @Override
        public final boolean isUnlocked() {
            this.checkCallerThread();
            return this.unlocked;
        }

        @Override
        public final void ensurePermittedAndLocked() {
            this.checkCallerThread();
            if (this.unlocked)
                throw new IllegalStateException("The acquisition has been already unlocked");
        }

        @Override
        public final @NotNull AcquisitionType acquisitionType() {
            return this.acquisitionType;
        }

        /**
         * Checks whether the caller thread owns this acquisition.
         *
         * @since 1.0
         */
        private void checkCallerThread() {
            if (Thread.currentThread() != this.owner)
                throw new IllegalArgumentException("The caller thread does not own the acquisition");
        }

        /**
         * Safely casts this acquisition to an acquisition supported by {@linkplain Acquirable an acquirable} that owns
         * this acquisition.
         *
         * @return the acquirable
         * @since 1.0
         * @throws IllegalArgumentException if the cast acquisition does not refer to the same instance as {@code this}
         */
        protected final @NotNull AN safeCast() {
            AN cast = this.cast();
            if (cast != this)
                throw new IllegalArgumentException("The cast acquisition must refer to the same instance as \"this\"");
            return cast;
        }

        /**
         * Casts this acquisition to an acquisition supported by {@linkplain Acquirable an acquirable} that owns
         * this acquisition.
         *
         * <p>In most cases {@link #safeCast()} should be used instead of directly calling this method.</p>
         *
         * @return the cast acquisition, must refer to the same instance as {@code this}
         * @since 1.0
         */
        protected abstract @NotNull AN cast();
    }

    /**
     * Represents an abstract implementation of {@linkplain Acquisition an acquisition}, which reuses
     * {@linkplain Acquisition an acquisition}, which has been already created.
     *
     * @param <A> a type of the acquisition that is being reused
     * @since 1.0
     * @see Acquisition
     */
    protected static class ReusedAcquisition<A extends Acquisition> implements Acquisition {

        protected final A originalAcquisition;

        /**
         * Constructs the {@linkplain ReusedAcquisition reused acquisition}.
         *
         * @param originalAcquisition an original acquisition to create the reused acquisition with
         * @since 1.0
         */
        protected ReusedAcquisition(@NotNull A originalAcquisition) {
            this.originalAcquisition = Objects.requireNonNull(originalAcquisition, "original acquisition");
        }

        @Override
        public final boolean isUnlocked() {
            // Assume that the acquisition has been unlocked, since it relies on another acquisition
            return true;
        }

        @Override
        public final void close() {
            // NOOP
        }

        @Override
        public final void ensurePermittedAndLocked() {
            this.originalAcquisition.ensurePermittedAndLocked();
        }

        @Override
        public final @NotNull AcquisitionType acquisitionType() {
            return this.originalAcquisition.acquisitionType();
        }
    }
}