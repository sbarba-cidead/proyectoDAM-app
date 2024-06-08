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


class ScoresAdapter(val context: Context, options: FirestoreRecyclerOptions<ScoreQuizAttemptModel>) :
    FirestoreRecyclerAdapter<ScoreQuizAttemptModel, ScoresAdapter.RecyclerViewHolder>(options) {

    /**
     * Se ejecuta al crear el ViewHolder.
     * Crea el ViewHolder (wrapper) y la vista a la que se asocia (quizzes_quiz_model)
     * @param viewGroup
     * @param viewType
     * @return RecyclerViewHolder
     */
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): RecyclerViewHolder {
        val view = LayoutInflater.from(viewGroup.context).inflate(R.layout.score_model, viewGroup, false)
        return RecyclerViewHolder(view)
    }

    /**
     * Vincula la vista (score_model) con los datos
     * @param viewHolder
     * @param position
     * @param note
     */
    override fun onBindViewHolder(viewHolder: RecyclerViewHolder, position: Int, score: ScoreQuizAttemptModel) {
        viewHolder.quizName.text = score.quizName
//        viewHolder.date.text = score.date
        viewHolder.correctAnswers.text = score.correctAnswers.toString()
        viewHolder.incorrectAnswers.text = score.incorrectAnswers.toString()
    }

    /**
     * Clase anidada para el ViewHolder de las vistas de los resultados
     * Permite vincular los datos del resultado con su vista
     * @param view
     * @return RecyclerView.ViewHolder(view)
     */
    class RecyclerViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val quizName: TextView = view.findViewById(R.id.quizName)
        val date: TextView = view.findViewById(R.id.date)
        val correctAnswers: TextView = view.findViewById(R.id.correctAnswers)
        val incorrectAnswers: TextView = view.findViewById(R.id.incorrectAnswers)
    }

}