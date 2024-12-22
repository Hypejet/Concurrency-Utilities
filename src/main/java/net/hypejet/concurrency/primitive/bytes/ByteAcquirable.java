package net.hypejet.concurrency.primitive.bytes;

import net.hypejet.concurrency.Acquirable;
import org.jetbrains.annotations.NotNull;

/**
 * Represents {@linkplain Acquirable an acquirable}, which guards a byte.
 *
 * @since 1.0
 * @see Acquirable
 */
public final class ByteAcquirable extends Acquirable<ByteAcquisition> {

    private byte value;

    /**
     * Constructs the {@linkplain ByteAcquisitionImpl byte acquisition} with an initial value
     * of {@code 0}.
     *
     * @since 1.0
     */
    public ByteAcquirable() {
        this((byte) 0);
    }

    /**
     * Constructs the {@linkplain ByteAcquisitionImpl byte acquisition}.
     *
     * @param value an initial value that the acquirable should have
     * @since 1.0
     */
    public ByteAcquirable(byte value) {
        this.value = value;
    }

    /**
     * Creates {@linkplain ByteAcquisition a byte acquisition} of a byte held by this
     * {@linkplain ByteAcquirable byte acquirable} that supports read-only operations.
     *
     * <p>If the caller thread has already created an acquisition a special implementation is used, which reuses it,
     * does nothing when {@link ByteAcquisition#close()} is called and always returns {@code true} when
     * {@link ByteAcquisition#isUnlocked()} is called.</p>
     *
     * <p>If the acquisition needs to be unlocked the already existing acquisition needs to be used to do that.</p>
     *
     * @return the acquisition
     * @since 1.0
     */
    @Override
    public @NotNull ByteAcquisition acquireRead() {
        ByteAcquisition foundAcquisition = this.findAcquisition();
        if (foundAcquisition != null)
            return new ReusedByteAcquisition<>(foundAcquisition);
        return new ByteAcquisitionImpl(this);
    }

    /**
     * Creates {@linkplain WriteByteAcquisition a write byte acquisition} of a byte held by
     * this {@linkplain ByteAcquirable byte acquirable} that supports write operations.
     *
     * <p>If the caller thread has already created a write acquisition a special implementation is used, which
     * reuses it, does nothing when {@link ByteAcquisition#close()} is called and always returns {@code true} when
     * {@link ByteAcquisition#isUnlocked()} is called.</p>
     *
     * <p>If the acquisition needs to be unlocked the already existing acquisition needs to be used to do that.</p>
     *
     * @return the acquisition
     * @since 1.0
     * @throws IllegalArgumentException if the caller thread has already created an acquisition, but it is not a write
     *                                  acquisition
     */
    @Override
    public @NotNull WriteByteAcquisition acquireWrite() {
        ByteAcquisition foundAcquisition = this.findAcquisition();
        if (foundAcquisition == null)
            return new WriteByteAcquisitionImpl(this);

        if (!(foundAcquisition instanceof WriteByteAcquisition writeAcquisition)) {
            throw new IllegalArgumentException("The caller thread has already created an acquisition," +
                    " but it is not a write acquisition");
        }
        return new ReusedWriteByteAcquisition(writeAcquisition);
    }

    /**
     * Represents an implementation of {@linkplain AbstractByteAcquisition an abstract byte acquisition}.
     *
     * @since 1.0
     * @see AbstractByteAcquisition
     */
    private static final class ByteAcquisitionImpl extends AbstractByteAcquisition {
        /**
         * Constructs the {@linkplain ByteAcquisitionImpl byte acquisition implementation}.
         *
         * @param acquirable an acquirable byte whose value is guarded by the lock
         * @since 1.0
         */
        private ByteAcquisitionImpl(@NotNull ByteAcquirable acquirable) {
            super(acquirable, AcquisitionType.READ);
        }
    }

