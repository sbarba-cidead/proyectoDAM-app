package com.sbg.appletreeapp.app_screens.secondary_screens

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.firestore.Query
import com.sbg.appletreeapp.R
import com.sbg.appletreeapp.models.ScoreQuizAttemptModel
import com.sbg.appletreeapp.utils.FirebaseQueries
import com.sbg.appletreeapp.view_adapters.ScoreQuizAttemptsAdapter

class QuizAttemptsScoreFragment : Fragment(), ScoreQuizAttemptsAdapter.OnClickCallback {

    private lateinit var applicationContext: Context
    private lateinit var quizNameTextView: TextView
    private lateinit var quizAttemptsTotalTextView: TextView
    private lateinit var quizzesAttemptsScoreRecyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar

    private var quizID: String? = null
    private var quizName: String? = null
    private var totalAttempts: String? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)

        applicationContext = requireActivity().applicationContext
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        val view: View = inflater.inflate(R.layout.score_quiz_attempts_score_fragment, container, false)

        quizNameTextView = view.findViewById(R.id.quizNameTextView)
        quizAttemptsTotalTextView = view.findViewById(R.id.quizAttemptsTotalTextView)
        quizzesAttemptsScoreRecyclerView = view.findViewById(R.id.quizzesAttemptsScoreRecyclerView)
        progressBar = view.findViewById(R.id.progressBar)

        quizID = requireArguments().getString("quizID")
        quizName = requireArguments().getString("quizName")
        totalAttempts = requireArguments().getString("totalAttempts")

        quizNameTextView.text = quizName
        quizAttemptsTotalTextView.text = totalAttempts

        retrieveAttempts()

        return view
    }

    override fun startNewActivity(attemptID: String, attemptNumber: String, attemptDate: String) {
        val intent = Intent(context, AttemptScoreScreen::class.java)

        // envía los datos a la actividad AttemptScoreScreen
        intent.putExtra("quizID", quizID)
        intent.putExtra("quizName", quizName)
        intent.putExtra("attemptID", attemptID)
        intent.putExtra("attemptNumber", attemptNumber)
        intent.putExtra("attemptDate", attemptDate)

        requireActivity().startActivity(intent)
    }

    private fun retrieveAttempts() {
        progressBar.visibility = View.VISIBLE
        quizzesAttemptsScoreRecyclerView.visibility = View.INVISIBLE

        val scorequizzesAttemptsQuery: Query = FirebaseQueries.getCollectionReferenceForScorequizzesAttempts()
            .whereEqualTo("quizID", quizID)
            .orderBy("date", Query.Direction.ASCENDING)

        scorequizzesAttemptsQuery.get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val options: FirestoreRecyclerOptions<ScoreQuizAttemptModel> =
                    FirestoreRecyclerOptions.Builder<ScoreQuizAttemptModel>()
                        .setQuery(scorequizzesAttemptsQuery, ScoreQuizAttemptModel::class.java).build()

                quizzesAttemptsScoreRecyclerView.layoutManager = LinearLayoutManager(applicationContext)

                val scoreQuizAttemptsAdapter = ScoreQuizAttemptsAdapter(applicationContext, options)
                scoreQuizAttemptsAdapter.onClickCallback = this
                quizzesAttemptsScoreRecyclerView.adapter = scoreQuizAttemptsAdapter

                scoreQuizAttemptsAdapter.startListening()

                progressBar.visibility = View.GONE
                quizzesAttemptsScoreRecyclerView.visibility = View.VISIBLE
            } else {
                Toast.makeText(context,
                    R.string.retrieveScoreError,
                    Toast.LENGTH_SHORT).show()

                progressBar.visibility = View.GONE
                quizzesAttemptsScoreRecyclerView.visibility = View.VISIBLE
            }
        }
    }
}