package com.stupidkeyboard.core.util

/**
 * Pure geometry helpers used by the keyboard view. Kept free of Android types so that
 * the arithmetic can be unit tested on the JVM.
 */
object KeyboardMetrics {

    /**
     * Height of a single row in pixels for a keyboard of [keyboardHeightPx] divided into
     * [rowCount] equal rows, minus the vertical gap between rows.
     */
    fun rowHeight(keyboardHeightPx: Int, rowCount: Int, verticalGapPx: Float): Float {
        if (rowCount <= 0) return 0f
        val usable = keyboardHeightPx - verticalGapPx * (rowCount + 1)
        return (usable / rowCount).coerceAtLeast(0f)
    }

    /**
     * Left/right edge of every key in a row for a row of [totalWeight], [width] pixels,
     * horizontal padding and gaps. Returns a flat array of `2 * keys` floats
     * (start, end pairs) to avoid allocating objects during layout.
     */
    fun rowBounds(
        weights: FloatArray,
        width: Int,
        horizontalPadding: Float,
        gap: Float,
    ): FloatArray {
        val count = weights.size
        val out = FloatArray(count * 2)
        if (count == 0) return out
        val innerWidth = width - horizontalPadding * 2f - gap * (count - 1)
        val totalWeight = weights.sum()
        if (innerWidth <= 0f || totalWeight <= 0f) return out
        val unit = innerWidth / totalWeight
        var cursor = horizontalPadding
        for (i in 0 until count) {
            val w = weights[i] * unit
            out[i * 2] = cursor
            out[i * 2 + 1] = cursor + w
            cursor += w + gap
        }
        return out
    }

    /** Index of the key under [x] within a set of bounds produced by [rowBounds], or -1. */
    fun hitTest(bounds: FloatArray, x: Float): Int {
        var i = 0
        val count = bounds.size / 2
        while (i < count) {
            if (x >= bounds[i * 2] && x <= bounds[i * 2 + 1]) return i
            i++
        }
        return -1
    }

    /** Maps a 0..100 user setting onto a vibration amplitude that respects the device range. */
    fun amplitudeForStrength(strengthPercent: Int, minAmplitude: Int, maxAmplitude: Int): Int {
        val clamped = strengthPercent.coerceIn(0, 100)
        if (maxAmplitude <= 0) return 0
        val amplitude = minAmplitude + ((maxAmplitude - minAmplitude) * (clamped / 100f)).toInt()
        return amplitude.coerceIn(1, maxAmplitude)
    }

    /** Clamps a keyboard height percentage to the values the user is allowed to pick. */
    fun clampHeightPercent(percent: Float, min: Float, max: Float): Float = percent.coerceIn(min, max)
}
