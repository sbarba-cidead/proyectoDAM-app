package com.sbg.appletreeapp.app_screens.secondary_screens

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentReference
import com.sbg.appletreeapp.R
import com.sbg.appletreeapp.models.NoteDataModel
import com.sbg.appletreeapp.utils.FirebaseQueries
import com.sbg.appletreeapp.utils.Utils


class CreateEditNoteScreen : AppCompatActivity() {

    private lateinit var mainLayout: LinearLayout
    private lateinit var closeButton: ImageView
    private lateinit var saveButton: ImageView
    private lateinit var colorButton: ImageView
    private lateinit var colorButton1: ImageView
    private lateinit var colorButton2: ImageView
    private lateinit var colorButton3: ImageView
    private lateinit var colorButton4: ImageView
    private lateinit var chooseColorsLayout: LinearLayout
    private lateinit var titleEditText: EditText
    private lateinit var contentEditText: EditText
    private lateinit var timestampTextView: TextView

    private var editMode = false
    private var noteID: String? = null

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.notes_create_edit_note_screen)

        mainLayout = findViewById(R.id.mainLayout)
        closeButton = findViewById(R.id.closeButton)
        saveButton = findViewById(R.id.saveButton)
        colorButton = findViewById(R.id.colorButton)
        colorButton1 = findViewById(R.id.colorButton1)
        colorButton2 = findViewById(R.id.colorButton2)
        colorButton3 = findViewById(R.id.colorButton3)
        colorButton4 = findViewById(R.id.colorButton4)
        chooseColorsLayout = findViewById(R.id.chooseColorsLayout)
        titleEditText = findViewById(R.id.titleEditText)
        contentEditText = findViewById(R.id.contentEditText)
        timestampTextView = findViewById(R.id.timestampTextView)

        // muestra el teclado con la primera letra en mayúscula
        // la información debe guardarse con la primera letra en mayúscula en firebase
        // ya que el searchquery de firebase es sensible a mayúsculas
        titleEditText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
        contentEditText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_SENTENCES

        val note = NoteDataModel.createNote()


        noteID = intent.getStringExtra("noteID").toString()
        if (noteID != "null"){ editMode = true }

        // Recibe datos de la nota si se está editando
        // Los datos son enviados por la nota pulsada en el recyclerview
        if (editMode) {
            titleEditText.setText(intent.getStringExtra("noteTitle"))
            contentEditText.setText(intent.getStringExtra("noteContent"))
            colorButton.imageTintList =
                Utils.convertHexStringToColorStateList(intent.getStringExtra("noteColor")!!)
            timestampTextView.text = intent.getStringExtra("noteTimestamp")
        }

        // Icono que muestra el color actual de la nota y al pulsar
        // permite ver los otros colores disponibles para cambiar
        colorButton.setOnClickListener{
            // Muestra todos los view con otros colores para las notas
            chooseColorsLayout.visibility = View.VISIBLE
            colorButton.visibility = View.GONE
        }

        // Color opción 1
        colorButton1.setOnClickListener {
            // Oculta todos los view con otros colores para las notas
            chooseColorsLayout.visibility = View.GONE
            colorButton.visibility = View.VISIBLE

            // Cambia el color del icono de color de la nota por el color elegido
            colorButton.imageTintList = colorButton1.imageTintList
        }

        // Color opción 2
        colorButton2.setOnClickListener {
            // Oculta todos los view con otros colores para las notas
            chooseColorsLayout.visibility = View.GONE
            colorButton.visibility = View.VISIBLE

            // Cambia el color del icono de color de la nota por el color elegido
            colorButton.imageTintList = colorButton2.imageTintList
        }

        // Color opción 3
        colorButton3.setOnClickListener {
            // Oculta todos los view con otros colores para las notas
            chooseColorsLayout.visibility = View.GONE
            colorButton.visibility = View.VISIBLE

            // Cambia el color del icono de color de la nota por el color elegido
            colorButton.imageTintList = colorButton3.imageTintList
        }

        // Color opción 4
        colorButton4.setOnClickListener {
            // Oculta todos los view con otros colores para las notas
            chooseColorsLayout.visibility = View.GONE
            colorButton.visibility = View.VISIBLE

            // Cambia el color del icono de color de la nota por el color elegido
            colorButton.imageTintList = colorButton4.imageTintList
        }

        // Guarda la nota en firebase con los datos introducidos
        saveButton.setOnClickListener {
            (getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager)
                .hideSoftInputFromWindow(mainLayout.windowToken, 0)

            note.noteTitle = titleEditText.text.toString()
            note.noteContent = contentEditText.text.toString()
            note.noteColor = Utils.convertColorStateListToHexString(colorButton.imageTintList!!)
            note.noteTimestamp = Timestamp.now()
            val documentReference = saveNote(note)
            documentReference.update(mapOf("noteID" to documentReference.id))

            // Actualiza la fecha de último guardado
            runOnUiThread { // permite actualizar el valor del textview en tiempo real
                timestampTextView.text = Utils.convertTimestampToDateString(note.noteTimestamp!!)
            }
        }

        //Cierra la creación o edición de nota
        closeButton.setOnClickListener {
            titleEditText.clearFocus()
            contentEditText.clearFocus()
            (getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager)
                .hideSoftInputFromWindow(mainLayout.windowToken, 0)

            closeNote()
        }
    }

    /**
     * Finaliza la actividad actual
     */
    private fun closeNote() {
        this.finish()
    }

    /**
     * Guarda los datos del objeto nota en Firebase
     * Devuelve la referencia del documento guardado
     *
     * @param note
     * @return DocumentReference
     */
    private fun saveNote(note: NoteDataModel) : DocumentReference {
        return if (editMode) {
            val documentReference = FirebaseQueries.getCollectionReferenceForNotes()
                .document(noteID!!)
            documentReference.set(note).addOnCompleteListener { editTask ->
                if (editTask.isSuccessful){
                    Toast.makeText(this, R.string.saveNote,
                        Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this,
                        R.string.saveNoteError,
                        Toast.LENGTH_SHORT).show()
                }
            }
            documentReference
        } else {
            val documentReference = FirebaseQueries.getCollectionReferenceForNotes()
                .document()
            documentReference.set(note).addOnCompleteListener { saveTask ->
                if (saveTask.isSuccessful){
                    Toast.makeText(this, R.string.saveNote,
                        Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this,
                        R.string.saveNoteError,
                        Toast.LENGTH_SHORT).show()
                }
            }
            documentReference
        }
    }
}