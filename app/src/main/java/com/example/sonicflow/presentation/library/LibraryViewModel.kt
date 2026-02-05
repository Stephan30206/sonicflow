package com.example.sonicflow.presentation.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sonicflow.data.database.entities.PlaylistEntity
import com.example.sonicflow.data.model.Track
import com.example.sonicflow.data.repository.MusicRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class LibraryTab {
    object Favorites : LibraryTab()
    object Playlists : LibraryTab()
    object Artists : LibraryTab()
    object Albums : LibraryTab()
}

@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val musicRepository: MusicRepository
) : ViewModel() {

    private val _selectedTab = MutableStateFlow<LibraryTab>(LibraryTab.Favorites)
    val selectedTab: StateFlow<LibraryTab> = _selectedTab.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // ========== FAVORIS ==========
    val favoriteTracks: StateFlow<List<Track>> = musicRepository.getAllFavoriteTracks()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // ========== PLAYLISTS ==========
    val playlists: StateFlow<List<PlaylistEntity>> = musicRepository.getAllPlaylists()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _selectedPlaylistId = MutableStateFlow<Long?>(null)
    val selectedPlaylistId: StateFlow<Long?> = _selectedPlaylistId.asStateFlow()

    val tracksInSelectedPlaylist: StateFlow<List<Track>> = _selectedPlaylistId
        .filterNotNull()
        .flatMapLatest { playlistId ->
            musicRepository.getTracksInPlaylist(playlistId)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // ========== ARTISTES ==========
    val artists: StateFlow<List<String>> = musicRepository.getAllArtists()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _selectedArtist = MutableStateFlow<String?>(null)
    val selectedArtist: StateFlow<String?> = _selectedArtist.asStateFlow()

    val tracksByArtist: StateFlow<List<Track>> = _selectedArtist
        .filterNotNull()
        .flatMapLatest { artist ->
            musicRepository.getTracksByArtist(artist)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // ========== ALBUMS ==========
    val albums: StateFlow<List<String>> = musicRepository.getAllAlbums()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _selectedAlbum = MutableStateFlow<String?>(null)
    val selectedAlbum: StateFlow<String?> = _selectedAlbum.asStateFlow()

    val tracksByAlbum: StateFlow<List<Track>> = _selectedAlbum
        .filterNotNull()
        .flatMapLatest { album ->
            musicRepository.getTracksByAlbum(album)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // ========== ACTIONS ==========
    fun selectTab(tab: LibraryTab) {
        _selectedTab.value = tab
        // Réinitialiser les sélections
        _selectedPlaylistId.value = null
        _selectedArtist.value = null
        _selectedAlbum.value = null
    }

    // Playlists
    fun createPlaylist(name: String, description: String = "") {
        viewModelScope.launch {
            musicRepository.createPlaylist(name, description)
        }
    }

    fun selectPlaylist(playlistId: Long?) {
        _selectedPlaylistId.value = playlistId
    }

    fun addTrackToPlaylist(playlistId: Long, trackId: Long) {
        viewModelScope.launch {
            musicRepository.addTrackToPlaylist(playlistId, trackId)
        }
    }

    fun removeTrackFromPlaylist(playlistId: Long, trackId: Long) {
        viewModelScope.launch {
            musicRepository.removeTrackFromPlaylist(playlistId, trackId)
        }
    }

    fun deletePlaylist(playlist: PlaylistEntity) {
        viewModelScope.launch {
            musicRepository.deletePlaylist(playlist)
            if (_selectedPlaylistId.value == playlist.id) {
                _selectedPlaylistId.value = null
            }
        }
    }

    fun renamePlaylist(playlistId: Long, newName: String) {
        viewModelScope.launch {
            playlists.value.find { it.id == playlistId }?.let { playlist ->
                musicRepository.updatePlaylist(playlist.copy(name = newName))
            }
        }
    }

    // Artistes
    fun selectArtist(artist: String?) {
        _selectedArtist.value = artist
    }

    // Albums
    fun selectAlbum(album: String?) {
        _selectedAlbum.value = album
    }

    /**
     * Toggle le statut favori d'une piste
     */
    fun toggleFavorite(trackId: Long) {
        viewModelScope.launch {
            musicRepository.toggleFavorite(trackId)
        }
    }
}