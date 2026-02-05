package com.example.sonicflow

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.disk.DiskCache
import coil.memory.MemoryCache
import coil.request.CachePolicy
import coil.util.DebugLogger
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class SonicFlowApplication : Application(), ImageLoaderFactory {

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                NOTIFICATION_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Music playback controls"
                setShowBadge(false)
            }

            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager?.createNotificationChannel(channel)
        }
    }

    /**
     * Configuration personnalisée de Coil pour optimiser le chargement des images d'albums
     */
    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            // Cache mémoire - 25% de la RAM disponible
            .memoryCache {
                MemoryCache.Builder(this)
                    .maxSizePercent(0.25)
                    .strongReferencesEnabled(true)
                    .build()
            }
            // Cache disque - 50 MB
            .diskCache {
                DiskCache.Builder()
                    .directory(cacheDir.resolve("image_cache"))
                    .maxSizeBytes(50 * 1024 * 1024)
                    .build()
            }
            // Politique de cache
            .memoryCachePolicy(CachePolicy.ENABLED)
            .diskCachePolicy(CachePolicy.ENABLED)
            .networkCachePolicy(CachePolicy.ENABLED)
            // Ne pas respecter les headers de cache pour les URIs locales
            .respectCacheHeaders(false)
            // Transition en douceur
            .crossfade(true)
            .crossfade(300)
            // Logger pour le débogage (à retirer en production si vous voulez)
            .logger(DebugLogger())
            .build()
    }

    companion object {
        const val NOTIFICATION_CHANNEL_ID = "music_playback"
        const val NOTIFICATION_CHANNEL_NAME = "Music Playback"
    }
}