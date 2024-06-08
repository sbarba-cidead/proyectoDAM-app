package com.sbg.appletreeapp.view_adapters

import android.content.Context
import android.content.Intent
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
import com.sbg.appletreeapp.app_screens.secondary_screens.QuizQuestionScreen
import com.sbg.appletreeapp.models.QuizDataModel
import com.sbg.appletreeapp.utils.Utils


class QuizzesAdapter(val context: Context, options: FirestoreRecyclerOptions<QuizDataModel>) :
    FirestoreRecyclerAdapter<QuizDataModel, QuizzesAdapter.RecyclerViewHolder>(options) {

    /**
     * Se ejecuta al crear el ViewHolder.
     * Crea el ViewHolder (wrapper) y la vista a la que se asocia (quizzes_quiz_model)
     * @param viewGroup
     * @param viewType
     * @return RecyclerViewHolder
     */
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): RecyclerViewHolder {
        val view = LayoutInflater.from(viewGroup.context).inflate(R.layout.quizzes_quiz_model, viewGroup, false)
        return RecyclerViewHolder(view)
    }

    /**
     * Vincula la vista (quizzes_quiz_model) con los datos
     * @param viewHolder
     * @param position
     * @param note
     */
    override fun onBindViewHolder(viewHolder: RecyclerViewHolder, position: Int, quiz: QuizDataModel) {
        viewHolder.quizName.text = quiz.quizName

        if (quiz.completed) {
            val colorInt = ContextCompat.getColor(context, R.color.complete)
            viewHolder.quizLayout.backgroundTintList = ColorStateList.valueOf(colorInt)
        }

        // Al pulsar en un test pasa a la actividad QuizQuestionScreen
        // y envía los datos del test
        viewHolder.itemView.setOnClickListener {
            val intent = Intent(context, QuizQuestionScreen::class.java)
            intent.putExtra("quizID", quiz.quizID)
            intent.putExtra("quizName", quiz.quizName)
            intent.putExtra("subject", quiz.subject)
            intent.putExtra("createdDate", Utils.convertTimestampToDateString(quiz.createdDate!!))

            context.startActivity(intent)
        }
    }

    /**
     * Clase anidada para el ViewHolder de las vistas de los tests
     * Permite vincular los datos del test con su vista
     * @param view
     * @return RecyclerView.ViewHolder(view)
     */
    class RecyclerViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val quizName: TextView = view.findViewById(R.id.quizName)
        val quizLayout: LinearLayout = view.findViewById(R.id.quizLayout)
    }

}