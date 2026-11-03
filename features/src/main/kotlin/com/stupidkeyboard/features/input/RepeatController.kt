package com.stupidkeyboard.features.input

import com.stupidkeyboard.features.feedback.RepeatPolicy

/**
 * Tracks how long a repeatable key has been held and how many repeats are due.
 *
 * Pure logic with no Android dependencies: the keyboard view feeds it elapsed time and
 * applies the returned count. All state is primitive, so ticking never allocates.
 */
class RepeatController(
    private val initialDelayMs: Long = RepeatPolicy.INITIAL_DELAY_MS,
    private val maxEventsPerTick: Int = RepeatPolicy.MAX_EVENTS_PER_TICK,
) {

    private var elapsedMs: Long = 0
    private var repeats: Int = 0
    private var nextRepeatAtMs: Long = initialDelayMs
    private var active: Boolean = false

    val isRepeating: Boolean
        get() = active

    /** Total repeats emitted since [start], exposed for diagnostics in the preview. */
    val repeatCount: Int
        get() = repeats

    fun start() {
        elapsedMs = 0
        repeats = 0
        nextRepeatAtMs = initialDelayMs
        active = true
    }

    fun stop() {
        active = false
        elapsedMs = 0
        repeats = 0
        nextRepeatAtMs = initialDelayMs
    }

    /**
     * Advances the internal clock and reports how many repeat events are due.
     *
     * @param deltaMs time since the previous tick
     * @return number of deletions to apply, 0 when nothing is due yet
     */
    fun advance(deltaMs: Long): Int {
        if (!active) return 0
        elapsedMs += deltaMs
        var events = 0
        while (active && elapsedMs >= nextRepeatAtMs && events < maxEventsPerTick) {
            events++
            repeats++
            nextRepeatAtMs = elapsedMs + RepeatPolicy.intervalForRepeats(repeats)
        }
        return events
    }
}
