package com.stupidkeyboard.features.text

import android.view.KeyEvent
import android.view.inputmethod.ExtractedTextRequest
import android.view.inputmethod.InputConnection

/**
 * All editor mutations in one place.
 *
 * Keeping the InputConnection calls here makes the privacy story easy to audit: nothing in
 * this object reads or writes the editor contents to disk, to logs or to the network. Text is
 * only ever handed back to the IME's own InputConnection.
 */
object EditorCommands {

    private const val MAX_DELETE_CHUNK = 64

    /** Text is committed in a single call so an app's watcher never sees a partial word. */
    fun commitText(ic: InputConnection?, text: CharSequence): Boolean {
        if (ic == null || text.isEmpty()) return false
        return runCatching { ic.commitText(text, 1) }.getOrDefault(false)
    }

    fun hasSelection(ic: InputConnection?): Boolean {
        if (ic == null) return false
        return runCatching {
            val selected = ic.getSelectedText(0)
            selected != null && selected.isNotEmpty()
        }.getOrDefault(false)
    }

    /** Deletes the selection when there is one, otherwise one character backwards. */
    fun deleteBackwards(ic: InputConnection?): Boolean {
        if (ic == null) return false
        return runCatching {
            if (hasSelection(ic)) {
                ic.commitText("", 1)
            } else {
                ic.deleteSurroundingText(1, 0)
            }
            true
        }.getOrDefault(false)
    }

    /**
     * Bulk delete used by backspace auto-repeat. One call per repeat burst is far cheaper
     * than N individual calls, and the chunk is capped so a long hold cannot stall.
     */
    fun deleteChunk(ic: InputConnection?, count: Int): Boolean {
        if (ic == null || count <= 0) return false
        val amount = count.coerceAtMost(MAX_DELETE_CHUNK)
        return runCatching {
            if (hasSelection(ic)) ic.commitText("", 1) else ic.deleteSurroundingText(amount, 0)
            true
        }.getOrDefault(false)
    }

    /** Deletes the word before the cursor (backspace long-press action). */
    fun deleteWordBackwards(ic: InputConnection?): Boolean {
        if (ic == null) return false
        if (hasSelection(ic)) return deleteBackwards(ic)
        val before = textBeforeCursor(ic, MAX_WORD_SCAN) ?: return deleteBackwards(ic)
        var length = 0
        var i = before.length - 1
        // Skip trailing spaces first, as most editors do.
        while (i >= 0 && before[i].isWhitespace()) {
            length++
            i--
        }
        while (i >= 0 && !before[i].isWhitespace()) {
            length++
            i--
        }
        if (length == 0) return false
        return runCatching { ic.deleteSurroundingText(length, 0) }.getOrDefault(true)
    }

    /** Text of the editor before the cursor, or null when the editor cannot be read. */
    fun textBeforeCursor(ic: InputConnection?, maxChars: Int = 64): CharSequence? {
        if (ic == null) return null
        return runCatching { ic.getTextBeforeCursor(maxChars, 0) }.getOrNull()
    }

    fun textAfterCursor(ic: InputConnection?, maxChars: Int = 64): CharSequence? {
        if (ic == null) return null
        return runCatching { ic.getTextAfterCursor(maxChars, 0) }.getOrNull()
    }

    fun performEnter(ic: InputConnection?, multiline: Boolean): Boolean {
        if (ic == null) return false
        return runCatching {
            if (multiline) {
                ic.commitText("\n", 1)
            } else {
                ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER))
                ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_ENTER))
            }
            true
        }.getOrDefault(false)
    }

    /** Moves the cursor by [delta] characters, clamped to the editor bounds. */
    fun moveCursor(ic: InputConnection?, delta: Int): Boolean {
        if (ic == null || delta == 0) return false
        return runCatching {
            val extracted = ic.getExtractedText(ExtractedTextRequest(), 0)
            val start = extracted?.selectionStart ?: -1
            if (start >= 0) {
                val target = (start + delta).coerceAtLeast(0)
                ic.setSelection(target, target)
            } else {
                val keyCode = if (delta < 0) KeyEvent.KEYCODE_DPAD_LEFT else KeyEvent.KEYCODE_DPAD_RIGHT
                repeat(kotlin.math.abs(delta)) {
                    ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, keyCode))
                    ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_UP, keyCode))
                }
            }
            true
        }.getOrDefault(false)
    }

    fun setSelection(ic: InputConnection?, start: Int, end: Int): Boolean {
        if (ic == null) return false
        return runCatching { ic.setSelection(start, end) }.getOrDefault(false)
    }

    /**
     * Start of the word before the cursor, used for "swipe back to delete word" style
     * gestures. Returns the number of characters to remove.
     */
    fun wordLengthBeforeCursor(ic: InputConnection?): Int {
        val before = textBeforeCursor(ic, MAX_WORD_SCAN) ?: return 0
        var length = 0
        var i = before.length - 1
        while (i >= 0 && before[i].isWhitespace()) {
            length++
            i--
        }
        while (i >= 0 && !before[i].isWhitespace()) {
            length++
            i--
        }
        return length
    }

    private const val MAX_WORD_SCAN = 48
}
