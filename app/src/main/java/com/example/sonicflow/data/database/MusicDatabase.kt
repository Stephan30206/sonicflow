package com.example.sonicflow.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.sonicflow.data.database.dao.PlaylistDao
import com.example.sonicflow.data.database.dao.TrackDao
import com.example.sonicflow.data.database.entities.PlaylistEntity
import com.example.sonicflow.data.database.entities.PlaylistTrackCrossRef
import com.example.sonicflow.data.database.entities.TrackEntity

@Database(
    entities = [
        TrackEntity::class,
        PlaylistEntity::class,
        PlaylistTrackCrossRef::class
    ],
    version = 1,
    exportSchema = false
)
abstract class MusicDatabase : RoomDatabase() {
    abstract fun trackDao(): TrackDao
    abstract fun playlistDao(): PlaylistDao
}
