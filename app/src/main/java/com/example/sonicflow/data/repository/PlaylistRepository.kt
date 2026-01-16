package com.example.sonicflow.data.repository

import com.example.sonicflow.data.database.dao.PlaylistDao
import com.example.sonicflow.data.database.entities.PlaylistEntity
import com.example.sonicflow.data.database.entities.PlaylistTrackCrossRef
import com.example.sonicflow.data.model.Playlist
import com.example.sonicflow.data.model.Track
import com.example.sonicflow.domain.mapper.TrackMapper.toDomain
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlaylistRepository @Inject constructor(
    private val playlistDao: PlaylistDao
) {

    fun getAllPlaylists(): Flow<List<Playlist>> {
        return playlistDao.getAllPlaylists().map { entities ->
            entities.map { entity ->
                Playlist(
                    id = entity.id,
                    name = entity.name,
                    createdAt = entity.createdAt,
                    coverUri = entity.coverUri,
                    trackCount = playlistDao.getPlaylistTrackCount(entity.id)
                )
            }
        }
    }

    fun getTracksInPlaylist(playlistId: Long): Flow<List<Track>> {
        return playlistDao.getTracksInPlaylist(playlistId).map { it.toDomain() }
    }

    suspend fun createPlaylist(name: String): Long {
        return playlistDao.insertPlaylist(
            PlaylistEntity(name = name)
        )
    }

    suspend fun updatePlaylist(playlist: Playlist) {
        playlistDao.updatePlaylist(
            PlaylistEntity(
                id = playlist.id,
                name = playlist.name,
                createdAt = playlist.createdAt,
                coverUri = playlist.coverUri
            )
        )
    }

    suspend fun deletePlaylist(playlist: Playlist) {
        playlistDao.deletePlaylist(
            PlaylistEntity(
                id = playlist.id,
                name = playlist.name,
                createdAt = playlist.createdAt,
                coverUri = playlist.coverUri
            )
        )
    }

    suspend fun addTrackToPlaylist(playlistId: Long, trackId: Long) {
        val position = playlistDao.getPlaylistTrackCount(playlistId)
        playlistDao.insertPlaylistTrackCrossRef(
            PlaylistTrackCrossRef(
                playlistId = playlistId,
                trackId = trackId,
                position = position
            )
        )
    }

    suspend fun removeTrackFromPlaylist(playlistId: Long, trackId: Long) {
        playlistDao.deletePlaylistTrackCrossRef(
            PlaylistTrackCrossRef(
                playlistId = playlistId,
                trackId = trackId
            )
        )
    }
}
