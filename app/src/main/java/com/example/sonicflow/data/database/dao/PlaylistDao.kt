package com.example.sonicflow.data.database.dao

import androidx.room.*
import com.example.sonicflow.data.database.entities.PlaylistEntity
import com.example.sonicflow.data.database.entities.PlaylistTrackCrossRef
import com.example.sonicflow.data.database.entities.TrackEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PlaylistDao {

    @Query("SELECT * FROM playlists ORDER BY createdAt DESC")
    fun getAllPlaylists(): Flow<List<PlaylistEntity>>

    @Query("SELECT * FROM playlists WHERE id = :playlistId")
    suspend fun getPlaylistById(playlistId: Long): PlaylistEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylist(playlist: PlaylistEntity): Long

    @Update
    suspend fun updatePlaylist(playlist: PlaylistEntity)

    @Delete
    suspend fun deletePlaylist(playlist: PlaylistEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylistTrackCrossRef(crossRef: PlaylistTrackCrossRef)

    @Delete
    suspend fun deletePlaylistTrackCrossRef(crossRef: PlaylistTrackCrossRef)

    @Query("""
        SELECT tracks.* FROM tracks
        INNER JOIN playlist_track_cross_ref ON tracks.id = playlist_track_cross_ref.trackId
        WHERE playlist_track_cross_ref.playlistId = :playlistId
        ORDER BY playlist_track_cross_ref.position ASC
    """)
    fun getTracksInPlaylist(playlistId: Long): Flow<List<TrackEntity>>

    @Query("SELECT COUNT(*) FROM playlist_track_cross_ref WHERE playlistId = :playlistId")
    suspend fun getPlaylistTrackCount(playlistId: Long): Int
}
