package com.example.sonicflow.presentation.library

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.SubcomposeAsyncImage
import com.example.sonicflow.data.model.Track
import com.example.sonicflow.presentation.player.PlayerViewModel
import androidx.compose.foundation.lazy.items
import com.example.sonicflow.data.model.PlaybackState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    onNavigateBack: () -> Unit,
    onNavigateToPlayer: () -> Unit,
    libraryViewModel: LibraryViewModel = hiltViewModel(),
    playerViewModel: PlayerViewModel = hiltViewModel()
) {
    val selectedTab by libraryViewModel.selectedTab.collectAsState()
    val tracks by playerViewModel.tracks.collectAsState()
    val playbackState by playerViewModel.playbackState.collectAsState()
    val isLoading by playerViewModel.isLoading.collectAsState()

    val albums by libraryViewModel.albums.collectAsState()
    val playlists by libraryViewModel.playlists.collectAsState()
    val selectedAlbum by libraryViewModel.selectedAlbum.collectAsState()
    val selectedPlaylist by libraryViewModel.selectedPlaylistId.collectAsState()
    val tracksByAlbum by libraryViewModel.tracksByAlbum.collectAsState()
    val tracksInPlaylist by libraryViewModel.tracksInSelectedPlaylist.collectAsState()

    var searchQuery by remember { mutableStateOf("") }

    Scaffold(
        containerColor = Color.Black,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Library",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 28.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Black
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color.Black)
        ) {
            // Tab bar
            TabBar(
                selectedTab = selectedTab,
                onTabSelected = { libraryViewModel.selectTab(it) }
            )

            Divider(color = Color.White.copy(alpha = 0.1f), thickness = 1.dp)

            // Search bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                placeholder = {
                    Text("Search in library...", color = Color.Gray)
                },
                leadingIcon = {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = "Search",
                        tint = Color.Gray
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(
                                Icons.Default.Clear,
                                contentDescription = "Clear",
                                tint = Color.Gray
                            )
                        }
                    }
                },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFF1E1E1E),
                    unfocusedContainerColor = Color(0xFF1E1E1E),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = Color(0xFFFFC107),
                    unfocusedBorderColor = Color.Gray
                ),
                singleLine = true
            )

            // CORRECTION : Box avec fillMaxSize() et contentAlignment
            Box(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                when (selectedTab) {
                    is LibraryTab.Albums -> {
                        if (selectedAlbum != null) {
                            AlbumDetailView(
                                album = selectedAlbum!!,
                                tracks = tracksByAlbum,
                                playbackState = playbackState,
                                onBackClick = { libraryViewModel.selectAlbum(null) },
                                onTrackClick = { track ->
                                    playerViewModel.playTrack(track)
                                    onNavigateToPlayer()
                                }
                            )
                        } else {
                            AlbumsGridView(
                                albums = albums.filter {
                                    if (searchQuery.isBlank()) true else it.contains(searchQuery, ignoreCase = true)
                                },
                                onAlbumClick = { libraryViewModel.selectAlbum(it) }
                            )
                        }
                    }
                    is LibraryTab.Playlists -> {
                        if (selectedPlaylist != null) {
                            PlaylistDetailView(
                                tracks = tracksInPlaylist,
                                playbackState = playbackState,
                                onBackClick = { libraryViewModel.selectPlaylist(null) },
                                onTrackClick = { track ->
                                    playerViewModel.playTrack(track)
                                    onNavigateToPlayer()
                                }
                            )
                        } else {
                            PlaylistsGridView(
                                playlists = playlists,
                                onPlaylistClick = { libraryViewModel.selectPlaylist(it.id) }
                            )
                        }
                    }
                    else -> {
                        // Tracks view (default)
                        TracksListView(
                            tracks = tracks.filter {
                                if (searchQuery.isBlank()) true else
                                    it.title.contains(searchQuery, ignoreCase = true) ||
                                            it.artist.contains(searchQuery, ignoreCase = true)
                            },
                            playbackState = playbackState,
                            isLoading = isLoading,
                            onTrackClick = { track, index ->
                                playerViewModel.playTrack(track)
                                onNavigateToPlayer()
                            }
                        )
                    }
                }
            }
        }
    }
}

// CORRECTION : Ajouter une hauteur fixe à Box dans les fonctions grid
@Composable
fun AlbumsGridView(
    albums: List<String>,
    onAlbumClick: (String) -> Unit
) {
    if (albums.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "No albums found",
                color = Color.Gray,
                fontSize = 16.sp
            )
        }
    } else {
        // CORRECTION : Retirer fillMaxSize() du LazyVerticalGrid et utiliser Column avec weight
        Column(modifier = Modifier.fillMaxSize()) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(albums) { album ->
                    AlbumGridItem(
                        album = album,
                        onClick = { onAlbumClick(album) }
                    )
                }
            }
        }
    }
}

@Composable
fun PlaylistsGridView(
    playlists: List<com.example.sonicflow.data.database.entities.PlaylistEntity>,
    onPlaylistClick: (com.example.sonicflow.data.database.entities.PlaylistEntity) -> Unit
) {
    if (playlists.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("No playlists created", color = Color.Gray)
        }
    } else {
        // CORRECTION : Retirer fillMaxSize() du LazyColumn et utiliser Column avec weight
        Column(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(playlists) { playlist ->
                    PlaylistItem(
                        playlist = playlist,
                        onClick = { onPlaylistClick(playlist) }
                    )
                }
            }
        }
    }
}

