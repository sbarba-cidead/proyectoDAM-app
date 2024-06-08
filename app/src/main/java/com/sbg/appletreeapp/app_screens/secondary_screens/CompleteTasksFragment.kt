package com.sbg.appletreeapp.app_screens.secondary_screens

import android.content.Context
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
import com.sbg.appletreeapp.models.TaskDataModel
import com.sbg.appletreeapp.utils.FirebaseQueries
import com.sbg.appletreeapp.view_adapters.TasksAdapter


class CompleteTasksFragment : Fragment(), TasksAdapter.CheckboxCallback {

    private lateinit var applicationContext: Context
    private lateinit var tasksAdapter: TasksAdapter
    private lateinit var tasksRecyclerView: RecyclerView
    private lateinit var notasksTextView: TextView
    private lateinit var progressBar: ProgressBar

    private var recheck = false

    override fun onAttach(context: Context) {
        super.onAttach(context)

        applicationContext = requireActivity().applicationContext
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.tasks_complete_tasks_fragment, container, false)
        tasksRecyclerView = view.findViewById(R.id.tasksRecyclerView)
        notasksTextView = view.findViewById(R.id.notasksTextView)
        progressBar = view.findViewById(R.id.progressBar)

        //Actualiza las tareas desde Firebase
        retrieveCompleteTasks()

        return view
    }

    override fun onResume(){
        super.onResume()

        if (recheck) { retrieveCompleteTasks() }

        checkTaskCount()
    }

    /**
     * Implementa la interfaz del TaskAdapter para comprobar el número de tareas.
     * Esta función se ejecuta cada vez que se pulsa el checkbox de una tarea.
     */
    override fun checkTaskCount() {
        // cuenta el número de tareas completadas
        FirebaseQueries.getCollectionReferenceForUserTasks()
            .whereEqualTo("done", true)
            .count()
            .get(AggregateSource.SERVER)
            .addOnCompleteListener { countTask ->
                if (countTask.isSuccessful) {
                    val snapshot = countTask.result

                    if (snapshot.count < 1) { // si no hay tareas completadas
                        // hides recyclerview
                        tasksRecyclerView.visibility = View.INVISIBLE

                        // shows textview message
                        notasksTextView.visibility = View.VISIBLE
                    } else { // si hay tareas completadas
                        // shows recyclerview
                        tasksRecyclerView.visibility = View.VISIBLE

                        // hides textview message
                        notasksTextView.visibility = View.GONE
                    }
                }
            }
    }

    /**
     * Actualiza las tareas completadas desde firebase y las muestra en el recyclerview
     */
    private fun retrieveCompleteTasks() {
        // shows progressbar
        progressBar.visibility = View.VISIBLE

        // recupera las tareas completadas del alumno de firebase
        val query: Query = FirebaseQueries.getCollectionReferenceForUserTasks()
            .whereEqualTo("done", true)
            .orderBy("completeDate", Query.Direction.ASCENDING)

        // cuenta el número de tareas completadas
        val countQuery = query.count()
        countQuery.get(AggregateSource.SERVER).addOnCompleteListener { countTask ->
            if (countTask.isSuccessful) {
                val snapshot = countTask.result

                if (snapshot.count > 0) { // si hay tareas completadas
                    // muestra las tareas completadas en el recyclerview
                    val options: FirestoreRecyclerOptions<TaskDataModel> =
                        FirestoreRecyclerOptions.Builder<TaskDataModel>()
                            .setQuery(query, TaskDataModel::class.java).build()

                    tasksRecyclerView.layoutManager = LinearLayoutManager(applicationContext)

                    tasksAdapter = TasksAdapter(applicationContext, options)
                    tasksAdapter.checkboxCallback = this
                    tasksRecyclerView.adapter = tasksAdapter

                    tasksAdapter.startListening()

                    tasksAdapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
                        override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                            tasksRecyclerView.smoothScrollToPosition(0)
                        }
                    })

                    // hides progressbar
                    progressBar.visibility = View.GONE

                    recheck = false
                } else {
                    recheck = true

                    // hides progressbar
                    progressBar.visibility = View.GONE
                }
            } else {
                Toast.makeText(context,
                    R.string.retrieveTasksError,
                    Toast.LENGTH_SHORT).show()

                // hides progressbar
                progressBar.visibility = View.GONE
            }
        }
    }


}