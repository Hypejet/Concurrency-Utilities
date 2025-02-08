package net.hypejet.concurrency.primitive.shorts;

import net.hypejet.concurrency.Acquirable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents {@linkplain Acquirable an acquirable}, which guards a short.
 *
 * @since 1.0
 * @see Acquirable
 */
public final class ShortAcquirable extends Acquirable<ShortAcquisition, WriteShortAcquisition> {

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

    @Override
    protected @NotNull ShortAcquisition createReadAcquisition() {
        return new ShortAcquisitionImpl(this);
    }

    @Override
    protected @NotNull WriteShortAcquisition createWriteAcquisition() {
        return new WriteShortAcquisitionImpl(this);
    }

    @Override
    protected @NotNull ShortAcquisition reuseReadAcquisition(@NotNull ShortAcquisition originalAcquisition) {
        return new ReusedShortAcquisition<>(originalAcquisition);
    }

    @Override
    protected @NotNull WriteShortAcquisition reuseWriteAcquisition(
            @NotNull WriteShortAcquisition originalAcquisition
    ) {
        return new ReusedWriteShortAcquisition(originalAcquisition);
    }

    @Override
    protected @NotNull WriteShortAcquisition createUpgradedAcquisition(@NotNull ShortAcquisition originalAcquisition) {
        return new UpgradedShortAcquisition(originalAcquisition, this);
    }

    @Override
    protected @Nullable WriteShortAcquisition castToWriteAcquisition(@NotNull ShortAcquisition acquisition) {
        if (acquisition instanceof WriteShortAcquisition castAcquisition)
            return castAcquisition;
        return null;
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
            implements SetOperationImplementation {
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
        public @NotNull ShortAcquirable acquirable() {
            return this.acquirable;
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
            this.ensurePermittedAndLocked();
            return this.acquirable.value;
        }

        @Override
        protected final @NotNull ShortAcquisition cast() {
            return this;
        }

    }

    /**
     * Represents an implementation of {@linkplain UpgradedAcquisition an upgraded acquisition}
     * and {@linkplain WriteShortAcquisition a write short acquisition}.
     *
     * @since 1.0
     * @see WriteShortAcquisition
     * @see UpgradedAcquisition
     */
    private static final class UpgradedShortAcquisition extends UpgradedAcquisition<ShortAcquisition, ShortAcquirable>
            implements SetOperationImplementation {
        /**
         * Constructs the {@linkplain UpgradedShortAcquisition upgraded short acquisition}.
         *
         * @param originalAcquisition an original acquisition that should be reused
         * @param acquirable an acquirable, which owns the acquisition that should be reused
         * @since 1.0
         */
        private UpgradedShortAcquisition(@NotNull ShortAcquisition originalAcquisition,
                                         @NotNull ShortAcquirable acquirable) {
            super(originalAcquisition, acquirable);
        }

        @Override
        public short get() {
            return this.originalAcquisition.get();
        }

        @Override
        public @NotNull ShortAcquirable acquirable() {
            return this.acquirable;
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
         * @param originalAcquisition an original acquisition that should be reused
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
         * @param originalAcquisition an original acquisition that should be reused
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

    /**
     * Represents {@linkplain WriteShortAcquisition a write short acquisition} with
     * the {@linkplain WriteShortAcquisition#set(short) set operation} implemented.
     *
     * @since 1.0
     * @see WriteShortAcquisition
     */
    private interface SetOperationImplementation extends WriteShortAcquisition {
        @Override
        default void set(short value) {
            this.ensurePermittedAndLocked();
            this.acquirable().value = value;
        }

        /**
         * Gets {@linkplain ShortAcquirable a short acquirable} that owns this acquisition.
         *
         * @return the short acquirable
         * @since 1.0
         */
        @NotNull ShortAcquirable acquirable();
    }
}