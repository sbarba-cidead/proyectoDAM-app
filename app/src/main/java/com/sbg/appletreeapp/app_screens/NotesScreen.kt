package com.sbg.appletreeapp.app_screens

import android.os.Bundle
import android.text.InputType
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.firestore.AggregateSource
import com.google.firebase.firestore.Query
import com.sbg.appletreeapp.R
import com.sbg.appletreeapp.models.NoteDataModel
import com.sbg.appletreeapp.utils.FirebaseQueries
import com.sbg.appletreeapp.utils.Transitions
import com.sbg.appletreeapp.view_adapters.NotesAdapter


class NotesScreen : AppCompatActivity(), NotesAdapter.DeleteCallback {

    private lateinit var notesAdapter: NotesAdapter
    private lateinit var searchView: SearchView
    private lateinit var titleTextView: TextView
    private lateinit var addButton: FloatingActionButton
    private lateinit var notesRecyclerView: RecyclerView
    private lateinit var nonotesTextView: TextView
    private lateinit var progressBar: ProgressBar

    private var searchRecheck = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.notes_screen)

        //Enlaza los elementos del layout con variables
        searchView = findViewById(R.id.searchView)
        titleTextView = findViewById(R.id.titleTextView)
        addButton = findViewById(R.id.addButton)
        notesRecyclerView = findViewById(R.id.notesRecyclerView)
        nonotesTextView = findViewById(R.id.nonotesTextView)
        progressBar = findViewById(R.id.progressBar)

        //Funcionamiento del botón de añadir nota
        addButton.setOnClickListener{ createNote() }

        // shows progressbar
        progressBar.visibility = View.VISIBLE

        //Actualiza las notas desde Firebase
        retrieveNotes()

        // SEARCH OPTION //
        searchView.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_SENTENCES

        searchView.setOnSearchClickListener {
            titleTextView.visibility = View.INVISIBLE
            addButton.visibility = View.INVISIBLE

            searchNotes("")
            searchRecheck = true
        }

        val seachViewCloseButton: View =
            searchView.findViewById(androidx.appcompat.R.id.search_close_btn)
        seachViewCloseButton.setOnClickListener {
            // closes search bar
            searchView.isIconified = true
        }

        searchView.setOnCloseListener {
            nonotesTextView.text = getString(R.string.nonotes_text)

            titleTextView.visibility = View.VISIBLE
            addButton.visibility = View.VISIBLE

            searchRecheck = true

            false
        }

        var oldQuery = ""
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                searchNotes(query!!)
                oldQuery = ""

                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText!!.isEmpty()) { searchRecheck = true } // si es una nueva búsqueda
                if (newText.length < oldQuery.length) { searchRecheck = true } // si se borraron caracteres
                oldQuery = newText
                searchNotes(newText)

                return true
            }
        })
    }

    override fun onResume(){
        super.onResume()

        retrieveNotes()

        checkNotesCount()
    }

    /**
     * Actualiza las notas desde firebase y las muestra en el recyclerview
     */
    private fun retrieveNotes() {
        val query: Query = FirebaseQueries.getCollectionReferenceForNotes()
            .orderBy("noteTimestamp", Query.Direction.DESCENDING)

        val countQuery = query.count()
        countQuery.get(AggregateSource.SERVER).addOnCompleteListener { countTask ->
            if (countTask.isSuccessful) {
                val snapshot = countTask.result

                if (snapshot.count > 0) {
                    val options: FirestoreRecyclerOptions<NoteDataModel> =
                        FirestoreRecyclerOptions.Builder<NoteDataModel>()
                            .setQuery(query, NoteDataModel::class.java).build()

                    notesRecyclerView.layoutManager = StaggeredGridLayoutManager(
                        2,
                        GridLayoutManager.VERTICAL
                    )

                    notesAdapter = NotesAdapter(this, options)
                    notesAdapter.deleteCallback = this
                    notesRecyclerView.adapter = notesAdapter

                    notesAdapter.startListening()

                    // hides progressbar
                    progressBar.visibility = View.GONE
                } else {
                    // hides recyclerview
                    notesRecyclerView.visibility = View.VISIBLE

                    // shows textview message
                    nonotesTextView.visibility = View.VISIBLE

                    // hides progressbar
                    progressBar.visibility = View.GONE
                }
            }
        }
    }

    override fun checkNotesCount() {
        // cuenta el número de notas
        FirebaseQueries.getCollectionReferenceForNotes()
            .count()
            .get(AggregateSource.SERVER)
            .addOnCompleteListener { countTask ->
                if (countTask.isSuccessful) {
                    val snapshot = countTask.result

                    if (snapshot.count < 1) { // si no hay notas
                        // hides recyclerview
                        notesRecyclerView.visibility = View.INVISIBLE

                        // shows textview message
                        nonotesTextView.visibility = View.VISIBLE
                    } else { // si hay notas
                        // shows recyclerview
                        notesRecyclerView.visibility = View.VISIBLE

                        // hides textview message
                        nonotesTextView.visibility = View.GONE
                    }
                }
            }
    }

    /**
     * Busca el texto en la consulta de notas en firebase
     * @param text
     */
    private fun searchNotes(text: String) {
        // solo hace la búsqueda si es una nueva búsqueda o se ha borrado el último caracter
        if (searchRecheck) {
            nonotesTextView.visibility = View.GONE
            progressBar.visibility = View.VISIBLE

            val query: Query = FirebaseQueries.getCollectionReferenceForNotes()
                .orderBy("noteTitle").startAt(text).endAt(text+"\uf8ff")

            val countQuery = query.count()
            countQuery.get(AggregateSource.SERVER).addOnCompleteListener { countTask ->
                if (countTask.isSuccessful) {
                    val snapshot = countTask.result

                    if (snapshot.count > 0) {
                        notesRecyclerView.visibility = View.VISIBLE

                        val options: FirestoreRecyclerOptions<NoteDataModel> =
                            FirestoreRecyclerOptions.Builder<NoteDataModel>()
                                .setQuery(query, NoteDataModel::class.java).build()

                        notesRecyclerView.layoutManager = StaggeredGridLayoutManager(
                            2,
                            GridLayoutManager.VERTICAL
                        )

                        notesAdapter = NotesAdapter(this, options)
                        notesAdapter.deleteCallback = this
                        notesRecyclerView.adapter = notesAdapter

                        notesAdapter.startListening()

                        progressBar.visibility = View.GONE
                    } else {
                        // Si no hay coincidencias, se desactiva la búsqueda en firebase
                        // hasta que se borre la búsqueda completa o el último caracter.
                        // Ahorra búsquedas en firebase porque ya se sabe que no habrá resultados
                        searchRecheck = false

                        notesRecyclerView.visibility = View.INVISIBLE
                        // shows textview message
                        nonotesTextView.text = getString(R.string.notFound)
                        nonotesTextView.visibility = View.VISIBLE

                        progressBar.visibility = View.GONE
                    }
                }
            }
        }
    }

    /**
     * Cambia a la actividad CreateNote para crear una nota
     */
    private fun createNote(){
        Transitions.goToCreateEditNote(this)
    }
}