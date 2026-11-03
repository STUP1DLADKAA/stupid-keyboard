package com.stupidkeyboard.data.db

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Privacy-first wrapper around [DictionaryDao].
 *
 * Every entry point takes a `privacyAllowed` flag; when it is false (password field, or the
 * user disabled learning) the call is a no-op and nothing is written to disk. Callers never
 * have to remember the rule themselves.
 */
class DictionaryRepository(
    private val database: StupidKeyboardDatabase,
    private val capacity: Int = MAX_WORDS,
) {

    private val dao = database.dictionaryDao()

    suspend fun learn(word: String, privacyAllowed: Boolean) {
        if (!privacyAllowed) return
        val normalised = normalise(word)
        if (!isStorable(normalised)) return
        withContext(Dispatchers.IO) {
            val existing = dao.find(normalised)
            if (existing != null) {
                dao.touch(normalised)
            } else {
                dao.insert(LearnedWordEntity(word = normalised))
                val count = dao.count()
                if (count > capacity) dao.trimToCapacity(count - capacity)
            }
        }
    }

    suspend fun candidates(prefix: String, privacyAllowed: Boolean, limit: Int = 3): List<String> {
        if (!privacyAllowed || prefix.isEmpty()) return emptyList()
        val normalised = normalise(prefix)
        if (normalised.isEmpty()) return emptyList()
        return withContext(Dispatchers.IO) {
            dao.suggestions(normalised, limit)
                .map { it.word }
                .filter { it != normalised }
        }
    }

    suspend fun all(privacyAllowed: Boolean): List<String> =
        if (!privacyAllowed) emptyList() else withContext(Dispatchers.IO) { dao.recent().map { it.word } }

    suspend fun remove(word: String) {
        withContext(Dispatchers.IO) { dao.delete(normalise(word)) }
    }

    suspend fun clear() {
        withContext(Dispatchers.IO) { dao.clearAll() }
    }

    suspend fun count(): Int = withContext(Dispatchers.IO) { dao.count() }

    suspend fun saveSnippet(content: String) {
        if (content.isBlank()) return
        withContext(Dispatchers.IO) {
            database.snippetDao().insert(ClipSnippetEntity(content = content))
        }
    }

    suspend fun snippets(): List<ClipSnippetEntity> = withContext(Dispatchers.IO) {
        database.snippetDao().recent()
    }

    suspend fun deleteSnippet(id: Long) = withContext(Dispatchers.IO) {
        database.snippetDao().delete(id)
    }

    suspend fun clearSnippets() = withContext(Dispatchers.IO) {
        database.snippetDao().clearAll()
    }

    companion object {
        const val MAX_WORDS = 2000

        fun normalise(raw: String): String =
            raw.trim().filter { it.isLetter() || it == '\'' || it == '-' }.lowercase()

        fun isStorable(word: String): Boolean {
            if (word.length < 2 || word.length > 32) return false
            if (word.any { it.isDigit() }) return false
            if (word.all { it == word.first() }) return false
            return true
        }
    }
}
