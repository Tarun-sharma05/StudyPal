package com.example.studypal.viewmodel

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.studypal.data.repository.AuthRepository
import com.example.studypal.model.User
import com.google.firebase.auth.FirebaseUser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AuthState(
    val isLoading: Boolean = false,
    val user: FirebaseUser? = null,
    val userData: User? = null,
    val error: String? = null
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _authState = MutableStateFlow(AuthState())
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    init {
        checkAuthState()
    }

    private fun checkAuthState() {
        val currentUser = authRepository.getCurrentUser()
        if (currentUser != null) {
            _authState.value = _authState.value.copy(user = currentUser)
            loadUserData(currentUser.uid)
        }
    }

    private fun loadUserData(userId: String) {
        viewModelScope.launch {
            _authState.value = _authState.value.copy(isLoading = true)
            authRepository.getUserData(userId)
                .onSuccess { userData ->
                    _authState.value = _authState.value.copy(
                        userData = userData,
                        isLoading = false
                    )
                }
                .onFailure { error ->
                    _authState.value = _authState.value.copy(
                        error = error.message,
                        isLoading = false
                    )
                }
        }
    }

    fun signIn(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = _authState.value.copy(isLoading = true)
            authRepository.signIn(email, password)
                .onSuccess { user ->
                    _authState.value = _authState.value.copy(
                        user = user,
                        isLoading = false
                    )
                    loadUserData(user.uid)
                }
                .onFailure { error ->
                    _authState.value = _authState.value.copy(
                        error = error.message,
                        isLoading = false
                    )
                }
        }
    }

    fun signUp(email: String, password: String, activity: Activity) {
        viewModelScope.launch {
            _authState.value = _authState.value.copy(isLoading = true)
            authRepository.signUp(email, password, activity)
                .onSuccess { user ->
                    _authState.value = _authState.value.copy(
                        user = user,
                        isLoading = false
                    )
                    loadUserData(user.uid)
                }
                .onFailure { error ->
                    _authState.value = _authState.value.copy(
                        error = error.message,
                        isLoading = false
                    )
                }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            authRepository.signOut()
            _authState.value = AuthState()
        }
    }

    fun clearError() {
        _authState.value = _authState.value.copy(error = null)
    }
} 