package net.hypejet.concurrency.primitive.longs;

import net.hypejet.concurrency.Acquirable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents {@linkplain Acquirable an acquirable}, which guards a long.
 *
 * @since 1.0
 * @see Acquirable
 */
public final class LongAcquirable extends Acquirable<LongAcquisition, WriteLongAcquisition> {

    private long value;

    /**
     * Constructs the {@linkplain LongAcquisitionImpl long acquisition} with an initial value
     * of {@code 0}.
     *
     * @since 1.0
     */
    public LongAcquirable() {
        this(0L);
    }

    /**
     * Constructs the {@linkplain LongAcquisitionImpl long acquisition}.
     *
     * @param value an initial value that the acquirable should have
     * @since 1.0
     */
    public LongAcquirable(long value) {
        this.value = value;
    }

    @Override
    protected @NotNull LongAcquisition createReadAcquisition() {
        return new LongAcquisitionImpl(this);
    }

    @Override
    protected @NotNull WriteLongAcquisition createWriteAcquisition() {
        return new WriteLongAcquisitionImpl(this);
    }

    @Override
    protected @NotNull LongAcquisition reuseReadAcquisition(@NotNull LongAcquisition originalAcquisition) {
        return new ReusedLongAcquisition<>(originalAcquisition);
    }

    @Override
    protected @NotNull WriteLongAcquisition reuseWriteAcquisition(@NotNull WriteLongAcquisition originalAcquisition) {
        return new ReusedWriteLongAcquisition(originalAcquisition);
    }

    @Override
    protected @NotNull WriteLongAcquisition createUpgradedAcquisition(@NotNull LongAcquisition originalAcquisition) {
        return new UpgradedLongAcquisition(originalAcquisition, this);
    }

    @Override
    protected @Nullable WriteLongAcquisition castToWriteAcquisition(@NotNull LongAcquisition acquisition) {
        if (acquisition instanceof WriteLongAcquisition castAcquisition)
            return castAcquisition;
        return null;
    }

    /**
     * Represents an implementation of {@linkplain AbstractLongAcquisition an abstract long acquisition}.
     *
     * @since 1.0
     * @see AbstractLongAcquisition
     */
    private static final class LongAcquisitionImpl extends AbstractLongAcquisition {
        /**
         * Constructs the {@linkplain LongAcquisitionImpl long acquisition implementation}.
         *
         * @param acquirable an acquirable long whose value is guarded by the lock
         * @since 1.0
         */
        private LongAcquisitionImpl(@NotNull LongAcquirable acquirable) {
            super(acquirable, AcquisitionType.READ);
        }
    }

    /**
     * Represents an implementation of {@linkplain AbstractLongAcquisition an abstract long acquisition}
     * and {@linkplain WriteLongAcquisition a write long acquisition}.
     *
     * @since 1.0
     * @see WriteLongAcquisition
     * @see AbstractLongAcquisition
     */
    private static final class WriteLongAcquisitionImpl extends AbstractLongAcquisition
            implements SetOperationImplementation {
        /**
         * Constructs the {@linkplain WriteLongAcquisitionImpl write long acquisition implementation}.
         *
         * @param acquirable an acquirable long whose value is guarded by the lock
         * @since 1.0
         */
        private WriteLongAcquisitionImpl(@NotNull LongAcquirable acquirable) {
            // There is no need to check whether the acquirable is null, the superclass will do that for us
            super(acquirable, AcquisitionType.WRITE);
        }

        @Override
        public @NotNull LongAcquirable acquirable() {
            return this.acquirable;
        }
    }

    /**
     * Represents a common implementation of {@linkplain AbstractAcquisition an abstract acquisition}
     * and {@linkplain LongAcquisition a long acquisition}.
     *
     * @since 1.0
     * @see LongAcquisition
     * @see AbstractAcquisition
     */
    private static abstract class AbstractLongAcquisition extends AbstractAcquisition<LongAcquisition, LongAcquirable>
            implements LongAcquisition {
        /**
         * Constructs the {@linkplain AbstractLongAcquisition abstract long acquisition}.
         *
         * @param acquirable an acquirable whose long is guarded by the lock
         * @param type a type, of which the acquisition should be
         * @since 1.0
         */
        protected AbstractLongAcquisition(@NotNull LongAcquirable acquirable, @NotNull AcquisitionType type) {
            // There is no need to check whether the acquirable is null, the superclass will do that for us
            super(acquirable, type);
        }

        @Override
        public final long get() {
            this.ensurePermittedAndLocked();
            return this.acquirable.value;
        }

        @Override
        protected final @NotNull LongAcquisition cast() {
            return this;
        }
    }

