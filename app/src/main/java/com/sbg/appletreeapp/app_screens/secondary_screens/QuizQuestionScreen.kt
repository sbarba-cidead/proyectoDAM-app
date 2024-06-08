package com.sbg.appletreeapp.app_screens.secondary_screens

import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.firebase.Timestamp
import com.google.firebase.firestore.AggregateSource
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.Query
import com.sbg.appletreeapp.R
import com.sbg.appletreeapp.models.QuestionDataModel
import com.sbg.appletreeapp.models.ScoreQuizAttemptModel
import com.sbg.appletreeapp.models.ScoreQuizModel
import com.sbg.appletreeapp.utils.FirebaseQueries
import com.sbg.appletreeapp.utils.Utils
import java.util.Date
import java.util.concurrent.TimeUnit
import kotlin.random.Random

class QuizQuestionScreen : AppCompatActivity() {

    private lateinit var countdownTimer: TextView
    private lateinit var counter: TextView
    private lateinit var questionNumberTextView: TextView
    private lateinit var questionContentTextView: TextView
    private lateinit var answer1Button: Button
    private lateinit var answer2Button: Button
    private lateinit var answer3Button: Button
    private lateinit var answer4Button: Button
    private lateinit var nextButton: Button
    private lateinit var finishButton: Button

    private lateinit var countDownTimer: CountDownTimer

    private var questionNumber: Int = 1
    private var quizID: String? = null
    private var quizName: String? = null
    private var subject: String? = null
    private var correctAnswersNumber = 0
    private var incorrectAnswersNumber = 0
    private var incorrectAnswersList = mutableListOf<String>()
    private var questionsTotal = 0
    private var startTime = 0
    private var endTime = 0
    private var grade = 0.0

