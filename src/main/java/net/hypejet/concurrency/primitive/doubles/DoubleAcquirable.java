package net.hypejet.concurrency.primitive.doubles;

import net.hypejet.concurrency.Acquirable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents {@linkplain Acquirable an acquirable}, which guards a double.
 *
 * @since 1.0
 * @see Acquirable
 */
public final class DoubleAcquirable extends Acquirable<DoubleAcquisition, WriteDoubleAcquisition> {

    private double value;

    /**
     * Constructs the {@linkplain DoubleAcquisitionImpl double acquisition} with an initial value
     * of {@code 0}.
     *
     * @since 1.0
     */
    public DoubleAcquirable() {
        this(0D);
    }

    /**
     * Constructs the {@linkplain DoubleAcquisitionImpl double acquisition}.
     *
     * @param value an initial value that the acquirable should have
     * @since 1.0
     */
    public DoubleAcquirable(double value) {
        this.value = value;
    }

    @Override
    protected @NotNull DoubleAcquisition createReadAcquisition() {
        return new DoubleAcquisitionImpl(this);
    }

    @Override
    protected @NotNull WriteDoubleAcquisition createWriteAcquisition() {
        return new WriteDoubleAcquisitionImpl(this);
    }

    @Override
    protected @NotNull DoubleAcquisition reuseReadAcquisition(@NotNull DoubleAcquisition originalAcquisition) {
        return new ReusedDoubleAcquisition<>(originalAcquisition);
    }

    @Override
    protected @NotNull WriteDoubleAcquisition reuseWriteAcquisition(
            @NotNull WriteDoubleAcquisition originalAcquisition
    ) {
        return new ReusedWriteDoubleAcquisition(originalAcquisition);
    }

    @Override
    protected @NotNull WriteDoubleAcquisition createUpgradedAcquisition(
            @NotNull DoubleAcquisition originalAcquisition
    ) {
        return new UpgradedDoubleAcquisition(originalAcquisition, this);
    }

    @Override
    protected @Nullable WriteDoubleAcquisition castToWriteAcquisition(@NotNull DoubleAcquisition acquisition) {
        if (acquisition instanceof WriteDoubleAcquisition castAcquisition)
            return castAcquisition;
        return null;
    }

    /**
     * Represents an implementation of {@linkplain AbstractDoubleAcquisition an abstract double acquisition}.
     *
     * @since 1.0
     * @see AbstractDoubleAcquisition
     */
    private static final class DoubleAcquisitionImpl extends AbstractDoubleAcquisition {
        /**
         * Constructs the {@linkplain DoubleAcquisitionImpl double acquisition implementation}.
         *
         * @param acquirable an acquirable double whose value is guarded by the lock
         * @since 1.0
         */
        private DoubleAcquisitionImpl(@NotNull DoubleAcquirable acquirable) {
            super(acquirable, AcquisitionType.READ);
        }
    }

    /**
     * Represents an implementation of {@linkplain AbstractDoubleAcquisition an abstract double acquisition}
     * and {@linkplain WriteDoubleAcquisition a write double acquisition}.
     *
     * @since 1.0
     * @see WriteDoubleAcquisition
     * @see AbstractDoubleAcquisition
     */
    private static final class WriteDoubleAcquisitionImpl extends AbstractDoubleAcquisition
            implements SetOperationImplementation {
        /**
         * Constructs the {@linkplain WriteDoubleAcquisitionImpl write double acquisition implementation}.
         *
         * @param acquirable an acquirable double whose value is guarded by the lock
         * @since 1.0
         */
        private WriteDoubleAcquisitionImpl(@NotNull DoubleAcquirable acquirable) {
            // There is no need to check whether the acquirable is null, the superclass will do that for us
            super(acquirable, AcquisitionType.WRITE);
        }

        @Override
        public @NotNull DoubleAcquirable acquirable() {
            return this.acquirable;
        }
    }

