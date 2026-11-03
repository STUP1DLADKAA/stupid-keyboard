package com.stupidkeyboard.features.input

import com.stupidkeyboard.core.model.CaseState

/**
 * Decides the capitalisation state for a new field or after the cursor moved.
 *
 * Pure logic, no Android types, so the behaviour is unit tested.
 */
object CapitalisationPolicy {

    /**
     * The case the keyboard should adopt given the text before the cursor.
     *
     * @param precedingText text of the editor before the cursor (may be empty)
     * @param capsMode the field's declared caps behaviour
     * @param userEnabled whether auto-capitalisation is enabled in settings
     * @param current the case the keyboard currently displays
     */
    fun resolve(
        precedingText: CharSequence,
        capsMode: CapsBehaviour,
        userEnabled: Boolean,
        current: CaseState,
    ): CaseState {
        if (!userEnabled && capsMode != CapsBehaviour.CHARACTERS) {
            return if (current == CaseState.CAPS) CaseState.CAPS else CaseState.NONE
        }
        return when (capsMode) {
            CapsBehaviour.CHARACTERS -> CaseState.CAPS
            CapsBehaviour.WORDS -> if (isWordStart(precedingText)) CaseState.SHIFT else CaseState.NONE
            CapsBehaviour.SENTENCES -> if (isSentenceStart(precedingText)) CaseState.SHIFT else preserve(current)
            CapsBehaviour.NONE -> if (isSentenceStart(precedingText)) CaseState.SHIFT else CaseState.NONE
        }
    }

    /** Caps-lock survives automatic re-evaluation; a single pending shift does not. */
    private fun preserve(current: CaseState): CaseState =
        if (current == CaseState.CAPS) CaseState.CAPS else CaseState.NONE

    fun isWordStart(text: CharSequence): Boolean {
        if (text.isEmpty()) return true
        val last = text[text.length - 1]
        return last.isWhitespace() || last == '.' || last == ',' || last == '"' || last == '\''
    }

    fun isSentenceStart(text: CharSequence): Boolean {
        if (text.isEmpty()) return true
        var i = text.length - 1
        // Skip trailing whitespace and closing punctuation.
        while (i >= 0) {
            val c = text[i]
            if (c.isWhitespace() || c == '"' || c == '\'' || c == ')' || c == ']' || c == '}') {
                i--
                continue
            }
            break
        }
        // Nothing but whitespace so far: this is the start of the field.
        if (i < 0) return true
        // A terminal sentence punctuation mark means a new sentence begins.
        val c = text[i]
        if (c == '.' || c == '!' || c == '?' || c == '\n') return true
        // Otherwise: capitalise only when there is no letter or digit before the cursor at
        // all (covers emoji-only greetings and empty drafts). One scan, no allocation.
        var j = i
        while (j >= 0) {
            if (text[j].isLetterOrDigit()) return false
            j--
        }
        return true
    }

    /** Translates the core classifier's caps mode into this module's enum. */
    fun fromClassifier(mode: com.stupidkeyboard.core.util.FieldClassifier.CapsMode): CapsBehaviour =
        when (mode) {
            com.stupidkeyboard.core.util.FieldClassifier.CapsMode.NONE -> CapsBehaviour.NONE
            com.stupidkeyboard.core.util.FieldClassifier.CapsMode.CHARACTERS -> CapsBehaviour.CHARACTERS
            com.stupidkeyboard.core.util.FieldClassifier.CapsMode.WORDS -> CapsBehaviour.WORDS
            com.stupidkeyboard.core.util.FieldClassifier.CapsMode.SENTENCES -> CapsBehaviour.SENTENCES
        }

    enum class CapsBehaviour { NONE, CHARACTERS, WORDS, SENTENCES }
}
