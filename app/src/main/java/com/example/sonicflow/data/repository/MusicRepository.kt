package com.example.sonicflow.data.repository

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import com.example.sonicflow.data.database.dao.TrackDao
import com.example.sonicflow.data.database.entities.TrackEntity
import com.example.sonicflow.data.model.SortType
import com.example.sonicflow.data.model.Track
import com.example.sonicflow.domain.mapper.TrackMapper.toDomain
import com.example.sonicflow.domain.mapper.TrackMapper.toEntity
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MusicRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val trackDao: TrackDao
) {

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
        }.map { it.toDomain() }
    }

    fun searchTracks(query: String): Flow<List<Track>> {
        return trackDao.searchTracks(query).map { it.toDomain() }
    }

    suspend fun getTrackById(trackId: Long): Track? {
        return trackDao.getTrackById(trackId)?.toDomain()
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

                // FIX : Utiliser l'URI du fichier audio directement pour l'album art
                val albumArtUri = try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        // Android 10+ : Utiliser l'URI de l'audio lui-même
                        ContentUris.withAppendedId(
                            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                            id
                        ).toString()
                    } else {
                        // Android < 10 : Utiliser l'ancienne méthode
                        ContentUris.withAppendedId(
                            Uri.parse("content://media/external/audio/albumart"),
                            albumId
                        ).toString()
                    }
                } catch (e: Exception) {
                    // En cas d'erreur, retourner null
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
}