    /**
     * Represents an implementation of {@linkplain UpgradedAcquisition an upgraded acquisition}
     * and {@linkplain WriteLongAcquisition a write long acquisition}.
     *
     * @since 1.0
     * @see WriteLongAcquisition
     * @see UpgradedAcquisition
     */
    private static final class UpgradedLongAcquisition extends UpgradedAcquisition<LongAcquisition, LongAcquirable>
            implements SetOperationImplementation {
        /**
         * Constructs the {@linkplain UpgradedLongAcquisition upgraded long acquisition}.
         *
         * @param originalAcquisition an original acquisition that should be reused
         * @param acquirable an acquirable, which owns the acquisition that should be reused
         * @since 1.0
         */
        private UpgradedLongAcquisition(@NotNull LongAcquisition originalAcquisition,
                                        @NotNull LongAcquirable acquirable) {
            super(originalAcquisition, acquirable);
        }

        @Override
        public long get() {
            return this.originalAcquisition.get();
        }

        @Override
        public @NotNull LongAcquirable acquirable() {
            return this.acquirable;
        }
    }

    /**
     * Represents {@linkplain ReusedLongAcquisition a reused long acquisition}, which reuses an already existing
     * {@linkplain WriteLongAcquisition write long acquisition}.
     *
     * @since 1.0
     * @see WriteLongAcquisition
     * @see ReusedLongAcquisition
     */
    private static final class ReusedWriteLongAcquisition extends ReusedLongAcquisition<WriteLongAcquisition>
            implements WriteLongAcquisition {
        /**
         * Constructs the {@linkplain ReusedWriteLongAcquisition reused write long acquisition}.
         *
         * @param originalAcquisition an original acquisition that should be reused
         * @since 1.0
         */
        private ReusedWriteLongAcquisition(@NotNull WriteLongAcquisition originalAcquisition) {
            // There is no need to do nullability checks, the superclass will do that for us
            super(originalAcquisition);
        }

        @Override
        public void set(long value) {
            this.originalAcquisition.set(value);
        }
    }

    /**
     * Represents an implementation of {@linkplain ReusedAcquisition a reused acquisition} and
     * {@linkplain LongAcquisition a long acquisition}.
     *
     * @param <A> a type of the acquisition that is being reused
     * @since 1.0
     * @see LongAcquisition
     * @see ReusedAcquisition
     */
    private static class ReusedLongAcquisition<A extends LongAcquisition> extends ReusedAcquisition<A>
            implements LongAcquisition {
        /**
         * Constructs the {@linkplain ReusedLongAcquisition reused long acquisition}.
         *
         * @param originalAcquisition an original acquisition that should be reused
         * @since 1.0
         */
        private ReusedLongAcquisition(@NotNull A originalAcquisition) {
            // There is no need to do nullability checks, the superclass will do that for us
            super(originalAcquisition);
        }

        @Override
        public final long get() {
            return this.originalAcquisition.get();
        }
    }

    /**
     * Represents {@linkplain WriteLongAcquisition a write long acquisition} with
     * the {@linkplain WriteLongAcquisition#set(long) set operation} implemented.
     *
     * @since 1.0
     * @see WriteLongAcquisition
     */
    private interface SetOperationImplementation extends WriteLongAcquisition {
        @Override
        default void set(long value) {
            this.ensurePermittedAndLocked();
            this.acquirable().value = value;
        }

        /**
         * Gets {@linkplain LongAcquirable a long acquirable} that owns this acquisition.
         *
         * @return the long acquirable
         * @since 1.0
         */
        @NotNull LongAcquirable acquirable();
    }
}