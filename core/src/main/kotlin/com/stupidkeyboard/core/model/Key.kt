package com.stupidkeyboard.core.model

/**
 * The behavioural class of a key.
 *
 * Character keys carry their own output text, every other type is a command that the
 * [com.stupidkeyboard.keyboard.KeyboardService] interprets.
 */
enum class KeyType {
    CHARACTER,
    SHIFT,
    BACKSPACE,
    ENTER,
    SPACE,
    LANGUAGE,
    ALPHABETIC_MODE,
    NUMERIC_MODE,
    SYMBOL_MODE,
    ;

    /** Keys that never change their appearance when shift is engaged. */
    val isModifier: Boolean
        get() = this != CHARACTER

    /**
     * True for keys that should repeat while held down. Only backspace repeats today.
     */
    val isRepeatable: Boolean
        get() = this == BACKSPACE
}

/**
 * A single key on the keyboard surface.
 *
 * Keys are immutable value objects. The keyboard view keeps the geometry and the
 * pointer state, never the key itself.
 */
data class Key(
    val type: KeyType = KeyType.CHARACTER,
    /** Text drawn on the key. For character keys this is the lowercase character. */
    val label: String = "",
    /** Text committed to the editor. For character keys the uppercase form is derived. */
    val output: String = "",
    /** Horizontal weight relative to the other keys of the row. */
    val weight: Float = DEFAULT_WEIGHT,
) {

    val isCharacter: Boolean
        get() = type == KeyType.CHARACTER

    /** The label to draw for the current shift state. */
    fun labelFor(shifted: Boolean): String = when {
        shifted && isCharacter -> label.uppercase()
        else -> label
    }

    /** The text to commit for the current shift state. */
    fun outputFor(shifted: Boolean): String = when {
        shifted && isCharacter -> output.uppercase()
        else -> output
    }

    companion object {
        const val DEFAULT_WEIGHT: Float = 1f
        const val MODIFIER_WEIGHT: Float = 1.5f
        const val SPACE_WEIGHT: Float = 4f
        const val LANGUAGE_WEIGHT: Float = 1f

        /** Builds a character key whose label and output are the same text. */
        fun character(text: String, weight: Float = DEFAULT_WEIGHT): Key =
            Key(type = KeyType.CHARACTER, label = text, output = text, weight = weight)

        /** Builds a character key that shows one glyph but commits another (e.g. digits). */
        fun character(label: String, output: String, weight: Float = DEFAULT_WEIGHT): Key =
            Key(type = KeyType.CHARACTER, label = label, output = output, weight = weight)

        fun of(type: KeyType, label: String = "", weight: Float = DEFAULT_WEIGHT): Key =
            Key(type = type, label = label, weight = weight)
    }
}

/** A horizontal row of keys. [sideInset] is expressed in key-weight units. */
data class KeyRow(
    val keys: List<Key>,
    val sideInset: Float = 0f,
) {
    /** Total horizontal weight of the row, including the two side insets. */
    val totalWeight: Float
        get() = keys.sumOf { it.weight.toDouble() }.toFloat() + sideInset * 2f
}

/**
 * A complete keyboard layout for one mode (alphabetic / numeric / symbols).
 *
 * All rows of a layout share the same [KeyRow.totalWeight] so that keys stay aligned
 * across rows. [com.stupidkeyboard.core.layout.RowWeightTest] guards this invariant.
 */
data class KeyboardLayout(
    /** Stable identifier, also used as the language/layout preference value. */
    val id: String,
    /** Human readable name shown in the companion app. */
    val displayName: String,
    /** Short badge shown on the language key, e.g. "EN". */
    val badge: String,
    val rows: List<KeyRow>,
    /** Whether the layout renders uppercase characters when shift is engaged. */
    val shiftable: Boolean = true,
)
