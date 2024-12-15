package net.hypejet.concurrency.primitive.integer;

import net.hypejet.concurrency.Acquirable;
import org.jetbrains.annotations.NotNull;

/**
 * Represents {@linkplain Acquirable an acquirable}, which guards an integer.
 *
 * @since 1.0
 * @see Acquirable
 */
public final class IntegerAcquirable extends Acquirable<IntegerAcquisition> {

    private int value;

    /**
     * Constructs the {@linkplain IntegerAcquisitionImpl integer acquisition} with an initial value
     * of {@code 0}.
     *
     * @since 1.0
     */
    public IntegerAcquirable() {
        this(0);
    }

    /**
     * Constructs the {@linkplain IntegerAcquisitionImpl integer acquisition}.
     *
     * @param value an initial value that the acquirable should have
     * @since 1.0
     */
    public IntegerAcquirable(int value) {
        this.value = value;
    }

    /**
     * Creates {@linkplain IntegerAcquisition an integer acquisition} of an integer held by this
     * {@linkplain IntegerAcquirable integer acquirable} that supports read-only operations.
     *
     * <p>If the caller thread has already created an acquisition a special implementation is used, which reuses it,
     * does nothing when {@link IntegerAcquisition#close()} is called and always returns {@code true} when
     * {@link IntegerAcquisition#isUnlocked()} is called.</p>
     *
     * <p>If the acquisition needs to be unlocked the already existing acquisition needs to be used to do that.</p>
     *
     * @return the acquisition
     * @since 1.0
     */
    @Override
    public @NotNull IntegerAcquisition acquireRead() {
        IntegerAcquisition foundAcquisition = this.findAcquisition();
        if (foundAcquisition != null)
            return new ReusedIntegerAcquisition<>(foundAcquisition);
        return new IntegerAcquisitionImpl(this);
    }

    /**
     * Creates {@linkplain WriteIntegerAcquisition a write integer acquisition} of an integer held by
     * this {@linkplain IntegerAcquirable integer acquirable} that supports write operations.
     *
     * <p>If the caller thread has already created a write acquisition a special implementation is used, which
     * reuses it, does nothing when {@link IntegerAcquisition#close()} is called and always returns {@code true} when
     * {@link IntegerAcquisition#isUnlocked()} is called.</p>
     *
     * <p>If the acquisition needs to be unlocked the already existing acquisition needs to be used to do that.</p>
     *
     * @return the acquisition
     * @since 1.0
     * @throws IllegalArgumentException if the caller thread has already created an acquisition, but it is not a write
     *                                  acquisition
     */
    @Override
    public @NotNull WriteIntegerAcquisition acquireWrite() {
        IntegerAcquisition foundAcquisition = this.findAcquisition();
        if (foundAcquisition == null)
            return new WriteIntegerAcquisitionImpl(this);

        if (!(foundAcquisition instanceof WriteIntegerAcquisition writeAcquisition)) {
            throw new IllegalArgumentException("The caller thread has already created an acquisition," +
                    "but it is not a write acquisition");
        }
        return new ReusedWriteIntegerAcquisition(writeAcquisition);
    }

    /**
     * Represents an implementation of {@linkplain AbstractIntegerAcquisition an abstract integer acquisition}.
     *
     * @since 1.0
     * @see AbstractIntegerAcquisition
     */
    private static final class IntegerAcquisitionImpl extends AbstractIntegerAcquisition {
        /**
         * Constructs the {@linkplain IntegerAcquisitionImpl integer acquisition implementation}.
         *
         * @param acquirable an acquirable integer whose value is guarded by the lock
         * @since 1.0
         */
        private IntegerAcquisitionImpl(@NotNull IntegerAcquirable acquirable) {
            super(acquirable, AcquisitionType.READ);
        }
    }

