package com.sbg.appletreeapp.models

/**
 * Clase base para crear un objeto question con datos sobre la pregunta
 * Se instancia en QuizQuestionScreen
 */
class QuestionDataModel(
    var id: String? = null,
    var questionNumber: String? = null,
    var questionContent: String? = null,
    var answersList: MutableList<String>? = null,
    var numbersCorrectAnswers: List<Int>? = null,

) {

    //for initialization of arguments
    init {

    }

    companion object Factory {
        fun createQuestion(): QuestionDataModel = QuestionDataModel()
    }

}