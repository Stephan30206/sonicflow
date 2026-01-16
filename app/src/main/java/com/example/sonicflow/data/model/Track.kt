package com.example.sonicflow.data.model

data class Track(
    val id: Long,
    val title: String,
    val artist: String,
    val album: String,
    val duration: Long,
    val path: String,
    val albumArtUri: String?,
    val dateAdded: Long,
    val waveformData: List<Float>? = null
)