    /**
     * Represents an implementation of {@linkplain AbstractIntegerAcquisition an abstract integer acquisition}
     * and {@linkplain WriteIntegerAcquisition a write integer acquisition}.
     *
     * @since 1.0
     * @see WriteIntegerAcquisition
     * @see AbstractIntegerAcquisition
     */
    private static final class WriteIntegerAcquisitionImpl extends AbstractIntegerAcquisition
            implements WriteIntegerAcquisition {
        /**
         * Constructs the {@linkplain WriteIntegerAcquisitionImpl write integer acquisition implementation}.
         *
         * @param acquirable an acquirable integer whose value is guarded by the lock
         * @since 1.0
         */
        private WriteIntegerAcquisitionImpl(@NotNull IntegerAcquirable acquirable) {
            // There is no need to check whether the acquirable is null, the superclass will do that for us
            super(acquirable, AcquisitionType.WRITE);
        }

        @Override
        public void set(int value) {
            this.runChecks();
            this.acquirable.value = value;
        }
    }

    /**
     * Represents a common implementation of {@linkplain Acquirable.AbstractAcquisition an abstract acquisition}
     * and {@linkplain IntegerAcquisition an integer acquisition}.
     *
     * @since 1.0
     * @see IntegerAcquisition
     * @see Acquirable.AbstractAcquisition
     */
    private static abstract class AbstractIntegerAcquisition
            extends AbstractAcquisition<IntegerAcquisition, IntegerAcquirable> implements IntegerAcquisition {
        /**
         * Constructs the {@linkplain AbstractIntegerAcquisition abstract integer acquisition}.
         *
         * @param acquirable an acquirable whose integer is guarded by the lock
         * @param type a type, of which the acquisition should be
         * @since 1.0
         */
        protected AbstractIntegerAcquisition(@NotNull IntegerAcquirable acquirable, @NotNull AcquisitionType type) {
            // There is no need to check whether the acquirable is null, the superclass will do that for us
            super(acquirable, type);
        }

        @Override
        public final int get() {
            this.runChecks();
            return this.acquirable.value;
        }

        @Override
        protected final @NotNull IntegerAcquisition cast() {
            return this;
        }
    }

    /**
     * Represents {@linkplain ReusedIntegerAcquisition a reused integer acquisition}, which reuses an already existing
     * {@linkplain WriteIntegerAcquisition write integer acquisition}.
     *
     * @since 1.
     * @see WriteIntegerAcquisition
     * @see ReusedIntegerAcquisition
     */
    private static final class ReusedWriteIntegerAcquisition extends ReusedIntegerAcquisition<WriteIntegerAcquisition>
            implements WriteIntegerAcquisition {
        /**
         * Constructs the {@linkplain ReusedWriteIntegerAcquisition reused write integer acquisition}.
         *
         * @param originalAcquisition an original acquisition to create the reused acquisition with
         * @since 1.0
         */
        private ReusedWriteIntegerAcquisition(@NotNull WriteIntegerAcquisition originalAcquisition) {
            // There is no need to do nullability checks, the superclass will do that for us
            super(originalAcquisition);
        }

        @Override
        public void set(int value) {
            this.originalAcquisition.set(value);
        }
    }

    /**
     * Represents an implementation of {@linkplain ReusedAcquisition a reused acquisition} and
     * {@linkplain IntegerAcquisition an integer acquisition}.
     *
     * @param <A> a type of the acquisition that is being reused
     * @since 1.0
     * @see IntegerAcquisition
     * @see ReusedAcquisition
     */
    private static class ReusedIntegerAcquisition<A extends IntegerAcquisition> extends ReusedAcquisition<A>
            implements IntegerAcquisition {
        /**
         * Constructs the {@linkplain ReusedIntegerAcquisition reused integer acquisition}.
         *
         * @param originalAcquisition an original acquisition to create the reused acquisition with
         * @since 1.0
         */
        private ReusedIntegerAcquisition(@NotNull A originalAcquisition) {
            // There is no need to do nullability checks, the superclass will do that for us
            super(originalAcquisition);
        }

        @Override
        public final int get() {
            return this.originalAcquisition.get();
        }
    }
}