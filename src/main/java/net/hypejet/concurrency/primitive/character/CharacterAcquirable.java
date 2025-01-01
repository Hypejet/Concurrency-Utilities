package net.hypejet.concurrency.primitive.character;

import net.hypejet.concurrency.Acquirable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * Represents {@linkplain Acquirable an acquirable}, which guards a character.
 *
 * @since 1.0
 * @see Acquirable
 */
public final class CharacterAcquirable extends Acquirable<CharacterAcquisition, WriteCharacterAcquisition> {

    private char value;

    /**
     * Constructs the {@linkplain CharacterAcquisitionImpl boo acquisition}.
     *
     * @param value an initial value that the acquirable should have
     * @since 1.0
     */
    public CharacterAcquirable(char value) {
        this.value = value;
    }

    @Override
    protected @NotNull CharacterAcquisition createReadAcquisition() {
        return new CharacterAcquisitionImpl(this);
    }

    @Override
    protected @NotNull WriteCharacterAcquisition createWriteAcquisition() {
        return new WriteCharacterAcquisitionImpl(this);
    }

    @Override
    protected @NotNull CharacterAcquisition reuseReadAcquisition(@NotNull CharacterAcquisition originalAcquisition) {
        return new ReusedCharacterAcquisition<>(originalAcquisition);
    }

    @Override
    protected @NotNull WriteCharacterAcquisition reuseWriteAcquisition(
            @NotNull WriteCharacterAcquisition originalAcquisition
    ) {
        return new ReusedWriteCharacterAcquisition(originalAcquisition);
    }

    @Override
    protected @NotNull WriteCharacterAcquisition createUpgradedAcquisition(
            @NotNull CharacterAcquisition originalAcquisition
    ) {
        return new UpgradedCharacterAcquisition(originalAcquisition, this);
    }

    @Override
    protected @Nullable WriteCharacterAcquisition castToWriteAcquisition(@NotNull CharacterAcquisition acquisition) {
        if (acquisition instanceof WriteCharacterAcquisition castAcquisition)
            return castAcquisition;
        return null;
    }

    /**
     * Represents an implementation of {@linkplain AbstractCharacterAcquisition an abstract character acquisition}.
     *
     * @since 1.0
     * @see AbstractCharacterAcquisition
     */
    private static final class CharacterAcquisitionImpl extends AbstractCharacterAcquisition {
        /**
         * Constructs the {@linkplain CharacterAcquisitionImpl character acquisition implementation}.
         *
         * @param acquirable an acquirable character whose value is guarded by the lock
         * @since 1.0
         */
        private CharacterAcquisitionImpl(@NotNull CharacterAcquirable acquirable) {
            super(acquirable, AcquisitionType.READ);
        }
    }

    /**
     * Represents an implementation of {@linkplain AbstractCharacterAcquisition an abstract character acquisition}
     * and {@linkplain WriteCharacterAcquisition a write character acquisition}.
     *
     * @since 1.0
     * @see WriteCharacterAcquisition
     * @see AbstractCharacterAcquisition
     */
    private static final class WriteCharacterAcquisitionImpl extends AbstractCharacterAcquisition
            implements WriteCharacterAcquisition, SetOperationImplementation {
        /**
         * Constructs the {@linkplain WriteCharacterAcquisitionImpl write character acquisition implementation}.
         *
         * @param acquirable an acquirable character whose value is guarded by the lock
         * @since 1.0
         */
        private WriteCharacterAcquisitionImpl(@NotNull CharacterAcquirable acquirable) {
            // There is no need to check whether the acquirable is null, the superclass will do that for us
            super(acquirable, AcquisitionType.WRITE);
        }

        @Override
        public @NotNull CharacterAcquirable acquirable() {
            return this.acquirable;
        }
    }

    /**
     * Represents a common implementation of {@linkplain AbstractAcquisition an abstract acquisition}
     * and {@linkplain CharacterAcquisition a character acquisition}.
     *
     * @since 1.0
     * @see CharacterAcquisition
     * @see AbstractAcquisition
     */
    private static abstract class AbstractCharacterAcquisition
            extends AbstractAcquisition<CharacterAcquisition, CharacterAcquirable>
            implements CharacterAcquisition {
        /**
         * Constructs the {@linkplain AbstractCharacterAcquisition abstract character acquisition}.
         *
         * @param acquirable an acquirable whose character is guarded by the lock
         * @param type a type, of which the acquisition should be
         * @since 1.0
         */
        protected AbstractCharacterAcquisition(@NotNull CharacterAcquirable acquirable, @NotNull AcquisitionType type) {
            // There is no need to check whether the acquirable is null, the superclass will do that for us
            super(acquirable, type);
        }

        @Override
        public final char get() {
            this.ensurePermittedAndLocked();
            return this.acquirable.value;
        }

        @Override
        protected final @NotNull CharacterAcquisition cast() {
            return this;
        }
    }

