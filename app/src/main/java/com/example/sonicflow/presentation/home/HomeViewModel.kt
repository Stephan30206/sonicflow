package com.example.sonicflow.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sonicflow.data.preferences.UserPreferences
import com.example.sonicflow.data.model.SortType
import com.example.sonicflow.data.model.Track
import com.example.sonicflow.domain.usecase.GetAllTracksUseCase
import com.example.sonicflow.domain.usecase.ScanMediaUseCase
import com.example.sonicflow.domain.usecase.SearchTracksUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getAllTracksUseCase: GetAllTracksUseCase,
    private val searchTracksUseCase: SearchTracksUseCase,
    private val scanMediaUseCase: ScanMediaUseCase,
    private val userPreferences: UserPreferences
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _sortType = MutableStateFlow(SortType.DATE_ADDED)
    val sortType: StateFlow<SortType> = _sortType.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    val tracks: StateFlow<List<Track>> = combine(
        _searchQuery,
        _sortType
    ) { query, sort ->
        Pair(query, sort)
    }.flatMapLatest { (query, sort) ->
        if (query.isBlank()) {
            getAllTracksUseCase(sort)
        } else {
            searchTracksUseCase(query)
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    init {
        loadUserPreferences()
        scanMedia()
    }

    private fun loadUserPreferences() {
        viewModelScope.launch {
            userPreferences.sortType.collectLatest { sortTypeString ->
                _sortType.value = SortType.valueOf(sortTypeString)
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun setSortType(sortType: SortType) {
        _sortType.value = sortType
        viewModelScope.launch {
            userPreferences.saveSortType(sortType.name)
        }
    }

    fun scanMedia() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                scanMediaUseCase()
            } catch (e: Exception) {
                _error.value = e.message ?: "Unknown error occurred"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearError() {
        _error.value = null
    }
}