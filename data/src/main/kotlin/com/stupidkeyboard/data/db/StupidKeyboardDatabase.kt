package com.stupidkeyboard.data.db

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Index
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * A word the user has explicitly chosen to teach the keyboard.
 *
 * Nothing is written here unless the user enables "Learn my words" *and* the focused field
 * is not a password field. Words are stored lowercase and capped in number.
 */
@Entity(
    tableName = "learned_words",
    indices = [Index(value = ["word"], unique = true)],
)
data class LearnedWordEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val word: String,
    val usageCount: Int = 1,
    /** Epoch millis of the last use, used for LRU eviction. */
    val lastUsedAt: Long = System.currentTimeMillis(),
)

/** Text snippet the user saves deliberately from the clipboard toolbar action. */
@Entity(tableName = "clip_snippets")
data class ClipSnippetEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val content: String,
    val createdAt: Long = System.currentTimeMillis(),
)

@Dao
interface DictionaryDao {

    @Query("SELECT * FROM learned_words WHERE word LIKE :prefix || '%' ORDER BY usageCount DESC, lastUsedAt DESC LIMIT :limit")
    suspend fun suggestions(prefix: String, limit: Int = 3): List<LearnedWordEntity>

    @Query("SELECT * FROM learned_words WHERE word = :word LIMIT 1")
    suspend fun find(word: String): LearnedWordEntity?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(word: LearnedWordEntity): Long

    @Query("UPDATE learned_words SET usageCount = usageCount + 1, lastUsedAt = :now WHERE word = :word")
    suspend fun touch(word: String, now: Long = System.currentTimeMillis())

    @Query("DELETE FROM learned_words WHERE id IN (SELECT id FROM learned_words ORDER BY lastUsedAt ASC LIMIT :count)")
    suspend fun trimToCapacity(count: Int)

    @Query("SELECT COUNT(*) FROM learned_words")
    suspend fun count(): Int

    @Query("DELETE FROM learned_words")
    suspend fun clearAll()

    @Query("DELETE FROM learned_words WHERE word = :word")
    suspend fun delete(word: String)

    @Query("SELECT * FROM learned_words ORDER BY lastUsedAt DESC LIMIT :limit")
    suspend fun recent(limit: Int = 200): List<LearnedWordEntity>
}

@Dao
interface SnippetDao {

    @Query("SELECT * FROM clip_snippets ORDER BY createdAt DESC LIMIT :limit")
    suspend fun recent(limit: Int = 20): List<ClipSnippetEntity>

    @Insert
    suspend fun insert(snippet: ClipSnippetEntity): Long

    @Query("DELETE FROM clip_snippets WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("DELETE FROM clip_snippets")
    suspend fun clearAll()
}

@Database(
    entities = [LearnedWordEntity::class, ClipSnippetEntity::class],
    version = 1,
    exportSchema = true,
)
abstract class StupidKeyboardDatabase : RoomDatabase() {

    abstract fun dictionaryDao(): DictionaryDao

    abstract fun snippetDao(): SnippetDao

    companion object {
        private const val NAME = "stupid_keyboard.db"

        @Volatile
        private var instance: StupidKeyboardDatabase? = null

        fun get(context: Context): StupidKeyboardDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    StupidKeyboardDatabase::class.java,
                    NAME,
                )
                    // The database only holds data the user chose to store, so a wipe is an
                    // acceptable fallback if a migration is ever missing.
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { instance = it }
            }
    }
}
