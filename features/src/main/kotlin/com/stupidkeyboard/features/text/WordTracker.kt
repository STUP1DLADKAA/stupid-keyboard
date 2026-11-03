package com.stupidkeyboard.features.text

import com.stupidkeyboard.core.util.WordHistory

/**
 * Tracks the word currently being composed so the keyboard can show suggestions and so
 * punctuation / spacing rules can look at what came before.
 *
 * Holds only the current in-flight word plus a bounded [WordHistory]; it never keeps a copy
 * of the whole editor text and is cleared whenever the field changes.
 */
class WordTracker(private val history: WordHistory = WordHistory()) {

    private val builder = StringBuilder(24)

    /** The partial word before the cursor, lowercase. Never logged. */
    val currentWord: String
        get() = builder.toString()

    val hasWord: Boolean
        get() = builder.isNotEmpty()

    val wordLength: Int
        get() = builder.length

    fun onLetter(letter: Char) {
        if (builder.length >= MAX_LENGTH) return
        builder.append(letter)
    }

    fun onDelete() {
        if (builder.isNotEmpty()) builder.setLength(builder.length - 1)
    }

    /** Called when a word boundary is committed (space, punctuation, enter). */
    fun commitBoundary(): String? {
        if (builder.isEmpty()) return null
        val word = builder.toString()
        builder.setLength(0)
        return word.takeIf { it.length >= 2 }
    }

    fun clear() {
        builder.setLength(0)
    }

    fun remember(word: String) {
        history.record(word)
    }

    fun suggestions(limit: Int = 3): List<String> {
        if (builder.length < 2) return emptyList()
        return history.candidates(builder.toString(), limit)
    }

    fun historySnapshot(): List<String> = history.snapshot()

    fun clearHistory() = history.clear()

    /** Mirrors the editor when the cursor moves: keeps only the trailing word. */
    fun syncWithPrecedingText(text: CharSequence) {
        builder.setLength(0)
        var i = text.length - 1
        while (i >= 0 && builder.length < MAX_LENGTH) {
            val c = text[i]
            if (!c.isLetter() && c != '\'' && c != '-') break
            builder.insert(0, c)
            i--
        }
    }

    companion object {
        const val MAX_LENGTH = 32
    }
}
