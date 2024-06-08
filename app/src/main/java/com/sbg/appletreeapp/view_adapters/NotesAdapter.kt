package com.sbg.appletreeapp.view_adapters

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.sbg.appletreeapp.R
import com.sbg.appletreeapp.app_screens.secondary_screens.CreateEditNoteScreen
import com.sbg.appletreeapp.models.NoteDataModel
import com.sbg.appletreeapp.utils.FirebaseQueries
import com.sbg.appletreeapp.utils.Utils


class NotesAdapter(val context: Context, options: FirestoreRecyclerOptions<NoteDataModel>) :
    FirestoreRecyclerAdapter<NoteDataModel, NotesAdapter.RecyclerViewHolder>(options) {

    var deleteCallback: DeleteCallback? = null

    /**
     * Se ejecuta al crear el ViewHolder.
     * Crea el ViewHolder (wrapper) y la vista a la que se asocia (notes_note_model)
     * @param viewGroup
     * @param viewType
     * @return RecyclerViewHolder
     */
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): RecyclerViewHolder {
        val view = LayoutInflater.from(viewGroup.context).inflate(R.layout.notes_note_model, viewGroup, false)
        return RecyclerViewHolder(view)
    }

    /**
     * Vincula la vista (notes_note_model) con los datos
     * @param viewHolder
     * @param position
     * @param note
     */
    override fun onBindViewHolder(viewHolder: RecyclerViewHolder, position: Int, note: NoteDataModel) {
        viewHolder.noteTitle.text = note.noteTitle
        viewHolder.noteContent.text = note.noteContent
        viewHolder.noteLayout.backgroundTintList = Utils.convertHexStringToColorStateList(note.noteColor!!)

        // Al pulsar en una nota pasa a la actividad CreateEditNoteScreen
        // y envía los datos de la nota
        viewHolder.itemView.setOnClickListener {
            val intent = Intent(context, CreateEditNoteScreen::class.java)
            intent.putExtra("noteTitle", note.noteTitle)
            intent.putExtra("noteContent", note.noteContent)
            intent.putExtra("noteID", note.noteID)
            intent.putExtra("noteTimestamp", Utils.convertTimestampToDateString(note.noteTimestamp!!))
            intent.putExtra("noteColor", note.noteColor)

            context.startActivity(intent)
        }

        // Al dejar pulsada una nota
        viewHolder.itemView.setOnLongClickListener {
            val context: Context = viewHolder.itemView.context

            val alertDialog = AlertDialog.Builder(context)
            alertDialog.setMessage(R.string.eraseNoteDialog)
            alertDialog.setPositiveButton(R.string.eraseButton) { dialog, i ->
                FirebaseQueries.getCollectionReferenceForNotes().document(note.noteID!!)
                    .delete().addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            deleteCallback!!.checkNotesCount()

                            Toast.makeText(context, R.string.eraseNoteSuccess, Toast.LENGTH_SHORT)
                                .show()
                        } else {
                            Toast.makeText(context, R.string.eraseNoteError, Toast.LENGTH_SHORT)
                                .show()
                        }

                        dialog.dismiss()
                    }
            }
            alertDialog.setNegativeButton(R.string.cancelButton) { dialog, i ->
                Toast.makeText(context, R.string.eraseNoteCancelled, Toast.LENGTH_SHORT).show()

                dialog.dismiss()
            }
            alertDialog.show()
            false
        }
    }

    /**
     * Clase anidada para el ViewHolder de las vistas de las notas
     * Permite vincular los datos de la nota con su vista
     * @param view
     * @return RecyclerView.ViewHolder(view)
     */
    class RecyclerViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val noteTitle: TextView = view.findViewById(R.id.noteTitle)
        val noteContent: TextView = view.findViewById(R.id.noteContent)
        val noteLayout: LinearLayout = view.findViewById(R.id.noteLayout)
    }

    interface DeleteCallback {
        fun checkNotesCount()
    }
}