// CORRECTION : Ajuster TracksListView
@Composable
fun TracksListView(
    tracks: List<Track>,
    playbackState: PlaybackState,
    isLoading: Boolean,
    onTrackClick: (Track, Int) -> Unit
) {
    if (isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = Color(0xFFFFC107))
        }
    } else if (tracks.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("No tracks found", color = Color.Gray)
        }
    } else {
        // CORRECTION : Retirer fillMaxSize() et utiliser Column avec weight
        Column(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemsIndexed(tracks) { index, track ->
                    TrackItem(
                        track = track,
                        isPlaying = playbackState.currentTrack?.id == track.id && playbackState.isPlaying,
                        onClick = { onTrackClick(track, index) }
                    )
                }
            }
        }
    }
}

// CORRECTION : Ajuster AlbumDetailView et PlaylistDetailView
@Composable
fun AlbumDetailView(
    album: String,
    tracks: List<Track>,
    playbackState: com.example.sonicflow.data.model.PlaybackState,
    onBackClick: () -> Unit,
    onTrackClick: (Track) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onBackClick)
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.ArrowBack,
                contentDescription = "Back",
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = album,
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }

        if (tracks.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("No tracks in this album", color = Color.Gray)
            }
        } else {
            // CORRECTION : Utiliser Column avec weight
            Column(modifier = Modifier.fillMaxSize()) {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    itemsIndexed(tracks) { _, track ->
                        TrackItem(
                            track = track,
                            isPlaying = playbackState.currentTrack?.id == track.id && playbackState.isPlaying,
                            onClick = { onTrackClick(track) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PlaylistDetailView(
    tracks: List<Track>,
    playbackState: PlaybackState,
    onBackClick: () -> Unit,
    onTrackClick: (Track) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onBackClick)
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.ArrowBack,
                contentDescription = "Back",
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "${tracks.size} tracks",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }

        if (tracks.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("No tracks in this playlist", color = Color.Gray)
            }
        } else {
            // CORRECTION : Utiliser Column avec weight
            Column(modifier = Modifier.fillMaxSize()) {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    itemsIndexed(tracks) { _, track ->
                        TrackItem(
                            track = track,
                            isPlaying = playbackState.currentTrack?.id == track.id && playbackState.isPlaying,
                            onClick = { onTrackClick(track) }
                        )
                    }
                }
            }
        }
    }
}

// Le reste du code reste inchangé...
@Composable
fun TabBar(
    selectedTab: LibraryTab,
    onTabSelected: (LibraryTab) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        TabChip(
            text = "Tracks",
            isSelected = selectedTab is LibraryTab.Favorites,
            onClick = { onTabSelected(LibraryTab.Favorites) }
        )
        TabChip(
            text = "Albums",
            isSelected = selectedTab is LibraryTab.Albums,
            onClick = { onTabSelected(LibraryTab.Albums) }
        )
        TabChip(
            text = "Playlists",
            isSelected = selectedTab is LibraryTab.Playlists,
            onClick = { onTabSelected(LibraryTab.Playlists) }
        )
    }
}

@Composable
fun TabChip(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .clickable(onClick = onClick)
            .clip(RoundedCornerShape(20.dp)),
        color = if (isSelected) Color(0xFFFFC107) else Color(0xFF2A2A2A),
        shape = RoundedCornerShape(20.dp)
    ) {
        Text(
            text = text,
            color = if (isSelected) Color.Black else Color.White,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            fontSize = 13.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
fun AlbumGridItem(
    album: String,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(12.dp))
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF1E88E5),
                            Color(0xFF00ACC1)
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.Album,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.7f),
                modifier = Modifier.size(40.dp)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = album,
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun PlaylistItem(
    playlist: com.example.sonicflow.data.database.entities.PlaylistEntity,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF1E1E1E))
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            Color(0xFFFF7043),
                            Color(0xFFE64A19)
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.LibraryMusic,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(32.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = playlist.name,
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (playlist.description.isNotEmpty()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = playlist.description,
                    color = Color.Gray,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Icon(
            Icons.Default.ChevronRight,
            contentDescription = null,
            tint = Color.Gray
        )
    }
}

@Composable
fun TrackItem(
    track: Track,
    isPlaying: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF1E1E1E))
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(8.dp))
        ) {
            if (!track.albumArtUri.isNullOrEmpty()) {
                SubcomposeAsyncImage(
                    model = track.albumArtUri,
                    contentDescription = "Album Art",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    loading = {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color(0xFF2A2A2A)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.MusicNote,
                                contentDescription = "Loading",
                                tint = Color(0xFFFFC107).copy(alpha = 0.5f),
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    },
                    error = {
                        com.example.sonicflow.presentation.components.AlbumArtPlaceholder(
                            title = track.album,
                            artist = track.artist,
                            size = 56.dp,
                            cornerRadius = 8.dp
                        )
                    }
                )
            } else {
                com.example.sonicflow.presentation.components.AlbumArtPlaceholder(
                    title = track.album,
                    artist = track.artist,
                    size = 56.dp,
                    cornerRadius = 8.dp
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = track.title,
                color = if (isPlaying) Color(0xFFFFC107) else Color.White,
                fontSize = 16.sp,
                fontWeight = if (isPlaying) FontWeight.Bold else FontWeight.Normal,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${track.artist} • ${formatDuration(track.duration)}",
                color = Color.Gray,
                fontSize = 13.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        if (isPlaying) {
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = "Playing",
                tint = Color(0xFFFFC107),
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

private fun formatDuration(milliseconds: Long): String {
    val totalSeconds = milliseconds / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("%d:%02d", minutes, seconds)
}