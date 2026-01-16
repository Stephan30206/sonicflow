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

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

@Singleton
class UserPreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val dataStore = context.dataStore

    companion object {
        private val LAST_PLAYED_TRACK_ID = longPreferencesKey("last_played_track_id")
        private val LAST_POSITION = longPreferencesKey("last_position")
        private val IS_SHUFFLE_ENABLED = booleanPreferencesKey("is_shuffle_enabled")
        private val REPEAT_MODE = stringPreferencesKey("repeat_mode")
        private val SORT_TYPE = stringPreferencesKey("sort_type")
    }

    val lastPlayedTrackId: Flow<Long?> = dataStore.data.map { prefs ->
        prefs[LAST_PLAYED_TRACK_ID]
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

    val sortType: Flow<String> = dataStore.data.map { prefs ->
        prefs[SORT_TYPE] ?: "DATE_ADDED"
    }

    suspend fun saveLastPlayedTrack(trackId: Long, position: Long) {
        dataStore.edit { prefs ->
            prefs[LAST_PLAYED_TRACK_ID] = trackId
            prefs[LAST_POSITION] = position
        }
    }

    suspend fun saveShuffleState(enabled: Boolean) {
        dataStore.edit { prefs ->
            prefs[IS_SHUFFLE_ENABLED] = enabled
        }
    }

    suspend fun saveRepeatMode(mode: String) {
        dataStore.edit { prefs ->
            prefs[REPEAT_MODE] = mode
        }
    }

    suspend fun saveSortType(type: String) {
        dataStore.edit { prefs ->
            prefs[SORT_TYPE] = type
        }
    }
}