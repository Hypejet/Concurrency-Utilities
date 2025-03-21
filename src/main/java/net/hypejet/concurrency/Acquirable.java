package net.hypejet.concurrency;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.StampedLock;

/**
 * Represents something that protects a state using {@linkplain Lock a lock} that requires to be acquired to read
 * or write the state.
 *
 * @param <A> a type of acquisition that the acquirable creates
 * @param <WA> a type of write acquisition that the acquirable creates
 * @since 1.0
 * @see Lock
 */
public abstract class Acquirable<A extends Acquisition, WA extends A> {

    private final Map<Thread, A> acquisitions = new ConcurrentHashMap<>();
    private final StampedLock lock = new StampedLock();

    /**
     * Creates {@linkplain A an acquisition} of state held by this {@linkplain Acquirable acquirable} that supports
     * read-only operations.
     *
     * <p>If the caller thread has already created an acquisition, a special implementation is used, which reuses it,
     * does nothing when {@link Acquisition#close()} is called and always returns {@code true} when
     * {@link Acquisition#isUnlocked()} is called.</p>
     *
     * <p>If the reused acquisition needs to be unlocked the already existing acquisition needs to be used to do
     * that.</p>
     *
     * @return the acquisition
     * @since 1.0
     */
    @Contract(pure = true)
    public final @NotNull A acquireRead() {
        A foundAcquisition = this.acquisitions.get(Thread.currentThread());
        if (foundAcquisition != null) {
            A reusedAcquisition = this.reuseReadAcquisition(foundAcquisition);
            ensureReused(reusedAcquisition);
            return reusedAcquisition;
        }

        A createdAcquisition = this.createReadAcquisition();
        AbstractAcquisition<?, ?> validatedAcquisition = this.validate(createdAcquisition);
        this.acquisitions.put(validatedAcquisition.owner, createdAcquisition);
        return createdAcquisition;
    }

    /**
     * Creates {@linkplain WA a write acquisition} of a state held by this {@linkplain Acquirable acquirable} that
     * supports write operations.
     *
     * <p>If the caller thread has already created a write acquisition, a special implementation is used, which
     * reuses it, does nothing when {@link Acquisition#close()} is called and always returns {@code true} when
     * {@link Acquisition#isUnlocked()} is called.</p>
     *
     * <p>If the caller thread has already created a read acquisition, lock of the read acquisition is upgraded to
     * a write lock and a special implementation is used, which reuses the read acquisition, does nothing when
     * {@link Acquisition#close()} is called and always returns {@code true} when {@link Acquisition#isUnlocked()}
     * is called.</p>
     *
     * <p>The return value may be ignored in case when a read acquisition has been already acquired and an upgrade
     * to write lock is needed.</p>
     *
     * <p>If the reused acquisition needs to be unlocked the already existing acquisition needs to be used to do
     * that.</p>
     *
     * @return the acquisition
     * @since 1.0
     */
    public final @NotNull WA acquireWrite() {
        A foundAcquisition = this.acquisitions.get(Thread.currentThread());
        if (foundAcquisition == null) {
            WA createdAcquisition = this.createWriteAcquisition();
            AbstractAcquisition<?, ?> validatedAcquisition = this.validate(createdAcquisition);
            this.acquisitions.put(validatedAcquisition.owner, createdAcquisition);
            return createdAcquisition;
        }

        WA castAcquisition = this.castToWriteAcquisition(foundAcquisition);
        if (castAcquisition != null) {
            WA reusedAcquisition = this.reuseWriteAcquisition(castAcquisition);
            ensureReused(reusedAcquisition);
            return reusedAcquisition;
        }

        WA upgrade = this.createUpgradedAcquisition(foundAcquisition);
        ensureUpgraded(upgrade);
        return upgrade;
    }

    /**
     * Creates a new acquisition, which supports read-only operations.
     *
     * @return the acquisition created
     * @since 1.0
     */
    protected abstract @NotNull A createReadAcquisition();

    /**
     * Creates a new acquisition, which supports write operations.
     *
     * @return the acquisition created
     * @since 1.0
     */
    protected abstract @NotNull WA createWriteAcquisition();

    /**
     * Creates a reused acquisition of the read acquisition specified.
     *
     * <p>The created acquisition must extend {@linkplain ReusedAcquisition reused acquisition}.</p>
     *
     * @param originalAcquisition the acquisition to reuse
     * @return the reused acquisition created
     * @since 1.0
     */
    protected abstract @NotNull A reuseReadAcquisition(@NotNull A originalAcquisition);

    /**
     * Creates a reused acquisition of the write acquisition specified.
     *
     * <p>The created acquisition must extend {@linkplain ReusedAcquisition reused acquisition}.</p>
     *
     * @param originalAcquisition the acquisition to reuse
     * @return the reused acquisition created
     * @since 1.0
     */
    protected abstract @NotNull WA reuseWriteAcquisition(@NotNull WA originalAcquisition);

