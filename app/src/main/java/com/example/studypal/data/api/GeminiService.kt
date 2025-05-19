package com.example.studypal.data.api

import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.GenerateContentResponse
import javax.inject.Inject
import javax.inject.Singleton
import java.time.LocalDate
import android.util.Log

@Singleton
class GeminiService @Inject constructor() {
    private val model = GenerativeModel(
        modelName = "gemini-2.0-flash",
        apiKey = " GEMINI-API-KEY"
    )

    suspend fun generateRevisionPlan(subjects: List<Subject>): String {
        Log.d("GeminiService", "Generating plan for subjects: $subjects")
        
        // Sort subjects by exam date
        val sortedSubjects = subjects.sortedBy { it.examDate }
        Log.d("GeminiService", "Sorted subjects: $sortedSubjects")
        
        // Calculate days between first and last exam
        val firstExamDate = LocalDate.parse(sortedSubjects.first().examDate)
        val lastExamDate = LocalDate.parse(sortedSubjects.last().examDate)
        val totalDays = java.time.temporal.ChronoUnit.DAYS.between(firstExamDate, lastExamDate) + 1
        Log.d("GeminiService", "Days until first exam: ${java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), firstExamDate)}")
        Log.d("GeminiService", "Total days between exams: $totalDays")

        val prompt = buildString {
            append("Create a comprehensive revision plan for the following subjects and their exam dates:\n\n")
            sortedSubjects.forEach { subject ->
                append("${subject.name}: ${subject.examDate}\n")
            }
            append("\nTotal days until first exam: ${java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), firstExamDate)}\n")
            append("Total days between first and last exam: $totalDays\n\n")
            append("Please create a detailed day-by-day revision schedule that includes:\n")
            append("1. Subject Prioritization:\n")
            append("   - Focus on subjects with earlier exam dates\n")
            append("   - Consider subject difficulty and student's strengths/weaknesses\n")
            append("   - Allocate more time to subjects with less time until exam\n")
            append("2. Daily Schedule Structure:\n")
            append("   - Morning session (2-3 hours)\n")
            append("   - Afternoon session (2-3 hours)\n")
            append("   - Evening review (1-2 hours)\n")
            append("3. Study Techniques:\n")
            append("   - Active recall methods\n")
            append("   - Practice questions\n")
            append("   - Mind mapping\n")
            append("   - Flashcards\n")
            append("4. Regular Review Sessions:\n")
            append("   - Weekly comprehensive reviews\n")
            append("   - Subject-specific reviews\n")
            append("   - Pre-exam intensive reviews\n")
            append("5. Break Management:\n")
            append("   - Short breaks between study sessions\n")
            append("   - Longer breaks for meals\n")
            append("   - Rest days\n")
            append("6. Exam Preparation:\n")
            append("   - Mock exams\n")
            append("   - Past paper practice\n")
            append("   - Time management practice\n")
            append("\nIMPORTANT: Follow this EXACT format for each day:\n")
            append("Day 1: [Date] (X days until first exam)\n")
            append("Morning Session (9:00 AM - 12:00 PM):\n")
            append("• [Subject] - [Topic]\n")
            append("  - [Specific Task]\n")
            append("  - [Study Technique]\n")
            append("  - [Resources Needed]\n")
            append("\nAfternoon Session (2:00 PM - 5:00 PM):\n")
            append("• [Subject] - [Topic]\n")
            append("  - [Specific Task]\n")
            append("  - [Study Technique]\n")
            append("  - [Resources Needed]\n")
            append("\nEvening Review (7:00 PM - 9:00 PM):\n")
            append("• [Review Topics]\n")
            append("• [Practice Questions]\n")
            append("\nBreaks:\n")
            append("• [Break Times]\n")
            append("• [Break Activities]\n")
            append("\nDaily Goals:\n")
            append("• [Goal 1]\n")
            append("• [Goal 2]\n")
            append("\nContinue this EXACT format for each day until the last exam date. Use bullet points (•) for main tasks and dashes (-) for subtasks.")
            append("\n\nIMPORTANT: Include countdown to each exam in the daily headers and adjust the intensity of study based on proximity to exams.")
        }
        Log.d("GeminiService", "Generated prompt: $prompt")

        return try {
            val response = model.generateContent(prompt)
            val responseText = response.text
            Log.d("GeminiService", "API Response: $responseText")
            
            if (responseText == null) {
                Log.e("GeminiService", "API returned null response")
                throw Exception("Failed to generate revision plan: API returned null response")
            }
            
            responseText
        } catch (e: Exception) {
            Log.e("GeminiService", "Error generating plan", e)
            throw Exception("Error generating revision plan: ${e.message}")
        }
    }
}

data class Subject(
    val name: String,
    val examDate: String
) 