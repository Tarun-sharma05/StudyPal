package com.example.studypal.model

import java.time.LocalDate

data class Subject(
    val id: String = "",
    val name: String = "",
    val examDate: LocalDate? = null,
    val userId: String = ""
) 