    /**
     * Creates a reused acquisition of the read acquisition specified, however, the reused acquisition should support
     * write operations. Lock of the read acquisition has been upgraded to a write lock.
     *
     * <p>The created acquisition must extend {@linkplain UpgradedAcquisition upgraded acquisition}.</p>
     *
     * @param originalAcquisition the acquisition to upgrade
     * @return the upgraded acquisition created
     * @since 1.0
     */
    protected abstract @NotNull WA createUpgradedAcquisition(@NotNull A originalAcquisition);

    /**
     * Casts the acquisition specified to a write acquisition.
     *
     * @param acquisition the acquisition to cast
     * @return the write acquisition, {@code null} if the acquisition specified is not a write acquisition
     * @since 1.0
     */
    protected abstract @Nullable WA castToWriteAcquisition(@NotNull A acquisition);

    /**
     * Unregisters an acquisition.
     *
     * @param acquisition the acquisition to unregister
     * @since 1.0
     */
    private void unregisterAcquisition(@NotNull A acquisition) {
        // There is no need for a nullability check, the validate method will do that for us
        AbstractAcquisition<?, ?> validatedAcquisition = this.validate(acquisition);
        this.acquisitions.remove(validatedAcquisition.owner, acquisition);
    }

    /**
     * Validates whether the acquisition specified can belong to this acquirable and casts it
     * to {@linkplain AbstractAcquisition an abstract acquisition}.
     *
     * @param acquisition the acquisition to validate
     * @return the abstract acquisition
     * @since 1.0
     */
    private @NotNull AbstractAcquisition<?, ?> validate(@NotNull Acquisition acquisition) {
        Objects.requireNonNull(acquisition, "the acquisition must not be null");

        if (!(acquisition instanceof Acquirable.AbstractAcquisition<?,?> abstractAcquisition))
            throw new IllegalArgumentException("The acquisition specified must extend an abstract acquisition");
        if (abstractAcquisition.acquirable != this)
            throw new IllegalArgumentException("The acquisition specified belongs to another acquirable");

        return abstractAcquisition;
    }

    /**
     * Ensures that {@linkplain Acquisition an acquisition} specified is
     * {@linkplain ReusedAcquisition a reused acquisition}.
     *
     * @param acquisition the acquisition to ensure that is a reused acquisition
     * @since 1.0
     */
    private static void ensureReused(@NotNull Acquisition acquisition) {
        Objects.requireNonNull(acquisition, "The acquisition must not be null");
        if (!(acquisition instanceof Acquirable.ReusedAcquisition<?>))
            throw new IllegalArgumentException("The acquisition specified must extend a reused acquisition");
    }

    /**
     * Ensures that {@linkplain Acquisition an acquisition} specified is
     * {@linkplain UpgradedAcquisition an upgraded acquisition}.
     *
     * @param acquisition the acquisition to ensure that is an upgraded acquisition
     * @since 1.0
     */
    private static void ensureUpgraded(@NotNull Acquisition acquisition) {
        Objects.requireNonNull(acquisition, "The acquisition must not be null");
        if (!(acquisition instanceof Acquirable.UpgradedAcquisition<?, ?>))
            throw new IllegalArgumentException("The acquisition specified must extend an upgrade acquisition");
    }

