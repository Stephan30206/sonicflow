package com.example.sonicflow.presentation.player

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import com.example.sonicflow.data.model.PlaybackState
import com.example.sonicflow.data.model.RepeatMode
import com.example.sonicflow.data.model.SortType
import com.example.sonicflow.data.model.Track
import com.example.sonicflow.data.repository.MusicRepository
import com.example.sonicflow.service.MusicServiceConnection
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlayerViewModel @Inject constructor(
    private val musicRepository: MusicRepository,
    private val musicServiceConnection: MusicServiceConnection
) : ViewModel() {

    // Liste de toutes les pistes
    private val _tracks = MutableStateFlow<List<Track>>(emptyList())
    val tracks: StateFlow<List<Track>> = _tracks.asStateFlow()

    // État de lecture
    private val _playbackState = MutableStateFlow(PlaybackState())
    val playbackState: StateFlow<PlaybackState> = _playbackState.asStateFlow()

    // Chargement des données
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Recherche
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Type de tri
    private val _sortType = MutableStateFlow(SortType.DATE_ADDED)
    val sortType: StateFlow<SortType> = _sortType.asStateFlow()

    init {
        // Scanner MediaStore au premier lancement
        scanMediaStore()
        loadTracks()
        observeMusicService()
    }

    /**
     * Scanner le MediaStore et sauvegarder dans Room
     */
    private fun scanMediaStore() {
        viewModelScope.launch {
            try {
                musicRepository.scanMediaStore()
            } catch (e: Exception) {
                // Gérer l'erreur
            }
        }
    }

    /**
     * Charge toutes les musiques depuis Room
     */
    private fun loadTracks() {
        viewModelScope.launch {
            _isLoading.value = true
            musicRepository.getAllTracks()
                .catch { error ->
                    _isLoading.value = false
                }
                .collect { trackList ->
                    _tracks.value = trackList
                    _isLoading.value = false
                }
        }
    }

    /**
     * Recherche des pistes
     */
    fun searchTracks(query: String) {
        _searchQuery.value = query
        if (query.isEmpty()) {
            loadTracksSorted(_sortType.value)
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            musicRepository.searchTracks(query)
                .catch { error ->
                    _isLoading.value = false
                }
                .collect { trackList ->
                    _tracks.value = trackList
                    _isLoading.value = false
                }
        }
    }

    /**
     * Charger les pistes avec tri
     */
    fun loadTracksSorted(sortType: SortType) {
        _sortType.value = sortType
        viewModelScope.launch {
            _isLoading.value = true
            musicRepository.getTracksSorted(sortType)
                .catch { error ->
                    _isLoading.value = false
                }
                .collect { trackList ->
                    _tracks.value = trackList
                    _isLoading.value = false
                }
        }
    }

    /**
     * Observer les changements du service de musique
     */
    private fun observeMusicService() {
        viewModelScope.launch {
            combine(
                musicServiceConnection.isPlaying,
                musicServiceConnection.currentPosition,
                musicServiceConnection.duration,
                musicServiceConnection.currentMediaItem
            ) { isPlaying, position, duration, mediaItem ->

                // Trouver la piste actuelle
                val currentTrack = mediaItem?.mediaId?.toLongOrNull()?.let { id ->
                    _tracks.value.find { it.id == id }
                }

                PlaybackState(
                    currentTrack = currentTrack,
                    isPlaying = isPlaying,
                    currentPosition = position,
                    duration = duration,
                    playbackSpeed = 1.0f,
                    isShuffleEnabled = false,
                    repeatMode = RepeatMode.OFF
                )
            }.collect { state ->
                _playbackState.value = state
            }
        }
    }

    /**
     * Jouer une piste spécifique
     */
    fun playTrack(track: Track, startIndex: Int = 0) {
        val mediaItems = _tracks.value.map { t ->
            MediaItem.Builder()
                .setUri(t.path)
                .setMediaId(t.id.toString())
                .setMediaMetadata(
                    androidx.media3.common.MediaMetadata.Builder()
                        .setTitle(t.title)
                        .setArtist(t.artist)
                        .setAlbumTitle(t.album)
                        .setArtworkUri(android.net.Uri.parse(t.albumArtUri ?: ""))
                        .build()
                )
                .build()
        }

        // Trouver l'index de la piste à jouer
        val index = _tracks.value.indexOfFirst { it.id == track.id }

        if (index != -1) {
            musicServiceConnection.setMediaItems(mediaItems, index)
        }
    }

    /**
     * Jouer toutes les pistes à partir d'un index
     */
    fun playAllTracks(startIndex: Int = 0) {
        val mediaItems = _tracks.value.map { track ->
            MediaItem.Builder()
                .setUri(track.path)
                .setMediaId(track.id.toString())
                .setMediaMetadata(
                    androidx.media3.common.MediaMetadata.Builder()
                        .setTitle(track.title)
                        .setArtist(track.artist)
                        .setAlbumTitle(track.album)
                        .setArtworkUri(android.net.Uri.parse(track.albumArtUri ?: ""))
                        .build()
                )
                .build()
        }

        musicServiceConnection.setMediaItems(mediaItems, startIndex)
    }

    /**
     * Play/Pause
     */
    fun togglePlayPause() {
        musicServiceConnection.playPause()
    }

    /**
     * Piste suivante
     */
    fun skipToNext() {
        musicServiceConnection.skipToNext()
    }

    /**
     * Piste précédente
     */
    fun skipToPrevious() {
        musicServiceConnection.skipToPrevious()
    }

    /**
     * Rechercher à une position spécifique
     */
    fun seekTo(position: Long) {
        musicServiceConnection.seekTo(position)
    }

    /**
     * Rafraîchir la liste des pistes
     */
    fun refreshTracks() {
        loadTracks()
    }

    /**
     * Formater la durée en mm:ss
     */
    fun formatDuration(milliseconds: Long): String {
        val totalSeconds = milliseconds / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return String.format("%d:%02d", minutes, seconds)
    }
}