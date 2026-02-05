package com.example.sonicflow.data.repository

import com.example.sonicflow.data.database.dao.PlaylistDao
import com.example.sonicflow.data.database.entities.PlaylistEntity
import com.example.sonicflow.data.database.entities.PlaylistTrackCrossRef
import com.example.sonicflow.data.model.Playlist
import com.example.sonicflow.data.model.Track
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlaylistRepository @Inject constructor(
    private val playlistDao: PlaylistDao,
    private val musicRepository: MusicRepository
) {

    fun getAllPlaylists(): Flow<List<Playlist>> {
        return playlistDao.getAllPlaylists().map { entities ->
            entities.map { entity ->
                Playlist(
                    id = entity.id,
                    name = entity.name,
                    createdAt = entity.createdAt,
                    coverUri = entity.coverUri,
                    trackCount = 0 // Initialisé à 0
                )
            }
        }
    }

    fun getPlaylistWithTrackCount(playlistId: Long): Flow<Playlist> {
        return playlistDao.getPlaylistById(playlistId).flatMapConcat { entity ->
            if (entity == null) {
                return@flatMapConcat flowOf<Playlist?>(null)
            }

            playlistDao.getPlaylistTrackCount(playlistId).map { count ->
                Playlist(
                    id = entity.id,
                    name = entity.name,
                    createdAt = entity.createdAt,
                    coverUri = entity.coverUri,
                    trackCount = count
                )
            }
        }.map { it ?: throw IllegalArgumentException("Playlist not found") }
    }

    fun getTracksInPlaylist(playlistId: Long): Flow<List<Track>> {
        return playlistDao.getTrackIdsInPlaylist(playlistId).flatMapConcat { trackIds ->
            musicRepository.getAllTracks().map { allTracks ->
                trackIds.mapNotNull { trackId ->
                    allTracks.find { it.id == trackId }
                }
            }
        }
    }

    suspend fun createPlaylist(name: String, description: String = ""): Long {
        return playlistDao.insertPlaylist(
            PlaylistEntity(
                name = name,
                description = description
            )
        )
    }

    suspend fun updatePlaylist(playlist: Playlist) {
        // D'abord, récupérer l'entité existante pour conserver les autres champs
        val existingEntity = playlistDao.getPlaylistByIdSync(playlist.id)

        playlistDao.updatePlaylist(
            PlaylistEntity(
                id = playlist.id,
                name = playlist.name,
                description = existingEntity?.description ?: "",
                createdAt = existingEntity?.createdAt ?: System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis(),
                coverUri = playlist.coverUri
            )
        )
    }

    suspend fun deletePlaylist(playlist: Playlist) {
        // D'abord, récupérer l'entité existante
        val existingEntity = playlistDao.getPlaylistByIdSync(playlist.id)

        if (existingEntity != null) {
            playlistDao.deletePlaylist(existingEntity)
        }
    }

    suspend fun addTrackToPlaylist(playlistId: Long, trackId: Long) {
        // Récupérer la position maximale actuelle
        val maxPosition = playlistDao.getMaxPosition(playlistId) ?: -1
        val crossRef = PlaylistTrackCrossRef(
            playlistId = playlistId,
            trackId = trackId,
            position = maxPosition + 1
        )
        playlistDao.addTrackToPlaylist(crossRef)
    }

    suspend fun removeTrackFromPlaylist(playlistId: Long, trackId: Long) {
        playlistDao.removeTrackFromPlaylist(playlistId, trackId)
    }

    suspend fun updatePlaylistTrackPosition(playlistId: Long, trackId: Long, newPosition: Int) {
        playlistDao.updateTrackPosition(playlistId, trackId, newPosition)
    }

    suspend fun clearPlaylist(playlistId: Long) {
        playlistDao.clearPlaylist(playlistId)
    }
}