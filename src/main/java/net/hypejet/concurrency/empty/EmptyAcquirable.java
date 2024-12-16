package net.hypejet.concurrency.empty;

import net.hypejet.concurrency.Acquirable;
import org.jetbrains.annotations.NotNull;

/**
 * Represents {@linkplain Acquirable an acquirable}, which does not contain any state.
 *
 * @since 1.0
 * @see Acquirable
 */
public final class EmptyAcquirable extends Acquirable<EmptyAcquisition> {
    /**
     * Creates {@linkplain EmptyAcquisition an empty acquisition} of this {@linkplain EmptyAcquirable empty acquirable}
     * that supports read-only operations.
     *
     * <p>If the caller thread has already created an acquisition a special implementation is used, which reuses it,
     * does nothing when {@link EmptyAcquisition#close()} is called and always returns {@code true} when
     * {@link EmptyAcquisition#isUnlocked()} is called.</p>
     *
     * <p>If the acquisition needs to be unlocked the already existing acquisition needs to be used to do that.</p>
     *
     * @return the acquisition
     * @since 1.0
     */
    @Override
    public @NotNull EmptyAcquisition acquireRead() {
        EmptyAcquisition foundAcquisition = this.findAcquisition();
        if (foundAcquisition != null)
            return new ReusedEmptyAcquisition(foundAcquisition);
        return new ReadEmptyAcquisition(this);
    }

    /**
     * Creates {@linkplain EmptyAcquisition an empty acquisition} of this {@linkplain EmptyAcquirable empty acquirable}
     * that supports write operations.
     *
     * <p>If the caller thread has already created a write acquisition a special implementation is used, which
     * reuses it, does nothing when {@link EmptyAcquisition#close()} is called and always returns {@code true} when
     * {@link EmptyAcquisition#isUnlocked()} is called.</p>
     *
     * <p>If the acquisition needs to be unlocked the already existing acquisition needs to be used to do that.</p>
     *
     * @return the acquisition
     * @since 1.0
     * @throws IllegalArgumentException if the caller thread has already created an acquisition, but it is not a write
     *                                  acquisition
     */
    @Override
    public @NotNull EmptyAcquisition acquireWrite() {
        EmptyAcquisition foundAcquisition = this.findAcquisition();
        if (foundAcquisition == null)
            return new WriteEmptyAcquisition(this);

        if (!(foundAcquisition instanceof WriteEmptyAcquisition writeAcquisition)) {
            throw new IllegalArgumentException("The caller thread has already created an acquisition," +
                    " but it is not a write acquisition");
        }
        return new ReusedEmptyAcquisition(writeAcquisition);
    }

    /**
     * Represents an implementation of {@linkplain AbstractEmptyAcquisition an abstract empty acquisition}, which
     * acquires a read lock.
     *
     * @since 1.0
     * @see AbstractEmptyAcquisition
     */
    private static final class ReadEmptyAcquisition extends AbstractEmptyAcquisition {
        /**
         * Constructs the {@linkplain ReadEmptyAcquisition read empty acquisition}.
         *
         * @param acquirable an acquirable whose state is guarded by the lock
         * @since 1.0
         */
        private ReadEmptyAcquisition(@NotNull EmptyAcquirable acquirable) {
            // There is no need to check whether the acquirable is null, the superclass will do that for us
            super(acquirable, AcquisitionType.READ);
        }
    }

    /**
     * Represents an implementation of {@linkplain AbstractEmptyAcquisition an abstract empty acquisition}, which
     * acquires a write lock.
     *
     * @since 1.0
     * @see AbstractEmptyAcquisition
     */
    private static final class WriteEmptyAcquisition extends AbstractEmptyAcquisition {
        /**
         * Constructs the {@linkplain WriteEmptyAcquisition write empty acquisition}.
         *
         * @param acquirable an acquirable whose state is guarded by the lock
         * @since 1.0
         */
        private WriteEmptyAcquisition(@NotNull EmptyAcquirable acquirable) {
            // There is no need to check whether the acquirable is null, the superclass will do that for us
            super(acquirable, AcquisitionType.WRITE);
        }
    }

    /**
     * Represents a common implementation of {@linkplain AbstractAcquisition an abstract acquisition}
     * and {@linkplain EmptyAcquisition an empty acquisition}.
     *
     * @since 1.0
     * @see EmptyAcquisition
     * @see AbstractAcquisition
     */
    private static abstract class AbstractEmptyAcquisition
            extends AbstractAcquisition<EmptyAcquisition, EmptyAcquirable>
            implements EmptyAcquisition {
        /**
         * Constructs the {@linkplain AbstractEmptyAcquisition abstract empty acquisition}.
         *
         * @param acquirable an acquirable whose state is guarded by the lock
         * @param type a type, of which the acquisition should be
         * @since 1.0
         */
        private AbstractEmptyAcquisition(@NotNull EmptyAcquirable acquirable, @NotNull AcquisitionType type) {
            // There is no need to check whether the acquirable is null, the superclass will do that for us
            super(acquirable, type);
        }

        @Override
        protected final @NotNull EmptyAcquisition cast() {
            return this;
        }
    }

    /**
     * Represents an implementation of {@linkplain ReusedAcquisition a reused acquisition} and
     * {@linkplain EmptyAcquisition an empty acquisition}
     *
     * @since 1.0
     * @see EmptyAcquisition
     * @see ReusedAcquisition
     */
    private static final class ReusedEmptyAcquisition extends ReusedAcquisition<EmptyAcquisition>
            implements EmptyAcquisition {
        /**
         * Constructs the {@linkplain ReusedAcquisition reused acquisition}.
         *
         * @param originalAcquisition an original acquisition to create the reused acquisition with
         * @since 1.0
         */
        private ReusedEmptyAcquisition(@NotNull EmptyAcquisition originalAcquisition) {
            // There is no need to check whether the acquirable is null, the superclass will do that for us
            super(originalAcquisition);
        }
    }
}