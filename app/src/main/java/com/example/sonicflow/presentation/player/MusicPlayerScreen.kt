package com.example.sonicflow.presentation.player

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.sonicflow.data.model.Track
import com.example.sonicflow.presentation.components.AlbumArtPlaceholder
import kotlin.math.sin
import kotlin.random.Random

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MusicPlayerScreen(
    viewModel: PlayerViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val playbackState by viewModel.playbackState.collectAsState()
    val currentTrack = playbackState.currentTrack
    val isShuffleEnabled by viewModel.isShuffleEnabled.collectAsState()
    val repeatMode by viewModel.repeatMode.collectAsState()
    val isFavorite by viewModel.isFavorite.collectAsState()

    // Configuration responsive
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val screenHeight = configuration.screenHeightDp.dp
    val isSmallScreen = screenWidth < 360.dp
    val isMediumScreen = screenWidth >= 360.dp && screenWidth < 400.dp
    var showTrackMenu by remember { mutableStateOf<Track?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFFFA500),
                        Color(0xFF1E1E1E)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Top Bar
            TopAppBar(
                title = {
                    Text(
                        text = "Now Playing",
                        color = Color.Black,
                        fontWeight = FontWeight.Bold,
                        fontSize = if (isSmallScreen) 18.sp else 20.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.Black
                        )
                    }
                },
                actions = {
                    // Bouton Favoris
                    IconButton(onClick = { viewModel.toggleFavorite() }) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Favorite",
                            tint = if (isFavorite) Color(0xFFFF4444) else Color.Black
                        )
                    }
                    IconButton(onClick = { /* Menu */ }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "More",
                            tint = Color.Black
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )

            if (currentTrack == null) {
                // État vide
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.MusicNote,
                            contentDescription = null,
                            modifier = Modifier.size(if (isSmallScreen) 60.dp else 80.dp),
                            tint = Color.White
                        )
                        Spacer(Modifier.height(16.dp))
                        Text(
                            text = "No track playing",
                            fontSize = if (isSmallScreen) 18.sp else 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = if (isSmallScreen) 16.dp else 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(if (isSmallScreen) 20.dp else 40.dp))

                    // Album Art avec cercle (comme dans l'image)
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(
                                when {
                                    isSmallScreen -> 240.dp
                                    isMediumScreen -> 280.dp
                                    else -> 320.dp
                                }
                            )
                    ) {
                        // Cercle de fond bleu
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    color = Color(0xFFF33249),
                                    shape = CircleShape
                                )
                        )

                        // Image album
                        AsyncImage(
                            model = currentTrack.albumArtUri,
                            contentDescription = "Album Art",
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(8.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    }

                    Spacer(modifier = Modifier.height(if (isSmallScreen) 24.dp else 40.dp))

                    // Titre de la chanson (UNE SEULE LIGNE)
                    Text(
                        text = currentTrack.title,
                        fontSize = when {
                            isSmallScreen -> 18.sp
                            isMediumScreen -> 20.sp
                            else -> 24.sp
                        },
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Artiste
                    Text(
                        text = currentTrack.artist,
                        fontSize = when {
                            isSmallScreen -> 14.sp
                            isMediumScreen -> 15.sp
                            else -> 16.sp
                        },
                        color = Color.Gray,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    )

                    Spacer(modifier = Modifier.height(if (isSmallScreen) 16.dp else 24.dp))

                    // WAVEFORM VISUALIZATION - Synchronized with playback
                    AnimatedWaveform(
                        isPlaying = playbackState.isPlaying,
                        trackId = currentTrack.id,
                        currentPosition = playbackState.currentPosition,
                        duration = playbackState.duration,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(
                                when {
                                    isSmallScreen -> (screenHeight * 0.08f).coerceIn(50.dp, 70.dp)
                                    isMediumScreen -> (screenHeight * 0.09f).coerceIn(60.dp, 80.dp)
                                    else -> (screenHeight * 0.10f).coerceIn(70.dp, 100.dp)
                                }
                            )
                            .padding(horizontal = 8.dp)
                    )

                    Spacer(modifier = Modifier.height(if (isSmallScreen) 16.dp else 24.dp))

                    // Progress Bar
                    Column(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        val duration = playbackState.duration.coerceAtLeast(1L)
                        val position = playbackState.currentPosition.coerceIn(0L, duration)
                        val progress = position.toFloat() / duration.toFloat()

                        // Custom Progress Bar
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(Color.Gray.copy(alpha = 0.3f))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth(progress)
                                    .background(Color(0xFFFFC107))
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = viewModel.formatDuration(position),
                                color = Color.White,
                                fontSize = if (isSmallScreen) 11.sp else 12.sp
                            )
                            Text(
                                text = viewModel.formatDuration(duration),
                                color = Color.White,
                                fontSize = if (isSmallScreen) 11.sp else 12.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(if (isSmallScreen) 24.dp else 32.dp))

                    // Contrôles de lecture
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Shuffle
                        IconButton(
                            onClick = { viewModel.toggleShuffle() },
                            modifier = Modifier.size(if (isSmallScreen) 40.dp else 48.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Shuffle,
                                contentDescription = "Shuffle",
                                tint = if (isShuffleEnabled) Color(0xFFFFC107) else Color.Gray,
                                modifier = Modifier.size(if (isSmallScreen) 24.dp else 28.dp)
                            )
                        }

                        // Previous
                        IconButton(
                            onClick = { viewModel.skipToPrevious() },
                            modifier = Modifier.size(if (isSmallScreen) 48.dp else 56.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.SkipPrevious,
                                contentDescription = "Previous",
                                tint = Color.White,
                                modifier = Modifier.size(if (isSmallScreen) 32.dp else 40.dp)
                            )
                        }

                        // Play/Pause
                        IconButton(
                            onClick = { viewModel.togglePlayPause() },
                            modifier = Modifier
                                .size(if (isSmallScreen) 64.dp else 72.dp)
                                .background(Color(0xFFFFC107), CircleShape)
                        ) {
                            Icon(
                                imageVector = if (playbackState.isPlaying) {
                                    Icons.Default.Pause
                                } else {
                                    Icons.Default.PlayArrow
                                },
                                contentDescription = if (playbackState.isPlaying) "Pause" else "Play",
                                tint = Color.Black,
                                modifier = Modifier.size(if (isSmallScreen) 36.dp else 40.dp)
                            )
                        }

                        // Next
                        IconButton(
                            onClick = { viewModel.skipToNext() },
                            modifier = Modifier.size(if (isSmallScreen) 48.dp else 56.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.SkipNext,
                                contentDescription = "Next",
                                tint = Color.White,
                                modifier = Modifier.size(if (isSmallScreen) 32.dp else 40.dp)
                            )
                        }

                        // Repeat
                        IconButton(
                            onClick = { viewModel.toggleRepeat() },
                            modifier = Modifier.size(if (isSmallScreen) 40.dp else 48.dp)
                        ) {
                            Icon(
                                imageVector = when (repeatMode) {
                                    com.example.sonicflow.data.model.RepeatMode.ONE -> Icons.Default.RepeatOne
                                    else -> Icons.Default.Repeat
                                },
                                contentDescription = "Repeat",
                                tint = when (repeatMode) {
                                    com.example.sonicflow.data.model.RepeatMode.OFF -> Color.Gray
                                    else -> Color(0xFFFFC107)
                                },
                                modifier = Modifier.size(if (isSmallScreen) 24.dp else 28.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
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
fun AnimatedWaveform(
    isPlaying: Boolean,
    trackId: Long,
    currentPosition: Long = 0L,
    duration: Long = 1L,
    modifier: Modifier = Modifier
) {
    // BoxWithConstraints pour obtenir les dimensions exactes disponibles
    BoxWithConstraints(modifier = modifier) {
        val availableWidth = constraints.maxWidth.toFloat()
        val availableHeight = constraints.maxHeight.toFloat()

        // Adapter le nombre de barres en fonction de la largeur de l'écran
        val barCount = remember(availableWidth) {
            (availableWidth / 10f).toInt().coerceIn(30, 50)
        }

        // Générer un pattern unique pour chaque chanson basé sur son ID
        val waveformData = remember(trackId, barCount) {
            generateWaveformPattern(trackId, barCount)
        }

        val infiniteTransition = rememberInfiniteTransition(label = "waveform")

        // Calculer la position de lecture relative (0 à 1)
        val progressRatio = remember(currentPosition, duration) {
            if (duration > 0) (currentPosition.toFloat() / duration).coerceIn(0f, 1f) else 0f
        }

        val animatedValues = waveformData.indices.map { index ->
            infiniteTransition.animateFloat(
                initialValue = if (isPlaying) 0f else 0.3f,
                targetValue = if (isPlaying) 1f else 0.3f,
                animationSpec = infiniteRepeatable(
                    animation = tween(
                        durationMillis = waveformData[index].speed,
                        easing = FastOutSlowInEasing
                    ),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "wave_$index"
            )
        }

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
        ) {
            val totalBars = waveformData.size
            // Calculer dynamiquement la largeur et l'espacement des barres
            val totalSpacing = size.width * 0.6f // 60% pour les espacements
            val totalBarWidth = size.width * 0.4f // 40% pour les barres
            val barWidth = (totalBarWidth / totalBars).coerceAtLeast(2f)
            val spacing = (totalSpacing / (totalBars - 1)).coerceAtLeast(2f)

            waveformData.forEachIndexed { index, data ->
                val animatedValue = if (isPlaying) {
                    animatedValues[index].value
                } else {
                    0.3f
                }

                // Hauteur minimale et maximale adaptées à la hauteur disponible
                val minHeight = size.height * 0.15f
                val maxHeight = size.height * 0.95f

                val barHeight = (data.height * animatedValue * maxHeight).coerceIn(
                    minHeight,
                    maxHeight
                )

                val x = index * (barWidth + spacing) + barWidth / 2 + spacing / 2

                // Déterminer si cette barre est passée ou en cours
                val barProgressRatio = index.toFloat() / totalBars
                val isBarPlayed = barProgressRatio < progressRatio
                val isNearPlayhead = kotlin.math.abs(barProgressRatio - progressRatio) < (1f / totalBars)

                // Couleur basée sur la position de lecture
                val barColor = when {
                    isNearPlayhead -> Color(0xFFFF6B35) // Orange rougeâtre pour la position actuelle
                    isBarPlayed -> Color(0xFFFFC107).copy(alpha = 0.9f) // Jaune lumineux pour le passé
                    else -> Color(0xFFFFC107).copy(alpha = 0.4f) // Jaune faible pour le futur
                }

                drawLine(
                    color = barColor,
                    start = Offset(x, size.height / 2 - barHeight / 2),
                    end = Offset(x, size.height / 2 + barHeight / 2),
                    strokeWidth = barWidth.coerceAtLeast(2f),
                    cap = StrokeCap.Round
                )
            }
        }
    }
}

data class WaveformBar(
    val height: Float,
    val speed: Int
)

fun generateWaveformPattern(trackId: Long, barCount: Int = 40): List<WaveformBar> {
    // Utiliser l'ID de la chanson comme seed pour générer un pattern unique
    val seed = trackId.hashCode()
    val random = Random(seed)

    val bars = mutableListOf<WaveformBar>()

    for (i in 0 until barCount) {
        // Créer un pattern qui varie mais reste cohérent pour la chanson
        // Utiliser plusieurs fréquences sinusoïdales pour un pattern plus naturel
        val wave1 = sin(i * 0.25).toFloat()
        val wave2 = sin(i * 0.5).toFloat()
        val baseHeight = (wave1 + wave2) / 2f

        val randomVariation = random.nextFloat() * 0.5f + 0.3f
        val height = ((baseHeight + 1f) / 2f * randomVariation + 0.35f).coerceIn(0.35f, 1f)

        // Vitesse d'animation variée pour un effet plus dynamique
        val speed = random.nextInt(350, 850)

        bars.add(WaveformBar(height, speed))
    }

    return bars
}
