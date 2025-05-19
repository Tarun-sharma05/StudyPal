package com.example.studypal.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.studypal.data.repository.SubjectRepository
import com.example.studypal.model.Subject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SubjectState(
    val subjects: List<Subject> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class SubjectViewModel @Inject constructor(
    private val subjectRepository: SubjectRepository
) : ViewModel() {

    private val _subjectState = MutableStateFlow(SubjectState())
    val subjectState: StateFlow<SubjectState> = _subjectState.asStateFlow()

    init {
        loadSubjects()
    }

    fun addSubject(subject: Subject) {
        viewModelScope.launch {
            _subjectState.value = _subjectState.value.copy(isLoading = true)
            try {
                Log.d("SubjectViewModel", "Adding subject: $subject")
                subjectRepository.addSubject(subject)
                Log.d("SubjectViewModel", "Subject added successfully")
                // Wait a bit to ensure Firestore has updated
                delay(1000)
                // Reload subjects after adding
                loadSubjects()
            } catch (e: Exception) {
                Log.e("SubjectViewModel", "Error adding subject", e)
                _subjectState.value = _subjectState.value.copy(
                    error = if (e.message?.contains("not authenticated") == true) {
                        "Please sign in to save subjects"
                    } else {
                        e.message ?: "Failed to save subject"
                    },
                    isLoading = false
                )
            }
        }
    }

    fun updateSubject(subject: Subject) {
        viewModelScope.launch {
            _subjectState.value = _subjectState.value.copy(isLoading = true)
            try {
                subjectRepository.updateSubject(subject)
                loadSubjects()
            } catch (e: Exception) {
                _subjectState.value = _subjectState.value.copy(
                    error = if (e.message?.contains("not authenticated") == true) {
                        "Please sign in to update subjects"
                    } else {
                        e.message ?: "Failed to update subject"
                    },
                    isLoading = false
                )
            }
        }
    }

    fun deleteSubject(subjectId: String) {
        viewModelScope.launch {
            _subjectState.value = _subjectState.value.copy(isLoading = true)
            try {
                subjectRepository.deleteSubject(subjectId)
                loadSubjects()
            } catch (e: Exception) {
                _subjectState.value = _subjectState.value.copy(
                    error = if (e.message?.contains("not authenticated") == true) {
                        "Please sign in to delete subjects"
                    } else {
                        e.message ?: "Failed to delete subject"
                    },
                    isLoading = false
                )
            }
        }
    }

    fun loadSubjects() {
        viewModelScope.launch {
            try {
                Log.d("SubjectViewModel", "Loading subjects")
                val subjects = subjectRepository.getSubjects()
                Log.d("SubjectViewModel", "Loaded ${subjects.size} subjects: $subjects")
                _subjectState.value = _subjectState.value.copy(
                    subjects = subjects,
                    isLoading = false,
                    error = null
                )
            } catch (e: Exception) {
                Log.e("SubjectViewModel", "Error loading subjects", e)
                _subjectState.value = _subjectState.value.copy(
                    error = if (e.message?.contains("not authenticated") == true) {
                        "Please sign in to view subjects"
                    } else {
                        e.message ?: "Failed to load subjects"
                    },
                    isLoading = false
                )
            }
        }
    }

    fun clearError() {
        _subjectState.value = _subjectState.value.copy(error = null)
    }
} 