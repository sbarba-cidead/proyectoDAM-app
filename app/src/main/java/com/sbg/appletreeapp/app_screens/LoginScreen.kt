package com.sbg.appletreeapp.app_screens

import android.app.AlertDialog
import android.os.Bundle
import android.text.InputType
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.view.KeyEvent
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.sbg.appletreeapp.R
import com.sbg.appletreeapp.utils.Transitions
import com.sbg.appletreeapp.utils.Utils


class LoginScreen : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    private lateinit var userEmailEditText: EditText
    private lateinit var userPasswordEditText: EditText
    private lateinit var passwordVisibilityButton: ImageButton
    private lateinit var resetPasswordLink: TextView
    private lateinit var loginButton: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var registerLink: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login_screen)

        // Inicia la autentificación con firebase
        auth = Firebase.auth

        // Enlaza las variables con los view de la interfaz
        userEmailEditText = findViewById(R.id.userEmailEditText)
        userPasswordEditText = findViewById(R.id.userPasswordEditText)
        passwordVisibilityButton = findViewById(R.id.passwordVisibilityButton)
        resetPasswordLink = findViewById(R.id.resetPasswordLink)
        loginButton = findViewById(R.id.loginButton)
        progressBar = findViewById(R.id.progressBar)
        registerLink = findViewById(R.id.registerLink)
        resetPasswordLink.paint?.isUnderlineText = true //texto subrayado
        registerLink.paint?.isUnderlineText = true //texto subrayado

        // Comportamiento del botón de visibilidad de la contraseña
        passwordVisibilityButton.setOnClickListener {
            if (userPasswordEditText.transformationMethod == PasswordTransformationMethod.getInstance()) {
                // Si la contraseña no es visible la muestra y cambia el icono del botón
                userPasswordEditText.transformationMethod =
                    HideReturnsTransformationMethod.getInstance()
                passwordVisibilityButton.setImageResource(R.drawable.login_password_visible_button)
            } else {
                // Si la contraseña es visible la oculta y cambia el icono del botón
                userPasswordEditText.transformationMethod =
                    PasswordTransformationMethod.getInstance()
                passwordVisibilityButton.setImageResource(R.drawable.login_password_notvisible_button)
            }
        }

        // Comportamiento del botón de reinicio de contraseña
        resetPasswordLink.setOnClickListener {
            val textEditText = EditText(this)
            textEditText.inputType = InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS

            val dialog: AlertDialog = AlertDialog.Builder(this)
                .setMessage(R.string.passwordRecoveryDialog)
                .setPositiveButton(
                    R.string.acceptButton,
                    null
                ) //listener es null porque será sobrescrito
                .setNegativeButton(
                    R.string.cancelButton,
                    null
                ) //listener es null porque será sobrescrito
                .setView(textEditText)
                .show()

            /* Nota: sobreescribir el listener permite que el
            diálogo solo se cierre si se llama a dismiss */

            // Botón de aceptar
            val positiveButton: Button = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            positiveButton.setOnClickListener {

                val email = textEditText.text.toString()

                // Comprueba si se ha introducido un email con formato válido
                if (Utils.validateEmail(email)) {
                    auth.sendPasswordResetEmail(email)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                Toast.makeText(
                                    this, R.string.recoveryEmail,
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                Toast.makeText(
                                    this, R.string.passwordRecoveryError, Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    dialog.dismiss()
                } else {
                    textEditText.error = getString(R.string.invalidEmailError)
                }
            }

            // Botón de cancelar
            val negativeButton: Button = dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
            negativeButton.setOnClickListener {
                Toast.makeText(this, R.string.passwordRecoveryCancelled, Toast.LENGTH_SHORT)
                    .show()
                dialog.dismiss()
            }
        }

        // Comportamiento del botón de registro de usuario
        registerLink.setOnClickListener {
            userEmailEditText.text.clear()
            userPasswordEditText.text.clear()

            Transitions.goToRegisterScreen(this)
        }

        // permite iniciar sesión pulsando intro en el teclado
        userEmailEditText.setOnKeyListener { view, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_ENTER) {
                if (event.action == KeyEvent.ACTION_DOWN) {
                    loginUser()
                }
                return@setOnKeyListener true
            }
            return@setOnKeyListener false
        }

        // permite iniciar sesión pulsando intro en el teclado
        userPasswordEditText.setOnKeyListener { view, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_ENTER) {
                if (event.action == KeyEvent.ACTION_DOWN) {
                    loginUser()
                }
                return@setOnKeyListener true
            }
            return@setOnKeyListener false
        }

        // Comportamiento del botón de inicio de sesión
        loginButton.setOnClickListener {
            loginUser()
        }
    }

    /**
     * Inicia sesión en la base de datos de firebase con el email y contraseña dados
     */
    private fun loginUser() {
        val userEmail = userEmailEditText.text.toString()
        val userPassword = userPasswordEditText.text.toString()

        // Antes de iniciar sesión, comprueba que los datos son correctos
        if (userEmail.isEmpty()) {
            userEmailEditText.error = getString(R.string.userError)
        } else if (userPassword.isEmpty()) {
            userPasswordEditText.error = getString(R.string.passwordError)
        } else {
            // Muestra la barra de progreso
            Utils.isProcessing(true, loginButton, progressBar)

            // Inicia sesión con el usuario
            auth.signInWithEmailAndPassword(userEmail, userPassword).addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {

                    // Este bloque puede utilizarse para comprobar la verificación de email
                    //                    if(!auth.currentUser!!.isEmailVerified){
                    //                        Toast.makeText(this, "Email no verificado.", Toast.LENGTH_SHORT
                    //                        ).show()
                    //                    }

                    Toast.makeText(this, R.string.correctLogin, Toast.LENGTH_SHORT
                    ).show()

                    // Limpia los campos del formulario
                    userEmailEditText.text.clear()
                    userPasswordEditText.text.clear()

                    // Oculta la barra de progreso
                    Utils.isProcessing(false, loginButton, progressBar)

                    // Va a la actividad principal de la app (MenuScreen)
                    Transitions.goToMainMenuScreen(this)
                } else {
                    Toast.makeText(this, R.string.errorLogin, Toast.LENGTH_SHORT
                    ).show()

                    // Oculta la barra de progreso
                    Utils.isProcessing(false, loginButton, progressBar)
                }
            }
        }
    }
}
