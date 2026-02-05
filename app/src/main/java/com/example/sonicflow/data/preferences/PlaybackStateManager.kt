package com.example.sonicflow.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.playbackDataStore: DataStore<Preferences> by preferencesDataStore(name = "playback_state")

@Singleton
class PlaybackStateManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val dataStore = context.playbackDataStore

    companion object {
        private val LAST_TRACK_ID = longPreferencesKey("last_track_id")
        private val LAST_POSITION = longPreferencesKey("last_position")
        private val IS_SHUFFLE_ENABLED = booleanPreferencesKey("is_shuffle_enabled")
        private val REPEAT_MODE = stringPreferencesKey("repeat_mode")
        private val WAS_PLAYING = booleanPreferencesKey("was_playing")
    }

    val lastTrackId: Flow<Long?> = dataStore.data.map { prefs ->
        prefs[LAST_TRACK_ID]
    }

    val lastPosition: Flow<Long> = dataStore.data.map { prefs ->
        prefs[LAST_POSITION] ?: 0L
    }

    val isShuffleEnabled: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[IS_SHUFFLE_ENABLED] ?: false
    }

    val repeatMode: Flow<String> = dataStore.data.map { prefs ->
        prefs[REPEAT_MODE] ?: "OFF"
    }

    val wasPlaying: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[WAS_PLAYING] ?: false
    }

    suspend fun savePlaybackState(
        trackId: Long,
        position: Long,
        shuffleEnabled: Boolean,
        repeatMode: String,
        isPlaying: Boolean
    ) {
        dataStore.edit { prefs ->
            prefs[LAST_TRACK_ID] = trackId
            prefs[LAST_POSITION] = position
            prefs[IS_SHUFFLE_ENABLED] = shuffleEnabled
            prefs[REPEAT_MODE] = repeatMode
            prefs[WAS_PLAYING] = isPlaying
        }
    }

    suspend fun updatePosition(position: Long) {
        dataStore.edit { prefs ->
            prefs[LAST_POSITION] = position
        }
    }

    suspend fun clearPlaybackState() {
        dataStore.edit { prefs ->
            prefs.remove(LAST_TRACK_ID)
            prefs.remove(LAST_POSITION)
            prefs.remove(WAS_PLAYING)
        }
    }
}