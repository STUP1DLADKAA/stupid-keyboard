package com.stupidkeyboard.core.layout

import com.stupidkeyboard.core.model.Key
import com.stupidkeyboard.core.model.KeyRow
import com.stupidkeyboard.core.model.KeyType
import com.stupidkeyboard.core.model.KeyboardLayout

/** Which family of layouts is currently shown. */
enum class LayoutMode { ALPHA, NUMERIC, SYMBOL }

/**
 * Central catalogue of all keyboard layouts.
 *
 * Layouts are plain immutable data, created once at class-init time. The keyboard
 * service never builds layouts on the fly, so switching layouts costs a single list
 * lookup and no allocation.
 */
object LayoutCatalog {

    /** Alphabetic layouts available to the user. The first entry is the default. */
    val alphabetic: List<KeyboardLayout> = listOf(
        EnglishQwerty,
        SpanishQwerty,
        GermanQwertz,
        FrenchAzerty,
    )

    val numeric: List<KeyboardLayout> = listOf(NumericLayout, NumericAltLayout)

    val symbols: List<KeyboardLayout> = listOf(SymbolsLayout, SymbolsAltLayout)

    val defaultAlphabeticId: String = alphabetic.first().id
    val defaultNumericId: String = numeric.first().id
    val defaultSymbolId: String = symbols.first().id

    fun alphabetic(id: String?): KeyboardLayout =
        alphabetic.firstOrNull { it.id == id } ?: alphabetic.first()

    fun numeric(id: String?): KeyboardLayout =
        numeric.firstOrNull { it.id == id } ?: numeric.first()

    fun symbols(id: String?): KeyboardLayout =
        symbols.firstOrNull { it.id == id } ?: symbols.first()

    fun byId(id: String?): KeyboardLayout? = (alphabetic + numeric + symbols).firstOrNull { it.id == id }

    /** All user-selectable alphabetic layouts, for the companion app. */
    fun selectable(): List<KeyboardLayout> = alphabetic

    // ---------------------------------------------------------------------------------
    // Builders
    // ---------------------------------------------------------------------------------

    /**
     * Latin alphabetic layout. Row structure mirrors what users expect from a modern
     * Android keyboard: shift and backspace flank the bottom two letter rows, and the
     * modifier bar sits on the last row.
     */
    private fun latinLayout(
        id: String,
        displayName: String,
        badge: String,
        row1: String,
        row2: String,
        row3: String,
    ): KeyboardLayout {
        fun letters(text: String) = text.map { Key.character(it.toString()) }

        val rows = listOf(
            KeyRow(keys = letters(row1)),
            KeyRow(keys = letters(row2) + Key.of(KeyType.BACKSPACE, weight = 1.5f)),
            KeyRow(
                keys = listOf(Key.of(KeyType.SHIFT, weight = 1.5f)) +
                    letters(row3) +
                    Key.of(KeyType.BACKSPACE, weight = 1.5f)
            ),
            KeyRow(
                keys = listOf(
                    Key.of(KeyType.NUMERIC_MODE, weight = 1.6f),
                    Key.of(KeyType.LANGUAGE, weight = 1.1f),
                    Key.of(KeyType.SPACE, weight = 4.3f),
                    Key.of(KeyType.SYMBOL_MODE, weight = 1.4f),
                    Key.of(KeyType.ENTER, weight = 1.6f),
                )
            ),
        )
        return KeyboardLayout(id = id, displayName = displayName, badge = badge, rows = rows, shiftable = true)
    }

    /** Shared bottom modifier bar for the numeric and symbol modes. */
    private fun modeBar(leading: Key, trailing: Key): KeyRow = KeyRow(
        keys = listOf(
            leading,
            Key.of(KeyType.LANGUAGE, weight = 1.1f),
            Key.of(KeyType.SPACE, weight = 4.3f),
            Key.of(KeyType.BACKSPACE, weight = 1.4f),
            trailing,
        )
    )

