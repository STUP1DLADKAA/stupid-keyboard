package com.stupidkeyboard.data.prefs

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.stupidkeyboard.core.model.KeyboardSettings
import com.stupidkeyboard.core.model.ThemeCatalog
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import java.io.IOException

/**
 * Persistence for every keyboard preference.
 *
 * DataStore is the only writer, so the IME and the companion app always observe the same
 * values. Reads are exposed as a cold [Flow] that is safe to collect from the main thread;
 * the actual disk I/O runs on DataStore's own dispatcher.
 */
class SettingsRepository private constructor(context: Context) {

    private val store: DataStore<Preferences> = context.applicationContext.settingsDataStore

    val settings: Flow<KeyboardSettings> = store.data
        .catch { throwable ->
            // A corrupted file should never crash the keyboard: fall back to defaults.
            if (throwable is IOException) emit(emptyPreferences()) else throw throwable
        }
        .map { prefs -> prefs.toSettings() }
        .distinctUntilChanged()

    suspend fun update(transform: (KeyboardSettings) -> KeyboardSettings) {
        store.edit { prefs ->
            val updated = transform(prefs.toSettings())
            prefs.write(updated)
        }
    }

    suspend fun resetToDefaults() {
        store.edit { prefs -> prefs.write(KeyboardSettings()) }
    }

    companion object {
        private val Context.settingsDataStore by preferencesDataStore(name = "stupid_keyboard_settings")

        @Volatile
        private var instance: SettingsRepository? = null

        fun get(context: Context): SettingsRepository =
            instance ?: synchronized(this) {
                instance ?: SettingsRepository(context).also { instance = it }
            }
    }

    // -------------------------------------------------------------------------------------
    // Keys
    // -------------------------------------------------------------------------------------

    private object Keys {
        val alphabeticLayout = stringPreferencesKey("alphabetic_layout")
        val numericLayout = stringPreferencesKey("numeric_layout")
        val symbolLayout = stringPreferencesKey("symbol_layout")
        val theme = stringPreferencesKey("theme")
        val heightPercent = floatPreferencesKey("height_percent")
        val hapticEnabled = booleanPreferencesKey("haptic_enabled")
        val hapticStrength = intPreferencesKey("haptic_strength")
        val soundEnabled = booleanPreferencesKey("sound_enabled")
        val keyPreview = booleanPreferencesKey("key_preview")
        val popupOnLongPress = booleanPreferencesKey("popup_long_press")
        val autoCapitalisation = booleanPreferencesKey("auto_capitalisation")
        val doubleSpacePeriod = booleanPreferencesKey("double_space_period")
        val backspaceRepeat = booleanPreferencesKey("backspace_repeat")
        val spacebarCursor = booleanPreferencesKey("spacebar_cursor")
        val languageKey = booleanPreferencesKey("language_key")
        val toolbar = booleanPreferencesKey("toolbar")
        val suggestionDelay = intPreferencesKey("suggestion_delay")
        val onboarding = booleanPreferencesKey("onboarding_completed")
    }

    private fun Preferences.toSettings(): KeyboardSettings {
        val defaults = KeyboardSettings()
        return KeyboardSettings(
            alphabeticLayoutId = this[Keys.alphabeticLayout] ?: defaults.alphabeticLayoutId,
            numericLayoutId = this[Keys.numericLayout] ?: defaults.numericLayoutId,
            symbolLayoutId = this[Keys.symbolLayout] ?: defaults.symbolLayoutId,
            themeId = this[Keys.theme] ?: ThemeCatalog.AUTO,
            heightPercent = this[Keys.heightPercent] ?: defaults.heightPercent,
            hapticEnabled = this[Keys.hapticEnabled] ?: defaults.hapticEnabled,
            hapticStrength = this[Keys.hapticStrength] ?: defaults.hapticStrength,
            soundEnabled = this[Keys.soundEnabled] ?: defaults.soundEnabled,
            keyPreviewEnabled = this[Keys.keyPreview] ?: defaults.keyPreviewEnabled,
            popupOnLongPressEnabled = this[Keys.popupOnLongPress] ?: defaults.popupOnLongPressEnabled,
            autoCapitalisationEnabled = this[Keys.autoCapitalisation] ?: defaults.autoCapitalisationEnabled,
            doubleSpacePeriodEnabled = this[Keys.doubleSpacePeriod] ?: defaults.doubleSpacePeriodEnabled,
            backspaceRepeatEnabled = this[Keys.backspaceRepeat] ?: defaults.backspaceRepeatEnabled,
            spacebarCursorControlEnabled = this[Keys.spacebarCursor] ?: defaults.spacebarCursorControlEnabled,
            languageKeyVisible = this[Keys.languageKey] ?: defaults.languageKeyVisible,
            toolbarEnabled = this[Keys.toolbar] ?: defaults.toolbarEnabled,
            suggestionDelayMs = this[Keys.suggestionDelay] ?: defaults.suggestionDelayMs,
            onboardingCompleted = this[Keys.onboarding] ?: defaults.onboardingCompleted,
        )
    }

    private fun androidx.datastore.preferences.core.MutablePreferences.write(settings: KeyboardSettings) {
        this[Keys.alphabeticLayout] = settings.alphabeticLayoutId
        this[Keys.numericLayout] = settings.numericLayoutId
        this[Keys.symbolLayout] = settings.symbolLayoutId
        this[Keys.theme] = settings.themeId
        this[Keys.heightPercent] = settings.heightPercent
        this[Keys.hapticEnabled] = settings.hapticEnabled
        this[Keys.hapticStrength] = settings.hapticStrength
        this[Keys.soundEnabled] = settings.soundEnabled
        this[Keys.keyPreview] = settings.keyPreviewEnabled
        this[Keys.popupOnLongPress] = settings.popupOnLongPressEnabled
        this[Keys.autoCapitalisation] = settings.autoCapitalisationEnabled
        this[Keys.doubleSpacePeriod] = settings.doubleSpacePeriodEnabled
        this[Keys.backspaceRepeat] = settings.backspaceRepeatEnabled
        this[Keys.spacebarCursor] = settings.spacebarCursorControlEnabled
        this[Keys.languageKey] = settings.languageKeyVisible
        this[Keys.toolbar] = settings.toolbarEnabled
        this[Keys.suggestionDelay] = settings.suggestionDelayMs
        this[Keys.onboarding] = settings.onboardingCompleted
    }
}
