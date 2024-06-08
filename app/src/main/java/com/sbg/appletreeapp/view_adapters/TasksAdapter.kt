package com.sbg.appletreeapp.view_adapters

import android.content.Context
import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.sbg.appletreeapp.R
import com.sbg.appletreeapp.models.TaskDataModel
import com.sbg.appletreeapp.utils.FirebaseQueries
import com.sbg.appletreeapp.utils.FirebaseQueries.Companion.getCurrentUser
import com.sbg.appletreeapp.utils.Utils

class TasksAdapter(val context: Context, options: FirestoreRecyclerOptions<TaskDataModel>) :
    FirestoreRecyclerAdapter<TaskDataModel, TasksAdapter.RecyclerViewHolder>(options) {

    var checkboxCallback: CheckboxCallback? = null

    /**
     * Se ejecuta al crear el ViewHolder.
     * Crea el ViewHolder (wrapper) y la vista a la que se asocia (tasks_task_model)
     * @param viewGroup
     * @param viewType
     * @return RecyclerViewHolder
     */
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): RecyclerViewHolder {
        val view = LayoutInflater.from(viewGroup.context).inflate(R.layout.tasks_task_model, viewGroup, false)
        return RecyclerViewHolder(view)
    }

    /**
     * Vincula la vista (tasks_task_model) con los datos
     * @param viewHolder
     * @param position
     * @param task
     */
    override fun onBindViewHolder(viewHolder: RecyclerViewHolder, position: Int, task: TaskDataModel) {
        viewHolder.taskContent.text = task.content
        viewHolder.taskCheckbox.isChecked = task.isDone

        // color de la tarea
        if (!task.isDone) { // si la tarea no se ha completado
            if (task.dueDate!! < Timestamp.now()) { // si la fecha límite ya ha pasado
                // se muestra la tarea en rojo
                val colorInt = ContextCompat.getColor(context, R.color.delayed)
                viewHolder.taskLayout.backgroundTintList = ColorStateList.valueOf(colorInt)
            }
        } else { // si la tarea se ha completado
            if (task.dueDate!!.seconds < task.completeDate!!.seconds) { // si fecha completada es posterior a fecha límite
                // se muestra la tarea en rojo
                val colorInt = ContextCompat.getColor(context, R.color.delayed)
                viewHolder.taskLayout.backgroundTintList = ColorStateList.valueOf(colorInt)
            }
        }

        // fecha de la tarea
        if (task.isDone) {
            // si la tarea está completada muestra la fecha en la que se completó
            viewHolder.taskDateLabel.text = context.getString(R.string.completeTimeTaskModelLabel)
            viewHolder.taskDate.text = Utils.convertTimestampToDateString(task.completeDate!!)
        } else {
            // si la tarea no está completada muestra la fecha límite
            viewHolder.taskDateLabel.text = context.getString(R.string.dueTimeTaskModelLabel)
            viewHolder.taskDate.text = Utils.convertTimestampToDateString(task.dueDate!!)
        }

        // checkbox function
        viewHolder.taskCheckbox.setOnClickListener{
            if (viewHolder.taskCheckbox.isChecked) {
                task.completeDate = Timestamp.now()
                task.isDone = true

                // marca la tarea como completada en tareas generales en firebase
                FirebaseQueries.getDocumentReferenceForUserData()
                    .get().addOnCompleteListener { retrieveTeacherTask ->
                        if (retrieveTeacherTask.isSuccessful) {
                            val teacherID = retrieveTeacherTask.result["teacherID"].toString()

                            FirebaseQueries.getCollectionReferenceForGeneralTasks(teacherID)
                                .document(task.taskID!!)
                                .update("completedStudents", FieldValue.arrayUnion(getCurrentUser()!!.uid))
                        }
                    }

                // marca la tarea como completada en tareas del alumno en firebase
                val documentReference = FirebaseQueries.getCollectionReferenceForUserTasks()
                    .document(task.taskID!!)
                documentReference.set(task).addOnCompleteListener { editTask ->
                    if (editTask.isSuccessful){
                        checkboxCallback!!.checkTaskCount()

                        Toast.makeText(context, R.string.completedTask,
                            Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context,
                            R.string.completedTaskError,
                            Toast.LENGTH_SHORT).show()
                    }
                }

                // marca
            } else {
                task.completeDate = null
                task.isDone = false

                // marca la tarea como no completa en tareas generales en firebase
                FirebaseQueries.getDocumentReferenceForUserData()
                    .get().addOnCompleteListener { retrieveTeacherTask ->
                        if (retrieveTeacherTask.isSuccessful) {
                            val teacherID = retrieveTeacherTask.result["teacherID"].toString()

                            FirebaseQueries.getCollectionReferenceForGeneralTasks(teacherID)
                                .document(task.taskID!!)
                                .update("completedStudents", FieldValue.arrayRemove(getCurrentUser()!!.uid))
                        }
                    }

                // marca la tarea como no completa en tareas del alumno en firebase
                val documentReference = FirebaseQueries.getCollectionReferenceForUserTasks()
                    .document(task.taskID!!)
                documentReference.set(task).addOnCompleteListener { editTask ->
                    if (editTask.isSuccessful){
                        checkboxCallback!!.checkTaskCount()

                        Toast.makeText(context, R.string.uncompletedTask,
                            Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, R.string.uncompletedTaskError, Toast.LENGTH_SHORT)
                            .show()
                    }
                }
            }
        }
    }

    /**
     * Clase anidada para el ViewHolder de las vistas de las tareas
     * Permite vincular los datos de la tarea con su vista
     * @param view
     * @return RecyclerView.ViewHolder(view)
     */
    class RecyclerViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val taskContent: TextView = view.findViewById(R.id.taskContent)
        val taskDate: TextView = view.findViewById(R.id.date)
        val taskDateLabel: TextView = view.findViewById(R.id.taskDateLabel)
        val taskCheckbox: MaterialCheckBox = view.findViewById(R.id.taskCheckbox)
        val taskLayout: LinearLayout = view.findViewById(R.id.taskLayout)
    }

    interface CheckboxCallback {
        fun checkTaskCount()
    }
}