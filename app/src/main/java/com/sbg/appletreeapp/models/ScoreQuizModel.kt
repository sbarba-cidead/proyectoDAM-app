package com.sbg.appletreeapp.models

/**
 * Clase base para crear un objeto scorequiz con datos sobre el score de un quiz
 * Se instancia en QuizQuestionScreen
 */
class ScoreQuizModel(
    var quizID: String? = null,
    var quizName: String? = null,
    var totalAttempts: Int? = null,
    var completed: Boolean = false,
    var subject: String? = null,
    var averageGrade: Double? = null
) {
    //for initialization of arguments
    init {

    }

    companion object Factory {
        fun createScoreQuiz(): ScoreQuizModel = ScoreQuizModel()
    }
}