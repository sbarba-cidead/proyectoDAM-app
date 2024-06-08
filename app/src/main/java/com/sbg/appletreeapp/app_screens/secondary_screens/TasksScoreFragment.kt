package com.sbg.appletreeapp.app_screens.secondary_screens

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.firestore.AggregateSource
import com.google.firebase.firestore.Query
import com.sbg.appletreeapp.R
import com.sbg.appletreeapp.models.TaskDataModel
import com.sbg.appletreeapp.utils.FirebaseQueries
import com.sbg.appletreeapp.view_adapters.ScoreTaskAdapter

class TasksScoreFragment : Fragment(), ScoreTaskAdapter.Callback {

    private lateinit var applicationContext: Context
    private lateinit var tasksScoreNumberTextView: TextView
    private lateinit var tasksTotalNumberTextView: TextView
    private lateinit var tasksScoreRecyclerView: RecyclerView
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
        val view: View = inflater.inflate(R.layout.score_tasks_score_fragment, container, false)

        tasksScoreNumberTextView = view.findViewById(R.id.tasksScoreNumberTextView)
        tasksTotalNumberTextView = view.findViewById(R.id.tasksTotalNumberTextView)
        tasksScoreRecyclerView = view.findViewById(R.id.tasksScoreRecyclerView)
        progressBar = view.findViewById(R.id.progressBar)
        noresultsTextView = view.findViewById(R.id.noresultsTextView)

        countTasksTotalNumber()
        retrieveTasksScoreData()

        return view
    }

    override fun changeLateDateLayoutVisibility(visible: Boolean, lateDateLayout: RelativeLayout) {
        if (visible) {
            lateDateLayout.visibility = View.VISIBLE
        }
    }

    private fun countTasksTotalNumber() {
        FirebaseQueries.getCollectionReferenceForUserTasks()
            .count()
            .get(AggregateSource.SERVER)
            .addOnCompleteListener { countTask ->
                if (countTask.isSuccessful) {
                    val snapshot = countTask.result
                    tasksTotalNumberTextView.text = snapshot.count.toString()
                } else {
                    Toast.makeText(context,
                        R.string.retrieveScoreError,
                        Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun retrieveTasksScoreData() {
        progressBar.visibility = View.VISIBLE
        tasksScoreRecyclerView.visibility = View.INVISIBLE

        val completeTasksReference = FirebaseQueries.getCollectionReferenceForUserTasks()
            .whereEqualTo("done", true)
            .orderBy("completeDate", Query.Direction.DESCENDING)

        completeTasksReference.count()
            .get(AggregateSource.SERVER)
            .addOnCompleteListener { countTask ->
                if (countTask.isSuccessful) {
                    val snapshot = countTask.result
                    tasksScoreNumberTextView.text = snapshot.count.toString()

                    if (snapshot.count > 0) {
                        completeTasksReference.get().addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                val options: FirestoreRecyclerOptions<TaskDataModel> =
                                    FirestoreRecyclerOptions.Builder<TaskDataModel>()
                                        .setQuery(completeTasksReference, TaskDataModel::class.java).build()

                                tasksScoreRecyclerView.layoutManager = LinearLayoutManager(applicationContext)

                                val scoreTaskAdapter = ScoreTaskAdapter(applicationContext, options)
                                scoreTaskAdapter.callback = this
                                tasksScoreRecyclerView.adapter = scoreTaskAdapter

                                scoreTaskAdapter.startListening()

                                progressBar.visibility = View.GONE
                                tasksScoreRecyclerView.visibility = View.VISIBLE
                            } else {
                                Toast.makeText(context,
                                    R.string.retrieveScoreError,
                                    Toast.LENGTH_SHORT).show()

                                progressBar.visibility = View.GONE
                                tasksScoreRecyclerView.visibility = View.VISIBLE
                            }
                        }
                    } else {
                        noresultsTextView.visibility = View.VISIBLE
                        progressBar.visibility = View.GONE
                    }
                } else {
                    Toast.makeText(context,
                        R.string.retrieveScoreError,
                        Toast.LENGTH_SHORT).show()

                    progressBar.visibility = View.GONE
                }
            }
    }
}