    /**
     * Represents an abstract implementation of {@linkplain Acquisition acquisition}.
     *
     * @param <AN> a type of acquisition that the following acquirable
     * @param <AE> a type of acquirable that the acquisition should be registered in
     * @since 1.0
     * @see Acquisition
     */
    protected static abstract class AbstractAcquisition<AN extends Acquisition, AE extends Acquirable<AN, ?>>
            implements Acquisition {

        protected final AE acquirable;

        private final Thread owner;
        private final AcquisitionType type;

        private long lockStamp;
        private boolean unlocked;

        private int upgradedLockCount;

        /**
         * Constructs the {@linkplain AbstractAcquisition abstract acquisition}.
         *
         * @param acquirable an acquirable whose state is guarded by the lock
         * @param type a type, of which the acquisition should be
         * @since 1.0
         */
        protected AbstractAcquisition(@NotNull AE acquirable, @NotNull AcquisitionType type) {
            this.acquirable = Objects.requireNonNull(acquirable, "The acquirable must not be null");

            this.owner = Thread.currentThread();
            this.type = Objects.requireNonNull(type, "The type must not be null");

            // Java for some reason needs a cast of the acquirable to access private methods and fields
            Acquirable<AN, ?> castAcquirable = acquirable;
            StampedLock acquirableLock = castAcquirable.lock;

            this.lockStamp = switch (type) {
                case READ -> acquirableLock.readLock();
                case WRITE -> acquirableLock.writeLock();
            };
        }

        @Override
        public final void close() {
            this.checkCallerThread();

            if (this.unlocked) return;
            this.unlocked = true;

            // Java for some reason needs a cast of the acquirable to access private methods and fields
            Acquirable<AN, ?> castAcquirable = this.acquirable;
            castAcquirable.unregisterAcquisition(this.safeCast());

            castAcquirable.lock.unlock(this.lockStamp);
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
            return this.type;
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

        private void checkCallerThread() {
            if (Thread.currentThread() != this.owner)
                throw new IllegalArgumentException("The caller thread does not own the acquisition");
        }

        private void incrementUpgradedLockCount() {
            if (this.upgradedLockCount++ > 0)
                return;

            // Java for some reason needs a cast of the acquirable to access private methods and fields
            Acquirable<AN, ?> castAcquirable = this.acquirable;

            long lockStamp = castAcquirable.lock.tryConvertToWriteLock(this.lockStamp);
            if (lockStamp == 0)
                throw new IllegalStateException("Could not convert the acquisition to a write acquisition");

            this.lockStamp = lockStamp;
        }

        private void decrementUpgradedLockCount() {
            this.upgradedLockCount--;
            if (this.upgradedLockCount != 0)
                return;

            // Java for some reason needs a cast of the acquirable to access private methods and fields
            Acquirable<AN, ?> castAcquirable = this.acquirable;

            long lockStamp = castAcquirable.lock.tryConvertToReadLock(this.lockStamp);
            if (lockStamp == 0)
                throw new IllegalStateException("Could not convert the acquisition to a read acquisition");

            this.lockStamp = lockStamp;
        }
    }


    /**
     * Represents an implementation of {@linkplain Acquisition an acquisition}, which reuses
     * {@linkplain Acquisition an acquisition}, which has been already created.
     *
     * @param <A> a type of the acquisition that is being reused
     * @since 1.0
     * @see Acquisition
     */
    protected static class ReusedAcquisition<A extends Acquisition> extends AbstractReusedAcquisition<A> {
        /**
         * Constructs the {@linkplain ReusedAcquisition reused acquisition}.
         *
         * @param originalAcquisition an original acquisition should be reused
         * @since 1.0
         */
        protected ReusedAcquisition(@NotNull A originalAcquisition) {
            super(originalAcquisition);
        }

        @Override
        public final void close() {
            // NOOP
        }

        @Override
        public @NotNull AcquisitionType acquisitionType() {
            return this.originalAcquisition.acquisitionType();
        }
    }

    /**
     * Represents an implementation of {@linkplain Acquisition an acquisition}, which provides write operations
     * by reusing {@linkplain Acquisition an acquisition}
     * of {@linkplain Acquisition.AcquisitionType#READ read acquisition type}, which has been already created.
     *
     * @param <AN> a type of the acquisition that is being reused
     * @param <AE> a type of acquirable of the acquisition that is being reused
     * @since 1.0
     * @see Acquisition
     */
    protected static class UpgradedAcquisition<AN extends Acquisition, AE extends Acquirable<AN, ?>>
            extends AbstractReusedAcquisition<AN> implements Acquisition {

        protected final AE acquirable;

        /**
         * Constructs the {@linkplain UpgradedAcquisition upgraded acquisition}.
         *
         * @param originalAcquisition an original acquisition that should be reused
         * @param acquirable an acquirable, which owns the acquisition that should be reused
         * @since 1.0
         */
        protected UpgradedAcquisition(@NotNull AN originalAcquisition, @NotNull AE acquirable) {
            super(originalAcquisition);
            this.acquirable = Objects.requireNonNull(acquirable, "The acquirable must not be null");

            // Java for some reason needs a cast of the acquirable to access private methods and fields
            Acquirable<AN, ?> castAcquirable = acquirable;
            AbstractAcquisition<?, ?> validatedAcquisition = castAcquirable.validate(originalAcquisition);
            validatedAcquisition.incrementUpgradedLockCount();
        }

        @Override
        public final void close() {
            this.ensurePermittedAndLocked();

            // Java for some reason needs a cast of the acquirable to access private methods and fields
            Acquirable<AN, ?> castAcquirable = this.acquirable;
            AbstractAcquisition<?, ?> validatedAcquisition = castAcquirable.validate(this.originalAcquisition);

            validatedAcquisition.decrementUpgradedLockCount();
        }

        @Override
        public final @NotNull AcquisitionType acquisitionType() {
            return AcquisitionType.WRITE;
        }
    }

    /**
     * Represents an abstract implementation of {@linkplain Acquisition an acquisition}, which reuses
     * {@linkplain Acquisition an acquisition}, which has been already created.
     *
     * @param <A> a type of the acquisition that is being reused
     * @since 1.0
     * @see Acquisition
     */
    private static abstract class AbstractReusedAcquisition<A extends Acquisition> implements Acquisition {

        protected final A originalAcquisition;

        /**
         * Constructs the {@linkplain ReusedAcquisition reused acquisition}.
         *
         * @param originalAcquisition an original acquisition should be reused
         * @since 1.0
         */
        private AbstractReusedAcquisition(@NotNull A originalAcquisition) {
            this.originalAcquisition = Objects.requireNonNull(
                    originalAcquisition,
                    "The original acquisition must not be null"
            );
        }

        @Override
        public final boolean isUnlocked() {
            // Assume that the acquisition has been unlocked, since it relies on another acquisition
            return true;
        }

        @Override
        public final void ensurePermittedAndLocked() {
            this.originalAcquisition.ensurePermittedAndLocked();
        }
    }
}