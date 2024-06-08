package com.sbg.appletreeapp.view_adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.sbg.appletreeapp.R
import com.sbg.appletreeapp.models.ScoreQuizAttemptModel


class ScoreQuizAttemptsAdapter(val context: Context, options: FirestoreRecyclerOptions<ScoreQuizAttemptModel>) :
    FirestoreRecyclerAdapter<ScoreQuizAttemptModel, ScoreQuizAttemptsAdapter.RecyclerViewHolder>(options) {

    var onClickCallback: OnClickCallback? = null

    /**
     * Se ejecuta al crear el ViewHolder.
     * Crea el ViewHolder (wrapper) y la vista a la que se asocia (score_quiz_attempt_model)
     * @param viewGroup
     * @param viewType
     * @return RecyclerViewHolder
     */
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): RecyclerViewHolder {
        val view = LayoutInflater.from(viewGroup.context).inflate(R.layout.score_quiz_attempt_model, viewGroup, false)
        return RecyclerViewHolder(view)
    }

    /**
     * Vincula la vista (score_quiz_attempt_model) con los datos
     * @param viewHolder
     * @param position
     * @param note
     */
    override fun onBindViewHolder(viewHolder: RecyclerViewHolder, position: Int, scoreQuizAttempt: ScoreQuizAttemptModel) {
        val attemptNumber = position+1
        viewHolder.attemptNumber.text = attemptNumber.toString()
        viewHolder.attemptDate.text = scoreQuizAttempt.formattedDate

        viewHolder.itemView.setOnClickListener {
            onClickCallback!!.startNewActivity(
                scoreQuizAttempt.attemptID.toString(),
                attemptNumber.toString(),
                scoreQuizAttempt.formattedDate.toString())
        }
    }

    /**
     * Clase anidada para el ViewHolder de las vistas de los resultados
     * Permite vincular los datos del resultado con su vista
     * @param view
     * @return RecyclerView.ViewHolder(view)
     */
    class RecyclerViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val attemptNumber: TextView = view.findViewById(R.id.attemptNumber)
        val attemptDate: TextView = view.findViewById(R.id.attemptDate)
    }

    interface OnClickCallback {
        fun startNewActivity(attemptID: String, attemptNumber: String, attemptDate: String)
    }
}