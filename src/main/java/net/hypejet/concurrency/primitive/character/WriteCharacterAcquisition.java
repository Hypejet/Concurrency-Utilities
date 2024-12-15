package net.hypejet.concurrency.primitive.character;

/**
 * Represents {@linkplain CharacterAcquisition a character acquisition} that allows for a character, which is guarded
 * to be updated.
 *
 * @since 1.0
 * @see CharacterAcquisition
 */
public interface WriteCharacterAcquisition extends CharacterAcquisition {
    /**
     * Updates the character.
     *
     * @param value a value to replace the character with
     * @since 1.0
     */
    void set(char value);
}