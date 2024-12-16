package net.hypejet.concurrency.primitive.floats;

import net.hypejet.concurrency.Acquirable;
import org.jetbrains.annotations.NotNull;

/**
 * Represents {@linkplain Acquirable an acquirable}, which guards a float.
 *
 * @since 1.0
 * @see Acquirable
 */
public final class FloatAcquirable extends Acquirable<FloatAcquisition> {

    private float value;

    /**
     * Constructs the {@linkplain FloatAcquisitionImpl float acquisition} with an initial value
     * of {@code 0}.
     *
     * @since 1.0
     */
    public FloatAcquirable() {
        this(0F);
    }

    /**
     * Constructs the {@linkplain FloatAcquisitionImpl float acquisition}.
     *
     * @param value an initial value that the acquirable should have
     * @since 1.0
     */
    public FloatAcquirable(float value) {
        this.value = value;
    }

    /**
     * Creates {@linkplain FloatAcquisition a float acquisition} of a float held by this
     * {@linkplain FloatAcquirable float acquirable} that supports read-only operations.
     *
     * <p>If the caller thread has already created an acquisition a special implementation is used, which reuses it,
     * does nothing when {@link FloatAcquisition#close()} is called and always returns {@code true} when
     * {@link FloatAcquisition#isUnlocked()} is called.</p>
     *
     * <p>If the acquisition needs to be unlocked the already existing acquisition needs to be used to do that.</p>
     *
     * @return the acquisition
     * @since 1.0
     */
    @Override
    public @NotNull FloatAcquisition acquireRead() {
        FloatAcquisition foundAcquisition = this.findAcquisition();
        if (foundAcquisition != null)
            return new ReusedFloatAcquisition<>(foundAcquisition);
        return new FloatAcquisitionImpl(this);
    }

    /**
     * Creates {@linkplain WriteFloatAcquisition a write float acquisition} of a float held by
     * this {@linkplain FloatAcquirable float acquirable} that supports write operations.
     *
     * <p>If the caller thread has already created a write acquisition a special implementation is used, which
     * reuses it, does nothing when {@link FloatAcquisition#close()} is called and always returns {@code true} when
     * {@link FloatAcquisition#isUnlocked()} is called.</p>
     *
     * <p>If the acquisition needs to be unlocked the already existing acquisition needs to be used to do that.</p>
     *
     * @return the acquisition
     * @since 1.0
     * @throws IllegalArgumentException if the caller thread has already created an acquisition, but it is not a write
     *                                  acquisition
     */
    @Override
    public @NotNull WriteFloatAcquisition acquireWrite() {
        FloatAcquisition foundAcquisition = this.findAcquisition();
        if (foundAcquisition == null)
            return new WriteFloatAcquisitionImpl(this);

        if (!(foundAcquisition instanceof WriteFloatAcquisition writeAcquisition)) {
            throw new IllegalArgumentException("The caller thread has already created an acquisition," +
                    " but it is not a write acquisition");
        }
        return new ReusedWriteFloatAcquisition(writeAcquisition);
    }

    /**
     * Represents an implementation of {@linkplain AbstractFloatAcquisition an abstract float acquisition}.
     *
     * @since 1.0
     * @see AbstractFloatAcquisition
     */
    private static final class FloatAcquisitionImpl extends AbstractFloatAcquisition {
        /**
         * Constructs the {@linkplain FloatAcquisitionImpl float acquisition implementation}.
         *
         * @param acquirable an acquirable float whose value is guarded by the lock
         * @since 1.0
         */
        private FloatAcquisitionImpl(@NotNull FloatAcquirable acquirable) {
            super(acquirable, AcquisitionType.READ);
        }
    }

    /**
     * Represents an implementation of {@linkplain AbstractFloatAcquisition an abstract float acquisition}
     * and {@linkplain WriteFloatAcquisition a write float acquisition}.
     *
     * @since 1.0
     * @see WriteFloatAcquisition
     * @see AbstractFloatAcquisition
     */
    private static final class WriteFloatAcquisitionImpl extends AbstractFloatAcquisition
            implements WriteFloatAcquisition {
        /**
         * Constructs the {@linkplain WriteFloatAcquisitionImpl write float acquisition implementation}.
         *
         * @param acquirable an acquirable float whose value is guarded by the lock
         * @since 1.0
         */
        private WriteFloatAcquisitionImpl(@NotNull FloatAcquirable acquirable) {
            // There is no need to check whether the acquirable is null, the superclass will do that for us
            super(acquirable, AcquisitionType.WRITE);
        }

        @Override
        public void set(float value) {
            this.runChecks();
            this.acquirable.value = value;
        }
    }

    /**
     * Represents a common implementation of {@linkplain AbstractAcquisition an abstract acquisition}
     * and {@linkplain FloatAcquisition a float acquisition}.
     *
     * @since 1.0
     * @see FloatAcquisition
     * @see AbstractAcquisition
     */
    private static abstract class AbstractFloatAcquisition
            extends AbstractAcquisition<FloatAcquisition, FloatAcquirable>
            implements FloatAcquisition {
        /**
         * Constructs the {@linkplain AbstractFloatAcquisition abstract float acquisition}.
         *
         * @param acquirable an acquirable whose float is guarded by the lock
         * @param type a type, of which the acquisition should be
         * @since 1.0
         */
        protected AbstractFloatAcquisition(@NotNull FloatAcquirable acquirable, @NotNull AcquisitionType type) {
            // There is no need to check whether the acquirable is null, the superclass will do that for us
            super(acquirable, type);
        }

        @Override
        public final float get() {
            this.runChecks();
            return this.acquirable.value;
        }

        @Override
        protected final @NotNull FloatAcquisition cast() {
            return this;
        }
    }

    /**
     * Represents {@linkplain ReusedFloatAcquisition a reused float acquisition}, which reuses an already existing
     * {@linkplain WriteFloatAcquisition write float acquisition}.
     *
     * @since 1.0
     * @see WriteFloatAcquisition
     * @see ReusedFloatAcquisition
     */
    private static final class ReusedWriteFloatAcquisition
            extends ReusedFloatAcquisition<WriteFloatAcquisition> implements WriteFloatAcquisition {
        /**
         * Constructs the {@linkplain ReusedWriteFloatAcquisition reused write float acquisition}.
         *
         * @param originalAcquisition an original acquisition to create the reused acquisition with
         * @since 1.0
         */
        private ReusedWriteFloatAcquisition(@NotNull WriteFloatAcquisition originalAcquisition) {
            // There is no need to do nullability checks, the superclass will do that for us
            super(originalAcquisition);
        }

        @Override
        public void set(float value) {
            this.originalAcquisition.set(value);
        }
    }

    /**
     * Represents an implementation of {@linkplain ReusedAcquisition a reused acquisition} and
     * {@linkplain FloatAcquisition a float acquisition}.
     *
     * @param <A> a type of the acquisition that is being reused
     * @since 1.0
     * @see FloatAcquisition
     * @see ReusedAcquisition
     */
    private static class ReusedFloatAcquisition<A extends FloatAcquisition> extends ReusedAcquisition<A>
            implements FloatAcquisition {
        /**
         * Constructs the {@linkplain ReusedFloatAcquisition reused float acquisition}.
         *
         * @param originalAcquisition an original acquisition to create the reused acquisition with
         * @since 1.0
         */
        private ReusedFloatAcquisition(@NotNull A originalAcquisition) {
            // There is no need to do nullability checks, the superclass will do that for us
            super(originalAcquisition);
        }

        @Override
        public final float get() {
            return this.originalAcquisition.get();
        }
    }
}