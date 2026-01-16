package com.example.sonicflow.domain.usecase

import com.example.sonicflow.data.model.Track
import com.example.sonicflow.data.repository.MusicRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SearchTracksUseCase @Inject constructor(
    private val repository: MusicRepository
) {
    operator fun invoke(query: String): Flow<List<Track>> {
        return repository.searchTracks(query)
    }
}