package net.hypejet.concurrency.empty;

import net.hypejet.concurrency.Acquirable;
import net.hypejet.concurrency.Acquisition;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents {@linkplain Acquirable an acquirable}, which does not contain any state.
 *
 * @since 1.0
 * @see Acquirable
 */
public final class EmptyAcquirable extends Acquirable<EmptyAcquisition, EmptyAcquisition> {
    @Override
    protected @NotNull EmptyAcquisition createReadAcquisition() {
        return new EmptyAcquisitionImpl(this, Acquisition.AcquisitionType.READ);
    }

    @Override
    protected @NotNull EmptyAcquisition createWriteAcquisition() {
        return new EmptyAcquisitionImpl(this, Acquisition.AcquisitionType.WRITE);
    }

    @Override
    protected @NotNull EmptyAcquisition reuseReadAcquisition(@NotNull EmptyAcquisition originalAcquisition) {
        return new ReusedEmptyAcquisition(originalAcquisition);
    }

    @Override
    protected @NotNull EmptyAcquisition reuseWriteAcquisition(@NotNull EmptyAcquisition originalAcquisition) {
        return new ReusedEmptyAcquisition(originalAcquisition);
    }

    @Override
    protected @NotNull EmptyAcquisition createUpgradedAcquisition(@NotNull EmptyAcquisition originalAcquisition) {
        return originalAcquisition;
    }

    @Override
    protected @Nullable EmptyAcquisition castToWriteAcquisition(@NotNull EmptyAcquisition acquisition) {
        return switch (acquisition.acquisitionType()) {
            case READ -> null;
            case WRITE -> acquisition;
        };
    }

    /**
     * Represents an implementation of {@linkplain AbstractAcquisition an abstract acquisition}
     * and {@linkplain EmptyAcquisition an empty acquisition}.
     *
     * @since 1.0
     * @see EmptyAcquisition
     * @see AbstractAcquisition
     */
    private static final class EmptyAcquisitionImpl extends AbstractAcquisition<EmptyAcquisition, EmptyAcquirable>
            implements EmptyAcquisition {
        /**
         * Constructs the {@linkplain EmptyAcquisitionImpl abstract empty acquisition}.
         *
         * @param acquirable an acquirable whose state is guarded by the lock
         * @param type a type, of which the acquisition should be
         * @since 1.0
         */
        private EmptyAcquisitionImpl(@NotNull EmptyAcquirable acquirable, @NotNull AcquisitionType type) {
            // There is no need to check whether the acquirable is null, the superclass will do that for us
            super(acquirable, type);
        }

        @Override
        protected @NotNull EmptyAcquisition cast() {
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