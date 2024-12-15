package net.hypejet.concurrency.primitive.booleans;

import net.hypejet.concurrency.Acquirable;
import org.jetbrains.annotations.NotNull;

/**
 * Represents {@linkplain Acquirable an acquirable}, which guards a boolean.
 *
 * @since 1.0
 * @see Acquirable
 */
public final class BooleanAcquirable extends Acquirable<BooleanAcquisition> {

    private boolean value;

    /**
     * Constructs the {@linkplain BooleanAcquisitionImpl boolean acquisition} with an initial value
     * of {@code false}.
     *
     * @since 1.0
     */
    public BooleanAcquirable() {
        this(false);
    }

    /**
     * Constructs the {@linkplain BooleanAcquisitionImpl boo acquisition}.
     *
     * @param value an initial value that the acquirable should have
     * @since 1.0
     */
    public BooleanAcquirable(boolean value) {
        this.value = value;
    }

    /**
     * Creates {@linkplain BooleanAcquisition a boolean acquisition} of a boolean held by this
     * {@linkplain BooleanAcquirable boolean acquirable} that supports read-only operations.
     *
     * <p>If the caller thread has already created an acquisition a special implementation is used, which reuses it,
     * does nothing when {@link BooleanAcquisition#close()} is called and always returns {@code true} when
     * {@link BooleanAcquisition#isUnlocked()} is called.</p>
     *
     * <p>If the acquisition needs to be unlocked the already existing acquisition needs to be used to do that.</p>
     *
     * @return the acquisition
     * @since 1.0
     */
    @Override
    public @NotNull BooleanAcquisition acquireRead() {
        BooleanAcquisition foundAcquisition = this.findAcquisition();
        if (foundAcquisition != null)
            return new ReusedBooleanAcquisition<>(foundAcquisition);
        return new BooleanAcquisitionImpl(this);
    }

    /**
     * Creates {@linkplain WriteBooleanAcquisition a write boolean acquisition} of a boolean held by
     * this {@linkplain BooleanAcquirable boolean acquirable} that supports write operations.
     *
     * <p>If the caller thread has already created a write acquisition a special implementation is used, which
     * reuses it, does nothing when {@link BooleanAcquisition#close()} is called and always returns {@code true} when
     * {@link BooleanAcquisition#isUnlocked()} is called.</p>
     *
     * <p>If the acquisition needs to be unlocked the already existing acquisition needs to be used to do that.</p>
     *
     * @return the acquisition
     * @since 1.0
     * @throws IllegalArgumentException if the caller thread has already created an acquisition, but it is not a write
     *                                  acquisition
     */
    @Override
    public @NotNull WriteBooleanAcquisition acquireWrite() {
        BooleanAcquisition foundAcquisition = this.findAcquisition();
        if (foundAcquisition == null)
            return new WriteBooleanAcquisitionImpl(this);

        if (!(foundAcquisition instanceof WriteBooleanAcquisition writeAcquisition)) {
            throw new IllegalArgumentException("The caller thread has already created an acquisition," +
                    "but it is not a write acquisition");
        }
        return new ReusedWriteBooleanAcquisition(writeAcquisition);
    }

    /**
     * Represents an implementation of {@linkplain AbstractBooleanAcquisition an abstract boolean acquisition}.
     *
     * @since 1.0
     * @see AbstractBooleanAcquisition
     */
    private static final class BooleanAcquisitionImpl extends AbstractBooleanAcquisition {
        /**
         * Constructs the {@linkplain BooleanAcquisitionImpl boolean acquisition implementation}.
         *
         * @param acquirable an acquirable boolean whose value is guarded by the lock
         * @since 1.0
         */
        private BooleanAcquisitionImpl(@NotNull BooleanAcquirable acquirable) {
            super(acquirable, AcquisitionType.READ);
        }
    }

