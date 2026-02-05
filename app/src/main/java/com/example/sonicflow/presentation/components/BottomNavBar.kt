package com.example.sonicflow.presentation.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp

@Composable
fun BottomNavBar(
    currentRoute: String,
    onNavigateToHome: () -> Unit,
    onNavigateToArtists: () -> Unit,
    onNavigateToAlbums: () -> Unit,
    onNavigateToPlaylists: () -> Unit
) {
    NavigationBar(
        containerColor = Color(0xFF1A1A1A),
        contentColor = Color.White
    ) {
        NavigationBarItem(
            icon = {
                Icon(
                    Icons.Default.Home,
                    "Musique",
                    tint = if (currentRoute == "home") Color(0xFFFFC107) else Color.Gray
                )
            },
            label = {
                Text(
                    "Musique",
                    color = if (currentRoute == "home") Color(0xFFFFC107) else Color.Gray,
                    fontSize = 11.sp
                )
            },
            selected = currentRoute == "home",
            onClick = onNavigateToHome
        )
        NavigationBarItem(
            icon = {
                Icon(
                    Icons.Default.Person,
                    "Artiste",
                    tint = if (currentRoute == "artists") Color(0xFFFFC107) else Color.Gray
                )
            },
            label = {
                Text(
                    "Artiste",
                    color = if (currentRoute == "artists") Color(0xFFFFC107) else Color.Gray,
                    fontSize = 11.sp
                )
            },
            selected = currentRoute == "artists",
            onClick = onNavigateToArtists
        )
        NavigationBarItem(
            icon = {
                Icon(
                    Icons.Default.Album,
                    "Albums",
                    tint = if (currentRoute == "albums") Color(0xFFFFC107) else Color.Gray
                )
            },
            label = {
                Text(
                    "Albums",
                    color = if (currentRoute == "albums") Color(0xFFFFC107) else Color.Gray,
                    fontSize = 11.sp
                )
            },
            selected = currentRoute == "albums",
            onClick = onNavigateToAlbums
        )
        NavigationBarItem(
            icon = {
                Icon(
                    Icons.Default.LibraryMusic,
                    "Listes",
                    tint = if (currentRoute == "playlists") Color(0xFFFFC107) else Color.Gray
                )
            },
            label = {
                Text(
                    "Listes",
                    color = if (currentRoute == "playlists") Color(0xFFFFC107) else Color.Gray,
                    fontSize = 10.sp
                )
            },
            selected = currentRoute == "playlists",
            onClick = onNavigateToPlaylists
        )
    }
}