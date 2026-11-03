package com.stupidkeyboard.core.util

/**
 * Bounded-memory history of recently typed words.
 *
 * Used only when the user explicitly enables "learn my words" in settings and never for
 * password fields. It stores nothing but whole words the user committed with a space or
 * a punctuation key, caps itself to [capacity] entries, and can be wiped at any time.
 *
 * The class is deliberately allocation-light and safe to call from the input thread.
 */
class WordHistory(val capacity: Int = 500) {

    private val order = ArrayDeque<String>(capacity)
    private val index = HashMap<String, Int>(capacity)

    val size: Int
        get() = index.size

    fun record(word: String) {
        val normalised = normalise(word)
        if (normalised.length < 2 || normalised.length > MAX_WORD_LENGTH) return
        if (!isLearnisable(normalised)) return
        if (index.containsKey(normalised)) {
            // Touch the entry so it stays in the recency window.
            order.remove(normalised)
            order.addLast(normalised)
            return
        }
        order.addLast(normalised)
        index[normalised] = normalised.length
        while (order.size > capacity) {
            val evicted = order.removeFirst()
            index.remove(evicted)
        }
    }

    /** Returns up to [limit] words that start with [prefix], most recent last. */
    fun candidates(prefix: String, limit: Int = 3): List<String> {
        if (prefix.isEmpty()) return emptyList()
        val lower = prefix.lowercase()
        val result = ArrayList<String>(limit)
        for (i in order.indices.reversed()) {
            val word = order[i]
            if (word.length != prefix.length && word.startsWith(lower)) {
                result.add(word)
                if (result.size == limit) break
            }
        }
        return result
    }

    fun contains(word: String): Boolean = index.containsKey(normalise(word))

    fun clear() {
        order.clear()
        index.clear()
    }

    fun snapshot(): List<String> = order.toList()

    companion object {
        const val MAX_WORD_LENGTH = 32

        fun normalise(raw: String): String =
            raw.trim().filter { it.isLetter() || it == '\'' || it == '-' }.lowercase()

        /** Words made of a single repeated letter ("aaa") or pure numbers teach nothing. */
        fun isLearnisable(word: String): Boolean {
            if (word.any { it.isDigit() }) return false
            if (word.all { it == word.first() }) return false
            return true
        }
    }
}
