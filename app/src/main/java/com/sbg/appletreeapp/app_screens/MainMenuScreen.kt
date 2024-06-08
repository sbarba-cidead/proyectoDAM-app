package com.sbg.appletreeapp.app_screens

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.cardview.widget.CardView
import androidx.core.os.LocaleListCompat
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.sbg.appletreeapp.R
import com.sbg.appletreeapp.utils.FirebaseQueries
import com.sbg.appletreeapp.utils.Transitions
import com.squareup.picasso.Picasso
import java.util.Locale


class MainMenuScreen : AppCompatActivity() {

    private lateinit var userimage: ImageView
    private lateinit var userNameTextView: TextView
    private lateinit var userConfigButton: LinearLayout
    private lateinit var logoutButton: ImageView
    private lateinit var goToQuizesButton: Button
    private lateinit var goToNotesButton: Button
    private lateinit var goToTasksButton: Button
    private lateinit var goToMessagesButton: Button
    private lateinit var goToScoreButton: ImageView
    private lateinit var goToLanguageButton: ImageView
    private lateinit var languageESButton: ImageView
    private lateinit var languageENButton: ImageView
    private lateinit var chooseLanguageLayout: LinearLayout
    private lateinit var userimageProgressBar: ProgressBar
    private lateinit var cardView: CardView

    private var loginUser: FirebaseUser? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.mainmenu_screen)

        userimage = findViewById(R.id.userimage)
        userNameTextView = findViewById(R.id.userNameTextView)
        userConfigButton = findViewById(R.id.userConfigButton)
        logoutButton = findViewById(R.id.logoutButton)
        goToQuizesButton = findViewById(R.id.goToQuizesButton)
        goToNotesButton = findViewById(R.id.goToNotesButton)
        goToTasksButton = findViewById(R.id.goToTasksButton)
        goToMessagesButton = findViewById(R.id.goToMessagesButton)
        goToScoreButton = findViewById(R.id.goToScoreButton)
        goToLanguageButton = findViewById(R.id.goToLanguageButton)
        languageESButton = findViewById(R.id.languageESButton)
        languageENButton = findViewById(R.id.languageENButton)
        chooseLanguageLayout = findViewById(R.id.chooseLanguageLayout)
        userimageProgressBar = findViewById(R.id.userimageProgressBar)
        cardView = findViewById(R.id.cardView)

        loginUser = FirebaseQueries.getCurrentUser()

        //Comprueba si hay un usuario con sesión iniciada
        if (loginUser == null) {
            Transitions.goToLoginScreen(this)
        } else {
            //Muestra la imagen del usuario en la parte superior
            retrieveUserimage()

            //Muestra el nombre de usuario en la parte superior
            retrieveUsername()

            //Funcionamiento del botón de configurar usuario
            userConfigButton.setOnClickListener{ Transitions.goToUserConfigScreen(this) }
            //Funcionamiento del botón de cerrar sesión
            logoutButton.setOnClickListener{
                FirebaseQueries.sigoutUser(this)
                Transitions.goToLoginScreen(this)}
            //Funcionamiento del botón de tests
            goToQuizesButton.setOnClickListener{ Transitions.goToQuizesScreen(this)}
            //Funcionamiento del botón de notas
            goToNotesButton.setOnClickListener{ Transitions.goToNotesScreen(this) }
            //Funcionamiento del botón de tareas
            goToTasksButton.setOnClickListener{ Transitions.goToTasksScreen(this) }
            //Funcionamiento del botón de mensajes
            goToMessagesButton.setOnClickListener{ Transitions.goToMessagesScreen(this) }
            //Funcionamiento del botón de logros
            goToScoreButton.setOnClickListener{ Transitions.goToScoreScreen(this) }
            //Funcionamiento del botón de idioma
            goToLanguageButton.setOnClickListener {
                // muestra opciones de idioma
                if (chooseLanguageLayout.visibility == View.GONE) {
                    chooseLanguageLayout.visibility = View.VISIBLE
                } else {
                    chooseLanguageLayout.visibility = View.GONE
                }
            }
            //Botón idioma español
            languageESButton.setOnClickListener {
                AppCompatDelegate.setApplicationLocales(
                    LocaleListCompat.create(Locale.forLanguageTag("es"))
                )
            }
            //Botón idioma inglés
            languageENButton.setOnClickListener {
                AppCompatDelegate.setApplicationLocales(
                    LocaleListCompat.create(Locale.forLanguageTag("en"))
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()

        retrieveUsername()
        retrieveUserimage()
    }

    /**
     * Actualiza el nombre de usuario en el textview desde firebase
     */
    private fun retrieveUsername() {
        FirebaseQueries.getDocumentReferenceForUserData()
            .get().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    userNameTextView.text = task.result.get("username") as String
                } else {
                    Toast.makeText(this,
                        R.string.newUsernameError,
                        Toast.LENGTH_SHORT).show()
                }
            }
    }

    /**
     * Actualiza la imagen de usuario en el imageview desde firebase
     */
    private fun retrieveUserimage() {
        // Muestra la barra de progreso
        userimageProgressBar.visibility = View.VISIBLE
        cardView.visibility = View.INVISIBLE
        userimage.visibility = View.INVISIBLE

        // comprueba si la imagen de usuario ya ha sido establecida
        FirebaseQueries.getDocumentReferenceForUserData()
            .get().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    if (task.result.get("userimageSet") as Boolean) {
                        val firebaseStoragePath = "usersimages" + "/" +
                                FirebaseQueries.getCurrentUser()!!.uid + "/" +
                                "userimage"
                        val ref = Firebase.storage.reference.child(firebaseStoragePath)
                        ref.downloadUrl.addOnCompleteListener { downloadTask ->
                            if (downloadTask.isSuccessful) {
                                val imageUri = downloadTask.result

                                // Cambia la imagen en el imageview
                                Picasso.get().load(imageUri).into(userimage)
                            } else {
                                Toast.makeText(this,
                                    R.string.newUserimageError,
                                    Toast.LENGTH_SHORT).show()
                            }

                            // Oculta la barra de progreso
                            userimageProgressBar.visibility = View.GONE
                            userimage.visibility = View.VISIBLE
                            cardView.visibility = View.VISIBLE
                        }
                    } else {
                        // imagen por defecto para el usuario
                        Picasso.get().load(R.drawable.userimagedefault).into(userimage)

                        // Oculta la barra de progreso
                        userimageProgressBar.visibility = View.GONE
                        userimage.visibility = View.VISIBLE
                        cardView.visibility = View.VISIBLE
                    }
                }
            }
    }
}