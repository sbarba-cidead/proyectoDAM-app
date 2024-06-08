package com.sbg.appletreeapp.view_adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.sbg.appletreeapp.R
import com.sbg.appletreeapp.models.ChatMessageModel
import com.sbg.appletreeapp.utils.FirebaseQueries
import com.sbg.appletreeapp.utils.Utils

class MessagesAdapter(val context: Context, options: FirestoreRecyclerOptions<ChatMessageModel>) :
    FirestoreRecyclerAdapter<ChatMessageModel, MessagesAdapter.RecyclerViewHolder>(options) {

    /**
     * Se ejecuta al crear el ViewHolder.
     * Crea el ViewHolder (wrapper) y la vista a la que se asocia (messages_chatmessage_model)
     * @param viewGroup
     * @param viewType
     * @return RecyclerViewHolder
     */
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int) : RecyclerViewHolder {
        val view = LayoutInflater.from(viewGroup.context).inflate(R.layout.messages_chatmessage_model, viewGroup, false)
        return RecyclerViewHolder(view)
    }

    /**
     * Vincula la vista (messages_chatmessage_model) con los datos
     * @param viewHolder
     * @param position
     * @param message
     */
    override fun onBindViewHolder(viewHolder: RecyclerViewHolder, position: Int, message: ChatMessageModel) {
        if(message.senderId.equals(FirebaseQueries.getCurrentUser()!!.uid)){
            // si el mensaje es enviado por el emisor
            viewHolder.receiverLayout.visibility = View.GONE

            viewHolder.senderLayout.visibility = View.VISIBLE
            viewHolder.senderTextView.text = message.message
            viewHolder.senderDate.text = Utils.convertTimestampToDateString(message.timestamp!!)
        } else {
            // si el mensaje es enviado por el receptor
            viewHolder.senderLayout.visibility = View.GONE

            viewHolder.receiverLayout.visibility = View.VISIBLE
            viewHolder.receiverTextView.text = message.message
            viewHolder.receiverDate.text = Utils.convertTimestampToDateString(message.timestamp!!)
        }
    }

    /**
     * Clase anidada para el ViewHolder de las vistas de los mensajes
     * Permite vincular los datos del mensaje con su vista
     * @param view
     * @return RecyclerView.ViewHolder(view)
     */
    class RecyclerViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val senderLayout: LinearLayout = view.findViewById(R.id.senderLayout)
        val receiverLayout: LinearLayout = view.findViewById(R.id.receiverLayout)
        val senderTextView: TextView = view.findViewById(R.id.senderTextView)
        val receiverTextView: TextView = view.findViewById(R.id.receiverTextView)
        val senderDate: TextView = view.findViewById(R.id.senderDate)
        val receiverDate: TextView = view.findViewById(R.id.receiverDate)
    }

}