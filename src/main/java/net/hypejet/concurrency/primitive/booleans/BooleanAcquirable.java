package net.hypejet.concurrency.primitive.booleans;

import net.hypejet.concurrency.Acquirable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents {@linkplain Acquirable an acquirable}, which guards a boolean.
 *
 * @since 1.0
 * @see Acquirable
 */
public final class BooleanAcquirable extends Acquirable<BooleanAcquisition, WriteBooleanAcquisition> {

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

    @Override
    protected @NotNull BooleanAcquisition createReadAcquisition() {
        return new BooleanAcquisitionImpl(this);
    }

    @Override
    protected @NotNull WriteBooleanAcquisition createWriteAcquisition() {
        return new WriteBooleanAcquisitionImpl(this);
    }

    @Override
    protected @NotNull BooleanAcquisition reuseReadAcquisition(@NotNull BooleanAcquisition originalAcquisition) {
        return new ReusedBooleanAcquisition<>(originalAcquisition);
    }

    @Override
    protected @NotNull WriteBooleanAcquisition reuseWriteAcquisition(
            @NotNull WriteBooleanAcquisition originalAcquisition
    ) {
        return new ReusedWriteBooleanAcquisition(originalAcquisition);
    }

    @Override
    protected @NotNull WriteBooleanAcquisition createUpgradedAcquisition(
            @NotNull BooleanAcquisition originalAcquisition
    ) {
        return new UpgradedBooleanAcquisition(originalAcquisition, this);
    }

    @Override
    protected @Nullable WriteBooleanAcquisition castToWriteAcquisition(@NotNull BooleanAcquisition acquisition) {
        if (acquisition instanceof WriteBooleanAcquisition castAcquisition)
            return castAcquisition;
        return null;
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
            implements SetOperationImplementation {
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
        public @NotNull BooleanAcquirable acquirable() {
            return this.acquirable;
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
            this.ensurePermittedAndLocked();
            return this.acquirable.value;
        }

        @Override
        protected final @NotNull BooleanAcquisition cast() {
            return this;
        }
    }

    /**
     * Represents an implementation of {@linkplain UpgradedAcquisition an upgraded acquisition}
     * and {@linkplain WriteBooleanAcquisition a write boolean acquisition}.
     *
     * @since 1.0
     * @see WriteBooleanAcquisition
     * @see UpgradedAcquisition
     */
    private static final class UpgradedBooleanAcquisition
            extends UpgradedAcquisition<BooleanAcquisition, BooleanAcquirable> implements SetOperationImplementation {
        /**
         * Constructs the {@linkplain UpgradedBooleanAcquisition upgraded boolean acquisition}.
         *
         * @param originalAcquisition an original acquisition that should be reused
         * @param acquirable an acquirable, which owns the acquisition that should be reused
         * @since 1.0
         */
        private UpgradedBooleanAcquisition(@NotNull BooleanAcquisition originalAcquisition,
                                           @NotNull BooleanAcquirable acquirable) {
            super(originalAcquisition, acquirable);
        }

        @Override
        public boolean get() {
            return this.originalAcquisition.get();
        }

        @Override
        public @NotNull BooleanAcquirable acquirable() {
            return this.acquirable;
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
         * @param originalAcquisition an original acquisition that should be reused
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
         * @param originalAcquisition an original acquisition that should be reused
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

    /**
     * Represents {@linkplain WriteBooleanAcquisition a write boolean acquisition} with
     * the {@linkplain WriteBooleanAcquisition#set(boolean) set operation} implemented.
     *
     * @since 1.0
     * @see WriteBooleanAcquisition
     */
    private interface SetOperationImplementation extends WriteBooleanAcquisition {
        @Override
        default void set(boolean value) {
            this.ensurePermittedAndLocked();
            this.acquirable().value = value;
        }

        /**
         * Gets {@linkplain BooleanAcquirable a boolean acquirable} that owns this acquisition.
         *
         * @return the boolean acquirable
         * @since 1.0
         */
        @NotNull BooleanAcquirable acquirable();
    }
}