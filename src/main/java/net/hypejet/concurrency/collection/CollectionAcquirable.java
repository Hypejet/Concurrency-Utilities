package net.hypejet.concurrency.collection;

import net.hypejet.concurrency.Acquirable;
import net.hypejet.concurrency.Acquisition;
import net.hypejet.concurrency.util.iterable.collection.GuardedCollection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

/**
 * Represents {@linkplain Acquirable an acquirable}, which guards {@linkplain Collection a collection}.
 *
 * @param <E> a type of elements of the collection
 * @param <C> a type of the collection
 * @since 1.0
 * @see Acquirable
 */
public abstract class CollectionAcquirable<E, C extends Collection<E>>
        extends Acquirable<CollectionAcquisition<E, C>> {

    private final @NotNull C collection;
    private final @NotNull C readOnlyView;

    /**
     * Constructs the {@linkplain CollectionAcquirable collection acquirable} with no initial
     * elements.
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
    public CollectionAcquirable(@Nullable Collection<E> initialElements) {
        this.collection = this.createCollection(initialElements);
        this.readOnlyView = this.createReadOnlyView(this.collection);
    }

    /**
     * Creates {@linkplain CollectionAcquisition a collection acquisition} of this
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
    public final @NotNull CollectionAcquisition<E, C> acquireRead() {
        CollectionAcquisition<E, C> foundAcquisition = this.findAcquisition();
        if (foundAcquisition != null)
            return new ReusedCollectionAcquisition<>(foundAcquisition);
        return new CollectionAcquisitionImpl<>(this, Acquisition.AcquisitionType.READ);
    }

    /**
     * Creates {@linkplain CollectionAcquisition a collection acquisition} of this
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
    public final @NotNull CollectionAcquisition<E, C> acquireWrite() {
        CollectionAcquisition<E, C> foundAcquisition = this.findAcquisition();
        if (foundAcquisition == null)
            return new CollectionAcquisitionImpl<>(this, Acquisition.AcquisitionType.WRITE);

        if (foundAcquisition.acquisitionType() != Acquisition.AcquisitionType.WRITE) {
            throw new IllegalArgumentException("The caller thread has already created an acquisition," +
                    " but it is not a write acquisition");
        }
        return new ReusedCollectionAcquisition<>(foundAcquisition);
    }

    /**
     * Creates a new mutable instance of the collection.
     *
     * @param initialElements a collection of elements that should be added to the collection during initialization,
     *                        {@code null} if none
     * @return the created instance
     * @since 1.0
     */
    protected abstract @NotNull C createCollection(@Nullable Collection<E> initialElements);

    /**
     * Creates a new read-only view of a collection specified.
     *
     * @param collection the collection to create the read-only view with
     * @return the read-only view created
     * @since 1.0
     */
    protected abstract @NotNull C createReadOnlyView(@NotNull C collection);

    /**
     * Creates a new view of a collection, which is guarded by an acquisition specified. This means that the collection
     * returned should do checks using the acquisition specified with {@link Acquisition#ensurePermittedAndLocked()}.
     *
     * <p>{@link GuardedCollection} is recommended as an implementation of the guarded collection.</p>
     *
     * @param collection a view of the collection - read-only or normal, depending on the acquisition - to create the
     *                   guarded view with
     * @param acquisition an acquisition that guards the collection
     * @return the guarded view
     * @since 1.0
     */
    protected abstract @NotNull C createGuardedView(@NotNull C collection,
                                                    @NotNull CollectionAcquisition<E, C> acquisition);

    /**
     * Represents an implementation of {@linkplain AbstractAcquisition an abstract acquisition} and
     * {@linkplain CollectionAcquisition a collection acquisition}.
     *
     * @param <E> a type of elements of the held collection
     * @param <C> a type of the held collection
     * @param <AE> a type of acquirable that the collection acquisition should be registered in
     * @since 1.0
     * @see CollectionAcquisition
     * @see AbstractAcquisition
     */
    private static final class CollectionAcquisitionImpl
            <E, C extends Collection<E>, AE extends CollectionAcquirable<E, C>>
            extends AbstractAcquisition<CollectionAcquisition<E, C>, AE> implements CollectionAcquisition<E, C> {

        private final C guardedView;

        /**
         * Constructs the {@linkplain CollectionAcquisitionImpl collection acquisition}.
         *
         * @param acquirable an acquirable whose collection is guarded by the lock
         * @param type a type, of which the acquisition should be
         * @since 1.0
         */
        private CollectionAcquisitionImpl(@NotNull AE acquirable, @NotNull AcquisitionType type) {
            super(acquirable, type);

            // Java requires a cast here for some reason
            CollectionAcquirable<E, C> castAcquirable = acquirable;
            this.guardedView = acquirable.createGuardedView(switch (this.acquisitionType()) {
                case READ -> castAcquirable.readOnlyView;
                case WRITE -> castAcquirable.collection;
            }, this.safeCast());
        }

        @Override
        public @NotNull C collection() {
            return this.guardedView;
        }

        @Override
        protected @NotNull CollectionAcquisition<E, C> cast() {
            return this;
        }
    }

    /**
     * Represents an implementation of {@linkplain ReusedAcquisition a reused acquisition} and
     * {@linkplain CollectionAcquisition a collection acquisition}.
     *
     * @param <E> a type of elements of the following collection
     * @param <C> a type of collection of the collection acquisition that is being reused
     * @param <A> a type of the acquisition that is being reused
     * @since 1.0
     * @see CollectionAcquisition
     * @see ReusedAcquisition
     */
    private final static class ReusedCollectionAcquisition
            <E, C extends Collection<E>, A extends CollectionAcquisition<E, C>>
            extends ReusedAcquisition<A> implements CollectionAcquisition<E, C> {
        /**
         * Constructs the {@linkplain ReusedCollectionAcquisition reused collection acquisition}.
         *
         * @param originalAcquisition an original acquisition to create the reused acquisition with
         * @since 1.0
         */
        private ReusedCollectionAcquisition(@NotNull A originalAcquisition) {
            super(originalAcquisition);
        }

        @Override
        public @NotNull C collection() {
            return this.originalAcquisition.collection();
        }
    }
}