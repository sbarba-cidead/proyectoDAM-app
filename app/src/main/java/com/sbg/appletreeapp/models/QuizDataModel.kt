package com.sbg.appletreeapp.models

import com.google.firebase.Timestamp

/**
 * Clase base para crear un objeto quiz con datos sobre el test
 * Se instancia en QuizzesScreen
 */
class QuizDataModel(
    var quizID: String? = null,
    var quizName: String? = null,
    var createdDate: Timestamp? = null,
    var level: String? = null,
    var group: String? = null,
    var subject: String? = null,
    var hidden: Boolean? = null,
    var completed: Boolean = false,
) {

    //for initialization of arguments
    init {

    }

    companion object Factory {
        fun createQuiz(): QuizDataModel = QuizDataModel()
    }

}