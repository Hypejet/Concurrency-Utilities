package net.hypejet.concurrency.object.notnull;

import net.hypejet.concurrency.Acquirable;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * Represents {@linkplain Acquirable an acquirable}, which guards {@linkplain O an object}, which is never
 * {@code null}.
 *
 * @param <O> a type of the object
 * @since 1.0
 * @see Acquirable
 */
public final class NotNullObjectAcquirable<O> extends Acquirable<NotNullObjectAcquisition<O>> {

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

    /**
     * Creates {@linkplain NotNullObjectAcquisition a not-null object acquisition} of an object held by this
     * {@linkplain NotNullObjectAcquirable not-null object acquirable} that supports read-only operations.
     *
     * <p>If the caller thread has already created an acquisition a special implementation is used, which reuses it,
     * does nothing when {@link NotNullObjectAcquisition#close()} is called and always returns {@code true} when
     * {@link NotNullObjectAcquisition#isUnlocked()} is called.</p>
     *
     * <p>If the acquisition needs to be unlocked the already existing acquisition needs to be used to do that.</p>
     *
     * @return the acquisition
     * @since 1.0
     */
    @Override
    public @NotNull NotNullObjectAcquisition<O> acquireRead() {
        NotNullObjectAcquisition<O> foundAcquisition = this.findAcquisition();
        if (foundAcquisition != null)
            return new ReusedNotNullObjectAcquisition<>(foundAcquisition);
        return new NotNullObjectAcquisitionImpl<>(this);
    }

    /**
     * Creates {@linkplain WriteNotNullObjectAcquisition a write not-null object acquisition} of an object held by
     * this {@linkplain NotNullObjectAcquirable not-null object acquirable} that supports write operations.
     *
     * <p>If the caller thread has already created a write acquisition a special implementation is used, which
     * reuses it, does nothing when {@link NotNullObjectAcquisition#close()} is called and always returns {@code true}
     * when {@link NotNullObjectAcquisition#isUnlocked()} is called.</p>
     *
     * <p>If the acquisition needs to be unlocked the already existing acquisition needs to be used to do that.</p>
     *
     * @return the acquisition
     * @since 1.0
     * @throws IllegalArgumentException if the caller thread has already created an acquisition, but it is not a write
     *                                  acquisition
     */
    @Override
    public @NotNull WriteNotNullObjectAcquisition<O> acquireWrite() {
        NotNullObjectAcquisition<O> foundAcquisition = this.findAcquisition();
        if (foundAcquisition == null)
            return new WriteNotNullObjectAcquisitionImpl<>(this);

        if (!(foundAcquisition instanceof WriteNotNullObjectAcquisition<O> writeAcquisition)) {
            throw new IllegalArgumentException("The caller thread has already created an acquisition," +
                    " but it is not a write acquisition");
        }
        return new ReusedWriteNotNullObjectAcquisition<>(writeAcquisition);
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
     * @param <V> a type of the object
     * @since 1.0
     * @see WriteNotNullObjectAcquisition
     * @see AbstractNotNullObjectAcquisition
     */
    private static final class WriteNotNullObjectAcquisitionImpl<V> extends AbstractNotNullObjectAcquisition<V>
            implements WriteNotNullObjectAcquisition<V> {
        /**
         * Constructs the {@linkplain WriteNotNullObjectAcquisitionImpl write not-null object acquisition
         * implementation}.
         *
         * @param acquirable an acquirable object whose object is guarded by the lock
         * @since 1.0
         */
        private WriteNotNullObjectAcquisitionImpl(@NotNull NotNullObjectAcquirable<V> acquirable) {
            // There is no need to check whether the acquirable is null, the superclass will do that for us
            super(acquirable, AcquisitionType.WRITE);
        }

        @Override
        public void set(@NotNull V value) {
            this.ensurePermittedAndLocked();
            this.acquirable.value = Objects.requireNonNull(value, "The value must not be null");
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
     * @param <V> a type of object of the object acquisition that is being reused
     * @param <A> a type of the acquisition that is being reused
     * @since 1.0
     * @see NotNullObjectAcquisition
     * @see ReusedAcquisition
     */
    private static class ReusedNotNullObjectAcquisition<V, A extends NotNullObjectAcquisition<V>>
            extends ReusedAcquisition<A> implements NotNullObjectAcquisition<V> {
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
        public final @NotNull V get() {
            return this.originalAcquisition.get();
        }
    }
}