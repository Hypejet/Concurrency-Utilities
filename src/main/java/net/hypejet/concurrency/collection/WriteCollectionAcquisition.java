package net.hypejet.concurrency.collection;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.function.Predicate;

/**
 * Represents {@linkplain CollectionAcquisition a collection acquisition}, which allows modifying the guarded
 * {@linkplain Collection collection}.
 *
 * @param <V> a type of value of the collection
 * @param <C> a type of the collection
 * @since 1.0
 * @see CollectionAcquisition
 */
public interface WriteCollectionAcquisition<V, C extends Collection<V>> extends CollectionAcquisition<V, C> {
    /**
     * Adds a value to the collection.
     *
     * @param value the value
     * @return {@code true} if the change was made, {@code false} otherwise
     * @since 1.0
     * @see Collection#add(Object)
     */
    boolean add(@NotNull V value);

    /**
     * Removes a value from the collection.
     *
     * @param value the value
     * @return {@code true} if the change was made, {@code false} otherwise
     * @since 1.0
     * @see Collection#remove(Object)
     */
    boolean remove(@NotNull V value);

    /**
     * Adds all values to the collection from another collection.
     *
     * @param collection the other collection
     * @return {@code true} if the change was made, {@code false} otherwise
     * @since 1.0
     * @see Collection#addAll(Collection)
     */
    boolean addAll(@NotNull Collection<? extends V> collection);

    /**
     * Removes all values from the collection, which are present in another collection.
     *
     * @param collection the other collection
     * @return {@code true} if the change was made, {@code false} otherwise
     * @since 1.0
     * @see Collection#removeAll(Collection)
     */
    boolean removeAll(@NotNull Collection<? extends V> collection);

    /**
     * Removes all values from the collection, which satisfy a predicate.
     *
     * @param predicate the predicate
     * @return {@code true} if the change was made, {@code false} otherwise
     * @since 1.0
     * @see Collection#removeIf(Predicate)
     */
    boolean removeIf(@NotNull Predicate<? super V> predicate);

    /**
     * Removes all values from the collection.
     *
     * @since 1.0
     * @see Collection#clear()
     */
    void clear();

    /**
     * Removes all elements from the collection, which are not present in another collection.
     *
     * @param collection the other collection
     * @since 1.0
     * @see Collection#retainAll(Collection)
     */
    boolean retainAll(@NotNull Collection<? extends V> collection);
}