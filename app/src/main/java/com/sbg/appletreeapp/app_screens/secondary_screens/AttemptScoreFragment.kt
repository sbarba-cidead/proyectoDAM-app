package com.sbg.appletreeapp.app_screens.secondary_screens

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.sbg.appletreeapp.R
import com.sbg.appletreeapp.utils.FirebaseQueries
import com.sbg.appletreeapp.utils.Utils
import com.sbg.appletreeapp.view_adapters.IncorrectAnswersAdapter
import java.text.SimpleDateFormat
import java.util.Locale


class AttemptScoreFragment : Fragment() {

    private lateinit var applicationContext: Context
    private lateinit var quizNameTextView: TextView
    private lateinit var attemptNumberTextView: TextView
    private lateinit var attemptDateTextView: TextView
    private lateinit var date: TextView
    private lateinit var duration: TextView
    private lateinit var totalQuestionsNumber: TextView
    private lateinit var correctAnswers: TextView
    private lateinit var incorrectAnswers: TextView
    private lateinit var grade: TextView
    private lateinit var incorrectQuestionsRecyclerView: RecyclerView
    private lateinit var noIncorrectQuestionsTextView: TextView

    private var quizID: String? = null
    private var quizName: String? = null
    private var attemptID: String? = null
    private var attemptNumber: String? = null
    private var attemptDate: String? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)

        applicationContext = requireActivity().applicationContext
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        val view: View = inflater.inflate(R.layout.score_attempt_score_fragment, container, false)

        quizNameTextView = view.findViewById(R.id.quizNameTextView)
        attemptNumberTextView = view.findViewById(R.id.attemptNumberTextView)
        attemptDateTextView = view.findViewById(R.id.attemptDateTextView)
        date = view.findViewById(R.id.date)
        duration = view.findViewById(R.id.duration)
        totalQuestionsNumber = view.findViewById(R.id.totalQuestionsNumber)
        correctAnswers = view.findViewById(R.id.correctAnswers)
        incorrectAnswers = view.findViewById(R.id.incorrectAnswers)
        grade = view.findViewById(R.id.grade)
        incorrectQuestionsRecyclerView = view.findViewById(R.id.incorrectQuestionsRecyclerView)
        noIncorrectQuestionsTextView = view.findViewById(R.id.noIncorrectQuestionsTextView)

        quizID = requireArguments().getString("quizID")
        quizName = requireArguments().getString("quizName")
        attemptID = requireArguments().getString("attemptID")
        attemptNumber = requireArguments().getString("attemptNumber")
        attemptDate = requireArguments().getString("attemptDate")

        quizNameTextView.text = quizName
        attemptNumberTextView.text = attemptNumber

        val reformatAttemptDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            .parse(attemptDate!!)
        attemptDateTextView.text = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            .format(reformatAttemptDate!!)

        retrieveAttemptData()

        return view
    }

    private fun retrieveAttemptData() {
        FirebaseQueries.getCollectionReferenceForScorequizzesAttempts()
            .document(attemptID.toString()).get().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    date.text = task.result.data!!["formattedDate"].toString()
                    duration.text = Utils.convertSecondsToHourMinuteSecond(task.result.data!!["completeTimeSecs"] as Long)
                    totalQuestionsNumber.text = task.result.data!!["totalQuestionsNumber"].toString()
                    correctAnswers.text = task.result.data!!["correctAnswers"].toString()
                    incorrectAnswers.text = task.result.data!!["incorrectAnswers"].toString()
                    grade.text = task.result.data!!["grade"].toString()

                    val incorrectAnswersList = task.result.data!!["incorrectAnswersList"] as List<*>
                    if (incorrectAnswersList.isNotEmpty()) {
                        val layoutManager = LinearLayoutManager(requireActivity(),
                            LinearLayoutManager.VERTICAL, false)
                        incorrectQuestionsRecyclerView.layoutManager = layoutManager
                        val incorrectAnswersAdapter = IncorrectAnswersAdapter(incorrectAnswersList)
                        incorrectQuestionsRecyclerView.adapter = incorrectAnswersAdapter
                    } else {
                        incorrectQuestionsRecyclerView.visibility = View.GONE
                        noIncorrectQuestionsTextView.visibility = View.VISIBLE
                    }

                } else {
                    Toast.makeText(context,
                        R.string.retrieveScoreError,
                        Toast.LENGTH_SHORT).show()
                }
            }
    }
}