    val correctAnswers = mutableListOf<String>()
    var currentQuestionContent = ""
    var answer1ButtonClicked = false
    var answer2ButtonClicked = false
    var answer3ButtonClicked = false
    var answer4ButtonClicked = false
    var numbersCorrectAnswersSize = 0
    var selectedAnswersNumber = 0
    private var shouldAddCorrect = true
    private var shouldAddIncorrect = true
    private var correctWasAdded = false
    private var incorrectWasAdded = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.quiz_question_screen)

        countdownTimer = findViewById(R.id.countdownTimer)
        counter = findViewById(R.id.counter)
        questionNumberTextView = findViewById(R.id.questionNumberTextView)
        questionContentTextView = findViewById(R.id.questionContentTextView)
        answer1Button = findViewById(R.id.answer1Button)
        answer2Button = findViewById(R.id.answer2Button)
        answer3Button = findViewById(R.id.answer3Button)
        answer4Button = findViewById(R.id.answer4Button)
        nextButton = findViewById(R.id.nextButton)
        finishButton = findViewById(R.id.finishButton)

        quizID = intent.getStringExtra("quizID").toString()
        quizName = intent.getStringExtra("quizName").toString()
        subject = intent.getStringExtra("subject").toString()

        startTime = (Date().time / 1000).toInt()

        retrieveQuestionData(questionNumber)

        countDownTimer = countDownTimer(1)
        countDownTimer.start()

        nextButton.setOnClickListener{
            nextQuestion()
        }

        finishButton.setOnClickListener {
            finishQuiz()
        }
    }

    private fun countDownTimer(minutes: Long) : CountDownTimer {
        val milliseconds = minutes * 60000

        val timer = object : CountDownTimer(milliseconds, 1000) {
            override fun onTick(p0: Long) {
                val millis: Long = p0
                val formattedTimer = String.format(
                    "%02d:%02d",
                    (TimeUnit.MILLISECONDS.toMinutes(millis) -
                            TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millis))),
                    (TimeUnit.MILLISECONDS.toSeconds(millis) - TimeUnit.MINUTES.toSeconds(
                        TimeUnit.MILLISECONDS.toMinutes(millis)
                    ))
                )

                if (millis < 30000) {
                    countdownTimer.setTextColor(ContextCompat.getColor(applicationContext, R.color.light_red))
                } else {
                    countdownTimer.setTextColor(ContextCompat.getColor(applicationContext, R.color.white))
                }

                countdownTimer.text = formattedTimer;//set text
            }

            override fun onFinish() {
                nextButton.isEnabled = true
                finishButton.isEnabled = true

                //disables buttons
                answer1Button.isClickable = false
                answer2Button.isClickable = false
                answer3Button.isClickable = false
                answer4Button.isClickable = false

                if (numbersCorrectAnswersSize > selectedAnswersNumber) {
                    //recalculates incorrect and correct answers
                    if (selectedAnswersNumber == 0) { incorrectAnswersNumber += 1 } else {
                        if (shouldAddIncorrect) {
                            incorrectAnswersNumber += 1
                            if (correctWasAdded) { correctAnswersNumber -= 1 }
                        }
                    }

                    //recalculates grade
                    grade = (correctAnswersNumber.toDouble() / questionsTotal.toDouble()) * 10

                    //adds question to incorrect list if it is not in it already
                    if (!incorrectAnswersList.contains(currentQuestionContent)) {
                        incorrectAnswersList.add(currentQuestionContent)
                    }

                    if (!answer1ButtonClicked && correctAnswers.contains(answer1Button.text)) {
                        answer1Button.setBackgroundColor(
                            ContextCompat.getColor(applicationContext, R.color.orange)
                        )
                    }
                    if (!answer2ButtonClicked && correctAnswers.contains(answer2Button.text)) {
                        answer2Button.setBackgroundColor(
                            ContextCompat.getColor(applicationContext, R.color.orange)
                        )
                    }
                    if (!answer3ButtonClicked && correctAnswers.contains(answer3Button.text)) {
                        answer3Button.setBackgroundColor(
                            ContextCompat.getColor(applicationContext, R.color.orange)
                        )
                    }
                    if (!answer4ButtonClicked && correctAnswers.contains(answer4Button.text)) {
                        answer4Button.setBackgroundColor(
                            ContextCompat.getColor(applicationContext, R.color.orange)
                        )
                    }
                }
            }
        }

        return timer
    }

    private fun finishQuiz() {
        endTime = (Date().time / 1000).toInt()

        saveScore()

        countDownTimer.cancel()

        this.finish()
    }

    private fun nextQuestion() {
        correctAnswers.clear()

        shouldAddCorrect = true
        shouldAddIncorrect = true
        correctWasAdded = false
        incorrectWasAdded = false

        answer1ButtonClicked = false
        answer2ButtonClicked = false
        answer3ButtonClicked = false
        answer4ButtonClicked = false

        //enables buttons
        answer1Button.isClickable = true
        answer2Button.isClickable = true
        answer3Button.isClickable = true
        answer4Button.isClickable = true

        questionNumber += 1

        answer1Button.setBackgroundColor(
            ContextCompat.getColor(this, R.color.primary_dark)
        )
        answer2Button.setBackgroundColor(
            ContextCompat.getColor(this, R.color.primary_dark)
        )
        answer3Button.setBackgroundColor(
            ContextCompat.getColor(this, R.color.primary_dark)
        )
        answer4Button.setBackgroundColor(
            ContextCompat.getColor(this, R.color.primary_dark)
        )

        retrieveQuestionData(questionNumber)

        countDownTimer.start()

        nextButton.isEnabled = false
        finishButton.isEnabled = false
    }

    private fun saveScore() {
        val query: Query = FirebaseQueries.getCollectionReferenceForScorequizzesAttempts()
            .whereEqualTo("quizID", quizID)

        query.get().addOnSuccessListener { documents ->
            val averageGrade: Double // cálculo de la nota media
            if (documents.size() > 0) {
                var sum = grade
                for (document in documents) {
                    val number: Double = document["grade"] as Double
                    sum += number
                }
                averageGrade = sum/(documents.size()+1)
            } else {
                averageGrade = grade
            }

            // 1) guardar datos en Users>quizesAttempts
            val score = ScoreQuizAttemptModel.createScoreQuiz()
            score.quizID = quizID
            score.quizName = quizName
            score.date = Timestamp.now()
            score.formattedDate = Utils.convertTimestampToDateString(Timestamp.now())
            score.correctAnswers = correctAnswersNumber
            score.incorrectAnswers = incorrectAnswersNumber
            score.grade = grade
            score.completeTimeSecs = endTime - startTime
            score.incorrectAnswersList = incorrectAnswersList
            score.totalQuestionsNumber = questionsTotal

            val attemptDocumentReference = FirebaseQueries.getCollectionReferenceForScorequizzesAttempts().document()
            attemptDocumentReference.set(score).addOnCompleteListener { task ->
                if (task.isSuccessful){
                    FirebaseQueries.getCollectionReferenceForScorequizzesAttempts()
                        .document()

                    Toast.makeText(this, R.string.finishedQuiz, Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(
                        this, R.string.finishedQuizError, Toast.LENGTH_SHORT).show()
                }
            }
            attemptDocumentReference.update(mapOf("attemptID" to attemptDocumentReference.id))

            // 2) guardar datos en Users>ScoreQuizes
            FirebaseQueries.getCollectionReferenceForScorequizzes()
                .get().addOnSuccessListener { result ->
                    var exists = false
                    for (document in result) {
                        if (document.id == quizID) { exists = true; break }
                    }

                    val documentReference = FirebaseQueries.getCollectionReferenceForScorequizzes()
                        .document(quizID!!)

                    if (exists) {
                        documentReference.get().addOnCompleteListener { getTask ->
                            if (getTask.isSuccessful) {
                                if (getTask.result["quizName"] == null) {
                                    documentReference.set(mapOf("quizName" to quizName))
                                } else {
                                    documentReference.update(mapOf("quizName" to quizName))
                                }

                                if (getTask.result["subject"] == null) {
                                    documentReference.set(mapOf("subject" to subject))
                                } else {
                                    documentReference.update(mapOf("subject" to subject))
                                }

                                if (getTask.result["totalAttempts"] == null) {
                                    documentReference.set(mapOf("totalAttempts" to 1))
                                } else {
                                    val attempts: Long = getTask.result["totalAttempts"] as Long
                                    documentReference.update(mapOf("totalAttempts" to attempts + 1))
                                }

                                if (getTask.result["averageGrade"] == null) {
                                    documentReference.set(mapOf("averageGrade" to averageGrade))
                                } else {
                                    documentReference.update(mapOf("averageGrade" to averageGrade))
                                }

                                if (getTask.result["completed"] == null) {
                                    if (incorrectAnswersNumber == 0) {
                                        documentReference.set(mapOf("completed" to true))
                                    } else {
                                        documentReference.set(mapOf("completed" to false))
                                    }
                                } else {
                                    if (getTask.result["completed"] == false) {
                                        if (incorrectAnswersNumber == 0) {
                                            documentReference.update(mapOf("completed" to true))
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        // para completar el quiz hay que responder todas sus respuestas bien
                        var completed = false
                        if (incorrectAnswersNumber == 0) { completed = true }

                        val newScoreQuizDocument = ScoreQuizModel(quizID, quizName, 1,
                            completed, subject, averageGrade)

                        documentReference.set(newScoreQuizDocument)
                    }
                }

            // 3) actualizar quiz completado en User>Quizes
            FirebaseQueries.getDocumentReferenceForUserData()
                .get().addOnCompleteListener { retrieveTeacherTask ->
                    if (retrieveTeacherTask.isSuccessful) {
                        val teacherID = retrieveTeacherTask.result["teacherID"].toString()

                        FirebaseQueries.getCollectionReferenceForGeneralQuizzes(teacherID)
                            .document(quizID!!)
                            .update("completedStudents", FieldValue.arrayUnion(FirebaseQueries.getCurrentUser()!!.uid))
                    }
                }

            // 4) actualizar quiz completado en Quizes
            FirebaseQueries.getCollectionReferenceForUserQuizzes()
                .document(quizID!!)
                .update(mapOf("completed" to true))
        }
    }

    private fun retrieveQuestionData(questionNumber: Int) {
        val questions = FirebaseQueries.getCollectionReferenceForUserQuizzes()
            .document(quizID!!)
            .collection("questions")

        questions.count()
            .get(AggregateSource.SERVER).addOnCompleteListener { countTask ->
                if (countTask.isSuccessful) {
                    questionsTotal = countTask.result.count.toInt()
                    val counterText = "$questionNumber / $questionsTotal"
                    counter.text = counterText

                    if (questionsTotal == questionNumber) {
                        nextButton.visibility = View.GONE
                        finishButton.visibility = View.VISIBLE
                    }
                }
            }

        questions
            .document("pregunta$questionNumber")
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val question = task.result.toObject(QuestionDataModel::class.java)

                    currentQuestionContent = question!!.questionContent.toString()

                    selectedAnswersNumber = 0
                    numbersCorrectAnswersSize = question.numbersCorrectAnswers!!.size
                    for (answerNumber in question.numbersCorrectAnswers!!) {
                        correctAnswers.add(question.answersList!![answerNumber-1])
                    }

                    questionNumberTextView.text = getString(R.string.question, questionNumber)
                    questionContentTextView.text = question.questionContent

                    var randomIndex = Random.nextInt(question.answersList!!.size)
                    answer1Button.text = question.answersList!![randomIndex]
                    question.answersList!!.removeAt(randomIndex)

                    randomIndex = Random.nextInt(question.answersList!!.size)
                    answer2Button.text = question.answersList!![randomIndex]
                    question.answersList!!.removeAt(randomIndex)

                    randomIndex = Random.nextInt(question.answersList!!.size)
                    answer3Button.text = question.answersList!![randomIndex]
                    question.answersList!!.removeAt(randomIndex)

                    randomIndex = Random.nextInt(question.answersList!!.size)
                    answer4Button.text = question.answersList!![randomIndex]
                    question.answersList!!.removeAt(randomIndex)

                    answer1Button.setOnClickListener{
                        selectedAnswersNumber += 1
                        answer1ButtonClicked = true

                        if (question.numbersCorrectAnswers!!.size <= selectedAnswersNumber) {
                            countDownTimer.cancel()

                            nextButton.isEnabled = true
                            finishButton.isEnabled = true

                            //disables other buttons
                            answer2Button.isClickable = false
                            answer3Button.isClickable = false
                            answer4Button.isClickable = false

                            if (correctAnswers.contains(answer2Button.text)) {
                                if (!answer2ButtonClicked) {
                                    answer2Button.setBackgroundColor(
                                        ContextCompat.getColor(this, R.color.orange)
                                    )
                                }
                            }
                            if (correctAnswers.contains(answer3Button.text)) {
                                if (!answer3ButtonClicked) {
                                    answer3Button.setBackgroundColor(
                                        ContextCompat.getColor(this, R.color.orange)
                                    )
                                }
                            }
                            if (correctAnswers.contains(answer4Button.text)) {
                                if (!answer4ButtonClicked) {
                                    answer4Button.setBackgroundColor(
                                        ContextCompat.getColor(this, R.color.orange)
                                    )
                                }
                            }
                        }

                        if (correctAnswers.contains(answer1Button.text)) {
                            answer1Button.setBackgroundColor(
                                ContextCompat.getColor(this, R.color.green)
                            )

                            if (!incorrectWasAdded) {
                                if (shouldAddCorrect && !correctWasAdded) {
                                    correctAnswersNumber += 1
                                    grade = (correctAnswersNumber.toDouble() / questionsTotal.toDouble()) * 10

                                    shouldAddCorrect = false
                                    correctWasAdded = true
                                }
                            }
                        } else {
                            answer1Button.setBackgroundColor(
                                ContextCompat.getColor(this, R.color.red)
                            )
                            //adds question to incorrect list if it is not in it already
                            if (!incorrectAnswersList.contains(question.questionContent.toString())) {
                                incorrectAnswersList.add(question.questionContent.toString())
                            }

                            //calculates correct and incorrect answers
                            if (shouldAddIncorrect) {
                                incorrectAnswersNumber += 1
                                shouldAddIncorrect = false
                                incorrectWasAdded = true
                                if (correctWasAdded) {
                                    correctAnswersNumber -= 1
                                    correctWasAdded = false
                                }
                                grade = (correctAnswersNumber.toDouble() / questionsTotal.toDouble()) * 10
                            }
                        }

                        //disables button
                        answer1Button.isClickable = false
                    }

                    answer2Button.setOnClickListener{
                        selectedAnswersNumber += 1
                        answer2ButtonClicked = true

                        if (question.numbersCorrectAnswers!!.size <= selectedAnswersNumber) {
                            countDownTimer.cancel()

                            nextButton.isEnabled = true
                            finishButton.isEnabled = true

                            //disables other buttons
                            answer1Button.isClickable = false
                            answer3Button.isClickable = false
                            answer4Button.isClickable = false

                            if (correctAnswers.contains(answer1Button.text)) {
                                if (!answer1ButtonClicked) {
                                    answer1Button.setBackgroundColor(
                                        ContextCompat.getColor(this, R.color.orange)
                                    )
                                }
                            }
                            if (correctAnswers.contains(answer3Button.text)) {
                                if (!answer3ButtonClicked) {
                                    answer3Button.setBackgroundColor(
                                        ContextCompat.getColor(this, R.color.orange)
                                    )
                                }
                            }
                            if (correctAnswers.contains(answer4Button.text)) {
                                if (!answer4ButtonClicked) {
                                    answer4Button.setBackgroundColor(
                                        ContextCompat.getColor(this, R.color.orange)
                                    )
                                }
                            }
                        }

                        if (correctAnswers.contains(answer2Button.text)) {
                            answer2Button.setBackgroundColor(
                                ContextCompat.getColor(this, R.color.green)
                            )

                            if (!incorrectWasAdded) {
                                if (shouldAddCorrect && !correctWasAdded) {
                                    correctAnswersNumber += 1
                                    grade = (correctAnswersNumber.toDouble() / questionsTotal.toDouble()) * 10

                                    shouldAddCorrect = false
                                    correctWasAdded = true
                                }
                            }
                        } else {
                            answer2Button.setBackgroundColor(
                                ContextCompat.getColor(this, R.color.red)
                            )
                            //adds question to incorrect list if it is not in it already
                            if (!incorrectAnswersList.contains(question.questionContent.toString())) {
                                incorrectAnswersList.add(question.questionContent.toString())
                            }

                            //calculates correct and incorrect answers
                            if (shouldAddIncorrect) {
                                incorrectAnswersNumber += 1
                                shouldAddIncorrect = false
                                incorrectWasAdded = true
                                if (correctWasAdded) {
                                    correctAnswersNumber -= 1
                                    correctWasAdded = false
                                }
                                grade = (correctAnswersNumber.toDouble() / questionsTotal.toDouble()) * 10
                            }
                        }

                        //disables button
                        answer2Button.isClickable = false
                    }

                    answer3Button.setOnClickListener{
                        selectedAnswersNumber += 1
                        answer3ButtonClicked = true

                        if (question.numbersCorrectAnswers!!.size <= selectedAnswersNumber) {
                            countDownTimer.cancel()

                            nextButton.isEnabled = true
                            finishButton.isEnabled = true

                            //disables other buttons
                            answer1Button.isClickable = false
                            answer2Button.isClickable = false
                            answer4Button.isClickable = false

                            if (correctAnswers.contains(answer1Button.text)) {
                                if (!answer1ButtonClicked) {
                                    answer1Button.setBackgroundColor(
                                        ContextCompat.getColor(this, R.color.orange)
                                    )
                                }
                            }
                            if (correctAnswers.contains(answer2Button.text)) {
                                if (!answer2ButtonClicked) {
                                    answer2Button.setBackgroundColor(
                                        ContextCompat.getColor(this, R.color.orange)
                                    )
                                }
                            }
                            if (correctAnswers.contains(answer4Button.text)) {
                                if (!answer4ButtonClicked) {
                                    answer4Button.setBackgroundColor(
                                        ContextCompat.getColor(this, R.color.orange)
                                    )
                                }
                            }
                        }

                        if (correctAnswers.contains(answer3Button.text)) {
                            answer3Button.setBackgroundColor(
                                ContextCompat.getColor(this, R.color.green)
                            )

                            if (!incorrectWasAdded) {
                                if (shouldAddCorrect && !correctWasAdded) {
                                    correctAnswersNumber += 1
                                    grade = (correctAnswersNumber.toDouble() / questionsTotal.toDouble()) * 10

                                    shouldAddCorrect = false
                                    correctWasAdded = true
                                }
                            }
                        } else {
                            answer3Button.setBackgroundColor(
                                ContextCompat.getColor(this, R.color.red)
                            )
                            //adds question to incorrect list if it is not in it already
                            if (!incorrectAnswersList.contains(question.questionContent.toString())) {
                                incorrectAnswersList.add(question.questionContent.toString())
                            }

                            //calculates correct and incorrect answers
                            if (shouldAddIncorrect) {
                                incorrectAnswersNumber += 1
                                shouldAddIncorrect = false
                                incorrectWasAdded = true
                                if (correctWasAdded) {
                                    correctAnswersNumber -= 1
                                    correctWasAdded = false
                                }
                                grade = (correctAnswersNumber.toDouble() / questionsTotal.toDouble()) * 10
                            }
                        }

                        //disables button
                        answer3Button.isClickable = false
                    }

                    answer4Button.setOnClickListener{
                        selectedAnswersNumber += 1
                        answer4ButtonClicked = true

                        if (question.numbersCorrectAnswers!!.size <= selectedAnswersNumber) {
                            countDownTimer.cancel()

                            nextButton.isEnabled = true
                            finishButton.isEnabled = true

                            //disables other buttons
                            answer1Button.isClickable = false
                            answer2Button.isClickable = false
                            answer3Button.isClickable = false

                            if (correctAnswers.contains(answer1Button.text)) {
                                if (!answer1ButtonClicked) {
                                    answer1Button.setBackgroundColor(
                                        ContextCompat.getColor(this, R.color.orange)
                                    )
                                }
                            }
                            if (correctAnswers.contains(answer2Button.text)) {
                                if (!answer2ButtonClicked) {
                                    answer2Button.setBackgroundColor(
                                        ContextCompat.getColor(this, R.color.orange)
                                    )
                                }
                            }
                            if (correctAnswers.contains(answer3Button.text)) {
                                if (!answer3ButtonClicked) {
                                    answer3Button.setBackgroundColor(
                                        ContextCompat.getColor(this, R.color.orange)
                                    )
                                }
                            }
                        }

                        if (correctAnswers.contains(answer4Button.text)) {
                            answer4Button.setBackgroundColor(
                                ContextCompat.getColor(this, R.color.green)
                            )

                            if (!incorrectWasAdded) {
                                if (shouldAddCorrect && !correctWasAdded) {
                                    correctAnswersNumber += 1
                                    grade = (correctAnswersNumber.toDouble() / questionsTotal.toDouble()) * 10

                                    shouldAddCorrect = false
                                    correctWasAdded = true
                                }
                            }
                        } else {
                            answer4Button.setBackgroundColor(
                                ContextCompat.getColor(this, R.color.red)
                            )
                            //adds question to incorrect list if it is not in it already
                            if (!incorrectAnswersList.contains(question.questionContent.toString())) {
                                incorrectAnswersList.add(question.questionContent.toString())
                            }

                            //calculates correct and incorrect answers
                            if (shouldAddIncorrect) {
                                incorrectAnswersNumber += 1
                                shouldAddIncorrect = false
                                incorrectWasAdded = true
                                if (correctWasAdded) {
                                    correctAnswersNumber -= 1
                                    correctWasAdded = false
                                }
                                grade = (correctAnswersNumber.toDouble() / questionsTotal.toDouble()) * 10
                            }
                        }

                        //disables button
                        answer4Button.isClickable = false
                    }
                }
            }
    }
}