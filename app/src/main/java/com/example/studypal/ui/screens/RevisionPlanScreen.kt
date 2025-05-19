package com.example.studypal.ui.screens

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.studypal.navigation.Screen
import com.example.studypal.viewmodel.RevisionPlanUiState
import com.example.studypal.viewmodel.RevisionPlanViewModel

@Composable
fun RevisionPlanScreen(
    navController: NavController,
    viewModel: RevisionPlanViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    Log.d("RevisionPlanScreen", "Current UI State: $uiState")

    LaunchedEffect(Unit) {
        viewModel.generatePlan()
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
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
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
                    text = "Your Revision Plan",
                    style = MaterialTheme.typography.headlineMedium
                )
            }

            IconButton(
                onClick = { viewModel.regeneratePlan() }
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Regenerate Plan"
                )
            }
        }

        when (uiState) {
            is RevisionPlanUiState.Loading -> {
                Log.d("RevisionPlanScreen", "Showing loading state")
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            is RevisionPlanUiState.Success -> {
                val plan = (uiState as RevisionPlanUiState.Success).plan
                Log.d("RevisionPlanScreen", "Showing success state with plan: $plan")
                
                if (plan.isEmpty()) {
                    Log.w("RevisionPlanScreen", "Plan is empty")
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "No revision plan generated.",
                                style = MaterialTheme.typography.bodyLarge,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = { viewModel.regeneratePlan() }
                            ) {
                                Text("Try Again")
                            }
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(plan) { dayPlan ->
                            DayPlanCard(dayPlan = dayPlan)
                        }
                    }
                }
            }
            is RevisionPlanUiState.Error -> {
                val error = (uiState as RevisionPlanUiState.Error).message
                Log.e("RevisionPlanScreen", "Showing error state: $error")
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = error,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = { viewModel.regeneratePlan() }
                        ) {
                            Text("Try Again")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DayPlanCard(dayPlan: PlanDay) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = dayPlan.date,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            dayPlan.tasks.forEach { task ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = task.isCompleted,
                        onCheckedChange = { /* TODO: Implement task completion */ }
                    )
                    
                    Text(
                        text = task.description,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
        }
    }
}

data class PlanDay(
    val date: String,
    val tasks: List<PlanTask>
)

data class PlanTask(
    val id: String,
    val description: String,
    val isCompleted: Boolean = false
) 