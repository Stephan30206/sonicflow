package com.example.sonicflow.domain.usecase

import com.example.sonicflow.data.model.SortType
import com.example.sonicflow.data.model.Track
import com.example.sonicflow.data.repository.MusicRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAllTracksUseCase @Inject constructor(
    private val repository: MusicRepository
) {
    operator fun invoke(sortType: SortType = SortType.DATE_ADDED): Flow<List<Track>> {
        return repository.getTracksSorted(sortType)
    }
}