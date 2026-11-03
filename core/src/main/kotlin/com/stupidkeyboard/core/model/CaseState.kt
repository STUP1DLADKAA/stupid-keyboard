package com.stupidkeyboard.core.model

/** Capitalisation state of the alphabetic layouts. */
enum class CaseState {
    /** Lowercase output; the shift key is off. */
    NONE,

    /** Uppercase for the next single character, then back to [NONE]. */
    SHIFT,

    /** Uppercase until the user turns it off. */
    CAPS,
    ;

    val isUpperCase: Boolean
        get() = this != NONE

    /** What a single tap on the shift key should produce. */
    fun tapped(): CaseState = when (this) {
        NONE -> SHIFT
        SHIFT -> CAPS
        CAPS -> NONE
    }

    /** Cycling from the keyboard's perspective: double tap from NONE lands on CAPS. */
    fun afterLetter(autoCapitalise: Boolean): CaseState = when (this) {
        SHIFT -> NONE
        NONE -> if (autoCapitalise) SHIFT else NONE
        CAPS -> CAPS
    }

    /** Shared state after a mode change back to the alphabetic layout. */
    fun afterModeSwitch(autoCapitalise: Boolean): CaseState = when (this) {
        CAPS -> CAPS
        else -> if (autoCapitalise) SHIFT else NONE
    }
}

/**
 * Small helper that decides which layout family to show for a given mode and whether
 * shift has any visual effect there.
 */
object LayoutResolution {
    fun hasVisualShift(shiftable: Boolean, mode: LayoutModeId): Boolean =
        shiftable && mode == LayoutModeId.ALPHA
}

/** Mode identifier kept separate from the Compose-side enum so core stays UI-free. */
enum class LayoutModeId { ALPHA, NUMERIC, SYMBOL }
