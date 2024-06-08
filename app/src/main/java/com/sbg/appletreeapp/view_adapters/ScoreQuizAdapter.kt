package com.sbg.appletreeapp.view_adapters

import android.content.Context
import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.sbg.appletreeapp.R
import com.sbg.appletreeapp.models.ScoreQuizModel
import java.math.BigDecimal
import java.math.RoundingMode


class ScoreQuizAdapter(val context: Context, options: FirestoreRecyclerOptions<ScoreQuizModel>) :
    FirestoreRecyclerAdapter<ScoreQuizModel, ScoreQuizAdapter.RecyclerViewHolder>(options) {

    var onClickCallback: OnClickCallback? = null

    /**
     * Se ejecuta al crear el ViewHolder.
     * Crea el ViewHolder (wrapper) y la vista a la que se asocia (quizzes_quiz_model)
     * @param viewGroup
     * @param viewType
     * @return RecyclerViewHolder
     */
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): RecyclerViewHolder {
        val view = LayoutInflater.from(viewGroup.context).inflate(R.layout.score_quiz_model, viewGroup, false)
        return RecyclerViewHolder(view)
    }

    /**
     * Vincula la vista (score_quiz_model) con los datos
     * @param viewHolder
     * @param position
     * @param note
     */
    override fun onBindViewHolder(viewHolder: RecyclerViewHolder, position: Int, scoreQuiz: ScoreQuizModel) {
        viewHolder.quizName.text = scoreQuiz.quizName
        viewHolder.totalAttempts.text = scoreQuiz.totalAttempts.toString()
        val roundedAverageGrade = BigDecimal(scoreQuiz.averageGrade!!).setScale(2, RoundingMode.HALF_EVEN)
        viewHolder.averageGrade.text = String.format(roundedAverageGrade.toString())

        if (scoreQuiz.completed) {
            val colorInt = ContextCompat.getColor(context, R.color.complete)
            viewHolder.noteLayout.backgroundTintList = ColorStateList.valueOf(colorInt)
        }

        viewHolder.itemView.setOnClickListener {
            onClickCallback!!.startNewActivity(
                scoreQuiz.quizID.toString(),
                scoreQuiz.quizName.toString(),
                scoreQuiz.totalAttempts.toString()
            )
        }
    }

    /**
     * Clase anidada para el ViewHolder de las vistas de los resultados
     * Permite vincular los datos del resultado con su vista
     * @param view
     * @return RecyclerView.ViewHolder(view)
     */
    class RecyclerViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val quizName: TextView = view.findViewById(R.id.quizName)
        val totalAttempts: TextView = view.findViewById(R.id.totalAttempts)
        val averageGrade: TextView = view.findViewById(R.id.averageGrade)
        val noteLayout : LinearLayout = view.findViewById(R.id.noteLayout)
    }

    interface OnClickCallback {
        fun startNewActivity(quizID: String, quizName: String, totalAttempts: String)
    }
}