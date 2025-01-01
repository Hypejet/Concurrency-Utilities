package net.hypejet.concurrency.primitive.floats;

import net.hypejet.concurrency.Acquirable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * Represents {@linkplain Acquirable an acquirable}, which guards a float.
 *
 * @since 1.0
 * @see Acquirable
 */
public final class FloatAcquirable extends Acquirable<FloatAcquisition, WriteFloatAcquisition> {

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

    @Override
    protected @NotNull FloatAcquisition createReadAcquisition() {
        return new FloatAcquisitionImpl(this);
    }

    @Override
    protected @NotNull WriteFloatAcquisition createWriteAcquisition() {
        return new WriteFloatAcquisitionImpl(this);
    }

    @Override
    protected @NotNull FloatAcquisition reuseReadAcquisition(@NotNull FloatAcquisition originalAcquisition) {
        return new ReusedFloatAcquisition<>(originalAcquisition);
    }

    @Override
    protected @NotNull WriteFloatAcquisition reuseWriteAcquisition(
            @NotNull WriteFloatAcquisition originalAcquisition
    ) {
        return new ReusedWriteFloatAcquisition(originalAcquisition);
    }

    @Override
    protected @NotNull WriteFloatAcquisition createUpgradedAcquisition(@NotNull FloatAcquisition originalAcquisition) {
        return new UpgradedFloatAcquisition(originalAcquisition, this);
    }

    @Override
    protected @Nullable WriteFloatAcquisition castToWriteAcquisition(@NotNull FloatAcquisition acquisition) {
        if (acquisition instanceof WriteFloatAcquisition castAcquisition)
            return castAcquisition;
        return null;
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
            implements WriteFloatAcquisition, SetOperationImplementation {
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
        public @NotNull FloatAcquirable acquirable() {
            return this.acquirable;
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
            this.ensurePermittedAndLocked();
            return this.acquirable.value;
        }

        @Override
        protected final @NotNull FloatAcquisition cast() {
            return this;
        }
    }

    /**
     * Represents {@linkplain ReusedFloatAcquisition a reused float acquisition}, which reuses
     * {@linkplain FloatAcquisition a float acquisition}, whose lock has been upgraded to a write lock.
     *
     * @since 1.0
     * @see FloatAcquisition
     * @see ReusedFloatAcquisition
     */
    private static final class UpgradedFloatAcquisition extends ReusedFloatAcquisition<FloatAcquisition>
            implements WriteFloatAcquisition, SetOperationImplementation {

        private final FloatAcquirable acquirable;

        /**
         * Constructs the {@linkplain UpgradedFloatAcquisition upgraded float acquisition}.
         *
         * @param originalAcquisition an original acquisition to create the reused acquisition with
         * @param acquirable an acquirable that owns the original acquisition
         * @since 1.0
         */
        private UpgradedFloatAcquisition(@NotNull FloatAcquisition originalAcquisition,
                                         @NotNull FloatAcquirable acquirable) {
            super(originalAcquisition);
            this.acquirable = Objects.requireNonNull(acquirable, "The acquirable must not be null");
        }

        @Override
        public @NotNull FloatAcquirable acquirable() {
            return this.acquirable;
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

    /**
     * Represents {@linkplain WriteFloatAcquisition a write float acquisition} with
     * the {@linkplain WriteFloatAcquisition#set(float) set operation} implemented.
     *
     * @since 1.0
     * @see WriteFloatAcquisition
     */
    private interface SetOperationImplementation extends WriteFloatAcquisition {
        @Override
        default void set(float value) {
            this.ensurePermittedAndLocked();
            this.acquirable().value = value;
        }

        /**
         * Gets {@linkplain FloatAcquirable a float acquirable} that owns this acquisition.
         *
         * @return the float acquirable
         * @since 1.0
         */
        @NotNull FloatAcquirable acquirable();
    }
}