    private fun numericLayout(id: String, displayName: String, badge: String, alt: Boolean): KeyboardLayout {
        val digits = "1234567890"
        val row2 = if (alt) "@#\$%&-+()/" else "@#\$%&*()-'"
        val row3 = if (alt) listOf("\"", "*", ":", ";", "!", "?", ",", ".", "+", "=")
        else listOf("\"", "*", ":", ";", "!", "?", ",", ".", "+", "=")

        val rows = listOf(
            KeyRow(keys = digits.map { Key.character(it.toString()) }),
            KeyRow(keys = row2.map { Key.character(it.toString()) }),
            KeyRow(keys = row3.map { Key.character(it) }),
            modeBar(
                leading = Key.of(KeyType.ALPHABETIC_MODE, weight = 1.6f),
                trailing = Key.of(KeyType.SYMBOL_MODE, weight = 1.6f),
            ),
        )
        return KeyboardLayout(id = id, displayName = displayName, badge = badge, rows = rows, shiftable = false)
    }

    private fun symbolsLayout(id: String, displayName: String, badge: String, alt: Boolean): KeyboardLayout {
        val row1 = listOf("[", "]", "{", "}", "#", "%", "^", "*", "+", "=")
        val row2 = if (alt) listOf("_", "\\", "|", "~", "<", ">", "§", "°", "©", "®")
        else listOf("_", "\\", "|", "~", "<", ">", "\$", "€", "£", "¥")
        val row3 = listOf("•", "“", "”", "‘", "’", "—", "…", "`", "/", "&")

        val rows = listOf(
            KeyRow(keys = row1.map { Key.character(it) }),
            KeyRow(keys = row2.map { Key.character(it) }),
            KeyRow(keys = row3.map { Key.character(it) }),
            modeBar(
                leading = Key.of(KeyType.NUMERIC_MODE, weight = 1.6f),
                trailing = Key.of(KeyType.ALPHABETIC_MODE, weight = 1.6f),
            ),
        )
        return KeyboardLayout(id = id, displayName = displayName, badge = badge, rows = rows, shiftable = false)
    }

    // ---------------------------------------------------------------------------------
    // Concrete layouts
    // ---------------------------------------------------------------------------------

    val EnglishQwerty: KeyboardLayout = latinLayout(
        id = "en_qwerty",
        displayName = "English (QWERTY)",
        badge = "EN",
        row1 = "qwertyuiop",
        row2 = "asdfghjkl",
        row3 = "zxcvbnm",
    )

    val SpanishQwerty: KeyboardLayout = latinLayout(
        id = "es_qwerty",
        displayName = "Español (QWERTY)",
        badge = "ES",
        row1 = "qwertyuiop",
        row2 = "asdfghjklñ",
        row3 = "zxcvbnm",
    )

    val GermanQwertz: KeyboardLayout = latinLayout(
        id = "de_qwertz",
        displayName = "Deutsch (QWERTZ)",
        badge = "DE",
        row1 = "qwertzuiopü",
        row2 = "asdfghjklöä",
        row3 = "yxcvbnm",
    )

    val FrenchAzerty: KeyboardLayout = latinLayout(
        id = "fr_azerty",
        displayName = "Français (AZERTY)",
        badge = "FR",
        row1 = "azertyuiop",
        row2 = "qsdfghjklm",
        row3 = "wxcvbn",
    )

    val NumericLayout: KeyboardLayout = numericLayout(
        id = "num_main",
        displayName = "Numbers & symbols",
        badge = "123",
        alt = false,
    )

    val NumericAltLayout: KeyboardLayout = numericLayout(
        id = "num_alt",
        displayName = "Numbers & symbols (alternate)",
        badge = "123",
        alt = true,
    )

    val SymbolsLayout: KeyboardLayout = symbolsLayout(
        id = "sym_main",
        displayName = "Symbols",
        badge = "=\\<",
        alt = false,
    )

    val SymbolsAltLayout: KeyboardLayout = symbolsLayout(
        id = "sym_alt",
        displayName = "Symbols (alternate)",
        badge = "=\\<",
        alt = true,
    )
}
