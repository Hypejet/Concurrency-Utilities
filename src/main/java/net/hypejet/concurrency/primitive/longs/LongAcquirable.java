package net.hypejet.concurrency.primitive.longs;

import net.hypejet.concurrency.Acquirable;
import org.jetbrains.annotations.NotNull;

/**
 * Represents {@linkplain Acquirable an acquirable}, which guards a long.
 *
 * @since 1.0
 * @see Acquirable
 */
public final class LongAcquirable extends Acquirable<LongAcquisition> {

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

    /**
     * Creates {@linkplain LongAcquisition a long acquisition} of a long held by this
     * {@linkplain LongAcquirable long acquirable} that supports read-only operations.
     *
     * <p>If the caller thread has already created an acquisition a special implementation is used, which reuses it,
     * does nothing when {@link LongAcquisition#close()} is called and always returns {@code true} when
     * {@link LongAcquisition#isUnlocked()} is called.</p>
     *
     * <p>If the acquisition needs to be unlocked the already existing acquisition needs to be used to do that.</p>
     *
     * @return the acquisition
     * @since 1.0
     */
    @Override
    public @NotNull LongAcquisition acquireRead() {
        LongAcquisition foundAcquisition = this.findAcquisition();
        if (foundAcquisition != null)
            return new ReusedLongAcquisition<>(foundAcquisition);
        return new LongAcquisitionImpl(this);
    }

    /**
     * Creates {@linkplain WriteLongAcquisition a write long acquisition} of a long held by
     * this {@linkplain LongAcquirable long acquirable} that supports write operations.
     *
     * <p>If the caller thread has already created a write acquisition a special implementation is used, which
     * reuses it, does nothing when {@link LongAcquisition#close()} is called and always returns {@code true} when
     * {@link LongAcquisition#isUnlocked()} is called.</p>
     *
     * <p>If the acquisition needs to be unlocked the already existing acquisition needs to be used to do that.</p>
     *
     * @return the acquisition
     * @since 1.0
     * @throws IllegalArgumentException if the caller thread has already created an acquisition, but it is not a write
     *                                  acquisition
     */
    @Override
    public @NotNull WriteLongAcquisition acquireWrite() {
        LongAcquisition foundAcquisition = this.findAcquisition();
        if (foundAcquisition == null)
            return new WriteLongAcquisitionImpl(this);

        if (!(foundAcquisition instanceof WriteLongAcquisition writeAcquisition)) {
            throw new IllegalArgumentException("The caller thread has already created an acquisition," +
                    "but it is not a write acquisition");
        }
        return new ReusedWriteLongAcquisition(writeAcquisition);
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
            implements WriteLongAcquisition {
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
        public void set(long value) {
            this.runChecks();
            this.acquirable.value = value;
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
            this.runChecks();
            return this.acquirable.value;
        }

        @Override
        protected final @NotNull LongAcquisition cast() {
            return this;
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
         * @param originalAcquisition an original acquisition to create the reused acquisition with
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
         * @param originalAcquisition an original acquisition to create the reused acquisition with
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
}