package com.example.sonicflow.presentation.auth

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor() : ViewModel() {

    private val _isAuthenticated = MutableStateFlow(false)
    val isAuthenticated: StateFlow<Boolean> = _isAuthenticated.asStateFlow()

    fun signIn(email: String, password: String) {
        // Simulate sign in
        _isAuthenticated.value = true
    }

    fun signUp(name: String, email: String, password: String) {
        // Simulate sign up
        _isAuthenticated.value = true
    }

    fun signOut() {
        _isAuthenticated.value = false
    }
}