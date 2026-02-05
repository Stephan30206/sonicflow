package com.example.sonicflow.presentation

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.sonicflow.data.preferences.UserPreferences
import com.example.sonicflow.presentation.albums.AlbumsScreen
import com.example.sonicflow.presentation.artists.ArtistsScreen
import com.example.sonicflow.presentation.components.BottomNavBar
import com.example.sonicflow.presentation.components.MiniPlayer
import com.example.sonicflow.presentation.favorites.FavoritesScreen
import com.example.sonicflow.presentation.home.HomeScreen
import com.example.sonicflow.presentation.player.MusicPlayerScreen
import com.example.sonicflow.presentation.player.PlayerViewModel
import com.example.sonicflow.presentation.playlists.PlaylistsScreen
import com.example.sonicflow.presentation.settings.SettingsScreen
import com.example.sonicflow.presentation.settings.SettingsViewModel
import com.example.sonicflow.service.MusicService
import com.example.sonicflow.service.MusicServiceConnection
import com.example.sonicflow.ui.theme.SonicFlowTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var musicServiceConnection: MusicServiceConnection

    @Inject
    lateinit var userPreferences: UserPreferences

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.values.all { it }
        if (allGranted) {
            startMusicService()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestPermissions()

        setContent {
            val isDarkMode by userPreferences.isDarkModeEnabled.collectAsState(initial = true)

            SonicFlowTheme(darkTheme = isDarkMode) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation(musicServiceConnection)
                }
            }
        }
    }

    private fun requestPermissions() {
        val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(
                Manifest.permission.READ_MEDIA_AUDIO,
                Manifest.permission.POST_NOTIFICATIONS
            )
        } else {
            arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
        }

        val allGranted = permissions.all {
            ContextCompat.checkSelfPermission(this, it) ==
                    android.content.pm.PackageManager.PERMISSION_GRANTED
        }

        if (allGranted) {
            startMusicService()
        } else {
            permissionLauncher.launch(permissions)
        }
    }

    private fun startMusicService() {
        val intent = Intent(this, MusicService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        musicServiceConnection.disconnect()
    }
}

@Composable
fun AppNavigation(musicServiceConnection: MusicServiceConnection) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val playerViewModel: PlayerViewModel = hiltViewModel()

    LaunchedEffect(Unit) {
        musicServiceConnection.connect()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(modifier = Modifier.weight(1f)) {
                NavHost(
                    navController = navController,
                    startDestination = "home"
                ) {
                    composable("home") {
                        HomeScreen(
                            onNavigateToPlayer = { navController.navigate("player") },
                            onNavigateToLibrary = { navController.navigate("favorites") },
                            onNavigateToSettings = { navController.navigate("settings") }
                        )
                    }

                    composable("artists") {
                        ArtistsScreen(
                            onNavigateToPlayer = { navController.navigate("player") }
                        )
                    }

                    composable("albums") {
                        AlbumsScreen(
                            onNavigateToPlayer = { navController.navigate("player") }
                        )
                    }

                    composable("playlists") {
                        PlaylistsScreen(
                            onNavigateToPlayer = { navController.navigate("player") }
                        )
                    }

                    composable("favorites") {
                        FavoritesScreen(
                            onNavigateBack = { navController.popBackStack() },
                            onNavigateToPlayer = { navController.navigate("player") }
                        )
                    }

                    composable("player") {
                        MusicPlayerScreen(
                            viewModel = playerViewModel,
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }

                    composable("settings") {
                        SettingsScreen(
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }
                }
            }

            if (currentRoute != "player") {
                MiniPlayer(
                    viewModel = playerViewModel,
                    onExpandPlayer = { navController.navigate("player") }
                )
            }

            if (currentRoute in listOf("home", "artists", "albums", "playlists")) {
                BottomNavBar(
                    currentRoute = currentRoute ?: "home",
                    onNavigateToHome = {
                        if (currentRoute != "home") {
                            navController.navigate("home") {
                                popUpTo("home") { inclusive = true }
                            }
                        }
                    },
                    onNavigateToArtists = {
                        if (currentRoute != "artists") {
                            navController.navigate("artists") {
                                popUpTo("home")
                            }
                        }
                    },
                    onNavigateToAlbums = {
                        if (currentRoute != "albums") {
                            navController.navigate("albums") {
                                popUpTo("home")
                            }
                        }
                    },
                    onNavigateToPlaylists = {
                        if (currentRoute != "playlists") {
                            navController.navigate("playlists") {
                                popUpTo("home")
                            }
                        }
                    }
                )
            }
        }
    }
}