    /**
     * Represents {@linkplain ReusedCharacterAcquisition a reused character acquisition}, which reuses
     * {@linkplain CharacterAcquisition a character acquisition}, whose lock has been upgraded to a write lock.
     *
     * @since 1.0
     * @see CharacterAcquisition
     * @see ReusedCharacterAcquisition
     */
    private static final class UpgradedCharacterAcquisition extends ReusedCharacterAcquisition<CharacterAcquisition>
            implements WriteCharacterAcquisition, SetOperationImplementation {

        private final CharacterAcquirable acquirable;

        /**
         * Constructs the {@linkplain UpgradedCharacterAcquisition upgraded character acquisition}.
         *
         * @param originalAcquisition an original acquisition to create the reused acquisition with
         * @param acquirable an acquirable that owns the original acquisition
         * @since 1.0
         */
        private UpgradedCharacterAcquisition(@NotNull CharacterAcquisition originalAcquisition,
                                             @NotNull CharacterAcquirable acquirable) {
            super(originalAcquisition);
            this.acquirable = Objects.requireNonNull(acquirable, "The acquirable must not be null");
        }

        @Override
        public @NotNull CharacterAcquirable acquirable() {
            return this.acquirable;
        }
    }

    /**
     * Represents {@linkplain ReusedCharacterAcquisition a reused character acquisition}, which reuses an already
     * existing {@linkplain WriteCharacterAcquisition write character acquisition}.
     *
     * @since 1.0
     * @see WriteCharacterAcquisition
     * @see ReusedCharacterAcquisition
     */
    private static final class ReusedWriteCharacterAcquisition
            extends ReusedCharacterAcquisition<WriteCharacterAcquisition>
            implements WriteCharacterAcquisition {
        /**
         * Constructs the {@linkplain ReusedWriteCharacterAcquisition reused write character acquisition}.
         *
         * @param originalAcquisition an original acquisition to create the reused acquisition with
         * @since 1.0
         */
        private ReusedWriteCharacterAcquisition(@NotNull WriteCharacterAcquisition originalAcquisition) {
            // There is no need to do nullability checks, the superclass will do that for us
            super(originalAcquisition);
        }

        @Override
        public void set(char value) {
            this.originalAcquisition.set(value);
        }
    }

    /**
     * Represents an implementation of {@linkplain ReusedAcquisition a reused acquisition} and
     * {@linkplain CharacterAcquisition a character acquisition}.
     *
     * @param <A> a type of the acquisition that is being reused
     * @since 1.0
     * @see CharacterAcquisition
     * @see ReusedAcquisition
     */
    private static class ReusedCharacterAcquisition<A extends CharacterAcquisition> extends ReusedAcquisition<A>
            implements CharacterAcquisition {
        /**
         * Constructs the {@linkplain ReusedCharacterAcquisition reused character acquisition}.
         *
         * @param originalAcquisition an original acquisition to create the reused acquisition with
         * @since 1.0
         */
        private ReusedCharacterAcquisition(@NotNull A originalAcquisition) {
            // There is no need to do nullability checks, the superclass will do that for us
            super(originalAcquisition);
        }

        @Override
        public final char get() {
            return this.originalAcquisition.get();
        }
    }

    /**
     * Represents {@linkplain WriteCharacterAcquisition a write character acquisition} with
     * the {@linkplain WriteCharacterAcquisition#set(char) set operation} implemented.
     *
     * @since 1.0
     * @see WriteCharacterAcquisition
     */
    private interface SetOperationImplementation extends WriteCharacterAcquisition {
        @Override
        default void set(char value) {
            this.ensurePermittedAndLocked();
            this.acquirable().value = value;
        }

        /**
         * Gets {@linkplain CharacterAcquirable a character acquirable} that owns this acquisition.
         *
         * @return the character acquirable
         * @since 1.0
         */
        @NotNull CharacterAcquirable acquirable();
    }
}