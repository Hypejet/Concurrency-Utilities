package net.hypejet.concurrency.primitive.shorts;

import net.hypejet.concurrency.Acquirable;
import org.jetbrains.annotations.NotNull;

/**
 * Represents {@linkplain Acquirable an acquirable}, which guards a short.
 *
 * @since 1.0
 * @see Acquirable
 */
public final class ShortAcquirable extends Acquirable<ShortAcquisition> {

    private short value;

    /**
     * Constructs the {@linkplain ShortAcquisitionImpl short acquisition} with an initial value
     * of {@code 0}.
     *
     * @since 1.0
     */
    public ShortAcquirable() {
        this((short) 0);
    }

    /**
     * Constructs the {@linkplain ShortAcquisitionImpl short acquisition}.
     *
     * @param value an initial value that the acquirable should have
     * @since 1.0
     */
    public ShortAcquirable(short value) {
        this.value = value;
    }

    /**
     * Creates {@linkplain ShortAcquisition a short acquisition} of a short held by this
     * {@linkplain ShortAcquirable short acquirable} that supports read-only operations.
     *
     * <p>If the caller thread has already created an acquisition a special implementation is used, which reuses it,
     * does nothing when {@link ShortAcquisition#close()} is called and always returns {@code true} when
     * {@link ShortAcquisition#isUnlocked()} is called.</p>
     *
     * <p>If the acquisition needs to be unlocked the already existing acquisition needs to be used to do that.</p>
     *
     * @return the acquisition
     * @since 1.0
     */
    @Override
    public @NotNull ShortAcquisition acquireRead() {
        ShortAcquisition foundAcquisition = this.findAcquisition();
        if (foundAcquisition != null)
            return new ReusedShortAcquisition<>(foundAcquisition);
        return new ShortAcquisitionImpl(this);
    }

    /**
     * Creates {@linkplain WriteShortAcquisition a write short acquisition} of a short held by
     * this {@linkplain ShortAcquirable short acquirable} that supports write operations.
     *
     * <p>If the caller thread has already created a write acquisition a special implementation is used, which
     * reuses it, does nothing when {@link ShortAcquisition#close()} is called and always returns {@code true} when
     * {@link ShortAcquisition#isUnlocked()} is called.</p>
     *
     * <p>If the acquisition needs to be unlocked the already existing acquisition needs to be used to do that.</p>
     *
     * @return the acquisition
     * @since 1.0
     * @throws IllegalArgumentException if the caller thread has already created an acquisition, but it is not a write
     *                                  acquisition
     */
    @Override
    public @NotNull WriteShortAcquisition acquireWrite() {
        ShortAcquisition foundAcquisition = this.findAcquisition();
        if (foundAcquisition == null)
            return new WriteShortAcquisitionImpl(this);

        if (!(foundAcquisition instanceof WriteShortAcquisition writeAcquisition)) {
            throw new IllegalArgumentException("The caller thread has already created an acquisition," +
                    "but it is not a write acquisition");
        }
        return new ReusedWriteShortAcquisition(writeAcquisition);
    }

    /**
     * Represents an implementation of {@linkplain AbstractShortAcquisition an abstract short acquisition}.
     *
     * @since 1.0
     * @see AbstractShortAcquisition
     */
    private static final class ShortAcquisitionImpl extends AbstractShortAcquisition {
        /**
         * Constructs the {@linkplain ShortAcquisitionImpl short acquisition implementation}.
         *
         * @param acquirable an acquirable short whose value is guarded by the lock
         * @since 1.0
         */
        private ShortAcquisitionImpl(@NotNull ShortAcquirable acquirable) {
            super(acquirable, AcquisitionType.READ);
        }
    }

    /**
     * Represents an implementation of {@linkplain AbstractShortAcquisition an abstract short acquisition}
     * and {@linkplain WriteShortAcquisition a write short acquisition}.
     *
     * @since 1.0
     * @see WriteShortAcquisition
     * @see AbstractShortAcquisition
     */
    private static final class WriteShortAcquisitionImpl extends AbstractShortAcquisition
            implements WriteShortAcquisition {
        /**
         * Constructs the {@linkplain WriteShortAcquisitionImpl write short acquisition implementation}.
         *
         * @param acquirable an acquirable short whose value is guarded by the lock
         * @since 1.0
         */
        private WriteShortAcquisitionImpl(@NotNull ShortAcquirable acquirable) {
            // There is no need to check whether the acquirable is null, the superclass will do that for us
            super(acquirable, AcquisitionType.WRITE);
        }

        @Override
        public void set(short value) {
            this.runChecks();
            this.acquirable.value = value;
        }
    }

    /**
     * Represents a common implementation of {@linkplain AbstractAcquisition an abstract acquisition}
     * and {@linkplain ShortAcquisition a short acquisition}.
     *
     * @since 1.0
     * @see ShortAcquisition
     * @see AbstractAcquisition
     */
    private static abstract class AbstractShortAcquisition
            extends AbstractAcquisition<ShortAcquisition, ShortAcquirable> implements ShortAcquisition {
        /**
         * Constructs the {@linkplain AbstractShortAcquisition abstract short acquisition}.
         *
         * @param acquirable an acquirable whose short is guarded by the lock
         * @param type a type, of which the acquisition should be
         * @since 1.0
         */
        protected AbstractShortAcquisition(@NotNull ShortAcquirable acquirable, @NotNull AcquisitionType type) {
            // There is no need to check whether the acquirable is null, the superclass will do that for us
            super(acquirable, type);
        }

        @Override
        public final short get() {
            this.runChecks();
            return this.acquirable.value;
        }

        @Override
        protected final @NotNull ShortAcquisition cast() {
            return this;
        }
    }

    /**
     * Represents {@linkplain ReusedShortAcquisition a reused short acquisition}, which reuses an already existing
     * {@linkplain WriteShortAcquisition write short acquisition}.
     *
     * @since 1.0
     * @see WriteShortAcquisition
     * @see ReusedShortAcquisition
     */
    private static final class ReusedWriteShortAcquisition
            extends ReusedShortAcquisition<WriteShortAcquisition> implements WriteShortAcquisition {
        /**
         * Constructs the {@linkplain ReusedWriteShortAcquisition reused write short acquisition}.
         *
         * @param originalAcquisition an original acquisition to create the reused acquisition with
         * @since 1.0
         */
        private ReusedWriteShortAcquisition(@NotNull WriteShortAcquisition originalAcquisition) {
            // There is no need to do nullability checks, the superclass will do that for us
            super(originalAcquisition);
        }

        @Override
        public void set(short value) {
            this.originalAcquisition.set(value);
        }
    }

    /**
     * Represents an implementation of {@linkplain ReusedAcquisition a reused acquisition} and
     * {@linkplain ShortAcquisition a short acquisition}.
     *
     * @param <A> a type of the acquisition that is being reused
     * @since 1.0
     * @see ShortAcquisition
     * @see ReusedAcquisition
     */
    private static class ReusedShortAcquisition<A extends ShortAcquisition> extends ReusedAcquisition<A>
            implements ShortAcquisition {
        /**
         * Constructs the {@linkplain ReusedShortAcquisition reused short acquisition}.
         *
         * @param originalAcquisition an original acquisition to create the reused acquisition with
         * @since 1.0
         */
        private ReusedShortAcquisition(@NotNull A originalAcquisition) {
            // There is no need to do nullability checks, the superclass will do that for us
            super(originalAcquisition);
        }

        @Override
        public final short get() {
            return this.originalAcquisition.get();
        }
    }
}