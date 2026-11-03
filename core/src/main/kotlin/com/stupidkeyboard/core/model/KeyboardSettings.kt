package com.stupidkeyboard.core.model

import com.stupidkeyboard.core.layout.LayoutCatalog

/**
 * Every user-tunable keyboard preference.
 *
 * This is the single source of truth shared by the DataStore layer (which persists it)
 * and the keyboard service (which consumes it). The class is a plain immutable value so
 * that it can cross module boundaries without pulling in Android types.
 */
data class KeyboardSettings(
    val alphabeticLayoutId: String = LayoutCatalog.defaultAlphabeticId,
    val numericLayoutId: String = LayoutCatalog.defaultNumericId,
    val symbolLayoutId: String = LayoutCatalog.defaultSymbolId,
    /** Stored theme id, or [ThemeCatalog.AUTO] to follow the system. */
    val themeId: String = ThemeCatalog.AUTO,
    /** Keyboard height as a percentage of the usable screen height (see [HEIGHT_PERCENTS]). */
    val heightPercent: Float = DEFAULT_HEIGHT_PERCENT,
    val hapticEnabled: Boolean = true,
    val hapticStrength: Int = DEFAULT_HAPTIC_STRENGTH,
    val soundEnabled: Boolean = false,
    val keyPreviewEnabled: Boolean = true,
    val popupOnLongPressEnabled: Boolean = true,
    val autoCapitalisationEnabled: Boolean = true,
    val doubleSpacePeriodEnabled: Boolean = true,
    val backspaceRepeatEnabled: Boolean = true,
    val spacebarCursorControlEnabled: Boolean = true,
    /** Show a persistent language switch key instead of relying on the system one. */
    val languageKeyVisible: Boolean = true,
    /** Show the suggestion / quick-action toolbar above the keys. */
    val toolbarEnabled: Boolean = true,
    /** Delay in ms before auto-correct style suggestions are requested. Reserved. */
    val suggestionDelayMs: Int = 200,
    /** Incremented every time the user changes anything; used for the "changed" flag. */
    val onboardingCompleted: Boolean = false,
) {
    val heightFraction: Float
        get() = (heightPercent / 100f).coerceIn(MIN_HEIGHT_PERCENT / 100f, MAX_HEIGHT_PERCENT / 100f)

    companion object {
        const val MIN_HEIGHT_PERCENT: Float = 26f
        const val MAX_HEIGHT_PERCENT: Float = 52f
        const val DEFAULT_HEIGHT_PERCENT: Float = 36f
        const val DEFAULT_HAPTIC_STRENGTH: Int = 60 // 0..100, mapped to an amplitude

        /** Selectable keyboard heights, in percent of the screen height. */
        val HEIGHT_PERCENTS: List<Float> = listOf(28f, 32f, 36f, 40f, 44f, 48f, 52f)
    }
}

/**
 * System-level keyboard toggles that the IME report to us, mirrored into a Snapshot so the
 * keyboard can react without querying InputMethodManager on every key press.
 */
data class InputFlags(
    val autoCapitalisation: Boolean = true,
    val autoCorrection: Boolean = false,
    val suggestions: Boolean = true,
    val inputTypeClass: Int = 0,
    val isPassword: Boolean = false,
    val imeAction: Int = 0,
    val isMultiline: Boolean = false,
    val hasSelection: Boolean = false,
    val textLength: Int = 0,
) {
    /** True when we must not offer any text retention feature (learning, suggestions, history). */
    val isSensitive: Boolean
        get() = isPassword
}
