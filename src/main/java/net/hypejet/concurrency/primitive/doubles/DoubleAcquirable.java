package net.hypejet.concurrency.primitive.doubles;

import net.hypejet.concurrency.Acquirable;
import org.jetbrains.annotations.NotNull;

/**
 * Represents {@linkplain Acquirable an acquirable}, which guards a double.
 *
 * @since 1.0
 * @see Acquirable
 */
public final class DoubleAcquirable extends Acquirable<DoubleAcquisition> {

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

    /**
     * Creates {@linkplain DoubleAcquisition a double acquisition} of a double held by this
     * {@linkplain DoubleAcquirable double acquirable} that supports read-only operations.
     *
     * <p>If the caller thread has already created an acquisition a special implementation is used, which reuses it,
     * does nothing when {@link DoubleAcquisition#close()} is called and always returns {@code true} when
     * {@link DoubleAcquisition#isUnlocked()} is called.</p>
     *
     * <p>If the acquisition needs to be unlocked the already existing acquisition needs to be used to do that.</p>
     *
     * @return the acquisition
     * @since 1.0
     */
    @Override
    public @NotNull DoubleAcquisition acquireRead() {
        DoubleAcquisition foundAcquisition = this.findAcquisition();
        if (foundAcquisition != null)
            return new ReusedDoubleAcquisition<>(foundAcquisition);
        return new DoubleAcquisitionImpl(this);
    }

    /**
     * Creates {@linkplain WriteDoubleAcquisition a write double acquisition} of a double held by
     * this {@linkplain DoubleAcquirable double acquirable} that supports write operations.
     *
     * <p>If the caller thread has already created a write acquisition a special implementation is used, which
     * reuses it, does nothing when {@link DoubleAcquisition#close()} is called and always returns {@code true} when
     * {@link DoubleAcquisition#isUnlocked()} is called.</p>
     *
     * <p>If the acquisition needs to be unlocked the already existing acquisition needs to be used to do that.</p>
     *
     * @return the acquisition
     * @since 1.0
     * @throws IllegalArgumentException if the caller thread has already created an acquisition, but it is not a write
     *                                  acquisition
     */
    @Override
    public @NotNull WriteDoubleAcquisition acquireWrite() {
        DoubleAcquisition foundAcquisition = this.findAcquisition();
        if (foundAcquisition == null)
            return new WriteDoubleAcquisitionImpl(this);

        if (!(foundAcquisition instanceof WriteDoubleAcquisition writeAcquisition)) {
            throw new IllegalArgumentException("The caller thread has already created an acquisition," +
                    " but it is not a write acquisition");
        }
        return new ReusedWriteDoubleAcquisition(writeAcquisition);
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
            implements WriteDoubleAcquisition {
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
        public void set(double value) {
            this.ensurePermittedAndLocked();
            this.acquirable.value = value;
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
         * @param originalAcquisition an original acquisition to create the reused acquisition with
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
         * @param originalAcquisition an original acquisition to create the reused acquisition with
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
}