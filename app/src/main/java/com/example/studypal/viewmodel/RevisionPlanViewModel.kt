package com.example.studypal.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.studypal.data.api.GeminiService
import com.example.studypal.data.repository.SubjectRepository
import com.example.studypal.model.Subject
import com.example.studypal.ui.screens.PlanDay
import com.example.studypal.ui.screens.PlanTask
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

data class RevisionPlanState(
    val planDays: List<PlanDay> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

sealed class RevisionPlanUiState {
    data object Loading : RevisionPlanUiState()
    data class Success(val plan: List<PlanDay>) : RevisionPlanUiState()
    data class Error(val message: String) : RevisionPlanUiState()
}

@HiltViewModel
class RevisionPlanViewModel @Inject constructor(
    private val subjectRepository: SubjectRepository,
    private val geminiService: GeminiService
) : ViewModel() {

    private val _uiState = MutableStateFlow<RevisionPlanUiState>(RevisionPlanUiState.Loading)
    val uiState: StateFlow<RevisionPlanUiState> = _uiState.asStateFlow()

    init {
        generatePlan()
    }

    fun regeneratePlan() {
        generatePlan()
    }

    fun generatePlan() {
        viewModelScope.launch {
            _uiState.value = RevisionPlanUiState.Loading
            try {
                val subjects = subjectRepository.getSubjects()
                Log.d("RevisionPlanViewModel", "Retrieved subjects: $subjects")
                
                if (subjects.isEmpty()) {
                    Log.e("RevisionPlanViewModel", "No subjects found")
                    _uiState.value = RevisionPlanUiState.Error("No subjects found. Please add subjects first.")
                    return@launch
                }
                
                val formattedSubjects = subjects.map { subject ->
                    com.example.studypal.data.api.Subject(
                        name = subject.name,
                        examDate = subject.examDate?.format(DateTimeFormatter.ISO_LOCAL_DATE) ?: ""
                    )
                }
                Log.d("RevisionPlanViewModel", "Formatted subjects: $formattedSubjects")

                val plan = geminiService.generateRevisionPlan(formattedSubjects)
                Log.d("RevisionPlanViewModel", "Generated plan: $plan")
                
                val planDays = parsePlan(plan)
                Log.d("RevisionPlanViewModel", "Parsed plan days: $planDays")
                
                if (planDays.isEmpty()) {
                    Log.e("RevisionPlanViewModel", "Failed to parse plan")
                    _uiState.value = RevisionPlanUiState.Error("Failed to generate revision plan. Please try again.")
                } else {
                    _uiState.value = RevisionPlanUiState.Success(planDays)
                }
            } catch (e: Exception) {
                Log.e("RevisionPlanViewModel", "Error generating plan", e)
                _uiState.value = RevisionPlanUiState.Error(e.message ?: "An unknown error occurred")
            }
        }
    }

    fun toggleTaskCompletion(date: String, taskId: String) {
        val currentState = _uiState.value
        if (currentState is RevisionPlanUiState.Success) {
            val currentDays = currentState.plan
            val updatedDays = currentDays.map { day ->
                if (day.date == date) {
                    day.copy(
                        tasks = day.tasks.map { task ->
                            if (task.id == taskId) {
                                task.copy(isCompleted = !task.isCompleted)
                            } else {
                                task
                            }
                        }
                    )
                } else {
                    day
                }
            }
            _uiState.value = RevisionPlanUiState.Success(updatedDays)
        }
    }

    private fun parsePlan(plan: String): List<PlanDay> {
        Log.d("RevisionPlanViewModel", "Starting to parse plan")
        val days = mutableListOf<PlanDay>()
        var currentDate: String? = null
        var currentTasks = mutableListOf<PlanTask>()
        var taskId = 1
        var currentSession: String? = null

        plan.lines().forEach { line ->
            val trimmedLine = line.trim()
            Log.d("RevisionPlanViewModel", "Processing line: $trimmedLine")
            
            when {
                // Look for day headers with countdown
                trimmedLine.matches(Regex("^\\*\\*Day \\d+:.*\\(\\d+ days until.*\\)\\*\\*$")) -> {
                    Log.d("RevisionPlanViewModel", "Found day header: $trimmedLine")
                    // Save previous day if exists
                    if (currentDate != null) {
                        days.add(PlanDay(currentDate, currentTasks))
                        currentTasks = mutableListOf()
                    }
                    // Remove markdown formatting
                    currentDate = trimmedLine.replace("**", "")
                    currentSession = null
                }
                // Look for session headers
                trimmedLine.matches(Regex("^(Morning|Afternoon|Evening) Session.*$")) -> {
                    Log.d("RevisionPlanViewModel", "Found session header: $trimmedLine")
                    currentSession = trimmedLine
                }
                // Look for task lines (starting with bullet points)
                trimmedLine.matches(Regex("^[•\\-]\\s.*")) -> {
                    Log.d("RevisionPlanViewModel", "Found task: $trimmedLine")
                    val taskText = trimmedLine.replaceFirst(Regex("^[•\\-]\\s*"), "")
                        .replace("*", "") // Remove markdown formatting
                    val sessionPrefix = when (currentSession) {
                        null -> ""
                        else -> "[${currentSession}] "
                    }
                    currentTasks.add(PlanTask(taskId.toString(), "$sessionPrefix$taskText"))
                    taskId++
                }
                // Look for subtasks (starting with dashes)
                trimmedLine.matches(Regex("^\\s+[-]\\s.*")) -> {
                    Log.d("RevisionPlanViewModel", "Found subtask: $trimmedLine")
                    val taskText = trimmedLine.replaceFirst(Regex("^\\s+[-]\\s*"), "")
                        .replace("*", "") // Remove markdown formatting
                    val sessionPrefix = when (currentSession) {
                        null -> ""
                        else -> "[${currentSession}] "
                    }
                    currentTasks.add(PlanTask(taskId.toString(), "$sessionPrefix$taskText"))
                    taskId++
                }
                // Look for exam-specific tasks
                trimmedLine.matches(Regex(".*exam.*", RegexOption.IGNORE_CASE)) -> {
                    Log.d("RevisionPlanViewModel", "Found exam task: $trimmedLine")
                    val sessionPrefix = when (currentSession) {
                        null -> ""
                        else -> "[${currentSession}] "
                    }
                    currentTasks.add(PlanTask(taskId.toString(), "$sessionPrefix$trimmedLine"))
                    taskId++
                }
            }
        }

        // Add the last day if exists
        if (currentDate != null) {
            days.add(PlanDay(currentDate, currentTasks))
        }

        Log.d("RevisionPlanViewModel", "Final parsed days: $days")
        
        return if (days.isEmpty()) {
            Log.d("RevisionPlanViewModel", "No days parsed, using fallback data")
            // Fallback to mock data if parsing fails
            listOf(
                PlanDay(
                    date = "Day 1 (10 days until first exam)",
                    tasks = listOf(
                        PlanTask("1", "[Morning Session] Review basic concepts"),
                        PlanTask("2", "[Morning Session] Practice problems"),
                        PlanTask("3", "[Afternoon Session] Advanced topics"),
                        PlanTask("4", "[Evening Review] Mock exam")
                    )
                ),
                PlanDay(
                    date = "Day 2 (9 days until first exam)",
                    tasks = listOf(
                        PlanTask("5", "[Morning Session] Review previous topics"),
                        PlanTask("6", "[Afternoon Session] Practice questions"),
                        PlanTask("7", "[Evening Review] Final review")
                    )
                )
            )
        } else {
            days
        }
    }
} 