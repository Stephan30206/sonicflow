package com.example.sonicflow.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sonicflow.data.preferences.UserPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userPreferences: UserPreferences
) : ViewModel() {

    private val _audioQuality = MutableStateFlow("High")
    val audioQuality: StateFlow<String> = _audioQuality

    private val _playbackSpeed = MutableStateFlow("1.0x")
    val playbackSpeed: StateFlow<String> = _playbackSpeed

    private val _cacheSize = MutableStateFlow("0 MB")
    val cacheSize: StateFlow<String> = _cacheSize

    val isDarkModeEnabled: StateFlow<Boolean> = userPreferences.isDarkModeEnabled
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = true
        )

    fun setDarkMode(isDarkMode: Boolean) {
        viewModelScope.launch {
            userPreferences.saveDarkModeState(isDarkMode)
        }
    }

    fun setAudioQuality(quality: String) {
        _audioQuality.value = quality
        saveSetting("audio_quality", quality)
    }

    fun setPlaybackSpeed(speed: String) {
        _playbackSpeed.value = speed
        saveSetting("playback_speed", speed)
    }

    fun clearCache() {
        viewModelScope.launch {
            try {
                _cacheSize.value = "0 MB"
                saveSetting("cache_cleared", "true")
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun openMusicFolders() {
        // This would typically open a file picker or folder selection dialog
        saveSetting("music_folders_opened", "true")
    }

    fun rateApp() {
        // This would open the app store or rating dialog
        saveSetting("rate_app_opened", "true")
    }

    fun openSourceLicenses() {
        // This would navigate to a licenses screen
        saveSetting("licenses_opened", "true")
    }

    private fun saveSetting(key: String, value: String) {
        // In a real app, this would save to SharedPreferences or a database
        viewModelScope.launch {
            try {
                // Settings would be persisted here
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
}