    /**
     * Represents a common implementation of {@linkplain AbstractAcquisition an abstract acquisition}
     * and {@linkplain DoubleAcquisition a double acquisition}.
     *
     * @since 1.0
     * @see DoubleAcquisition
     * @see AbstractAcquisition
     */
    private static abstract class AbstractDoubleAcquisition
            extends AbstractAcquisition<DoubleAcquisition, DoubleAcquirable>
            implements DoubleAcquisition {
        /**
         * Constructs the {@linkplain AbstractDoubleAcquisition abstract double acquisition}.
         *
         * @param acquirable an acquirable whose double is guarded by the lock
         * @param type a type, of which the acquisition should be
         * @since 1.0
         */
        protected AbstractDoubleAcquisition(@NotNull DoubleAcquirable acquirable, @NotNull AcquisitionType type) {
            // There is no need to check whether the acquirable is null, the superclass will do that for us
            super(acquirable, type);
        }

        @Override
        public final double get() {
            this.ensurePermittedAndLocked();
            return this.acquirable.value;
        }

        @Override
        protected final @NotNull DoubleAcquisition cast() {
            return this;
        }
    }

    /**
     * Represents an implementation of {@linkplain UpgradedAcquisition an upgraded acquisition}
     * and {@linkplain WriteDoubleAcquisition a write double acquisition}.
     *
     * @since 1.0
     * @see WriteDoubleAcquisition
     * @see UpgradedAcquisition
     */
    private static final class UpgradedDoubleAcquisition
            extends UpgradedAcquisition<DoubleAcquisition, DoubleAcquirable> implements SetOperationImplementation {
        /**
         * Constructs the {@linkplain UpgradedDoubleAcquisition upgraded double acquisition}.
         *
         * @param originalAcquisition an original acquisition that should be reused
         * @param acquirable an acquirable, which owns the acquisition that should be reused
         * @since 1.0
         */
        private UpgradedDoubleAcquisition(@NotNull DoubleAcquisition originalAcquisition,
                                          @NotNull DoubleAcquirable acquirable) {
            super(originalAcquisition, acquirable);
        }

        @Override
        public double get() {
            return this.originalAcquisition.get();
        }

        @Override
        public @NotNull DoubleAcquirable acquirable() {
            return this.acquirable;
        }
    }

    /**
     * Represents {@linkplain ReusedDoubleAcquisition a reused double acquisition}, which reuses an already existing
     * {@linkplain WriteDoubleAcquisition write double acquisition}.
     *
     * @since 1.0
     * @see WriteDoubleAcquisition
     * @see ReusedDoubleAcquisition
     */
    private static final class ReusedWriteDoubleAcquisition
            extends ReusedDoubleAcquisition<WriteDoubleAcquisition> implements WriteDoubleAcquisition {
        /**
         * Constructs the {@linkplain ReusedWriteDoubleAcquisition reused write double acquisition}.
         *
         * @param originalAcquisition an original acquisition that should be reused
         * @since 1.0
         */
        private ReusedWriteDoubleAcquisition(@NotNull WriteDoubleAcquisition originalAcquisition) {
            // There is no need to do nullability checks, the superclass will do that for us
            super(originalAcquisition);
        }

        @Override
        public void set(double value) {
            this.originalAcquisition.set(value);
        }
    }

    /**
     * Represents an implementation of {@linkplain ReusedAcquisition a reused acquisition} and
     * {@linkplain DoubleAcquisition a double acquisition}.
     *
     * @param <A> a type of the acquisition that is being reused
     * @since 1.0
     * @see DoubleAcquisition
     * @see ReusedAcquisition
     */
    private static class ReusedDoubleAcquisition<A extends DoubleAcquisition> extends ReusedAcquisition<A>
            implements DoubleAcquisition {
        /**
         * Constructs the {@linkplain ReusedDoubleAcquisition reused double acquisition}.
         *
         * @param originalAcquisition an original acquisition that should be reused
         * @since 1.0
         */
        private ReusedDoubleAcquisition(@NotNull A originalAcquisition) {
            // There is no need to do nullability checks, the superclass will do that for us
            super(originalAcquisition);
        }

        @Override
        public final double get() {
            return this.originalAcquisition.get();
        }
    }

    /**
     * Represents {@linkplain WriteDoubleAcquisition a write double acquisition} with
     * the {@linkplain WriteDoubleAcquisition#set(double) set operation} implemented.
     *
     * @since 1.0
     * @see WriteDoubleAcquisition
     */
    private interface SetOperationImplementation extends WriteDoubleAcquisition {
        @Override
        default void set(double value) {
            this.ensurePermittedAndLocked();
            this.acquirable().value = value;
        }

        /**
         * Gets {@linkplain DoubleAcquirable a double acquirable} that owns this acquisition.
         *
         * @return the double acquirable
         * @since 1.0
         */
        @NotNull DoubleAcquirable acquirable();
    }
}