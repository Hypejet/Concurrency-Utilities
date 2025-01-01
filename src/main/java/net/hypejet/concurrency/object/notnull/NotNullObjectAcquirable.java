package net.hypejet.concurrency.object.notnull;

import net.hypejet.concurrency.Acquirable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * Represents {@linkplain Acquirable an acquirable}, which guards {@linkplain O an object}, which is never
 * {@code null}.
 *
 * @param <O> a type of the object
 * @since 1.0
 * @see Acquirable
 */
public final class NotNullObjectAcquirable<O>
        extends Acquirable<NotNullObjectAcquisition<O>, WriteNotNullObjectAcquisition<O>> {

    private @NotNull O value;

    /**
     * Constructs the {@linkplain NotNullObjectAcquirable not-null object acquirable}.
     *
     * @param value an initial value that the acquirable should have
     * @since 1.0
     */
    public NotNullObjectAcquirable(@NotNull O value) {
        this.value = Objects.requireNonNull(value, "The value must not be null");
    }

    @Override
    protected @NotNull NotNullObjectAcquisition<O> createReadAcquisition() {
        return new NotNullObjectAcquisitionImpl<>(this);
    }

    @Override
    protected @NotNull WriteNotNullObjectAcquisition<O> createWriteAcquisition() {
        return new WriteNotNullObjectAcquisitionImpl<>(this);
    }

    @Override
    protected @NotNull NotNullObjectAcquisition<O> reuseReadAcquisition(
            @NotNull NotNullObjectAcquisition<O> originalAcquisition
    ) {
        return new ReusedNotNullObjectAcquisition<>(originalAcquisition);
    }

    @Override
    protected @NotNull WriteNotNullObjectAcquisition<O> reuseWriteAcquisition(
            @NotNull WriteNotNullObjectAcquisition<O> originalAcquisition
    ) {
        return new ReusedWriteNotNullObjectAcquisition<>(originalAcquisition);
    }

    @Override
    protected @NotNull WriteNotNullObjectAcquisition<O> createUpgradedAcquisition(
            @NotNull NotNullObjectAcquisition<O> originalAcquisition
    ) {
        return new UpgradedNotNullObjectAcquisition<>(originalAcquisition, this);
    }

    @Override
    protected @Nullable WriteNotNullObjectAcquisition<O> castToWriteAcquisition(
            @NotNull NotNullObjectAcquisition<O> acquisition
    ) {
        return acquisition instanceof WriteNotNullObjectAcquisition<O> castAcquisition ? castAcquisition : null;
    }

    /**
     * Represents an implementation of {@linkplain AbstractNotNullObjectAcquisition an abstract not-null object
     * acquisition}.
     *
     * @param <O> a type of the object
     * @since 1.0
     * @see AbstractNotNullObjectAcquisition
     */
    private static final class NotNullObjectAcquisitionImpl<O> extends AbstractNotNullObjectAcquisition<O> {
        /**
         * Constructs the {@linkplain NotNullObjectAcquisitionImpl not-null object acquisition implementation}.
         *
         * @param acquirable an acquirable object whose object is guarded by the lock
         * @since 1.0
         */
        private NotNullObjectAcquisitionImpl(@NotNull NotNullObjectAcquirable<O> acquirable) {
            // There is no need to check whether the acquirable is null, the superclass will do that for us
            super(acquirable, AcquisitionType.READ);
        }
    }

    /**
     * Represents an implementation of {@linkplain AbstractNotNullObjectAcquisition an abstract not-null object
     * acquisition} and {@linkplain WriteNotNullObjectAcquisition a write not-null object acquisition}.
     *
     * @param <O> a type of the object
     * @since 1.0
     * @see WriteNotNullObjectAcquisition
     * @see AbstractNotNullObjectAcquisition
     */
    private static final class WriteNotNullObjectAcquisitionImpl<O> extends AbstractNotNullObjectAcquisition<O>
            implements WriteNotNullObjectAcquisition<O>, SetOperationImplementation<O> {
        /**
         * Constructs the {@linkplain WriteNotNullObjectAcquisitionImpl write not-null object acquisition
         * implementation}.
         *
         * @param acquirable an acquirable object whose object is guarded by the lock
         * @since 1.0
         */
        private WriteNotNullObjectAcquisitionImpl(@NotNull NotNullObjectAcquirable<O> acquirable) {
            // There is no need to check whether the acquirable is null, the superclass will do that for us
            super(acquirable, AcquisitionType.WRITE);
        }

        @Override
        public @NotNull NotNullObjectAcquirable<O> acquirable() {
            return this.acquirable;
        }
    }

    /**
     * Represents a common implementation of {@linkplain AbstractAcquisition an abstract acquisition}
     * and {@linkplain NotNullObjectAcquisition an not-null object acquisition}.
     *
     * @param <O> a type of the object
     * @since 1.0
     * @see NotNullObjectAcquisition
     * @see AbstractAcquisition
     */
    private static abstract class AbstractNotNullObjectAcquisition<O>
            extends AbstractAcquisition<NotNullObjectAcquisition<O>, NotNullObjectAcquirable<O>>
            implements NotNullObjectAcquisition<O> {
        /**
         * Constructs the {@linkplain AbstractNotNullObjectAcquisition abstract object acquisition}.
         *
         * @param acquirable an acquirable object whose object is guarded by the lock
         * @param type a type, of which the object acquisition should be
         * @since 1.0
         */
        private AbstractNotNullObjectAcquisition(@NotNull NotNullObjectAcquirable<O> acquirable,
                                                 @NotNull AcquisitionType type) {
            // There is no need to do nullability checks, the superclass will do that for us
            super(acquirable, type);
        }

        @Override
        public final @NotNull O get() {
            this.ensurePermittedAndLocked();
            return this.acquirable.value;
        }

        @Override
        protected final @NotNull NotNullObjectAcquisition<O> cast() {
            return this;
        }
    }

    /**
     * Represents {@linkplain ReusedNotNullObjectAcquisition a reused not-null object acquisition}, which reuses
     * {@linkplain NotNullObjectAcquisition a not-null object acquisition}, whose lock has been upgraded to a write
     * lock.
     *
     * @param <O> a type of the object
     * @since 1.0
     * @see NotNullObjectAcquisition
     * @see ReusedNotNullObjectAcquisition
     */
    private static final class UpgradedNotNullObjectAcquisition<O>
            extends ReusedNotNullObjectAcquisition<O, NotNullObjectAcquisition<O>>
            implements WriteNotNullObjectAcquisition<O>, SetOperationImplementation<O> {

        private final NotNullObjectAcquirable<O> acquirable;

        /**
         * Constructs the {@linkplain UpgradedNotNullObjectAcquisition upgraded not-null object acquisition}.
         *
         * @param originalAcquisition an original acquisition to create the reused acquisition with
         * @param acquirable an acquirable that owns the original acquisition
         * @since 1.0
         */
        private UpgradedNotNullObjectAcquisition(@NotNull NotNullObjectAcquisition<O> originalAcquisition,
                                                 @NotNull NotNullObjectAcquirable<O> acquirable) {
            super(originalAcquisition);
            this.acquirable = Objects.requireNonNull(acquirable, "The acquirable must not be null");
        }

        @Override
        public @NotNull NotNullObjectAcquirable<O> acquirable() {
            return this.acquirable;
        }
    }

    /**
     * Represents {@linkplain ReusedNotNullObjectAcquisition a reused not-null object acquisition}, which reuses
     * an already existing {@linkplain WriteNotNullObjectAcquisition write not-null object acquisition}.
     *
     * @param <O> a type of the object
     * @since 1.0
     * @see WriteNotNullObjectAcquisition
     * @see ReusedNotNullObjectAcquisition
     */
    private static final class ReusedWriteNotNullObjectAcquisition<O>
            extends ReusedNotNullObjectAcquisition<O, WriteNotNullObjectAcquisition<O>>
            implements WriteNotNullObjectAcquisition<O> {
        /**
         * Constructs the {@linkplain ReusedWriteNotNullObjectAcquisition reused write not-null object acquisition}.
         *
         * @param originalAcquisition an original acquisition to create the reused acquisition with
         * @since 1.0
         */
        private ReusedWriteNotNullObjectAcquisition(@NotNull WriteNotNullObjectAcquisition<O> originalAcquisition) {
            // There is no need to do nullability checks, the superclass will do that for us
            super(originalAcquisition);
        }

        @Override
        public void set(@NotNull O value) {
            // There is no need for a nullability check, the method will do that for us
            this.originalAcquisition.set(value);
        }
    }

    /**
     * Represents an implementation of {@linkplain ReusedAcquisition a reused acquisition} and
     * {@linkplain NotNullObjectAcquisition a not-null object acquisition}
     *
     * @param <O> a type of object of the object acquisition that is being reused
     * @param <A> a type of the acquisition that is being reused
     * @since 1.0
     * @see NotNullObjectAcquisition
     * @see ReusedAcquisition
     */
    private static class ReusedNotNullObjectAcquisition<O, A extends NotNullObjectAcquisition<O>>
            extends ReusedAcquisition<A> implements NotNullObjectAcquisition<O> {
        /**
         * Constructs the {@linkplain ReusedNotNullObjectAcquisition reused not-null object acquisition}.
         *
         * @param originalAcquisition an original acquisition to create the reused acquisition with
         * @since 1.0
         */
        private ReusedNotNullObjectAcquisition(@NotNull A originalAcquisition) {
            // There is no need to do nullability checks, the superclass will do that for us
            super(originalAcquisition);
        }

        @Override
        public final @NotNull O get() {
            return this.originalAcquisition.get();
        }
    }

    /**
     * Represents {@linkplain WriteNotNullObjectAcquisition a write not-null object acquisition} with
     * the {@linkplain WriteNotNullObjectAcquisition#set(Object) set operation} implemented.
     *
     * @param <O> a type of the object
     * @since 1.0
     * @see WriteNotNullObjectAcquisition
     */
    private interface SetOperationImplementation<O> extends WriteNotNullObjectAcquisition<O> {
        @Override
        default void set(@NotNull O value) {
            this.ensurePermittedAndLocked();
            this.acquirable().value = Objects.requireNonNull(value, "The value must not be null");
        }

        /**
         * Gets {@linkplain NotNullObjectAcquirable a not-null object acquirable} that owns this acquisition.
         *
         * @return the acquirable
         * @since 1.0
         */
        @NotNull NotNullObjectAcquirable<O> acquirable();
    }
}