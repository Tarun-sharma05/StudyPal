package com.example.studypal.model

data class User(
    val id: String = "",
    val email: String = "",
    val subjects: List<String> = emptyList(),
    val examDates: Map<String, String> = emptyMap(),
    val revisionPlan: Map<String, List<String>> = emptyMap()
) 