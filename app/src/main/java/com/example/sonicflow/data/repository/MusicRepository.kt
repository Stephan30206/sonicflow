package com.example.sonicflow.data.repository

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import com.example.sonicflow.data.database.dao.FavoriteDao
import com.example.sonicflow.data.database.dao.PlaylistDao
import com.example.sonicflow.data.database.dao.TrackDao
import com.example.sonicflow.data.database.entities.*
import com.example.sonicflow.data.model.SortType
import com.example.sonicflow.data.model.Track
import com.example.sonicflow.domain.mapper.TrackMapper.toDomain
import com.example.sonicflow.domain.mapper.TrackMapper.toEntity
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MusicRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val trackDao: TrackDao,
    private val favoriteDao: FavoriteDao,
    private val playlistDao: PlaylistDao
) {

    // ========== TRACKS ==========
    fun getAllTracks(): Flow<List<Track>> {
        return trackDao.getAllTracks().map { entities ->
            entities.toDomain()
        }
    }

    fun getTracksSorted(sortType: SortType): Flow<List<Track>> {
        return when (sortType) {
            SortType.DATE_ADDED -> trackDao.getTracksSortedByDateAdded()
            SortType.TITLE -> trackDao.getTracksSortedByTitle()
            SortType.ARTIST -> trackDao.getTracksSortedByArtist()
            SortType.DURATION -> trackDao.getTracksSortedByDuration()
            SortType.ALBUM -> trackDao.getTracksSortedByAlbum() // Fix: Ajout du cas ALBUM
        }.map { it.toDomain() }
    }

    fun searchTracks(query: String): Flow<List<Track>> {
        return trackDao.searchTracks(query).map { it.toDomain() }
    }

    suspend fun getTrackById(trackId: Long): Track? {
        return trackDao.getTrackById(trackId)?.toDomain()
    }

    fun getTrackByIdFlow(trackId: Long): Flow<Track?> {
        return trackDao.getTrackByIdFlow(trackId).map { it?.toDomain() }
    }

    suspend fun scanMediaStore() {
        val tracks = mutableListOf<TrackEntity>()

        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.DATE_ADDED
        )

        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"

        context.contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            projection,
            selection,
            null,
            "${MediaStore.Audio.Media.DATE_ADDED} DESC"
        )?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val artistColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
            val albumColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
            val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
            val dataColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
            val albumIdColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
            val dateAddedColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_ADDED)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val title = cursor.getString(titleColumn) ?: "Unknown"
                val artist = cursor.getString(artistColumn) ?: "Unknown Artist"
                val album = cursor.getString(albumColumn) ?: "Unknown Album"
                val duration = cursor.getLong(durationColumn)
                val path = cursor.getString(dataColumn)
                val albumId = cursor.getLong(albumIdColumn)
                val dateAdded = cursor.getLong(dateAddedColumn)

                // Generate album art URI compatible with Android 10+
                val albumArtUri = try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        // For Android 10+, use the MediaStore Albums URI
                        ContentUris.withAppendedId(
                            MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
                            albumId
                        ).toString()
                    } else {
                        // For Android 9 and below, use the legacy albumart URI
                        ContentUris.withAppendedId(
                            Uri.parse("content://media/external/audio/albumart"),
                            albumId
                        ).toString()
                    }
                } catch (e: Exception) {
                    null
                }

                tracks.add(
                    TrackEntity(
                        id = id,
                        title = title,
                        artist = artist,
                        album = album,
                        duration = duration,
                        path = path,
                        albumArtUri = albumArtUri,
                        dateAdded = dateAdded * 1000
                    )
                )
            }
        }

        trackDao.insertTracks(tracks)
    }

    suspend fun updateTrackWaveform(trackId: Long, waveformData: List<Float>) {
        trackDao.getTrackById(trackId)?.let { entity ->
            val track = entity.toDomain().copy(waveformData = waveformData)
            trackDao.updateTrack(track.toEntity())
        }
    }

    // ========== FAVORIS ==========
    fun getAllFavoriteTracks(): Flow<List<Track>> {
        return favoriteDao.getFavoriteTrackIds().flatMapLatest { favoriteIds ->
            getAllTracks().map { allTracks ->
                allTracks.filter { it.id in favoriteIds }
            }
        }
    }

    fun isFavorite(trackId: Long): Flow<Boolean> {
        return favoriteDao.isFavorite(trackId)
    }

    suspend fun toggleFavorite(trackId: Long) {
        val isFav = favoriteDao.isFavoriteSync(trackId)
        if (isFav) {
            favoriteDao.removeFavorite(trackId)
        } else {
            favoriteDao.addFavorite(FavoriteEntity(trackId))
        }
    }

    suspend fun addToFavorites(trackId: Long) {
        favoriteDao.addFavorite(FavoriteEntity(trackId))
    }

    suspend fun removeFromFavorites(trackId: Long) {
        favoriteDao.removeFavorite(trackId)
    }

    // ========== ARTISTES ==========
    fun getAllArtists(): Flow<List<String>> {
        return getAllTracks().map { tracks ->
            tracks.map { it.artist }
                .distinct()
                .filter { it.isNotBlank() && it != "Unknown Artist" }
                .sorted()
        }
    }

    fun getTracksByArtist(artist: String): Flow<List<Track>> {
        return getAllTracks().map { tracks ->
            tracks.filter { it.artist == artist }
                .sortedBy { it.title }
        }
    }

    // ========== ALBUMS ==========
    fun getAllAlbums(): Flow<List<String>> {
        return getAllTracks().map { tracks ->
            tracks.map { it.album }
                .distinct()
                .filter { it.isNotBlank() && it != "Unknown Album" }
                .sorted()
        }
    }

    fun getTracksByAlbum(album: String): Flow<List<Track>> {
        return getAllTracks().map { tracks ->
            tracks.filter { it.album == album }
                .sortedBy { it.title }
        }
    }

    // ========== PLAYLISTS ==========
    fun getAllPlaylists(): Flow<List<PlaylistEntity>> {
        return playlistDao.getAllPlaylists()
    }

    suspend fun getPlaylistById(playlistId: Long): Flow<PlaylistEntity?> {
        return playlistDao.getPlaylistById(playlistId)
    }

    suspend fun createPlaylist(name: String, description: String = ""): Long {
        val playlist = PlaylistEntity(name = name, description = description)
        return playlistDao.insertPlaylist(playlist)
    }

    suspend fun updatePlaylist(playlist: PlaylistEntity) {
        playlistDao.updatePlaylist(playlist.copy(updatedAt = System.currentTimeMillis()))
    }

    suspend fun deletePlaylist(playlist: PlaylistEntity) {
        playlistDao.deletePlaylist(playlist)
    }

    fun getTracksInPlaylist(playlistId: Long): Flow<List<Track>> {
        return playlistDao.getTrackIdsInPlaylist(playlistId).flatMapLatest { trackIds ->
            getAllTracks().map { allTracks ->
                trackIds.mapNotNull { id ->
                    allTracks.find { it.id == id }
                }
            }
        }
    }

    suspend fun addTrackToPlaylist(playlistId: Long, trackId: Long) {
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

    suspend fun getPlaylistTrackCount(playlistId: Long): Flow<Int> {
        return playlistDao.getPlaylistTrackCount(playlistId)
    }
}
