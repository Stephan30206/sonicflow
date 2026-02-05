package com.example.sonicflow.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.sonicflow.data.database.dao.TrackDao
import com.example.sonicflow.data.database.dao.FavoriteDao
import com.example.sonicflow.data.database.dao.PlaylistDao
import com.example.sonicflow.data.database.entities.*

@Database(
    entities = [
        TrackEntity::class,
        PlaylistEntity::class,
        PlaylistTrackCrossRef::class,
        FavoriteEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun trackDao(): TrackDao
    abstract fun playlistDao(): PlaylistDao
    abstract fun favoriteDao(): FavoriteDao
}
