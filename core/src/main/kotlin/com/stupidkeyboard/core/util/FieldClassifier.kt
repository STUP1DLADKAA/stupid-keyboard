package com.stupidkeyboard.core.util

/**
 * Describes the text field the keyboard is attached to, in privacy-safe terms.
 *
 * Only classification flags are kept — never the contents of the field.
 */
object FieldClassifier {

    // android.text.InputType constants mirrored here so core stays Android-free.
    const val TYPE_CLASS_TEXT = 0x00000001
    const val TYPE_CLASS_NUMBER = 0x00000002
    const val TYPE_CLASS_PHONE = 0x00000003
    const val TYPE_CLASS_DATETIME = 0x00000004

    const val TYPE_TEXT_VARIATION_PASSWORD = 0x00000080
    const val TYPE_TEXT_VARIATION_VISIBLE_PASSWORD = 0x00000090
    const val TYPE_TEXT_VARIATION_WEB_PASSWORD = 0x000000e0
    const val TYPE_NUMBER_VARIATION_PASSWORD = 0x00000010
    const val TYPE_TEXT_VARIATION_EMAIL_ADDRESS = 0x00000020
    const val TYPE_TEXT_VARIATION_URI = 0x00000010
    const val TYPE_TEXT_FLAG_MULTI_LINE = 0x00020000
    const val TYPE_TEXT_FLAG_CAP_SENTENCES = 0x00004000
    const val TYPE_TEXT_FLAG_CAP_WORDS = 0x00002000
    const val TYPE_TEXT_FLAG_CAP_CHARACTERS = 0x00001000
    const val TYPE_TEXT_FLAG_NO_SUGGESTIONS = 0x00080000

    private const val VARIATION_MASK = 0x00000ff0
    private const val CLASS_MASK = 0x0000000f

    fun inputClass(inputType: Int): Int = inputType and CLASS_MASK

    fun variation(inputType: Int): Int = inputType and VARIATION_MASK

    /** Password detection covers numeric PINs and visible-password fields too. */
    fun isPassword(inputType: Int): Boolean {
        val variation = variation(inputType)
        val cls = inputClass(inputType)
        if (cls == TYPE_CLASS_TEXT) {
            return variation == TYPE_TEXT_VARIATION_PASSWORD ||
                variation == TYPE_TEXT_VARIATION_VISIBLE_PASSWORD ||
                variation == TYPE_TEXT_VARIATION_WEB_PASSWORD
        }
        if (cls == TYPE_CLASS_NUMBER) {
            return variation == TYPE_NUMBER_VARIATION_PASSWORD
        }
        return false
    }

    fun isEmailOrUri(inputType: Int): Boolean {
        val variation = variation(inputType)
        return variation == TYPE_TEXT_VARIATION_EMAIL_ADDRESS || variation == TYPE_TEXT_VARIATION_URI
    }

    fun isMultiline(inputType: Int): Boolean =
        inputClass(inputType) == TYPE_CLASS_TEXT && (inputType and TYPE_TEXT_FLAG_MULTI_LINE) != 0

    fun capsMode(inputType: Int): CapsMode {
        if (inputClass(inputType) != TYPE_CLASS_TEXT) return CapsMode.NONE
        return when {
            inputType and TYPE_TEXT_FLAG_CAP_CHARACTERS != 0 -> CapsMode.CHARACTERS
            inputType and TYPE_TEXT_FLAG_CAP_WORDS != 0 -> CapsMode.WORDS
            inputType and TYPE_TEXT_FLAG_CAP_SENTENCES != 0 -> CapsMode.SENTENCES
            else -> CapsMode.NONE
        }
    }

    fun allowsSuggestions(inputType: Int): Boolean {
        if (isPassword(inputType)) return false
        if (inputClass(inputType) != TYPE_CLASS_TEXT) return false
        return inputType and TYPE_TEXT_FLAG_NO_SUGGESTIONS == 0
    }

    enum class CapsMode { NONE, CHARACTERS, WORDS, SENTENCES }
}
