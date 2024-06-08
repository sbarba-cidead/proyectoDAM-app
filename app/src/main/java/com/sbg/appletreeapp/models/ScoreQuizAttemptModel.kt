package com.sbg.appletreeapp.models

import com.google.firebase.Timestamp

/**
 * Clase base para crear un objeto scorequiz con datos sobre el score de un quiz
 * Se instancia en QuizQuestionScreen
 */
class ScoreQuizAttemptModel(
    var attemptID: String? = null,
    var quizID: String? = null,
    var quizName: String? = null,
    var date: Timestamp? = null, //fecha de realización en Timestamp
    var formattedDate: String? = null, //fecha de realización en dd/MM/yyyy HH:mm
    var completeTimeSecs: Int? = null, //tiempo total para terminar test en segundos
    var correctAnswers: Int? = null, //número de preguntas acertadas
    var incorrectAnswers: Int? = null, //número de preguntas falladas
    var grade: Double? = null, //nota del test
    var incorrectAnswersList: List<String>? = null, //enunciados de las preguntas falladas
    var totalQuestionsNumber: Int? = null, //número total de preguntas del test
) {
    //for initialization of arguments
    init {

    }

    companion object Factory {
        fun createScoreQuiz(): ScoreQuizAttemptModel = ScoreQuizAttemptModel()
    }
}