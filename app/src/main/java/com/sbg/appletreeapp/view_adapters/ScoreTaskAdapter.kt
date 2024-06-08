package com.sbg.appletreeapp.view_adapters

import android.content.Context
import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.sbg.appletreeapp.R
import com.sbg.appletreeapp.models.TaskDataModel
import com.sbg.appletreeapp.utils.Utils
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.TimeUnit


class ScoreTaskAdapter(val context: Context, options: FirestoreRecyclerOptions<TaskDataModel>) :
    FirestoreRecyclerAdapter<TaskDataModel, ScoreTaskAdapter.RecyclerViewHolder>(options) {

    var callback: Callback? = null

    /**
     * Se ejecuta al crear el ViewHolder.
     * Crea el ViewHolder (wrapper) y la vista a la que se asocia (score_quiz_attempt_model)
     * @param viewGroup
     * @param viewType
     * @return RecyclerViewHolder
     */
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): RecyclerViewHolder {
        val view = LayoutInflater.from(viewGroup.context).inflate(R.layout.score_task_model, viewGroup, false)
        return RecyclerViewHolder(view)
    }

    /**
     * Vincula la vista (score_quiz_attempt_model) con los datos
     * @param viewHolder
     * @param position
     * @param note
     */
    override fun onBindViewHolder(viewHolder: RecyclerViewHolder, position: Int, task: TaskDataModel) {
        viewHolder.taskName.text = task.content
        viewHolder.dueDate.text = Utils.convertTimestampToDateString(task.dueDate!!)
        viewHolder.completeDate.text = Utils.convertTimestampToDateString(task.completeDate!!)

        if (task.dueDate!!.seconds < task.completeDate!!.seconds) { //si se ha completado tarde
            callback!!.changeLateDateLayoutVisibility(true, viewHolder.lateDateLayout) //mostrar días de retraso

            val endDateString = Utils.convertTimestampToDateString(task.completeDate!!)
            val startDateString = Utils.convertTimestampToDateString(task.dueDate!!)

            val endDateValue = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).parse(endDateString)
            val startDateValue = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).parse(startDateString)

            val differenceMilliseconds: Long = endDateValue!!.time - startDateValue!!.time
            val differenceDays = TimeUnit.DAYS.convert(differenceMilliseconds, TimeUnit.MILLISECONDS)

            viewHolder.difference.text = differenceDays.toString()

            //cambia color de fondo
            val colorInt = ContextCompat.getColor(context, R.color.delayed)
            viewHolder.taskLayout.backgroundTintList = ColorStateList.valueOf(colorInt)
        }
    }

    /**
     * Clase anidada para el ViewHolder de las vistas de los resultados
     * Permite vincular los datos del resultado con su vista
     * @param view
     * @return RecyclerView.ViewHolder(view)
     */
    class RecyclerViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val taskName: TextView = view.findViewById(R.id.taskName)
        val dueDate: TextView = view.findViewById(R.id.dueDate)
        val completeDate: TextView = view.findViewById(R.id.completeDate)
        val difference: TextView = view.findViewById(R.id.difference)

        val lateDateLayout: RelativeLayout = view.findViewById(R.id.lateDateLayout)
        val taskLayout: LinearLayout = view.findViewById(R.id.taskLayout)
    }

    interface Callback {
        fun changeLateDateLayoutVisibility(visible: Boolean, lateDateLayout: RelativeLayout)
    }
}