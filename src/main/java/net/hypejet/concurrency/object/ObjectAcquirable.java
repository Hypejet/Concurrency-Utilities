package net.hypejet.concurrency.object;

import net.hypejet.concurrency.Acquirable;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * Represents {@linkplain Acquirable an acquirable}, which guards {@linkplain O an object}.
 *
 * @param <O> a type of the objects
 * @since 1.0
 * @see Acquirable
 */
public final class ObjectAcquirable<O> extends Acquirable<ObjectAcquisition<O>> {

    private @NotNull O value;

    /**
     * Constructs the {@linkplain ObjectAcquirable object acquirable}.
     *
     * @param value an initial value that the acquirable should have
     * @since 1.0
     */
    public ObjectAcquirable(@NotNull O value) {
        this.value = Objects.requireNonNull(value, "The value must not be null");
    }

    /**
     * Creates {@linkplain ObjectAcquisition an object acquisition} of an object held by this
     * {@linkplain ObjectAcquisition object acquirable} that supports read-only operations.
     *
     * <p>If the caller thread has already created an acquisition a special implementation is used, which reuses it,
     * does nothing when {@link ObjectAcquisition#close()} is called and always returns {@code true} when
     * {@link ObjectAcquisition#isUnlocked()} is called.</p>
     *
     * <p>If the acquisition needs to be unlocked the already existing acquisition needs to be used to do that.</p>
     *
     * @return the acquisition
     * @since 1.0
     */
    @Override
    public @NotNull ObjectAcquisition<O> acquireRead() {
        ObjectAcquisition<O> foundAcquisition = this.findAcquisition();
        if (foundAcquisition != null)
            return new ReusedObjectAcquisition<>(foundAcquisition);
        return new ObjectAcquisitionImpl<>(this);
    }

    /**
     * Creates {@linkplain WriteObjectAcquisition a write object acquisition} of an object held by
     * this {@linkplain ObjectAcquisition object acquirable} that supports write operations.
     *
     * <p>If the caller thread has already created a write acquisition a special implementation is used, which
     * reuses it, does nothing when {@link ObjectAcquisition#close()} is called and always returns {@code true} when
     * {@link ObjectAcquisition#isUnlocked()} is called.</p>
     *
     * <p>If the acquisition needs to be unlocked the already existing acquisition needs to be used to do that.</p>
     *
     * @return the acquisition
     * @since 1.0
     * @throws IllegalArgumentException if the caller thread has already created an acquisition, but it is not a write
     *                                  acquisition
     */
    @Override
    public @NotNull WriteObjectAcquisition<O> acquireWrite() {
        ObjectAcquisition<O> foundAcquisition = this.findAcquisition();
        if (foundAcquisition == null)
            return new WriteObjectAcquisitionImpl<>(this);

        if (!(foundAcquisition instanceof WriteObjectAcquisition<O> writeAcquisition)) {
            throw new IllegalArgumentException("The caller thread has already created an acquisition," +
                    " but it is not a write acquisition");
        }
        return new ReusedWriteObjectAcquisition<>(writeAcquisition);
    }

    /**
     * Represents an implementation of {@linkplain AbstractObjectAcquisition an abstract object acquisition}.
     *
     * @param <O> a type of the object
     * @since 1.0
     * @see AbstractObjectAcquisition
     */
    private static final class ObjectAcquisitionImpl<O> extends AbstractObjectAcquisition<O> {
        /**
         * Constructs the {@linkplain ObjectAcquisitionImpl object acquisition implementation}.
         *
         * @param acquirable an acquirable object whose object is guarded by the lock
         * @since 1.0
         */
        private ObjectAcquisitionImpl(@NotNull ObjectAcquirable<O> acquirable) {
            // There is no need to check whether the acquirable is null, the superclass will do that for us
            super(acquirable, AcquisitionType.READ);
        }
    }

    /**
     * Represents an implementation of {@linkplain AbstractObjectAcquisition an abstract object acquisition}
     * and {@linkplain WriteObjectAcquisition a write object acquisition}.
     *
     * @param <V> a type of the object
     * @since 1.0
     * @see WriteObjectAcquisition
     * @see AbstractObjectAcquisition
     */
    private static final class WriteObjectAcquisitionImpl<V> extends AbstractObjectAcquisition<V>
            implements WriteObjectAcquisition<V> {
        /**
         * Constructs the {@linkplain WriteObjectAcquisitionImpl write object acquisition implementation}.
         *
         * @param acquirable an acquirable object whose object is guarded by the lock
         * @since 1.0
         */
        private WriteObjectAcquisitionImpl(@NotNull ObjectAcquirable<V> acquirable) {
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
     * and {@linkplain ObjectAcquisition an object acquisition}.
     *
     * @param <O> a type of the object
     * @since 1.0
     * @see ObjectAcquisition
     * @see AbstractAcquisition
     */
    private static abstract class AbstractObjectAcquisition<O>
            extends AbstractAcquisition<ObjectAcquisition<O>, ObjectAcquirable<O>>
            implements ObjectAcquisition<O> {
        /**
         * Constructs the {@linkplain AbstractObjectAcquisition abstract object acquisition}.
         *
         * @param acquirable an acquirable object whose object is guarded by the lock
         * @param type a type, of which the object acquisition should be
         * @since 1.0
         */
        private AbstractObjectAcquisition(@NotNull ObjectAcquirable<O> acquirable, @NotNull AcquisitionType type) {
            // There is no need to do nullability checks, the superclass will do that for us
            super(acquirable, type);
        }

        @Override
        public final @NotNull O get() {
            this.ensurePermittedAndLocked();
            return this.acquirable.value;
        }

        @Override
        protected final @NotNull ObjectAcquisition<O> cast() {
            return this;
        }
    }

    /**
     * Represents {@linkplain ReusedObjectAcquisition a reused object acquisition}, which reuses an already existing
     * {@linkplain WriteObjectAcquisition write object acquisition}.
     *
     * @param <O> a type of the object
     * @since 1.0
     * @see WriteObjectAcquisition
     * @see ReusedObjectAcquisition
     */
    private static final class ReusedWriteObjectAcquisition<O>
            extends ReusedObjectAcquisition<O, WriteObjectAcquisition<O>>
            implements WriteObjectAcquisition<O> {
        /**
         * Constructs the {@linkplain ReusedWriteObjectAcquisition reused write object acquisition}.
         *
         * @param originalAcquisition an original acquisition to create the reused acquisition with
         * @since 1.0
         */
        private ReusedWriteObjectAcquisition(@NotNull WriteObjectAcquisition<O> originalAcquisition) {
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
     * {@linkplain ObjectAcquisition an object acquisition}
     *
     * @param <V> a type of object of the object acquisition that is being reused
     * @param <A> a type of the acquisition that is being reused
     * @since 1.0
     * @see ObjectAcquisition
     * @see ReusedAcquisition
     */
    private static class ReusedObjectAcquisition<V, A extends ObjectAcquisition<V>> extends ReusedAcquisition<A>
            implements ObjectAcquisition<V> {
        /**
         * Constructs the {@linkplain ReusedObjectAcquisition reused object acquisition}.
         *
         * @param originalAcquisition an original acquisition to create the reused acquisition with
         * @since 1.0
         */
        private ReusedObjectAcquisition(@NotNull A originalAcquisition) {
            // There is no need to do nullability checks, the superclass will do that for us
            super(originalAcquisition);
        }

        @Override
        public final @NotNull V get() {
            return this.originalAcquisition.get();
        }
    }
}