    /**
     * Represents an implementation of {@linkplain AbstractBooleanAcquisition an abstract boolean acquisition}
     * and {@linkplain WriteBooleanAcquisition a write boolean acquisition}.
     *
     * @since 1.0
     * @see WriteBooleanAcquisition
     * @see AbstractBooleanAcquisition
     */
    private static final class WriteBooleanAcquisitionImpl extends AbstractBooleanAcquisition
            implements WriteBooleanAcquisition {
        /**
         * Constructs the {@linkplain WriteBooleanAcquisitionImpl write boolean acquisition implementation}.
         *
         * @param acquirable an acquirable boolean whose value is guarded by the lock
         * @since 1.0
         */
        private WriteBooleanAcquisitionImpl(@NotNull BooleanAcquirable acquirable) {
            // There is no need to check whether the acquirable is null, the superclass will do that for us
            super(acquirable, AcquisitionType.WRITE);
        }

        @Override
        public void set(boolean value) {
            this.runChecks();
            this.acquirable.value = value;
        }
    }

    /**
     * Represents a common implementation of {@linkplain AbstractAcquisition an abstract acquisition}
     * and {@linkplain BooleanAcquisition a boolean acquisition}.
     *
     * @since 1.0
     * @see BooleanAcquisition
     * @see AbstractAcquisition
     */
    private static abstract class AbstractBooleanAcquisition
            extends AbstractAcquisition<BooleanAcquisition, BooleanAcquirable> implements BooleanAcquisition {
        /**
         * Constructs the {@linkplain AbstractBooleanAcquisition abstract boolean acquisition}.
         *
         * @param acquirable an acquirable whose boolean is guarded by the lock
         * @param type a type, of which the acquisition should be
         * @since 1.0
         */
        protected AbstractBooleanAcquisition(@NotNull BooleanAcquirable acquirable, @NotNull AcquisitionType type) {
            // There is no need to check whether the acquirable is null, the superclass will do that for us
            super(acquirable, type);
        }

        @Override
        public final boolean get() {
            this.runChecks();
            return this.acquirable.value;
        }

        @Override
        protected final @NotNull BooleanAcquisition cast() {
            return this;
        }
    }

    /**
     * Represents {@linkplain ReusedBooleanAcquisition a reused boolean acquisition}, which reuses an already existing
     * {@linkplain WriteBooleanAcquisition write boolean acquisition}.
     *
     * @since 1.0
     * @see WriteBooleanAcquisition
     * @see ReusedBooleanAcquisition
     */
    private static final class ReusedWriteBooleanAcquisition
            extends ReusedBooleanAcquisition<WriteBooleanAcquisition>
            implements WriteBooleanAcquisition {
        /**
         * Constructs the {@linkplain ReusedWriteBooleanAcquisition reused write boolean acquisition}.
         *
         * @param originalAcquisition an original acquisition to create the reused acquisition with
         * @since 1.0
         */
        private ReusedWriteBooleanAcquisition(@NotNull WriteBooleanAcquisition originalAcquisition) {
            // There is no need to do nullability checks, the superclass will do that for us
            super(originalAcquisition);
        }

        @Override
        public void set(boolean value) {
            this.originalAcquisition.set(value);
        }
    }

    /**
     * Represents an implementation of {@linkplain ReusedAcquisition a reused acquisition} and
     * {@linkplain BooleanAcquisition a boolean acquisition}.
     *
     * @param <A> a type of the acquisition that is being reused
     * @since 1.0
     * @see BooleanAcquisition
     * @see ReusedAcquisition
     */
    private static class ReusedBooleanAcquisition<A extends BooleanAcquisition> extends ReusedAcquisition<A>
            implements BooleanAcquisition {
        /**
         * Constructs the {@linkplain ReusedBooleanAcquisition reused boolean acquisition}.
         *
         * @param originalAcquisition an original acquisition to create the reused acquisition with
         * @since 1.0
         */
        private ReusedBooleanAcquisition(@NotNull A originalAcquisition) {
            // There is no need to do nullability checks, the superclass will do that for us
            super(originalAcquisition);
        }

        @Override
        public final boolean get() {
            return this.originalAcquisition.get();
        }
    }
}