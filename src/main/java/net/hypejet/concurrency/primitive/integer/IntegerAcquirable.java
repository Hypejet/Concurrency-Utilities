package net.hypejet.concurrency.primitive.integer;

import net.hypejet.concurrency.Acquirable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents {@linkplain Acquirable an acquirable}, which guards an integer.
 *
 * @since 1.0
 * @see Acquirable
 */
public final class IntegerAcquirable extends Acquirable<IntegerAcquisition, WriteIntegerAcquisition> {

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

    @Override
    protected @NotNull IntegerAcquisition createReadAcquisition() {
        return new IntegerAcquisitionImpl(this);
    }

    @Override
    protected @NotNull WriteIntegerAcquisition createWriteAcquisition() {
        return new WriteIntegerAcquisitionImpl(this);
    }

    @Override
    protected @NotNull IntegerAcquisition reuseReadAcquisition(@NotNull IntegerAcquisition originalAcquisition) {
        return new ReusedIntegerAcquisition<>(originalAcquisition);
    }

    @Override
    protected @NotNull WriteIntegerAcquisition reuseWriteAcquisition(
            @NotNull WriteIntegerAcquisition originalAcquisition
    ) {
        return new ReusedWriteIntegerAcquisition(originalAcquisition);
    }

    @Override
    protected @NotNull WriteIntegerAcquisition createUpgradedAcquisition(
            @NotNull IntegerAcquisition originalAcquisition
    ) {
        return new UpgradedIntegerAcquisition(originalAcquisition, this);
    }

    @Override
    protected @Nullable WriteIntegerAcquisition castToWriteAcquisition(@NotNull IntegerAcquisition acquisition) {
        if (acquisition instanceof WriteIntegerAcquisition castAcquisition)
            return castAcquisition;
        return null;
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
            implements SetOperationImplementation {
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
        public @NotNull IntegerAcquirable acquirable() {
            return this.acquirable;
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
            this.ensurePermittedAndLocked();
            return this.acquirable.value;
        }

        @Override
        protected final @NotNull IntegerAcquisition cast() {
            return this;
        }
    }

    /**
     * Represents {@linkplain UpgradedAcquisition an upgraded acquisition}
     * and {@linkplain WriteIntegerAcquisition a write integer acquisition}.
     *
     * @since 1.0
     * @see WriteIntegerAcquisition
     * @see UpgradedAcquisition
     */
    private static final class UpgradedIntegerAcquisition
            extends UpgradedAcquisition<IntegerAcquisition, IntegerAcquirable> implements SetOperationImplementation {
        /**
         * Constructs the {@linkplain UpgradedIntegerAcquisition upgraded integer acquisition}.
         *
         * @param originalAcquisition an original acquisition that should be reused
         * @param acquirable an acquirable, which owns the acquisition that should be reused
         * @since 1.0
         */
        private UpgradedIntegerAcquisition(@NotNull IntegerAcquisition originalAcquisition,
                                           @NotNull IntegerAcquirable acquirable) {
            super(originalAcquisition, acquirable);
        }

        @Override
        public int get() {
            return this.originalAcquisition.get();
        }

        @Override
        public @NotNull IntegerAcquirable acquirable() {
            return this.acquirable;
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
         * @param originalAcquisition an original acquisition that should be reused
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
         * @param originalAcquisition an original acquisition that should be reused
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

    /**
     * Represents {@linkplain WriteIntegerAcquisition a write integer acquisition} with
     * the {@linkplain WriteIntegerAcquisition#set(int) set operation} implemented.
     *
     * @since 1.0
     * @see WriteIntegerAcquisition
     */
    private interface SetOperationImplementation extends WriteIntegerAcquisition {
        @Override
        default void set(int value) {
            this.ensurePermittedAndLocked();
            this.acquirable().value = value;
        }

        /**
         * Gets {@linkplain IntegerAcquirable an integer acquirable} that owns this acquisition.
         *
         * @return the integer acquirable
         * @since 1.0
         */
        @NotNull IntegerAcquirable acquirable();
    }
}