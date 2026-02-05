package com.example.sonicflow.presentation.player

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import com.example.sonicflow.data.model.PlaybackState
import com.example.sonicflow.data.model.RepeatMode
import com.example.sonicflow.data.model.SortType
import com.example.sonicflow.data.model.Track
import com.example.sonicflow.data.repository.MusicRepository
import com.example.sonicflow.data.preferences.PlaybackStateManager
import com.example.sonicflow.service.MusicServiceConnection
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlayerViewModel @Inject constructor(
    private val musicRepository: MusicRepository,
    private val musicServiceConnection: MusicServiceConnection,
    private val playbackStateManager: PlaybackStateManager
) : ViewModel() {

    private val _tracks = MutableStateFlow<List<Track>>(emptyList())
    val tracks: StateFlow<List<Track>> = _tracks.asStateFlow()

    private val _playbackState = MutableStateFlow(PlaybackState())
    val playbackState: StateFlow<PlaybackState> = _playbackState.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _sortType = MutableStateFlow(SortType.DATE_ADDED)
    val sortType: StateFlow<SortType> = _sortType.asStateFlow()

    private val _isShuffleEnabled = MutableStateFlow(false)
    val isShuffleEnabled: StateFlow<Boolean> = _isShuffleEnabled.asStateFlow()

    private val _repeatMode = MutableStateFlow(RepeatMode.OFF)
    val repeatMode: StateFlow<RepeatMode> = _repeatMode.asStateFlow()

    private val _isFavorite = MutableStateFlow(false)
    val isFavorite: StateFlow<Boolean> = _isFavorite.asStateFlow()

    // Queue management
    private val _queue = MutableStateFlow<List<Track>>(emptyList())
    val queue: StateFlow<List<Track>> = _queue.asStateFlow()

    private val _currentTrackIndex = MutableStateFlow(0)
    val currentTrackIndex: StateFlow<Int> = _currentTrackIndex.asStateFlow()

    private var originalTracksList = listOf<Track>()
    private var isRestoringState = false

    init {
        scanMediaStore()
        loadTracks()
        observeMusicService()
        restorePlaybackState()
        observeFavoriteStatus()
    }

    private fun scanMediaStore() {
        viewModelScope.launch {
            try {
                musicRepository.scanMediaStore()
            } catch (e: Exception) {
                // Handle error silently
            }
        }
    }

    private fun loadTracks() {
        viewModelScope.launch {
            _isLoading.value = true
            musicRepository.getAllTracks()
                .catch { error ->
                    _isLoading.value = false
                }
                .collect { trackList ->
                    _tracks.value = trackList
                    originalTracksList = trackList
                    _isLoading.value = false
                }
        }
    }

    private fun restorePlaybackState() {
        viewModelScope.launch {
            _tracks.filter { it.isNotEmpty() }.first()

            playbackStateManager.lastTrackId.first()?.let { trackId ->
                val track = _tracks.value.find { it.id == trackId }
                if (track != null) {
                    isRestoringState = true

                    val shuffleEnabled = playbackStateManager.isShuffleEnabled.first()
                    val repeatModeStr = playbackStateManager.repeatMode.first()

                    _isShuffleEnabled.value = shuffleEnabled
                    _repeatMode.value = RepeatMode.valueOf(repeatModeStr)

                    playTrack(track, 0, autoPlay = false)

                    val position = playbackStateManager.lastPosition.first()
                    if (position > 0) {
                        kotlinx.coroutines.delay(300)
                        seekTo(position)
                    }

                    isRestoringState = false
                }
            }
        }
    }

    private fun observeFavoriteStatus() {
        viewModelScope.launch {
            _playbackState.collectLatest { state ->
                state.currentTrack?.let { track ->
                    musicRepository.isFavorite(track.id).collectLatest { isFav ->
                        _isFavorite.value = isFav
                    }
                }
            }
        }
    }

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

    private fun observeMusicService() {
        viewModelScope.launch {
            combine(
                musicServiceConnection.isPlaying,
                musicServiceConnection.currentPosition,
                musicServiceConnection.duration,
                musicServiceConnection.currentMediaItem
            ) { isPlaying, position, duration, mediaItem ->

                val currentTrack = mediaItem?.mediaId?.toLongOrNull()?.let { id ->
                    _tracks.value.find { it.id == id }
                }

                if (currentTrack != null && !isRestoringState && position > 1000) {
                    savePlaybackState(currentTrack.id, position, isPlaying)
                }

                PlaybackState(
                    currentTrack = currentTrack,
                    isPlaying = isPlaying,
                    currentPosition = position,
                    duration = duration,
                    playbackSpeed = 1.0f,
                    isShuffleEnabled = _isShuffleEnabled.value,
                    repeatMode = _repeatMode.value
                )
            }.collect { state ->
                _playbackState.value = state
            }
        }
    }

    private fun savePlaybackState(trackId: Long, position: Long, isPlaying: Boolean) {
        viewModelScope.launch {
            playbackStateManager.savePlaybackState(
                trackId = trackId,
                position = position,
                shuffleEnabled = _isShuffleEnabled.value,
                repeatMode = _repeatMode.value.name,
                isPlaying = isPlaying
            )
        }
    }

    fun playTrack(track: Track, startIndex: Int = 0, autoPlay: Boolean = true) {
        val tracksToPlay = if (_isShuffleEnabled.value) {
            val shuffled = _tracks.value.shuffled().toMutableList()
            shuffled.remove(track)
            listOf(track) + shuffled
        } else {
            _tracks.value
        }

        // Update queue
        _queue.value = tracksToPlay
        _currentTrackIndex.value = tracksToPlay.indexOfFirst { it.id == track.id }

        val mediaItems = tracksToPlay.map { t ->
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

        val index = tracksToPlay.indexOfFirst { it.id == track.id }

        if (index != -1) {
            musicServiceConnection.setMediaItems(mediaItems, index)
            if (autoPlay) {
                musicServiceConnection.play()
            }
            musicServiceConnection.setShuffleMode(_isShuffleEnabled.value)
            musicServiceConnection.setRepeatMode(
                when (_repeatMode.value) {
                    RepeatMode.OFF -> Player.REPEAT_MODE_OFF
                    RepeatMode.ONE -> Player.REPEAT_MODE_ONE
                    RepeatMode.ALL -> Player.REPEAT_MODE_ALL
                }
            )
        }
    }

    fun playAllTracks(startIndex: Int = 0) {
        val tracksToPlay = if (_isShuffleEnabled.value) {
            _tracks.value.shuffled()
        } else {
            _tracks.value
        }

        // Update queue
        _queue.value = tracksToPlay
        _currentTrackIndex.value = startIndex

        val mediaItems = tracksToPlay.map { track ->
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
        musicServiceConnection.play()
    }

    fun togglePlayPause() {
        musicServiceConnection.playPause()
    }

    fun skipToNext() {
        musicServiceConnection.skipToNext()
        if (_currentTrackIndex.value < _queue.value.size - 1) {
            _currentTrackIndex.value += 1
        }
    }

    fun skipToPrevious() {
        musicServiceConnection.skipToPrevious()
        if (_currentTrackIndex.value > 0) {
            _currentTrackIndex.value -= 1
        }
    }

    fun seekTo(position: Long) {
        musicServiceConnection.seekTo(position)
    }

    fun toggleShuffle() {
        _isShuffleEnabled.value = !_isShuffleEnabled.value
        musicServiceConnection.setShuffleMode(_isShuffleEnabled.value)

        viewModelScope.launch {
            _playbackState.value.currentTrack?.let { track ->
                savePlaybackState(
                    trackId = track.id,
                    position = _playbackState.value.currentPosition,
                    isPlaying = _playbackState.value.isPlaying
                )
            }
        }
    }

    fun toggleRepeat() {
        _repeatMode.value = when (_repeatMode.value) {
            RepeatMode.OFF -> RepeatMode.ALL
            RepeatMode.ALL -> RepeatMode.ONE
            RepeatMode.ONE -> RepeatMode.OFF
        }

        val repeatModeValue = when (_repeatMode.value) {
            RepeatMode.OFF -> Player.REPEAT_MODE_OFF
            RepeatMode.ONE -> Player.REPEAT_MODE_ONE
            RepeatMode.ALL -> Player.REPEAT_MODE_ALL
        }
        musicServiceConnection.setRepeatMode(repeatModeValue)

        viewModelScope.launch {
            _playbackState.value.currentTrack?.let { track ->
                savePlaybackState(
                    trackId = track.id,
                    position = _playbackState.value.currentPosition,
                    isPlaying = _playbackState.value.isPlaying
                )
            }
        }
    }

    fun toggleFavorite() {
        viewModelScope.launch {
            _playbackState.value.currentTrack?.let { track ->
                musicRepository.toggleFavorite(track.id)
            }
        }
    }

    fun refreshTracks() {
        loadTracks()
    }

    fun formatDuration(milliseconds: Long): String {
        val totalSeconds = milliseconds / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return String.format("%d:%02d", minutes, seconds)
    }

    override fun onCleared() {
        super.onCleared()
        viewModelScope.launch {
            _playbackState.value.currentTrack?.let { track ->
                savePlaybackState(
                    trackId = track.id,
                    position = _playbackState.value.currentPosition,
                    isPlaying = _playbackState.value.isPlaying
                )
            }
        }
    }

    /**
     * Ajoute une piste à la file d'attente
     */
    fun addToQueue(track: Track) {
        viewModelScope.launch {
            // Obtenir la queue actuelle
            val currentQueue = _queue.value.toMutableList()

            // Ajouter la nouvelle piste
            currentQueue.add(track)

            // Mettre à jour la queue
            _queue.value = currentQueue
        }
    }

    /**
     * Ajoute une piste pour être jouée ensuite (juste après la piste actuelle)
     */
    fun playNext(track: Track) {
        viewModelScope.launch {
            val currentQueue = _queue.value.toMutableList()
            val currentIndex = _currentTrackIndex.value

            // Insérer juste après l'index actuel
            if (currentIndex < currentQueue.size - 1) {
                currentQueue.add(currentIndex + 1, track)
            } else {
                currentQueue.add(track)
            }

            _queue.value = currentQueue
        }
    }
}