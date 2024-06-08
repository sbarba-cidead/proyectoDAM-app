package com.sbg.appletreeapp.models

import com.google.firebase.Timestamp

/**
 * Clase base para crear un objeto note con datos sobre la nota
 * Se instancia en NotesScreen
 */
class NoteDataModel(
    var noteID: String? = null,
    var noteTitle: String? = null,
    var noteContent: String? = null,
    var noteColor: String? = null,
    var noteTimestamp: Timestamp? = null
) {

    //for initialization of arguments
    init {

    }

    companion object Factory {
        fun createNote(): NoteDataModel = NoteDataModel()
    }

}