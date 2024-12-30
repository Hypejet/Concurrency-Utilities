package net.hypejet.concurrency.object.nullable;

import net.hypejet.concurrency.Acquirable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents {@linkplain Acquirable an acquirable}, which guards {@linkplain O an object}, which is allowed to be
 * {@code null}.
 *
 * @param <O> a type of the object
 * @since 1.0
 * @see Acquirable
 */
public final class NullableObjectAcquirable<O> extends Acquirable<NullableObjectAcquisition<O>> {

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

    /**
     * Creates {@linkplain NullableObjectAcquisition a nullable object acquisition} of an object held by this
     * {@linkplain NullableObjectAcquirable nullable object acquirable} that supports read-only operations.
     *
     * <p>If the caller thread has already created an acquisition a special implementation is used, which reuses it,
     * does nothing when {@link NullableObjectAcquisition#close()} is called and always returns {@code true} when
     * {@link NullableObjectAcquisition#isUnlocked()} is called.</p>
     *
     * <p>If the acquisition needs to be unlocked the already existing acquisition needs to be used to do that.</p>
     *
     * @return the acquisition
     * @since 1.0
     */
    @Override
    public @NotNull NullableObjectAcquisition<O> acquireRead() {
        NullableObjectAcquisition<O> foundAcquisition = this.findAcquisition();
        if (foundAcquisition != null)
            return new ReusedNullableObjectAcquisition<>(foundAcquisition);
        return new NullableObjectAcquisitionImpl<>(this);
    }

    /**
     * Creates {@linkplain WriteNullableObjectAcquisition a write nullable object acquisition} of an object held by
     * this {@linkplain NullableObjectAcquirable nullable object acquirable} that supports write operations.
     *
     * <p>If the caller thread has already created a write acquisition a special implementation is used, which
     * reuses it, does nothing when {@link NullableObjectAcquisition#close()} is called and always returns {@code true}
     * when {@link NullableObjectAcquisition#isUnlocked()} is called.</p>
     *
     * <p>If the acquisition needs to be unlocked the already existing acquisition needs to be used to do that.</p>
     *
     * @return the acquisition
     * @since 1.0
     * @throws IllegalArgumentException if the caller thread has already created an acquisition, but it is not a write
     *                                  acquisition
     */
    @Override
    public @NotNull WriteNullableObjectAcquisition<O> acquireWrite() {
        NullableObjectAcquisition<O> foundAcquisition = this.findAcquisition();
        if (foundAcquisition == null)
            return new WriteNullableObjectAcquisitionImpl<>(this);

        if (!(foundAcquisition instanceof WriteNullableObjectAcquisition<O> writeAcquisition)) {
            throw new IllegalArgumentException("The caller thread has already created an acquisition," +
                    " but it is not a write acquisition");
        }
        return new ReusedWriteNullableObjectAcquisition<>(writeAcquisition);
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
     * @param <V> a type of the object
     * @since 1.0
     * @see WriteNullableObjectAcquisition
     * @see AbstractNullableObjectAcquisition
     */
    private static final class WriteNullableObjectAcquisitionImpl<V> extends AbstractNullableObjectAcquisition<V>
            implements WriteNullableObjectAcquisition<V> {
        /**
         * Constructs the {@linkplain WriteNullableObjectAcquisitionImpl write nullable object acquisition
         * implementation}.
         *
         * @param acquirable an acquirable object whose object is guarded by the lock
         * @since 1.0
         */
        private WriteNullableObjectAcquisitionImpl(@NotNull NullableObjectAcquirable<V> acquirable) {
            // There is no need to check whether the acquirable is null, the superclass will do that for us
            super(acquirable, AcquisitionType.WRITE);
        }

        @Override
        public void set(@Nullable V value) {
            this.ensurePermittedAndLocked();
            this.acquirable.value = value;
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
}