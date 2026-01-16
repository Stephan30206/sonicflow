package com.example.sonicflow.domain.usecase

import com.example.sonicflow.data.repository.MusicRepository
import javax.inject.Inject

class ScanMediaUseCase @Inject constructor(
    private val repository: MusicRepository
) {
    suspend operator fun invoke() {
        repository.scanMediaStore()
    }
}