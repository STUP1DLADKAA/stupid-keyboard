package com.stupidkeyboard.features.feedback

import android.content.Context
import android.media.AudioManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.HapticFeedbackConstants
import android.view.View
import com.stupidkeyboard.core.util.KeyboardMetrics

/**
 * Haptic and audible feedback for key presses.
 *
 * Design notes:
 *  - The vibrator handle is resolved once, never per key press.
 *  - Amplitude is pre-computed whenever settings change.
 *  - On API 30+ we prefer the system-provided [View.performHapticFeedback] constants when
 *    the user asked for the default strength, since that respects the device's own
 *    haptic settings; custom amplitudes fall back to a cached [VibrationEffect].
 *  - Nothing here allocates during a normal key press except a reused effect object.
 */
class FeedbackController(context: Context) {

    private val appContext = context.applicationContext

    private val vibrator: Vibrator? = runCatching {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val manager = appContext.getSystemService(VibratorManager::class.java)
            manager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            appContext.getSystemService(Vibrator::class.java)
        }
    }.getOrNull()

    private val hasAmplitudeControl: Boolean =
        vibrator?.hasAmplitudeControl() == true

    private var enabled: Boolean = true
    private var strengthPercent: Int = 60
    private var amplitude: Int = 0
    private var soundEnabled: Boolean = false

    @Volatile
    private var cachedTick: VibrationEffect? = null

    fun configure(hapticsEnabled: Boolean, strength: Int, sounds: Boolean) {
        enabled = hapticsEnabled
        soundEnabled = sounds
        if (strength != strengthPercent || cachedTick == null) {
            strengthPercent = strength
            amplitude = KeyboardMetrics.amplitudeForStrength(
                strengthPercent = strength,
                minAmplitude = MIN_AMPLITUDE,
                maxAmplitude = MAX_AMPLITUDE,
            )
            cachedTick = null
        }
    }

    /** Short confirmation tick for a key press. */
    fun onKeyPress(view: View?) {
        if (enabled) {
            // System constant respects the user's "touch vibration" system setting.
            if (strengthPercent == DEFAULT_STRENGTH && view != null) {
                if (!view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)) {
                    vibrateTick()
                }
            } else {
                vibrateTick()
            }
        }
        if (soundEnabled && view != null) {
            view.playSoundEffect(android.view.SoundEffectConstants.CLICK)
        }
    }

    /** Slightly stronger tick for destructive or state-changing keys (shift, delete). */
    fun onModifierPress(view: View?) {
        if (!enabled) return
        if (view != null && view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)) return
        vibrateTick()
    }

    /** Long press feedback, e.g. when the popup opens or caps lock engages. */
    fun onLongPress(view: View?) {
        if (!enabled) return
        if (view != null && view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)) return
        vibrateTick()
    }

    private fun vibrateTick() {
        val v = vibrator ?: return
        if (!v.hasVibrator()) return
        runCatching {
            if (hasAmplitudeControl) {
                val effect = cachedTick ?: VibrationEffect.createOneShot(
                    DURATION_MS,
                    amplitude.coerceAtLeast(1),
                ).also { cachedTick = it }
                v.vibrate(effect)
            } else {
                v.vibrate(VibrationEffect.createOneShot(DURATION_MS, VibrationEffect.DEFAULT_AMPLITUDE))
            }
        }
    }

    fun clickSoundEnabled(): Boolean = soundEnabled

    /** Whether the device can actually produce haptics, used by the settings screen. */
    fun deviceSupportsHaptics(): Boolean =
        vibrator?.hasVibrator() == true && (appContext.getSystemService(Context.AUDIO_SERVICE) as? AudioManager) != null

    companion object {
        private const val DURATION_MS = 12L
        private const val MIN_AMPLITUDE = 30
        private const val MAX_AMPLITUDE = 255
        private const val DEFAULT_STRENGTH = 60
    }
}
