package com.example.sonicflow.presentation

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.sonicflow.presentation.home.HomeScreen
import com.example.sonicflow.presentation.library.LibraryScreen
import com.example.sonicflow.presentation.player.MusicPlayerScreen
import com.example.sonicflow.presentation.settings.SettingsScreen
import com.example.sonicflow.service.MusicService
import com.example.sonicflow.service.MusicServiceConnection
import com.example.sonicflow.ui.theme.SonicFlowTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var musicServiceConnection: MusicServiceConnection

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

        // Demander les permissions
        requestPermissions()

        setContent {
            SonicFlowTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()

                    // Connecter au service de musique
                    LaunchedEffect(Unit) {
                        musicServiceConnection.connect()
                    }

                    // Navigation
                    NavHost(
                        navController = navController,
                        startDestination = "home"
                    ) {
                        // Écran Home (Liste des morceaux)
                        composable("home") {
                            HomeScreen(
                                onNavigateToPlayer = {
                                    navController.navigate("player")
                                },
                                onNavigateToLibrary = {
                                    navController.navigate("library")
                                },
                                onNavigateToSettings = {
                                    navController.navigate("settings")
                                }
                            )
                        }

                        // Écran Player (Lecteur de musique)
                        composable("player") {
                            MusicPlayerScreen(
                                onNavigateBack = {
                                    navController.navigateUp()
                                }
                            )
                        }

                        // Écran Library (Playlists)
                        composable("library") {
                            LibraryScreen(
                                onNavigateBack = {
                                    navController.navigateUp()
                                },
                                onNavigateToPlayer = {
                                    navController.navigate("player")
                                }
                            )
                        }

                        // Écran Settings
                        composable("settings") {
                            SettingsScreen(
                                onNavigateBack = {
                                    navController.navigateUp()
                                }
                            )
                        }
                    }
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