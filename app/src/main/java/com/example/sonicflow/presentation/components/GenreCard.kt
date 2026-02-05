package com.example.sonicflow.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class Genre(
    val id: Int,
    val name: String,
    val trackCount: Int,
    val colors: List<Color>
)

@Composable
fun GenreCard(
    genre: Genre,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(16.dp))
            .background(
                brush = Brush.linearGradient(
                    colors = genre.colors
                )
            )
            .clickable(onClick = onClick)
            .padding(12.dp),
        contentAlignment = Alignment.BottomStart
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.End)
                    .size(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.White.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.MusicNote,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
            
            Column {
                Text(
                    text = genre.name,
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${genre.trackCount} tracks",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

// Mock genres data
val mockGenres = listOf(
    Genre(
        id = 1,
        name = "Techno",
        trackCount = 434,
        colors = listOf(Color(0xFFCDDC39), Color(0xFFF57F17))
    ),
    Genre(
        id = 2,
        name = "Trance",
        trackCount = 298,
        colors = listOf(Color(0xFF00BCD4), Color(0xFF00695C))
    ),
    Genre(
        id = 3,
        name = "Hardstyle",
        trackCount = 376,
        colors = listOf(Color(0xFFFFEB3B), Color(0xFFF57F17))
    ),
    Genre(
        id = 4,
        name = "House",
        trackCount = 312,
        colors = listOf(Color(0xFFFF7043), Color(0xFFE64A19))
    ),
    Genre(
        id = 5,
        name = "Drum & Bass",
        trackCount = 267,
        colors = listOf(Color(0xFF9C27B0), Color(0xFF6A1B9A))
    ),
    Genre(
        id = 6,
        name = "Dubstep",
        trackCount = 189,
        colors = listOf(Color(0xFF3F51B5), Color(0xFF1A237E))
    )
)
