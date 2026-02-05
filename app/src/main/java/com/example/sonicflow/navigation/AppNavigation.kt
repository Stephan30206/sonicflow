package com.example.sonicflow.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.sonicflow.presentation.components.MiniPlayer
import com.example.sonicflow.presentation.home.HomeScreen
import com.example.sonicflow.presentation.library.LibraryScreen
import com.example.sonicflow.presentation.player.MusicPlayerScreen
import com.example.sonicflow.presentation.player.PlayerViewModel
import com.example.sonicflow.presentation.settings.SettingsScreen

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Library : Screen("library")
    object Player : Screen("player")
    object Settings : Screen("settings")
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(modifier = Modifier.weight(1f)) {
                NavHost(
                    navController = navController,
                    startDestination = Screen.Home.route
                ) {
                    composable(Screen.Home.route) {
                        HomeScreen(
                            onNavigateToPlayer = {
                                navController.navigate(Screen.Player.route)
                            },
                            onNavigateToLibrary = {
                                navController.navigate(Screen.Library.route)
                            },
                            onNavigateToSettings = {
                                navController.navigate(Screen.Settings.route)
                            }
                        )
                    }

                    composable(Screen.Library.route) {
                        LibraryScreen(
                            onNavigateBack = {
                                navController.popBackStack()
                            },
                            onNavigateToPlayer = {
                                navController.navigate(Screen.Player.route)
                            }
                        )
                    }

                    composable(Screen.Player.route) {
                        val playerViewModel: PlayerViewModel = hiltViewModel()
                        MusicPlayerScreen(
                            viewModel = playerViewModel,
                            onNavigateBack = {
                                navController.popBackStack()
                            }
                        )
                    }

                    composable(Screen.Settings.route) {
                        SettingsScreen(
                            onNavigateBack = {
                                navController.popBackStack()
                            }
                        )
                    }
                }
            }

            if (navController.currentBackStackEntry?.destination?.route != Screen.Player.route) {
                val playerViewModel: PlayerViewModel = hiltViewModel()
                MiniPlayer(
                    viewModel = playerViewModel,
                    onExpandPlayer = {
                        navController.navigate(Screen.Player.route)
                    }
                )
            }
        }
    }
}