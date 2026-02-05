package com.example.sonicflow.presentation.home

import android.Manifest
import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.sonicflow.data.model.Track
import com.example.sonicflow.presentation.components.AlbumArtPlaceholder
import com.example.sonicflow.presentation.library.LibraryViewModel
import com.example.sonicflow.presentation.player.PlayerViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun HomeScreen(
    onNavigateToPlayer: () -> Unit,
    onNavigateToLibrary: () -> Unit,
    onNavigateToSettings: () -> Unit,
    homeViewModel: HomeViewModel = hiltViewModel(),
    playerViewModel: PlayerViewModel = hiltViewModel(),
    libraryViewModel: LibraryViewModel = hiltViewModel()
) {
    val tracks by playerViewModel.tracks.collectAsState()
    val playbackState by playerViewModel.playbackState.collectAsState()
    val favoriteTracks by libraryViewModel.favoriteTracks.collectAsState()
    val artists by libraryViewModel.artists.collectAsState()
    val albums by libraryViewModel.albums.collectAsState()
    val playlists by libraryViewModel.playlists.collectAsState()
    val isLoading by homeViewModel.isLoading.collectAsState()

    var showSearchBar by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var showTrackMenu by remember { mutableStateOf<Track?>(null) }
    var showPlaylistSelector by remember { mutableStateOf<Track?>(null) }

    val keyboardController = LocalSoftwareKeyboardController.current
    val focusRequester = remember { FocusRequester() }

    val recentTracks = remember(tracks, searchQuery) {
        val filteredTracks = if (searchQuery.isBlank()) {
            tracks
        } else {
            tracks.filter {
                it.title.contains(searchQuery, ignoreCase = true) ||
                        it.artist.contains(searchQuery, ignoreCase = true) ||
                        it.album.contains(searchQuery, ignoreCase = true)
            }
        }
        filteredTracks.sortedByDescending { it.dateAdded }.take(10)
    }

    val displayedTracks = remember(tracks, searchQuery) {
        if (searchQuery.isBlank()) {
            tracks
        } else {
            tracks.filter {
                it.title.contains(searchQuery, ignoreCase = true) ||
                        it.artist.contains(searchQuery, ignoreCase = true) ||
                        it.album.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    // Gestion des permissions
    val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        listOf(
            Manifest.permission.READ_MEDIA_AUDIO,
            Manifest.permission.POST_NOTIFICATIONS
        )
    } else {
        listOf(
            Manifest.permission.READ_EXTERNAL_STORAGE
        )
    }

    val permissionsState = rememberMultiplePermissionsState(permissions = permissions) { permissionsMap ->
        val allGranted = permissionsMap.values.all { it }
        if (allGranted) {
            homeViewModel.onPermissionsGranted()
        }
    }

    // Auto-reload avec retry mechanism
    LaunchedEffect(permissionsState.allPermissionsGranted) {
        if (permissionsState.allPermissionsGranted) {
            // Premier essai immédiat
            homeViewModel.refreshTracks()

            // Si toujours vide après 1 seconde, réessayer
            delay(1000)
            if (tracks.isEmpty()) {
                homeViewModel.scanMedia()

                // Réessayer encore après 2 secondes
                delay(2000)
                if (tracks.isEmpty()) {
                    homeViewModel.scanMedia()
                }
            }
        }
    }

    // Focus sur la barre de recherche quand elle s'ouvre
    LaunchedEffect(showSearchBar) {
        if (showSearchBar) {
            delay(100)
            focusRequester.requestFocus()
        }
    }

    Scaffold(
        containerColor = Color(0xFF000000),
        topBar = {
            TopAppBar(
                title = {
                    if (showSearchBar) {
                        TextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = {
                                Text(
                                    "Rechercher...",
                                    color = Color.Gray
                                )
                            },
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                cursorColor = Color(0xFFFFC107),
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            ),
                            keyboardOptions = KeyboardOptions(
                                imeAction = ImeAction.Search
                            ),
                            keyboardActions = KeyboardActions(
                                onSearch = {
                                    keyboardController?.hide()
                                }
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .focusRequester(focusRequester),
                            singleLine = true
                        )
                    } else {
                        Text(
                            "Musique",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 28.sp
                        )
                    }
                },
                navigationIcon = {
                    if (showSearchBar) {
                        IconButton(onClick = {
                            showSearchBar = false
                            searchQuery = ""
                            keyboardController?.hide()
                        }) {
                            Icon(
                                Icons.Default.ArrowBack,
                                contentDescription = "Retour",
                                tint = Color.White
                            )
                        }
                    }
                },
                actions = {
                    if (!showSearchBar) {
                        IconButton(onClick = { showSearchBar = true }) {
                            Icon(
                                Icons.Default.Search,
                                contentDescription = "Rechercher",
                                tint = Color.White
                            )
                        }
                        IconButton(onClick = onNavigateToSettings) {
                            Icon(
                                Icons.Default.Settings,
                                contentDescription = "Paramètres",
                                tint = Color.White
                            )
                        }
                    } else if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Effacer",
                                tint = Color.White
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Black
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                // Indicateur de chargement
                if (isLoading) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                CircularProgressIndicator(
                                    color = Color(0xFFFFC107),
                                    modifier = Modifier.size(48.dp)
                                )
                                Text(
                                    "Chargement de votre musique...",
                                    color = Color.White,
                                    fontSize = 16.sp
                                )
                            }
                        }
                    }
                }

                // État vide
                if (!isLoading && displayedTracks.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Icon(
                                    Icons.Default.MusicNote,
                                    contentDescription = null,
                                    tint = Color.Gray,
                                    modifier = Modifier.size(64.dp)
                                )
                                Text(
                                    if (searchQuery.isNotEmpty()) "Aucun résultat" else "Aucune musique trouvée",
                                    color = Color.White,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )

                                if (searchQuery.isEmpty() && !permissionsState.allPermissionsGranted) {
                                    Text(
                                        "Autorisez l'accès pour voir votre musique",
                                        color = Color.Gray,
                                        fontSize = 14.sp
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Button(
                                        onClick = { permissionsState.launchMultiplePermissionRequest() },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Color(0xFFFFC107)
                                        )
                                    ) {
                                        Text(
                                            "Autoriser l'accès",
                                            color = Color.Black,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Afficher les sections seulement si pas de recherche active
                if (searchQuery.isEmpty() && !isLoading) {
                    // Section Favoris
                    if (favoriteTracks.isNotEmpty()) {
                        item {
                            SectionHeader(
                                title = "Aimé(s)",
                                subtitle = "${favoriteTracks.size} morceaux",
                                icon = Icons.Default.Favorite,
                                iconTint = Color(0xFFFF4444),
                                onClick = {
                                    libraryViewModel.selectTab(com.example.sonicflow.presentation.library.LibraryTab.Favorites)
                                    onNavigateToLibrary()
                                }
                            )
                        }

                        item {
                            LazyRow(
                                contentPadding = PaddingValues(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(favoriteTracks.take(10)) { track ->
                                    FavoriteTrackCard(
                                        track = track,
                                        isPlaying = playbackState.currentTrack?.id == track.id && playbackState.isPlaying,
                                        onClick = {
                                            playerViewModel.playTrack(track)
                                            onNavigateToPlayer()
                                        }
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(24.dp))
                        }
                    }

                    // Section Lu récemment
                    if (recentTracks.isNotEmpty()) {
                        item {
                            SectionHeader(
                                title = "Lu récemment",
                                subtitle = "${recentTracks.size} morceaux",
                                icon = Icons.Default.AccessTime,
                                iconTint = Color(0xFF4285F4)
                            )
                        }

                        item {
                            LazyRow(
                                contentPadding = PaddingValues(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(recentTracks) { track ->
                                    RecentTrackCard(
                                        track = track,
                                        isPlaying = playbackState.currentTrack?.id == track.id && playbackState.isPlaying,
                                        onClick = {
                                            playerViewModel.playTrack(track)
                                            onNavigateToPlayer()
                                        }
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(24.dp))
                        }
                    }

                    // Section Artistes
                    if (artists.isNotEmpty()) {
                        item {
                            SectionHeader(
                                title = "Artistes",
                                subtitle = "${artists.size} artistes",
                                icon = Icons.Default.Person,
                                iconTint = Color(0xFFAB47BC),
                                onClick = {
                                    libraryViewModel.selectTab(com.example.sonicflow.presentation.library.LibraryTab.Artists)
                                    onNavigateToLibrary()
                                }
                            )
                        }

                        item {
                            LazyRow(
                                contentPadding = PaddingValues(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(artists.take(10)) { artist ->
                                    ArtistCard(
                                        artist = artist,
                                        onClick = {
                                            libraryViewModel.selectArtist(artist)
                                            libraryViewModel.selectTab(com.example.sonicflow.presentation.library.LibraryTab.Artists)
                                            onNavigateToLibrary()
                                        }
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(24.dp))
                        }
                    }

                    // Section Albums
                    if (albums.isNotEmpty()) {
                        item {
                            SectionHeader(
                                title = "Albums",
                                subtitle = "${albums.size} albums",
                                icon = Icons.Default.Album,
                                iconTint = Color(0xFF00BCD4),
                                onClick = {
                                    libraryViewModel.selectTab(com.example.sonicflow.presentation.library.LibraryTab.Albums)
                                    onNavigateToLibrary()
                                }
                            )
                        }

                        item {
                            LazyRow(
                                contentPadding = PaddingValues(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(albums.take(10)) { album ->
                                    AlbumCard(
                                        album = album,
                                        onClick = {
                                            libraryViewModel.selectAlbum(album)
                                            libraryViewModel.selectTab(com.example.sonicflow.presentation.library.LibraryTab.Albums)
                                            onNavigateToLibrary()
                                        }
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(24.dp))
                        }
                    }
                }

                // Liste de toutes les chansons (ou résultats de recherche)
                if (displayedTracks.isNotEmpty()) {
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                if (searchQuery.isEmpty()) "Toutes les chansons" else "Résultats",
                                color = Color.White,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "${displayedTracks.size} morceaux",
                                color = Color.Gray,
                                fontSize = 14.sp
                            )
                        }
                    }

                    itemsIndexed(displayedTracks) { index, track ->
                        TrackListItem(
                            track = track,
                            isPlaying = playbackState.currentTrack?.id == track.id && playbackState.isPlaying,
                            onClick = {
                                playerViewModel.playTrack(track)
                                onNavigateToPlayer()
                            },
                            onMenuClick = {
                                showTrackMenu = track
                            }
                        )
                        if (index < displayedTracks.size - 1) {
                            Divider(
                                modifier = Modifier.padding(horizontal = 16.dp),
                                color = Color.White.copy(alpha = 0.1f)
                            )
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }
        }
    }

    // Menu contextuel pour les pistes
    if (showTrackMenu != null) {
        TrackContextMenu(
            track = showTrackMenu!!,
            onDismiss = { showTrackMenu = null },
            onPlayNext = {
                playerViewModel.addToQueue(showTrackMenu!!)
                showTrackMenu = null
            },
            onAddToQueue = {
                playerViewModel.addToQueue(showTrackMenu!!)
                showTrackMenu = null
            },
            onToggleFavorite = {
                libraryViewModel.toggleFavorite(showTrackMenu!!.id)
                showTrackMenu = null
            },
            onShareTrack = {
                // TODO: Implémenter le partage
                showTrackMenu = null
            },
            onAddToPlaylist = {
                showPlaylistSelector = showTrackMenu
                showTrackMenu = null
            },
            isFavorite = favoriteTracks.any { it.id == showTrackMenu!!.id }
        )
    }

    // Dialog pour sélectionner une playlist
    if (showPlaylistSelector != null && playlists.isNotEmpty()) {
        AlertDialog(
            onDismissRequest = { showPlaylistSelector = null },
            title = { Text("Ajouter à une playlist") },
            text = {
                LazyColumn {
                    items(playlists) { playlist ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    libraryViewModel.addTrackToPlaylist(
                                        playlist.id!!,
                                        showPlaylistSelector!!.id
                                    )
                                    showPlaylistSelector = null
                                }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.LibraryMusic,
                                contentDescription = null,
                                modifier = Modifier.size(24.dp),
                                tint = Color(0xFFFFC107)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                playlist.name,
                                color = Color.White,
                                fontSize = 16.sp
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showPlaylistSelector = null }) {
                    Text("Annuler")
                }
            },
            containerColor = Color(0xFF1E1E1E)
        )
    } else if (showPlaylistSelector != null && playlists.isEmpty()) {
        AlertDialog(
            onDismissRequest = { showPlaylistSelector = null },
            title = { Text("Aucune playlist") },
            text = { Text("Créez une playlist d'abord") },
            confirmButton = {
                TextButton(onClick = { showPlaylistSelector = null }) {
                    Text("OK")
                }
            },
            containerColor = Color(0xFF1E1E1E)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrackContextMenu(
    track: Track,
    onDismiss: () -> Unit,
    onPlayNext: () -> Unit,
    onAddToQueue: () -> Unit,
    onToggleFavorite: () -> Unit,
    onShareTrack: () -> Unit,
    onAddToPlaylist: () -> Unit,
    isFavorite: Boolean
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1E1E1E)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            // En-tête avec info de la piste
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(8.dp))
                ) {
                    if (track.albumArtUri != null) {
                        AsyncImage(
                            model = track.albumArtUri,
                            contentDescription = "${track.title} album art",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop,
                            error = painterResource(id = android.R.drawable.ic_media_play)
                        )
                    } else {
                        AlbumArtPlaceholder(
                            title = track.album,
                            artist = track.artist,
                            size = 56.dp,
                            cornerRadius = 8.dp
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        track.title,
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        track.artist,
                        color = Color.Gray,
                        fontSize = 14.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Divider(color = Color.White.copy(alpha = 0.1f))

            // Options du menu
            MenuOption(
                icon = Icons.Default.PlayArrow,
                text = "Lire ensuite",
                onClick = onPlayNext
            )

            MenuOption(
                icon = Icons.Default.QueueMusic,
                text = "Ajouter à la file d'attente",
                onClick = onAddToQueue
            )

            MenuOption(
                icon = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                text = if (isFavorite) "Retirer des favoris" else "Ajouter aux favoris",
                iconTint = if (isFavorite) Color(0xFFFF4444) else Color.White,
                onClick = onToggleFavorite
            )

            MenuOption(
                icon = Icons.Default.PlaylistAdd,
                text = "Ajouter à une playlist",
                onClick = onAddToPlaylist
            )

            MenuOption(
                icon = Icons.Default.Share,
                text = "Partager",
                onClick = onShareTrack
            )

            MenuOption(
                icon = Icons.Default.Album,
                text = "Voir l'album",
                onClick = onDismiss
            )

            MenuOption(
                icon = Icons.Default.Person,
                text = "Voir l'artiste",
                onClick = onDismiss
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun MenuOption(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    iconTint: Color = Color.White,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text,
            color = Color.White,
            fontSize = 16.sp
        )
    }
}

@Composable
fun SectionHeader(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    onClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (onClick != null) {
                    Modifier.clickable(onClick = onClick)
                } else {
                    Modifier
                }
            )
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(24.dp)
            )
            Column {
                Text(
                    title,
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    subtitle,
                    color = Color.Gray,
                    fontSize = 12.sp
                )
            }
        }
        if (onClick != null) {
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = "Voir plus",
                tint = Color.Gray
            )
        }
    }
}

@Composable
fun FavoriteTrackCard(
    track: Track,
    isPlaying: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(140.dp)
            .clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .size(140.dp)
                .clip(RoundedCornerShape(12.dp))
        ) {
            if (track.albumArtUri != null) {
                AsyncImage(
                    model = track.albumArtUri,
                    contentDescription = "${track.title} album art",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    error = painterResource(id = android.R.drawable.ic_media_play)
                )
            } else {
                AlbumArtPlaceholder(
                    title = track.album,
                    artist = track.artist,
                    size = 140.dp,
                    cornerRadius = 12.dp
                )
            }

            if (isPlaying) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = Color(0xFFFFC107),
                        modifier = Modifier.size(48.dp)
                    )
                }
            }

            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFFF4444))
            ) {
                Icon(
                    Icons.Default.Favorite,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier
                        .size(16.dp)
                        .align(Alignment.Center)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            track.title,
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            track.artist,
            color = Color.Gray,
            fontSize = 12.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun RecentTrackCard(
    track: Track,
    isPlaying: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(120.dp)
            .clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(RoundedCornerShape(8.dp))
        ) {
            if (track.albumArtUri != null) {
                AsyncImage(
                    model = track.albumArtUri,
                    contentDescription = "${track.title} album art",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    error = painterResource(id = android.R.drawable.ic_media_play)
                )
            } else {
                AlbumArtPlaceholder(
                    title = track.album,
                    artist = track.artist,
                    size = 120.dp,
                    cornerRadius = 8.dp
                )
            }

            if (isPlaying) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = Color(0xFFFFC107),
                        modifier = Modifier.size(40.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            track.title,
            color = Color.White,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun ArtistCard(
    artist: String,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(100.dp)
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF9C27B0),
                            Color(0xFF673AB7)
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.Person,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(48.dp)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            artist,
            color = Color.White,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}

@Composable
fun AlbumCard(
    album: String,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(120.dp)
            .clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(RoundedCornerShape(8.dp))
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
                modifier = Modifier.size(48.dp)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            album,
            color = Color.White,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun TrackListItem(
    track: Track,
    isPlaying: Boolean,
    onClick: () -> Unit,
    onMenuClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(6.dp))
        ) {
            if (track.albumArtUri != null) {
                AsyncImage(
                    model = track.albumArtUri,
                    contentDescription = "${track.title} album art",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    error = painterResource(id = android.R.drawable.ic_media_play)
                )
            } else {
                AlbumArtPlaceholder(
                    title = track.album,
                    artist = track.artist,
                    size = 48.dp,
                    cornerRadius = 6.dp
                )
            }

            if (isPlaying) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = Color(0xFFFFC107),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                track.title,
                color = if (isPlaying) Color(0xFFFFC107) else Color.White,
                fontSize = 15.sp,
                fontWeight = if (isPlaying) FontWeight.Bold else FontWeight.Normal,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                track.artist,
                color = Color.Gray,
                fontSize = 13.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        IconButton(onClick = onMenuClick) {
            Icon(
                Icons.Default.MoreVert,
                contentDescription = "Options",
                tint = Color.Gray
            )
        }
    }
}
