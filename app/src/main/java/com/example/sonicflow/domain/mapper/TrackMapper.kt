package com.example.sonicflow.domain.mapper

import com.example.sonicflow.data.database.entities.TrackEntity
import com.example.sonicflow.data.model.Track
import org.json.JSONArray

object TrackMapper {

    fun TrackEntity.toDomain(): Track {
        return Track(
            id = id,
            title = title,
            artist = artist,
            album = album,
            duration = duration,
            path = path,
            albumArtUri = albumArtUri,
            dateAdded = dateAdded,
            waveformData = parseWaveformData(waveformData)
        )
    }

    fun Track.toEntity(): TrackEntity {
        return TrackEntity(
            id = id,
            title = title,
            artist = artist,
            album = album,
            duration = duration,
            path = path,
            albumArtUri = albumArtUri,
            dateAdded = dateAdded,
            waveformData = waveformData?.let { serializeWaveformData(it) }
        )
    }

    fun List<TrackEntity>.toDomain(): List<Track> {
        return map { it.toDomain() }
    }

    private fun parseWaveformData(jsonString: String?): List<Float>? {
        if (jsonString.isNullOrBlank() || jsonString == "[]") return null

        return try {
            val jsonArray = JSONArray(jsonString)
            List(jsonArray.length()) { i ->
                jsonArray.getDouble(i).toFloat()
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun serializeWaveformData(data: List<Float>): String {
        return JSONArray(data).toString()
    }
}