package com.stupidkeyboard.features.feedback

/**
 * Decides the auto-repeat behaviour of the backspace key and translates the schedule into
 * editor deletions.
 *
 * Kept separate from the keyboard view so the timing rules can be unit tested and tuned
 * without touching rendering code.
 */
object RepeatPolicy {

    /** Delay before the first repeat, in milliseconds. */
    const val INITIAL_DELAY_MS = 380L

    /** Interval between repeats once the key is held steadily. */
    const val SLOW_INTERVAL_MS = 85L

    /** Fastest interval the backspace will reach. */
    const val FAST_INTERVAL_MS = 30L

    /** Number of repeats before the fastest interval is reached. */
    const val ACCELERATION_STEPS = 10

    /** Repeats that happen per tick are capped so a whole word can be removed at speed. */
    const val MAX_EVENTS_PER_TICK = 4

    fun intervalForRepeats(count: Int): Long {
        if (count >= ACCELERATION_STEPS) return FAST_INTERVAL_MS
        val progress = count.toFloat() / ACCELERATION_STEPS
        return slowToFast(progress)
    }

    private fun slowToFast(progress: Float): Long =
        (SLOW_INTERVAL_MS - (SLOW_INTERVAL_MS - FAST_INTERVAL_MS) * progress).toLong()

    /** Human readable description used in the settings screen. */
    val description: String
        get() = "Hold delete to remove text quickly. Speeds up after ${INITIAL_DELAY_MS}ms."
}
