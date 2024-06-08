package com.sbg.appletreeapp.app_screens

import android.os.Bundle
import android.text.InputType
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.firestore.AggregateSource
import com.google.firebase.firestore.Query
import com.sbg.appletreeapp.R
import com.sbg.appletreeapp.models.QuestionDataModel
import com.sbg.appletreeapp.models.QuizDataModel
import com.sbg.appletreeapp.utils.FirebaseQueries
import com.sbg.appletreeapp.view_adapters.QuizzesAdapter

class QuizzesScreen : AppCompatActivity() {

    private lateinit var titleTextView: TextView
    private lateinit var quizzesRecyclerView: RecyclerView
    private lateinit var noquizzesTextView: TextView
    private lateinit var searchView: SearchView
    private lateinit var progressBar: ProgressBar
    private lateinit var quizzesAdapter: QuizzesAdapter

    private var teacherID = ""
    private var searchRecheck = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.quizzes_screen)

        //Enlaza los elementos del layout con variables
        titleTextView = findViewById(R.id.titleTextView)
        quizzesRecyclerView = findViewById(R.id.quizzesRecyclerView)
        noquizzesTextView = findViewById(R.id.noquizzesTextView)
        searchView = findViewById(R.id.searchView)
        progressBar = findViewById(R.id.progressBar)

        // SEARCH OPTION //
        searchView.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_SENTENCES

        searchView.setOnSearchClickListener {
            titleTextView.visibility = View.INVISIBLE

            searchRecheck = true
        }

        val seachViewCloseButton: View =
            searchView.findViewById(androidx.appcompat.R.id.search_close_btn)
        seachViewCloseButton.setOnClickListener {
            // closes search bar
            searchView.isIconified = true
        }

        searchView.setOnCloseListener {
            titleTextView.visibility = View.VISIBLE

            searchRecheck = true

            false
        }

        var oldQuery = ""
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                searchquizzes(query!!)
                searchRecheck = true

                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText!!.isEmpty()) { searchRecheck = true } // si es una nueva búsqueda
                if (newText.length < oldQuery.length) { searchRecheck = true } // si se borraron caracteres
                oldQuery = newText
                searchquizzes(newText)

                return true
            }
        })

        // END OF SEARCH OPTION //

        //Actualiza los tests desde Firebase
        retrieveQuizzes()
    }

    override fun onResume() {
        super.onResume()

        //Actualiza los tests desde Firebase
        retrieveQuizzes()
    }

    /**
     * Actualiza los tests desde firebase y los muestra en el recyclerview
     * @param quizzesRecyclerView
     */
    private fun retrieveQuizzes() {
        // shows progressbar
        progressBar.visibility = View.VISIBLE

        FirebaseQueries.getDocumentReferenceForUserData()
            .get().addOnCompleteListener { retrieveTeacherTask ->
                if (retrieveTeacherTask.isSuccessful) {
                    teacherID = retrieveTeacherTask.result["teacherID"].toString()

                    FirebaseQueries.getDocumentReferenceForUserData().get().addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val groupID = task.result["classGroupID"].toString()

                            // elimina los cuestionarios eliminados por el profesor
                            FirebaseQueries.getCollectionReferenceForUserQuizzes().get()
                                .addOnSuccessListener { userQuizzes ->
                                    if (userQuizzes.size() > 0) {
                                        val collectionReference =
                                            FirebaseQueries.getCollectionReferenceForGeneralQuizzes(teacherID)
                                        for (userQuiz in userQuizzes) {
                                            collectionReference.document(userQuiz.id).get()
                                                .addOnCompleteListener { find ->
                                                    if (find.isSuccessful) {
                                                        if (!find.result.exists()) {
                                                            FirebaseQueries.getCollectionReferenceForUserQuizzes()
                                                                .document(userQuiz.id).delete()
                                                        }
                                                    }
                                                }
                                        }
                                    }
                                }

                            // recupera los cuestionarios de firebase
                            FirebaseQueries.getCollectionReferenceForGeneralQuizzes(teacherID)
                                .whereEqualTo("group", groupID)
                                .get()
                                .addOnSuccessListener { generalQuizzes ->
                                    if (generalQuizzes.size() > 0) { // si hay cuestionarios
                                        for (quiz in generalQuizzes) { // por cada cuestionario
                                            // comprueba si el alumno lo ha completado
                                            val studentsArray = quiz.get("completedStudents") as ArrayList<*>
                                            if (!studentsArray
                                                    .contains(FirebaseQueries.getCurrentUser()!!.uid)) { // si no completado
                                                val quizObject =
                                                    quiz.toObject(QuizDataModel::class.java)

                                                // añade el cuestionario a los cuestionarios del alumno
                                                FirebaseQueries.getCollectionReferenceForUserQuizzes()
                                                    .document(quiz.id).set(quizObject)
                                                    .addOnCompleteListener { task ->
                                                        //añade las preguntas
                                                        FirebaseQueries.getCollectionReferenceForGeneralQuizzesQuestions(teacherID, quiz.id)
                                                            .get().addOnSuccessListener {generalQuestions ->
                                                                for (generalQuestion in generalQuestions) {
                                                                    val questionObject =
                                                                        generalQuestion.toObject(QuestionDataModel::class.java)
                                                                    FirebaseQueries.getCollectionReferenceForUserQuizzesQuestions(quiz.id)
                                                                        .document(generalQuestion.id).set(questionObject)
                                                                }
                                                            }
                                                    }
                                            }

                                            calculateScoreAdvanceQuizzes()
                                            calculateScoreReviewQuizzes()

                                            val query =
                                                FirebaseQueries.getCollectionReferenceForUserQuizzes()
                                                    .whereEqualTo("hidden", false)
                                                    .orderBy(
                                                        "createdDate",
                                                        Query.Direction.DESCENDING
                                                    )

                                            val options: FirestoreRecyclerOptions<QuizDataModel> =
                                                FirestoreRecyclerOptions.Builder<QuizDataModel>()
                                                    .setQuery(query, QuizDataModel::class.java)
                                                    .build()

                                            quizzesRecyclerView.layoutManager =
                                                StaggeredGridLayoutManager(
                                                    2,
                                                    GridLayoutManager.VERTICAL
                                                )

                                            val quizzesAdapter = QuizzesAdapter(this, options)
                                            quizzesRecyclerView.adapter = quizzesAdapter

                                            quizzesAdapter.startListening()

                                            // hides progressbar
                                            progressBar.visibility = View.GONE

                                        }
                                    } else {
                                        // hides recyclerview
                                        quizzesRecyclerView.visibility = View.GONE

                                        // shows textview message
                                        noquizzesTextView.visibility = View.VISIBLE

                                        // hides progressbar
                                        progressBar.visibility = View.GONE
                                    }
                                }
                        }
                    }

                }
            }
    }

    /**
     * Busca el texto en la consulta de tests en firebase
     * @param text
     */
    private fun searchquizzes(text: String) {
        // solo hace la búsqueda si es una nueva búsqueda o se ha borrado el último caracter
        if (searchRecheck) {
            noquizzesTextView.visibility = View.GONE
            progressBar.visibility = View.VISIBLE

            val query: Query = FirebaseQueries.getCollectionReferenceForUserQuizzes()
                .whereEqualTo("hidden", false)
                .orderBy("quizName").startAt(text).endAt(text+"\uf8ff")

            val countQuery = query.count()
            countQuery.get(AggregateSource.SERVER).addOnCompleteListener { countTask ->
                if (countTask.isSuccessful) {
                    val snapshot = countTask.result

                    if (snapshot.count > 0) {
                        quizzesRecyclerView.visibility = View.VISIBLE

                        val options: FirestoreRecyclerOptions<QuizDataModel> =
                            FirestoreRecyclerOptions.Builder<QuizDataModel>()
                                .setQuery(query, QuizDataModel::class.java).build()

                        quizzesRecyclerView.layoutManager = StaggeredGridLayoutManager(
                            2,
                            GridLayoutManager.VERTICAL
                        )

                        quizzesAdapter = QuizzesAdapter(this, options)
                        quizzesRecyclerView.adapter = quizzesAdapter

                        quizzesAdapter.startListening()

                        progressBar.visibility = View.GONE
                    } else {
                        // Si no hay coincidencias, se desactiva la búsqueda en firebase
                        // hasta que se borre la búsqueda completa o el último caracter.
                        // Ahorra búsquedas en firebase porque ya se sabe que no habrá resultados
                        searchRecheck = false

                        quizzesRecyclerView.visibility = View.INVISIBLE
                        // shows textview message
                        noquizzesTextView.text = getString(R.string.notFound)
                        noquizzesTextView.visibility = View.VISIBLE

                        progressBar.visibility = View.GONE
                    }
                }
            }
        }
    }

    private fun calculateScoreAdvanceQuizzes() {
        // tiene que tener al menos 3 intentos en el mismo tema de quiz
        // y tener al menos una media de nota superior a 8

        val query: Query = FirebaseQueries.getCollectionReferenceForUserQuizzes()
            .whereEqualTo("level", "advance")

        query.get().addOnSuccessListener { quizzes ->
            if (quizzes.size() > 0) {
                val subjects = mutableListOf<String>()

                for (quiz in quizzes) {
                    if (!subjects.contains(quiz["subject"].toString())) {
                        subjects.add(quiz["subject"].toString())
                    }
                }

                FirebaseQueries.getCollectionReferenceForScorequizzes().get()
                    .addOnSuccessListener { attemptQuizzes ->
                        for (subject in subjects) {
                            var attemptsOK: Boolean? = null
                            var sum = 0.0
                            var subjectAverage = 0.0
                            for (attemptQuiz in attemptQuizzes) {
                                val number: Double = attemptQuiz["averageGrade"] as Double
                                sum += number

                                if (attemptsOK == null) {
                                    if ((attemptQuiz["totalAttempts"] as Long) < 3) {
                                        attemptsOK = false
                                    }
                                }
                            }

                            if (attemptsOK == null) {
                                attemptsOK = true
                            }
                            subjectAverage = sum / attemptQuizzes.size()

                            if (subjectAverage > 8.0 && attemptsOK) {
                                //marca los quizes como visibles
                                FirebaseQueries.getCollectionReferenceForUserQuizzes().get()
                                    .addOnSuccessListener { quizzes ->
                                        for (quiz in quizzes) {
                                            quiz.reference.update("hidden", false)
                                        }
                                    }
                            }
                        }
                    }
            }
        }
    }

    private fun calculateScoreReviewQuizzes() {
        // tiene que tener al menos 3 intentos en el mismo tema de quiz
        // y tener al menos una media de nota inferior a 5

        val query: Query = FirebaseQueries.getCollectionReferenceForUserQuizzes()
            .whereEqualTo("level", "advance")

        query.get().addOnSuccessListener { quizzes ->
            if (quizzes.size() > 0) {
                val subjects = mutableListOf<String>()

                for (quiz in quizzes) {
                    if (!subjects.contains(quiz["subject"].toString())) {
                        subjects.add(quiz["subject"].toString())
                    }
                }

                FirebaseQueries.getCollectionReferenceForScorequizzes().get()
                    .addOnSuccessListener { attemptQuizzes ->
                        for (subject in subjects) {
                            var attemptsOK: Boolean? = null
                            var sum = 0.0
                            var subjectAverage = 0.0
                            for (attemptQuiz in attemptQuizzes) {
                                val number: Double = attemptQuiz["averageGrade"] as Double
                                sum += number

                                if (attemptsOK == null) {
                                    if ((attemptQuiz["totalAttempts"] as Long) < 3) {
                                        attemptsOK = false
                                    }
                                }
                            }

                            if (attemptsOK == null) {
                                attemptsOK = true
                            }
                            subjectAverage = sum / attemptQuizzes.size()

                            if (subjectAverage < 5.0 && attemptsOK) {
                                //marca los quizes como visibles
                                FirebaseQueries.getCollectionReferenceForUserQuizzes().get()
                                    .addOnSuccessListener { quizzes ->
                                        for (quiz in quizzes) {
                                            quiz.reference.update("hidden", false)
                                        }
                                    }
                            }
                        }
                    }
            }
        }
    }

}