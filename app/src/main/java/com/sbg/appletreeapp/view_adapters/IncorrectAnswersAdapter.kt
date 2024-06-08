package com.sbg.appletreeapp.view_adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.sbg.appletreeapp.R


class IncorrectAnswersAdapter(private val incorrectAnswersList: List<*>) :
    RecyclerView.Adapter<IncorrectAnswersAdapter.RecyclerViewHolder>() {

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): RecyclerViewHolder {
        val itemView: View = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.simple_textview_model, viewGroup, false)

        return RecyclerViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return incorrectAnswersList.size
    }

    override fun onBindViewHolder(viewHolder: RecyclerViewHolder, position: Int) {
        // asigna el valor para el textview del R.layout.simple_textview_model
        viewHolder.textView.text = incorrectAnswersList[position].toString()
    }

    class RecyclerViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        // textview del R.layout.simple_textview_model
        val textView: TextView = view.findViewById(R.id.textView)
    }

}