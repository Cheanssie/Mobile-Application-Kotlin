package com.lamont.assignment.model

data class Quiz(
    val questions: String,
    val opt1: String,
    val opt2: String,
    val opt3: String,
    val opt4: String,
    val answer: String,
    var selectedAnswer: String
)