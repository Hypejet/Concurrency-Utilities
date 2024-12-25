package net.hypejet.concurrency.util.guard;

import net.hypejet.concurrency.Acquisition;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * Represents {@linkplain Object an object} wrapper, which ensures that {@linkplain Acquisition an acquisition} is
 * locked and a caller thread has a permission to it during doing any operation as well as forwards all default object
 * method calls to the wrapped object.
 *
 * @param <O> a type of the wrapped object
 * @since 1.0
 * @see Object
 */
public class GuardedObject<O> {

    protected final O delegate;
    protected final Acquisition acquisition;

    /**
     * Constructs the {@linkplain GuardedObject guarded object}.
     *
     * @param delegate the object that should be wrapped
     * @param acquisition an acquisition that should guard the object
     */
    public GuardedObject(@NotNull O delegate, @NotNull Acquisition acquisition) {
        this.delegate = Objects.requireNonNull(delegate, "The delegate must not be null");
        this.acquisition = Objects.requireNonNull(acquisition, "The acquisition must not be null");
    }

    @Override
    public final int hashCode() {
        this.acquisition.ensurePermittedAndLocked();
        return this.delegate.hashCode();
    }

    @Override
    public final boolean equals(Object obj) {
        this.acquisition.ensurePermittedAndLocked();
        return this.delegate.equals(obj);
    }

    @Override
    public String toString() {
        this.acquisition.ensurePermittedAndLocked();
        return this.delegate.toString();
    }
}