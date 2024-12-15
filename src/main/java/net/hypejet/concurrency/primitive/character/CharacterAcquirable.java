package net.hypejet.concurrency.primitive.character;

import net.hypejet.concurrency.Acquirable;
import org.jetbrains.annotations.NotNull;

/**
 * Represents {@linkplain Acquirable an acquirable}, which guards a character.
 *
 * @since 1.0
 * @see Acquirable
 */
public final class CharacterAcquirable extends Acquirable<CharacterAcquisition> {

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

    /**
     * Creates {@linkplain CharacterAcquisition a character acquisition} of a character held by this
     * {@linkplain CharacterAcquirable character acquirable} that supports read-only operations.
     *
     * <p>If the caller thread has already created an acquisition a special implementation is used, which reuses it,
     * does nothing when {@link CharacterAcquisition#close()} is called and always returns {@code true} when
     * {@link CharacterAcquisition#isUnlocked()} is called.</p>
     *
     * <p>If the acquisition needs to be unlocked the already existing acquisition needs to be used to do that.</p>
     *
     * @return the acquisition
     * @since 1.0
     */
    @Override
    public @NotNull CharacterAcquisition acquireRead() {
        CharacterAcquisition foundAcquisition = this.findAcquisition();
        if (foundAcquisition != null)
            return new ReusedCharacterAcquisition<>(foundAcquisition);
        return new CharacterAcquisitionImpl(this);
    }

    /**
     * Creates {@linkplain WriteCharacterAcquisition a write character acquisition} of a character held by
     * this {@linkplain CharacterAcquirable character acquirable} that supports write operations.
     *
     * <p>If the caller thread has already created a write acquisition a special implementation is used, which
     * reuses it, does nothing when {@link CharacterAcquisition#close()} is called and always returns {@code true} when
     * {@link CharacterAcquisition#isUnlocked()} is called.</p>
     *
     * <p>If the acquisition needs to be unlocked the already existing acquisition needs to be used to do that.</p>
     *
     * @return the acquisition
     * @since 1.0
     * @throws IllegalArgumentException if the caller thread has already created an acquisition, but it is not a write
     *                                  acquisition
     */
    @Override
    public @NotNull WriteCharacterAcquisition acquireWrite() {
        CharacterAcquisition foundAcquisition = this.findAcquisition();
        if (foundAcquisition == null)
            return new WriteCharacterAcquisitionImpl(this);

        if (!(foundAcquisition instanceof WriteCharacterAcquisition writeAcquisition)) {
            throw new IllegalArgumentException("The caller thread has already created an acquisition," +
                    "but it is not a write acquisition");
        }
        return new ReusedWriteCharacterAcquisition(writeAcquisition);
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
            implements WriteCharacterAcquisition {
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
        public void set(char value) {
            this.runChecks();
            this.acquirable.value = value;
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
            this.runChecks();
            return this.acquirable.value;
        }

        @Override
        protected final @NotNull CharacterAcquisition cast() {
            return this;
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
}