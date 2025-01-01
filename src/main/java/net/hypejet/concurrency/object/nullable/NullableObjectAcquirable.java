package net.hypejet.concurrency.object.nullable;

import net.hypejet.concurrency.Acquirable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * Represents {@linkplain Acquirable an acquirable}, which guards {@linkplain O an object}, which is allowed to be
 * {@code null}.
 *
 * @param <O> a type of the object
 * @since 1.0
 * @see Acquirable
 */
public final class NullableObjectAcquirable<O>
        extends Acquirable<NullableObjectAcquisition<O>, WriteNullableObjectAcquisition<O>> {

    private @Nullable O value;

    /**
     * Constructs the {@linkplain NullableObjectAcquirable nullable object acquirable} with initial value of
     * {@code null}.
     *
     * @since 1.0
     */
    public NullableObjectAcquirable() {
        this(null);
    }

    /**
     * Constructs the {@linkplain NullableObjectAcquirable nullable object acquirable}.
     *
     * @param value an initial value that the acquirable should have
     * @since 1.0
     */
    public NullableObjectAcquirable(@Nullable O value) {
        this.value = value;
    }

    @Override
    protected @NotNull NullableObjectAcquisition<O> createReadAcquisition() {
        return new NullableObjectAcquisitionImpl<>(this);
    }

    @Override
    protected @NotNull WriteNullableObjectAcquisition<O> createWriteAcquisition() {
        return new WriteNullableObjectAcquisitionImpl<>(this);
    }

    @Override
    protected @NotNull NullableObjectAcquisition<O> reuseReadAcquisition(
            @NotNull NullableObjectAcquisition<O> originalAcquisition
    ) {
        return new ReusedNullableObjectAcquisition<>(originalAcquisition);
    }

    @Override
    protected @NotNull WriteNullableObjectAcquisition<O> reuseWriteAcquisition(
            @NotNull WriteNullableObjectAcquisition<O> originalAcquisition
    ) {
        return new ReusedWriteNullableObjectAcquisition<>(originalAcquisition);
    }

    @Override
    protected @NotNull WriteNullableObjectAcquisition<O> createUpgradedAcquisition(
            @NotNull NullableObjectAcquisition<O> originalAcquisition
    ) {
        return new UpgradedNullableObjectAcquisition<>(originalAcquisition, this);
    }

    @Override
    protected @Nullable WriteNullableObjectAcquisition<O> castToWriteAcquisition(
            @NotNull NullableObjectAcquisition<O> acquisition
    ) {
        if (acquisition instanceof WriteNullableObjectAcquisition<O> castAcquisition)
            return castAcquisition;
        return null;
    }

    /**
     * Represents an implementation of {@linkplain AbstractNullableObjectAcquisition an abstract nullable object
     * acquisition}.
     *
     * @param <O> a type of the object
     * @since 1.0
     * @see AbstractNullableObjectAcquisition
     */
    private static final class NullableObjectAcquisitionImpl<O> extends AbstractNullableObjectAcquisition<O> {
        /**
         * Constructs the {@linkplain NullableObjectAcquisitionImpl nullable object acquisition implementation}.
         *
         * @param acquirable an acquirable object whose object is guarded by the lock
         * @since 1.0
         */
        private NullableObjectAcquisitionImpl(@NotNull NullableObjectAcquirable<O> acquirable) {
            // There is no need to check whether the acquirable is null, the superclass will do that for us
            super(acquirable, AcquisitionType.READ);
        }
    }

    /**
     * Represents an implementation of {@linkplain AbstractNullableObjectAcquisition an abstract nullable object
     * acquisition} and {@linkplain WriteNullableObjectAcquisition a write nullable object acquisition}.
     *
     * @param <O> a type of the object
     * @since 1.0
     * @see WriteNullableObjectAcquisition
     * @see AbstractNullableObjectAcquisition
     */
    private static final class WriteNullableObjectAcquisitionImpl<O> extends AbstractNullableObjectAcquisition<O>
            implements WriteNullableObjectAcquisition<O>, SetOperationImplementation<O> {
        /**
         * Constructs the {@linkplain WriteNullableObjectAcquisitionImpl write nullable object acquisition
         * implementation}.
         *
         * @param acquirable an acquirable object whose object is guarded by the lock
         * @since 1.0
         */
        private WriteNullableObjectAcquisitionImpl(@NotNull NullableObjectAcquirable<O> acquirable) {
            // There is no need to check whether the acquirable is null, the superclass will do that for us
            super(acquirable, AcquisitionType.WRITE);
        }

        @Override
        public @NotNull NullableObjectAcquirable<O> acquirable() {
            return this.acquirable;
        }
    }

    /**
     * Represents a common implementation of {@linkplain AbstractAcquisition an abstract acquisition}
     * and {@linkplain NullableObjectAcquisition an nullable object acquisition}.
     *
     * @param <O> a type of the object
     * @since 1.0
     * @see NullableObjectAcquisition
     * @see AbstractAcquisition
     */
    private static abstract class AbstractNullableObjectAcquisition<O>
            extends AbstractAcquisition<NullableObjectAcquisition<O>, NullableObjectAcquirable<O>>
            implements NullableObjectAcquisition<O> {
        /**
         * Constructs the {@linkplain AbstractNullableObjectAcquisition abstract object acquisition}.
         *
         * @param acquirable an acquirable object whose object is guarded by the lock
         * @param type a type, of which the object acquisition should be
         * @since 1.0
         */
        private AbstractNullableObjectAcquisition(@NotNull NullableObjectAcquirable<O> acquirable,
                                                  @NotNull AcquisitionType type) {
            // There is no need to do nullability checks, the superclass will do that for us
            super(acquirable, type);
        }

        @Override
        public final @Nullable O get() {
            this.ensurePermittedAndLocked();
            return this.acquirable.value;
        }

        @Override
        protected final @NotNull NullableObjectAcquisition<O> cast() {
            return this;
        }
    }

    /**
     * Represents {@linkplain ReusedNullableObjectAcquisition a reused nullable object acquisition}, which reuses
     * {@linkplain NullableObjectAcquisition a nullable object acquisition}, whose lock has been upgraded to a write
     * lock.
     *
     * @param <O> a type of the object
     * @since 1.0
     * @see NullableObjectAcquisition
     * @see ReusedNullableObjectAcquisition
     */
    private static final class UpgradedNullableObjectAcquisition<O>
            extends ReusedNullableObjectAcquisition<O, NullableObjectAcquisition<O>>
            implements WriteNullableObjectAcquisition<O>, SetOperationImplementation<O> {

        private final NullableObjectAcquirable<O> acquirable;

        /**
         * Constructs the {@linkplain UpgradedNullableObjectAcquisition upgraded nullable object acquisition}.
         *
         * @param originalAcquisition an original acquisition to create the reused acquisition with
         * @param acquirable an acquirable that owns the original acquisition
         * @since 1.0
         */
        private UpgradedNullableObjectAcquisition(@NotNull NullableObjectAcquisition<O> originalAcquisition,
                                                  @NotNull NullableObjectAcquirable<O> acquirable) {
            super(originalAcquisition);
            this.acquirable = Objects.requireNonNull(acquirable, "The acquirable must not be null");
        }

        @Override
        public @NotNull NullableObjectAcquirable<O> acquirable() {
            return this.acquirable;
        }
    }

    /**
     * Represents {@linkplain ReusedNullableObjectAcquisition a reused nullable object acquisition}, which reuses
     * an already existing {@linkplain WriteNullableObjectAcquisition write nullable object acquisition}.
     *
     * @param <O> a type of the object
     * @since 1.0
     * @see WriteNullableObjectAcquisition
     * @see ReusedNullableObjectAcquisition
     */
    private static final class ReusedWriteNullableObjectAcquisition<O>
            extends ReusedNullableObjectAcquisition<O, WriteNullableObjectAcquisition<O>>
            implements WriteNullableObjectAcquisition<O> {
        /**
         * Constructs the {@linkplain ReusedWriteNullableObjectAcquisition reused write nullable object acquisition}.
         *
         * @param originalAcquisition an original acquisition to create the reused acquisition with
         * @since 1.0
         */
        private ReusedWriteNullableObjectAcquisition(@NotNull WriteNullableObjectAcquisition<O> originalAcquisition) {
            // There is no need to do nullability checks, the superclass will do that for us
            super(originalAcquisition);
        }

        @Override
        public void set(@Nullable O value) {
            // There is no need for a nullability check, the method will do that for us
            this.originalAcquisition.set(value);
        }
    }

    /**
     * Represents an implementation of {@linkplain ReusedAcquisition a reused acquisition} and
     * {@linkplain NullableObjectAcquisition a nullable object acquisition}
     *
     * @param <V> a type of object of the object acquisition that is being reused
     * @param <A> a type of the acquisition that is being reused
     * @since 1.0
     * @see NullableObjectAcquisition
     * @see ReusedAcquisition
     */
    private static class ReusedNullableObjectAcquisition<V, A extends NullableObjectAcquisition<V>>
            extends ReusedAcquisition<A> implements NullableObjectAcquisition<V> {
        /**
         * Constructs the {@linkplain ReusedNullableObjectAcquisition reused nullable object acquisition}.
         *
         * @param originalAcquisition an original acquisition to create the reused acquisition with
         * @since 1.0
         */
        private ReusedNullableObjectAcquisition(@NotNull A originalAcquisition) {
            // There is no need to do nullability checks, the superclass will do that for us
            super(originalAcquisition);
        }

        @Override
        public final @Nullable V get() {
            return this.originalAcquisition.get();
        }
    }

    /**
     * Represents {@linkplain WriteNullableObjectAcquisition a write nullable object acquisition} with
     * the {@linkplain WriteNullableObjectAcquisition#set(Object) set operation} implemented.
     *
     * @param <O> a type of the object
     * @since 1.0
     * @see WriteNullableObjectAcquisition
     */
    private interface SetOperationImplementation<O> extends WriteNullableObjectAcquisition<O> {
        @Override
        default void set(@Nullable O value) {
            this.ensurePermittedAndLocked();
            this.acquirable().value = value;
        }

        /**
         * Gets {@linkplain NullableObjectAcquirable a nullable object acquirable} that owns this acquisition.
         *
         * @return the acquirable
         * @since 1.0
         */
        @NotNull NullableObjectAcquirable<O> acquirable();
    }
}