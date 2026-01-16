package com.example.sonicflow.data.model

data class Playlist(
    val id: Long = 0,
    val name: String,
    val createdAt: Long = System.currentTimeMillis(),
    val coverUri: String? = null,
    val trackCount: Int = 0
)