    /**
     * Represents an implementation of {@linkplain AbstractByteAcquisition an abstract byte acquisition}
     * and {@linkplain WriteByteAcquisition a write byte acquisition}.
     *
     * @since 1.0
     * @see WriteByteAcquisition
     * @see AbstractByteAcquisition
     */
    private static final class WriteByteAcquisitionImpl extends AbstractByteAcquisition
            implements WriteByteAcquisition {
        /**
         * Constructs the {@linkplain WriteByteAcquisitionImpl write byte acquisition implementation}.
         *
         * @param acquirable an acquirable byte whose value is guarded by the lock
         * @since 1.0
         */
        private WriteByteAcquisitionImpl(@NotNull ByteAcquirable acquirable) {
            // There is no need to check whether the acquirable is null, the superclass will do that for us
            super(acquirable, AcquisitionType.WRITE);
        }

        @Override
        public void set(byte value) {
            this.ensurePermittedAndLocked();
            this.acquirable.value = value;
        }
    }

    /**
     * Represents a common implementation of {@linkplain AbstractAcquisition an abstract acquisition}
     * and {@linkplain ByteAcquisition a byte acquisition}.
     *
     * @since 1.0
     * @see ByteAcquisition
     * @see AbstractAcquisition
     */
    private static abstract class AbstractByteAcquisition
            extends AbstractAcquisition<ByteAcquisition, ByteAcquirable>
            implements ByteAcquisition {
        /**
         * Constructs the {@linkplain AbstractByteAcquisition abstract byte acquisition}.
         *
         * @param acquirable an acquirable whose byte is guarded by the lock
         * @param type a type, of which the acquisition should be
         * @since 1.0
         */
        protected AbstractByteAcquisition(@NotNull ByteAcquirable acquirable, @NotNull AcquisitionType type) {
            // There is no need to check whether the acquirable is null, the superclass will do that for us
            super(acquirable, type);
        }

        @Override
        public final byte get() {
            this.ensurePermittedAndLocked();
            return this.acquirable.value;
        }

        @Override
        protected final @NotNull ByteAcquisition cast() {
            return this;
        }
    }

    /**
     * Represents {@linkplain ReusedByteAcquisition a reused byte acquisition}, which reuses an already existing
     * {@linkplain WriteByteAcquisition write byte acquisition}.
     *
     * @since 1.0
     * @see WriteByteAcquisition
     * @see ReusedByteAcquisition
     */
    private static final class ReusedWriteByteAcquisition
            extends ReusedByteAcquisition<WriteByteAcquisition>
            implements WriteByteAcquisition {
        /**
         * Constructs the {@linkplain ReusedWriteByteAcquisition reused write byte acquisition}.
         *
         * @param originalAcquisition an original acquisition to create the reused acquisition with
         * @since 1.0
         */
        private ReusedWriteByteAcquisition(@NotNull WriteByteAcquisition originalAcquisition) {
            // There is no need to do nullability checks, the superclass will do that for us
            super(originalAcquisition);
        }

        @Override
        public void set(byte value) {
            this.originalAcquisition.set(value);
        }
    }

    /**
     * Represents an implementation of {@linkplain ReusedAcquisition a reused acquisition} and
     * {@linkplain ByteAcquisition a byte acquisition}.
     *
     * @param <A> a type of the acquisition that is being reused
     * @since 1.0
     * @see ByteAcquisition
     * @see ReusedAcquisition
     */
    private static class ReusedByteAcquisition<A extends ByteAcquisition> extends ReusedAcquisition<A>
            implements ByteAcquisition {
        /**
         * Constructs the {@linkplain ReusedByteAcquisition reused byte acquisition}.
         *
         * @param originalAcquisition an original acquisition to create the reused acquisition with
         * @since 1.0
         */
        private ReusedByteAcquisition(@NotNull A originalAcquisition) {
            // There is no need to do nullability checks, the superclass will do that for us
            super(originalAcquisition);
        }

        @Override
        public final byte get() {
            return this.originalAcquisition.get();
        }
    }
}