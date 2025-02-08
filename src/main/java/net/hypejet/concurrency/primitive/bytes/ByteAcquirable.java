package net.hypejet.concurrency.primitive.bytes;

import net.hypejet.concurrency.Acquirable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents {@linkplain Acquirable an acquirable}, which guards a byte.
 *
 * @since 1.0
 * @see Acquirable
 */
public final class ByteAcquirable extends Acquirable<ByteAcquisition, WriteByteAcquisition> {

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

    @Override
    protected @NotNull ByteAcquisition createReadAcquisition() {
        return new ByteAcquisitionImpl(this);
    }

    @Override
    protected @NotNull WriteByteAcquisition createWriteAcquisition() {
        return new WriteByteAcquisitionImpl(this);
    }

    @Override
    protected @NotNull ByteAcquisition reuseReadAcquisition(@NotNull ByteAcquisition originalAcquisition) {
        return new ReusedByteAcquisition<>(originalAcquisition);
    }

    @Override
    protected @NotNull WriteByteAcquisition reuseWriteAcquisition(@NotNull WriteByteAcquisition originalAcquisition) {
        return new ReusedWriteByteAcquisition(originalAcquisition);
    }

    @Override
    protected @NotNull WriteByteAcquisition createUpgradedAcquisition(@NotNull ByteAcquisition originalAcquisition) {
        return new UpgradedByteAcquisition(originalAcquisition, this);
    }

    @Override
    protected @Nullable WriteByteAcquisition castToWriteAcquisition(@NotNull ByteAcquisition acquisition) {
        if (acquisition instanceof WriteByteAcquisition castAcquisition)
            return castAcquisition;
        return null;
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
            implements SetOperationImplementation {
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
        public @NotNull ByteAcquirable acquirable() {
            return this.acquirable;
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
     * Represents an implementation of {@linkplain UpgradedAcquisition an upgraded acquisition}
     * and {@linkplain WriteByteAcquisition a write byte acquisition}.
     *
     * @since 1.0
     * @see WriteByteAcquisition
     * @see UpgradedAcquisition
     */
    private static final class UpgradedByteAcquisition extends UpgradedAcquisition<ByteAcquisition, ByteAcquirable>
            implements SetOperationImplementation {
        /**
         * Constructs the {@linkplain UpgradedByteAcquisition upgraded byte acquisition}.
         *
         * @param originalAcquisition an original acquisition that should be reused
         * @param acquirable an acquirable, which owns the acquisition that should be reused
         * @since 1.0
         */
        private UpgradedByteAcquisition(@NotNull ByteAcquisition originalAcquisition,
                                        @NotNull ByteAcquirable acquirable) {
            super(originalAcquisition, acquirable);
        }

        @Override
        public byte get() {
            return this.originalAcquisition.get();
        }

        @Override
        public @NotNull ByteAcquirable acquirable() {
            return this.acquirable;
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
         * @param originalAcquisition an original acquisition that should be reusedd
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
         * @param originalAcquisition an original acquisition that should be reused
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

    /**
     * Represents {@linkplain WriteByteAcquisition a write byte acquisition} with
     * the {@linkplain WriteByteAcquisition#set(byte) set operation} implemented.
     *
     * @since 1.0
     * @see WriteByteAcquisition
     */
    private interface SetOperationImplementation extends WriteByteAcquisition {
        @Override
        default void set(byte value) {
            this.ensurePermittedAndLocked();
            this.acquirable().value = value;
        }

        /**
         * Gets {@linkplain ByteAcquirable a byte acquirable} that owns this acquisition.
         *
         * @return the boolean acquirable
         * @since 1.0
         */
        @NotNull ByteAcquirable acquirable();
    }
}