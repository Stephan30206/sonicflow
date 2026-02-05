package com.example.sonicflow.presentation.player

import com.example.sonicflow.data.model.Track

/**
 * Represents the current state of audio playback.
 *
 * @param currentTrack   The track currently loaded/playing, or null if nothing is loaded.
 * @param isPlaying      Whether playback is actively running (true) or paused/stopped (false).
 * @param currentPosition Current playback position in milliseconds.
 * @param duration        Total duration of the current track in milliseconds.
 */
data class PlaybackState(
    val currentTrack: Track? = null,
    val isPlaying: Boolean = false,
    val currentPosition: Long = 0L,
    val duration: Long = 0L
)