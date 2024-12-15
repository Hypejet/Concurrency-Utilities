package net.hypejet.concurrency.primitive.character;

import net.hypejet.concurrency.Acquisition;
import org.jetbrains.annotations.Contract;

/**
 * Represents {@linkplain Acquisition an acquisition} that allows getting a guarded character.
 *
 * @since 1.0
 * @see Acquisition
 */
public interface CharacterAcquisition extends Acquisition {
    /**
     * Gets the guarded character.
     *
     * @return a value of the character
     * @since 1.0
     */
    @Contract(pure = true)
    char get();
}