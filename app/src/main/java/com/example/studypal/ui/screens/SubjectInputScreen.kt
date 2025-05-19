package com.example.studypal.ui.screens

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.studypal.model.Subject
import com.example.studypal.navigation.Screen
import com.example.studypal.viewmodel.SubjectViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubjectInputScreen(
    navController: NavController,
    subjectViewModel: SubjectViewModel = hiltViewModel()
) {
    var subjects by remember { mutableStateOf(listOf(Subject())) }
    var showDatePicker by remember { mutableStateOf<Int?>(null) }
    val dateFormatter = remember { DateTimeFormatter.ofPattern("MMM dd, yyyy") }
    val subjectState by subjectViewModel.subjectState.collectAsState()
    var showError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    LaunchedEffect(subjectState.error) {
        subjectState.error?.let {
            errorMessage = it
            showError = true
            subjectViewModel.clearError()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { navController.navigateUp() }
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back"
                )
            }
            Text(
                text = "Add Your Subjects",
                style = MaterialTheme.typography.headlineMedium
            )
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            itemsIndexed(subjects) { index, subject ->
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        OutlinedTextField(
                            value = subject.name,
                            onValueChange = { newName ->
                                subjects = subjects.toMutableList().apply {
                                    this[index] = subject.copy(name = newName)
                                }
                            },
                            label = { Text("Subject Name") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedButton(
                                onClick = { showDatePicker = index },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DateRange,
                                    contentDescription = "Select Date"
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = subject.examDate?.format(dateFormatter) ?: "Select Exam Date"
                                )
                            }

                            if (subjects.size > 1) {
                                IconButton(
                                    onClick = {
                                        subjects = subjects.toMutableList().apply {
                                            removeAt(index)
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Remove Subject"
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                subjects = subjects + Subject()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add Subject"
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Add Subject")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                try {
                    Log.d("SubjectInputScreen", "Attempting to save subjects: $subjects")
                    // Save all subjects sequentially
                    scope.launch {
                        try {
                            var allSaved = true
                            subjects.forEach { subject ->
                                if (subject.name.isNotBlank() && subject.examDate != null) {
                                    Log.d("SubjectInputScreen", "Saving subject: $subject")
                                    try {
                                        subjectViewModel.addSubject(subject)
                                        // Wait for the subject to be saved
                                        kotlinx.coroutines.delay(1000)
                                    } catch (e: Exception) {
                                        Log.e("SubjectInputScreen", "Error saving subject: $subject", e)
                                        allSaved = false
                                        errorMessage = "Failed to save subject: ${e.message}"
                                        showError = true
                                    }
                                }
                            }
                            
                            if (allSaved) {
                                Log.d("SubjectInputScreen", "All subjects saved, navigating to revision plan")
                                // Navigate to revision plan only after all subjects are saved
                                navController.navigate(Screen.RevisionPlan.route)
                            }
                        } catch (e: Exception) {
                            Log.e("SubjectInputScreen", "Error saving subjects", e)
                            errorMessage = "Failed to save subjects: ${e.message}"
                            showError = true
                        }
                    }
                } catch (e: Exception) {
                    Log.e("SubjectInputScreen", "Error in save operation", e)
                    errorMessage = "Please sign in to save subjects"
                    showError = true
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = subjects.all { it.name.isNotBlank() && it.examDate != null } && !subjectState.isLoading
        ) {
            if (subjectState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text("Generate Revision Plan")
            }
        }
    }

    showDatePicker?.let { index ->
        val subject = subjects[index]
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = subject.examDate?.toEpochDay()?.times(86400000)
        )
        
        DatePickerDialog(
            onDismissRequest = { showDatePicker = null },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val selectedDate = LocalDate.ofEpochDay(millis / 86400000)
                            subjects = subjects.toMutableList().apply {
                                this[index] = subject.copy(examDate = selectedDate)
                            }
                        }
                        showDatePicker = null
                    }
                ) {
                    Text("OK")
                }
            }
        ) {
            DatePicker(
                state = datePickerState,
                title = { Text("Select Exam Date") },
                headline = { Text("Select Exam Date") },
                showModeToggle = false
            )
        }
    }

    if (showError) {
        Snackbar(
            modifier = Modifier.padding(16.dp),
            action = {
                TextButton(onClick = { showError = false }) {
                    Text("Dismiss")
                }
            }
        ) {
            Text(errorMessage)
        }
    }
} 
