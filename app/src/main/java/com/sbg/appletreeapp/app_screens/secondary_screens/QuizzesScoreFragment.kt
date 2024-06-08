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
import com.google.firebase.firestore.AggregateSource
import com.google.firebase.firestore.Query
import com.sbg.appletreeapp.R
import com.sbg.appletreeapp.models.ScoreQuizModel
import com.sbg.appletreeapp.utils.FirebaseQueries
import com.sbg.appletreeapp.view_adapters.ScoreQuizAdapter

class QuizzesScoreFragment : Fragment(), ScoreQuizAdapter.OnClickCallback {

    private lateinit var applicationContext: Context
    private lateinit var quizzesScoreNumberTextView: TextView
    private lateinit var quizzesTotalNumberTextView: TextView
    private lateinit var quizzesScoreRecyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var noresultsTextView: TextView

    override fun onAttach(context: Context) {
        super.onAttach(context)

        applicationContext = requireActivity().applicationContext
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        val view: View = inflater.inflate(R.layout.score_quizzes_score_fragment, container, false)

        quizzesScoreNumberTextView = view.findViewById(R.id.quizzesScoreNumberTextView)
        quizzesTotalNumberTextView = view.findViewById(R.id.quizzesTotalNumberTextView)
        quizzesScoreRecyclerView = view.findViewById(R.id.quizzesScoreRecyclerView)
        progressBar = view.findViewById(R.id.progressBar)
        noresultsTextView = view.findViewById(R.id.noresultsTextView)

        countquizzesTotalNumber()
        countCompletequizzesNumber()
        retrieveCompletequizzes()

        return view
    }

    override fun startNewActivity(quizID: String, quizName: String, totalAttempts: String) {
        val intent = Intent(context, QuizAttemptsScoreScreen::class.java)

        // envía los datos a la actividad QuizAttemptsScoreScreen
        intent.putExtra("quizID", quizID)
        intent.putExtra("quizName", quizName)
        intent.putExtra("totalAttempts", totalAttempts)

        requireActivity().startActivity(intent)
    }

    private fun countquizzesTotalNumber() {
        val query: Query = FirebaseQueries.getCollectionReferenceForUserQuizzes()
            .orderBy("createdDate", Query.Direction.DESCENDING)

        val countQuery = query.count()
        countQuery.get(AggregateSource.SERVER).addOnCompleteListener { countTask ->
            if (countTask.isSuccessful) {
                val snapshot = countTask.result
                quizzesTotalNumberTextView.text = snapshot.count.toString()
            }
        }
    }

    private fun countCompletequizzesNumber() {
        FirebaseQueries.getCollectionReferenceForScorequizzes()
            .get()
            .addOnSuccessListener { retrievequizzesScoreTask ->
                if (retrievequizzesScoreTask.size() > 0) {
                    var oldScore = 0
                    for (quizScore in retrievequizzesScoreTask) {
                        if (quizScore.get("completed") as Boolean) {
                            oldScore += 1
                        }
                    }
                    quizzesScoreNumberTextView.text = oldScore.toString()
                } else {
                    quizzesScoreNumberTextView.text = 0.toString()
                }
            }
    }

    private fun retrieveCompletequizzes() {
        progressBar.visibility = View.VISIBLE
        quizzesScoreRecyclerView.visibility = View.INVISIBLE

        val scorequizzesQuery: Query = FirebaseQueries.getCollectionReferenceForScorequizzes()
            .orderBy("quizName", Query.Direction.DESCENDING)

        val countQuery = scorequizzesQuery.count()
        countQuery.get(AggregateSource.SERVER).addOnCompleteListener { countTask ->
            if (countTask.isSuccessful) {
                val snapshot = countTask.result

                if (snapshot.count > 0) {
                    scorequizzesQuery.get().addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val options: FirestoreRecyclerOptions<ScoreQuizModel> =
                                FirestoreRecyclerOptions.Builder<ScoreQuizModel>()
                                    .setQuery(scorequizzesQuery, ScoreQuizModel::class.java).build()

                            quizzesScoreRecyclerView.layoutManager = LinearLayoutManager(applicationContext)

                            val scoreQuizAdapter = ScoreQuizAdapter(applicationContext, options)
                            scoreQuizAdapter.onClickCallback = this
                            quizzesScoreRecyclerView.adapter = scoreQuizAdapter

                            scoreQuizAdapter.startListening()

                            progressBar.visibility = View.GONE
                            quizzesScoreRecyclerView.visibility = View.VISIBLE
                        } else {
                            Toast.makeText(context,
                                R.string.retrieveScoreError,
                                Toast.LENGTH_SHORT).show()

                            progressBar.visibility = View.GONE
                            quizzesScoreRecyclerView.visibility = View.VISIBLE
                        }
                    }
                } else {
                    noresultsTextView.visibility = View.VISIBLE
                    progressBar.visibility = View.GONE
                }
            }
        }
    }
}