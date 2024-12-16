package net.hypejet.concurrency.collection;

import net.hypejet.concurrency.Acquirable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Objects;
import java.util.function.Predicate;

/**
 * Represents {@linkplain Acquirable an acquirable}, which guards {@linkplain Collection a collection}.
 *
 * @param <V> a type of value of the collection
 * @param <C> a type of the collection
 * @since 1.0
 * @see Acquirable
 */
public abstract class CollectionAcquirable<V, C extends Collection<V>>
        extends Acquirable<CollectionAcquisition<V, C>> {

    private final @NotNull C collection;
    private final @NotNull C readOnlyView;

    /**
     * Constructs the {@linkplain CollectionAcquirable collection acquirable} with no initial elements.
     *
     * @since 1.0
     */
    public CollectionAcquirable() {
        this(null);
    }

    /**
     * Constructs the {@linkplain CollectionAcquirable collection acquirable}.
     *
     * @param initialElements a collection of elements that should be added to the collection during initialization
     * @since 1.0
     */
    public CollectionAcquirable(@Nullable Collection<V> initialElements) {
        this.collection = this.createCollection();
        if (initialElements != null)
            this.collection.addAll(initialElements);
        this.readOnlyView = this.createReadOnlyView(this.collection);
    }

    /**
     * Creates {@linkplain CollectionAcquisition a collection acquisition} of a collection held by this
     * {@linkplain CollectionAcquirable collection acquirable} that supports read-only operations.
     *
     * <p>If the caller thread has already created an acquisition a special implementation is used, which reuses it,
     * does nothing when {@link CollectionAcquisition#close()} is called and always returns {@code true} when
     * {@link CollectionAcquisition#isUnlocked()} is called.</p>
     *
     * <p>If the acquisition needs to be unlocked the already existing acquisition needs to be used to do that.</p>
     *
     * @return the acquisition
     * @since 1.0
     */
    @Override
    public final @NotNull CollectionAcquisition<V, C> acquireRead() {
        CollectionAcquisition<V, C> foundAcquisition = this.findAcquisition();
        if (foundAcquisition != null)
            return new ReusedCollectionAcquisition<>(foundAcquisition);
        return new CollectionAcquisitionImpl<>(this);
    }

    /**
     * Creates {@linkplain WriteCollectionAcquisition a write empty acquisition} of this
     * {@linkplain CollectionAcquirable collection acquirable} that supports write operations.
     *
     * <p>If the caller thread has already created a write acquisition a special implementation is used, which
     * reuses it, does nothing when {@link CollectionAcquisition#close()} is called and always returns {@code true}
     * when {@link CollectionAcquisition#isUnlocked()} is called.</p>
     *
     * <p>If the acquisition needs to be unlocked the already existing acquisition needs to be used to do that.</p>
     *
     * @return the acquisition
     * @since 1.0
     * @throws IllegalArgumentException if the caller thread has already created an acquisition, but it is not a write
     *                                  acquisition
     */
    @Override
    public final @NotNull WriteCollectionAcquisition<V, C> acquireWrite() {
        CollectionAcquisition<V, C> foundAcquisition = this.findAcquisition();
        if (foundAcquisition == null)
            return new WriteCollectionAcquisitionImpl<>(this);

        if (!(foundAcquisition instanceof WriteCollectionAcquisition<V, C> writeAcquisition)) {
            throw new IllegalArgumentException("The caller thread has already created an acquisition," +
                    " but it is not a write acquisition");
        }
        return new ReusedWriteCollectionAcquisition<>(writeAcquisition);
    }

    /**
     * Creates a new mutable instance of the collection.
     *
     * @return the created instance
     * @since 1.0
     */
    protected abstract @NotNull C createCollection();

    /**
     * Creates a new read-only view of a collection specified.
     *
     * @param collection the collection to create the read-only view with
     * @return the read-only view created
     * @since 1.0
     */
    protected abstract @NotNull C createReadOnlyView(@NotNull C collection);

    /**
     * Represents an implementation of {@linkplain AbstractCollectionAcquisition an abstract collection acquisition}.
     *
     * @param <V> a type of value of the held collection
     * @param <C> a type of the held collection
     * @since 1.0
     * @see AbstractCollectionAcquisition
     */
    private static final class CollectionAcquisitionImpl<V, C extends Collection<V>>
            extends AbstractCollectionAcquisition<V, C> {
        /**
         * Constructs the {@linkplain AbstractAcquisition abstract acquisition}.
         *
         * @param acquirable an acquirable whose state is guarded by the lock
         * @since 1.0
         */
        private CollectionAcquisitionImpl(@NotNull CollectionAcquirable<V, C> acquirable) {
            // There is no need to do nullability checks, the superclass will do that for us
            super(acquirable, AcquisitionType.READ);
        }
    }

    /**
     * Represents an implementation of {@linkplain AbstractCollectionAcquisition an abstract collection acquisition}
     * and {@linkplain WriteCollectionAcquisition a write collection acquisition}.
     *
     * @param <V> a type of value of the held collection
     * @param <C> a type of the held collection
     * @since 1.0
     * @see WriteCollectionAcquisition
     * @see AbstractCollectionAcquisition
     */
    private static final class WriteCollectionAcquisitionImpl<V, C extends Collection<V>>
            extends AbstractCollectionAcquisition<V, C> implements WriteCollectionAcquisition<V, C> {
        /**
         * Constructs the {@linkplain WriteCollectionAcquisitionImpl write collection acquisition implementation}.
         *
         * @param acquirable an acquirable whose state is guarded by the lock
         * @since 1.0
         */
        private WriteCollectionAcquisitionImpl(@NotNull CollectionAcquirable<V, C> acquirable) {
            // There is no need to do nullability checks, the superclass will do that for us
            super(acquirable, AcquisitionType.WRITE);
        }

        @Override
        public boolean add(@NotNull V value) {
            this.runChecks();
            Objects.requireNonNull(value, "The value must not be null");
            return this.acquirable.collection.add(value);
        }

        @Override
        public boolean remove(@NotNull V value) {
            this.runChecks();
            Objects.requireNonNull(value, "The value must not be null");
            return this.acquirable.collection.remove(value);
        }

        @Override
        public boolean addAll(@NotNull Collection<? extends V> collection) {
            this.runChecks();
            Objects.requireNonNull(collection, "The collection must not be null");
            return this.acquirable.collection.addAll(collection);
        }

        @Override
        public boolean removeAll(@NotNull Collection<? extends V> collection) {
            this.runChecks();
            Objects.requireNonNull(collection, "The collection must not be null");
            return this.acquirable.collection.removeAll(collection);
        }

        @Override
        public boolean removeIf(@NotNull Predicate<? super V> predicate) {
            this.runChecks();
            Objects.requireNonNull(predicate, "The predicate must not be null");
            return this.acquirable.collection.removeIf(predicate);
        }

        @Override
        public void clear() {
            this.runChecks();
            this.acquirable.collection.clear();
        }

        @Override
        public boolean retainAll(@NotNull Collection<? extends V> collection) {
            this.runChecks();
            Objects.requireNonNull(collection, "The collection must not be null");
            return this.acquirable.collection.retainAll(collection);
        }
    }

    /**
     * Represents a common implementation of {@linkplain AbstractAcquisition an abstract acquisition}
     * and {@linkplain CollectionAcquisition a collection acquisition}.
     *
     * @param <V> a type of value of the held collection
     * @param <C> a type of the held collection
     * @since 1.0
     * @see CollectionAcquisition
     * @see AbstractAcquisition
     */
    private static abstract class AbstractCollectionAcquisition<V, C extends Collection<V>>
            extends AbstractAcquisition<CollectionAcquisition<V, C>, CollectionAcquirable<V, C>>
            implements CollectionAcquisition<V, C> {
        /**
         * Constructs the {@linkplain AbstractCollectionAcquisition abstract collection acquisition}.
         *
         * @param acquirable an acquirable whose collection is guarded by the lock
         * @param type a type, of which the acquisition should be
         * @since 1.0
         */
        protected AbstractCollectionAcquisition(@NotNull CollectionAcquirable<V, C> acquirable,
                                                @NotNull AcquisitionType type) {
            // There is no need to do nullability checks, the superclass will do that for us
            super(acquirable, type);
        }

        @Override
        public final @NotNull C collection() {
            this.runChecks();
            return this.acquirable.readOnlyView;
        }

        @Override
        protected final @NotNull CollectionAcquisition<V, C> cast() {
            return this;
        }
    }

    /**
     * Represents {@linkplain ReusedCollectionAcquisition a reused collection acquisition}, which reuses an already
     * existing {@linkplain WriteCollectionAcquisition write collection acquisition}.
     *
     * @param <V> a type of value of the following collection
     * @param <C> a type of collection of the collection acquisition that is being reused
     * @since 1.0
     * @see WriteCollectionAcquisition
     * @see ReusedCollectionAcquisition
     */
    private static final class ReusedWriteCollectionAcquisition<V, C extends Collection<V>>
            extends ReusedCollectionAcquisition<V, C, WriteCollectionAcquisition<V, C>>
            implements WriteCollectionAcquisition<V, C> {
        /**
         * Constructs the {@linkplain ReusedWriteCollectionAcquisition reused write collection acquisition}.
         *
         * @param originalAcquisition an original acquisition to create the reused acquisition with
         * @since 1.0
         */
        private ReusedWriteCollectionAcquisition(@NotNull WriteCollectionAcquisition<V, C> originalAcquisition) {
            // There is no need to do nullability checks, the superclass will do that for us
            super(originalAcquisition);
        }

        @Override
        public boolean add(@NotNull V value) {
            // There is no need for a nullability check, the method will do that for us
            return this.originalAcquisition.add(value);
        }

        @Override
        public boolean remove(@NotNull V value) {
            // There is no need for a nullability check, the method will do that for us
            return this.originalAcquisition.remove(value);
        }

        @Override
        public boolean addAll(@NotNull Collection<? extends V> collection) {
            // There is no need for a nullability check, the method will do that for us
            return this.originalAcquisition.addAll(collection);
        }

        @Override
        public boolean removeAll(@NotNull Collection<? extends V> collection) {
            // There is no need for a nullability check, the method will do that for us
            return this.originalAcquisition.removeAll(collection);
        }

        @Override
        public boolean removeIf(@NotNull Predicate<? super V> predicate) {
            // There is no need for a nullability check, the method will do that for us
            return this.originalAcquisition.removeIf(predicate);
        }

        @Override
        public void clear() {
            this.originalAcquisition.clear();
        }

        @Override
        public boolean retainAll(@NotNull Collection<? extends V> collection) {
            // There is no need for a nullability check, the method will do that for us
            return this.originalAcquisition.retainAll(collection);
        }
    }

    /**
     * Represents an implementation of {@linkplain ReusedAcquisition a reused acquisition} and
     * {@linkplain CollectionAcquisition a collection acquisition}.
     *
     * @param <V> a type of value of the following collection
     * @param <C> a type of collection of the collection acquisition that is being reused
     * @param <A> a type of the acquisition that is being reused
     * @since 1.0
     * @see CollectionAcquisition
     * @see ReusedAcquisition
     */
    private static class ReusedCollectionAcquisition<V, C extends Collection<V>, A extends CollectionAcquisition<V, C>>
            extends ReusedAcquisition<A> implements CollectionAcquisition<V, C> {
        /**
         * Constructs the {@linkplain ReusedAcquisition reused acquisition}.
         *
         * @param originalAcquisition an original acquisition to create the reused acquisition with
         * @since 1.0
         */
        protected ReusedCollectionAcquisition(@NotNull A originalAcquisition) {
            // There is no need to do nullability checks, the superclass will do that for us
            super(originalAcquisition);
        }

        @Override
        public @NotNull C collection() {
            return this.originalAcquisition.collection();
        }
    }
}