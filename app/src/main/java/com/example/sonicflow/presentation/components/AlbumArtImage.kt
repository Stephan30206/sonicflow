package com.example.sonicflow.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.Icon
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import android.net.Uri

/**
 * Composant amélioré pour afficher l'album art avec gestion complète des erreurs
 */
@Composable
fun AlbumArtImage(
    albumArtUri: String?,
    modifier: Modifier = Modifier,
    size: Dp = 56.dp,
    cornerRadius: Dp = 8.dp,
    iconSize: Dp = 32.dp,
    backgroundColor: Color = Color(0xFF2A2A2A),
    iconTint: Color = Color(0xFFFFC107)
) {
    val context = LocalContext.current

    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(cornerRadius))
            .background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        when {
            albumArtUri.isNullOrEmpty() -> {
                // Pas d'URI - afficher l'icône par défaut
                Icon(
                    imageVector = Icons.Default.MusicNote,
                    contentDescription = "Default Album Art",
                    tint = iconTint,
                    modifier = Modifier.size(iconSize)
                )
            }
            else -> {
                // Essayer de charger l'image
                SubcomposeAsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(parseAlbumArtUri(albumArtUri))
                        .crossfade(true)
                        .build(),
                    contentDescription = "Album Art",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    loading = {
                        // Pendant le chargement, afficher l'icône
                        Icon(
                            imageVector = Icons.Default.MusicNote,
                            contentDescription = "Loading",
                            tint = iconTint.copy(alpha = 0.5f),
                            modifier = Modifier.size(iconSize)
                        )
                    },
                    error = {
                        // En cas d'erreur, afficher l'icône
                        Icon(
                            imageVector = Icons.Default.MusicNote,
                            contentDescription = "Error loading image",
                            tint = iconTint,
                            modifier = Modifier.size(iconSize)
                        )
                    }
                )
            }
        }
    }
}

/**
 * Parse l'URI de l'album pour s'assurer qu'elle est dans le bon format
 */
private fun parseAlbumArtUri(uri: String): Any {
    return when {
        uri.startsWith("content://") -> Uri.parse(uri)
        uri.startsWith("file://") -> Uri.parse(uri)
        uri.startsWith("/") -> Uri.parse("file://$uri")
        else -> uri
    }
}

/**
 * Version simple pour usage direct dans les composants
 */
@Composable
fun SimpleAlbumArt(
    albumArtUri: String?,
    modifier: Modifier = Modifier
) {
    AlbumArtImage(
        albumArtUri = albumArtUri,
        modifier = modifier,
        size = 56.dp,
        cornerRadius = 8.dp,
        iconSize = 32.dp
    )
}

/**
 * Version large pour le player
 */
@Composable
fun LargeAlbumArt(
    albumArtUri: String?,
    modifier: Modifier = Modifier
) {
    AlbumArtImage(
        albumArtUri = albumArtUri,
        modifier = modifier,
        size = 280.dp,
        cornerRadius = 16.dp,
        iconSize = 120.dp,
        backgroundColor = Color(0xFF1E1E1E)
    )
}

/**
 * Version mini pour la bottom bar
 */
@Composable
fun MiniAlbumArt(
    albumArtUri: String?,
    modifier: Modifier = Modifier
) {
    AlbumArtImage(
        albumArtUri = albumArtUri,
        modifier = modifier,
        size = 48.dp,
        cornerRadius = 6.dp,
        iconSize = 24.dp
    )
}