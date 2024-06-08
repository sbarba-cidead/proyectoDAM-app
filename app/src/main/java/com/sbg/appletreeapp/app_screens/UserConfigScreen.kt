package com.sbg.appletreeapp.app_screens

import android.app.AlertDialog
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.ktx.userProfileChangeRequest
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.sbg.appletreeapp.R
import com.sbg.appletreeapp.utils.FirebaseQueries
import com.sbg.appletreeapp.utils.Transitions
import com.sbg.appletreeapp.utils.Utils
import com.squareup.picasso.Picasso


class UserConfigScreen : AppCompatActivity() {

    private lateinit var userimage: ImageView
    private lateinit var userimageProgressBar: ProgressBar

    private lateinit var username: TextView
    private lateinit var email: TextView
    private lateinit var password: TextView
    private lateinit var noVerifiedEmailText: TextView

    private lateinit var usernameEditButton: ImageButton
    private lateinit var emailEditButton: ImageButton
    private lateinit var passwordEditButton: ImageButton

    private lateinit var usernameLayout: LinearLayout
    private lateinit var emailLayout: LinearLayout
    private lateinit var passwordLayout: LinearLayout

    private lateinit var usernameEditLayout: LinearLayout
    private lateinit var emailEditLayout: LinearLayout
    private lateinit var passwordEditLayout: LinearLayout

    private lateinit var userEditText: EditText
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText

    private lateinit var usernameChangeCancelButton: ImageButton
    private lateinit var emailChangeCancelButton: ImageButton
    private lateinit var passwordChangeCancelButton: ImageButton

    private lateinit var usernameChangeAcceptButton: ImageButton
    private lateinit var userProgressBar: ProgressBar
    private lateinit var emailChangeAcceptButton: ImageButton
    private lateinit var emailProgressBar: ProgressBar
    private lateinit var passwordChangeAcceptButton: ImageButton
    private lateinit var passwordProgressBar: ProgressBar

    private lateinit var deleteUserButton: Button
    private lateinit var deleteUserProgressBar: ProgressBar

    private lateinit var passwordVisibilityButton: ImageButton

    private var pickImage = registerForActivityResult(PickVisualMedia()) { imageUri: Uri? ->
        if (imageUri != null) {
            changeUserimage(imageUri)
        } else {
            Toast.makeText(this, R.string.newUserimageCancelled,
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.userconfig_screen)

        ///ENLACE DE VARIABLES CON LA INTERFAZ///
        userimage = findViewById(R.id.userimage)
        userimageProgressBar = findViewById(R.id.userimageProgressBar)

        username = findViewById(R.id.username)
        email = findViewById(R.id.email)
        password = findViewById(R.id.password)
        noVerifiedEmailText = findViewById(R.id.noVerifiedEmailText)

        usernameEditButton = findViewById(R.id.usernameEditButton)
        emailEditButton = findViewById(R.id.emailEditButton)
        passwordEditButton = findViewById(R.id.passwordEditButton)

        usernameLayout = findViewById(R.id.usernameLayout)
        emailLayout = findViewById(R.id.emailLayout)
        passwordLayout = findViewById(R.id.passwordLayout)

        usernameEditLayout = findViewById(R.id.usernameEditLayout)
        emailEditLayout = findViewById(R.id.emailEditLayout)
        passwordEditLayout = findViewById(R.id.passwordEditLayout)

        userEditText = findViewById(R.id.userEditText)
        emailEditText = findViewById(R.id.emailEditText)
        passwordEditText = findViewById(R.id.passwordEditText)

        usernameChangeCancelButton = findViewById(R.id.usernameChangeCancelButton)
        emailChangeCancelButton = findViewById(R.id.emailChangeCancelButton)
        passwordChangeCancelButton = findViewById(R.id.passwordChangeCancelButton)

        usernameChangeAcceptButton = findViewById(R.id.usernameChangeAcceptButton)
        userProgressBar = findViewById(R.id.userProgressBar)
        emailChangeAcceptButton = findViewById(R.id.emailChangeAcceptButton)
        emailProgressBar = findViewById(R.id.emailProgressBar)
        passwordChangeAcceptButton = findViewById(R.id.passwordChangeAcceptButton)
        passwordProgressBar = findViewById(R.id.passwordProgressBar)

        deleteUserButton = findViewById(R.id.deleteUserButton)
        deleteUserProgressBar = findViewById(R.id.deleteUserProgressBar)

        passwordVisibilityButton = findViewById(R.id.passwordVisibilityButton)
        ///FIN DE ENLACE DE VARIABLES CON LA INTERFAZ///

        retrieveUserimage()

        ///FUNCIONAMIENTO DE BOTONES///
        //EDICIÓN DE IMAGEN DE USUARIO//
        userimage.setOnClickListener{
            pickImage.launch(PickVisualMediaRequest.Builder()
                .setMediaType(PickVisualMedia.ImageOnly)
                .build())
        }

        //BOTONES DE EDICIÓN//
        // Funcionamiento botón editar nombre de usuario
        usernameEditButton.setOnClickListener {
            // Muestra el edittext del nombre de usuario
            usernameLayout.visibility = View.GONE
            usernameEditLayout.visibility = View.VISIBLE
            userEditText.requestFocus()
            showKeyboard(userEditText)
        }

        // Funcionamiento botón editar email
        emailEditButton.setOnClickListener {
            // Muestra el edittext del email
            emailLayout.visibility = View.GONE
            emailEditLayout.visibility = View.VISIBLE
            emailEditLayout.requestFocus()
            showKeyboard(emailEditText)
        }

        // Funcionamiento botón editar contraseña
        passwordEditButton.setOnClickListener {
            // Muestra el edittext de la contraseña
            passwordLayout.visibility = View.GONE
            passwordEditLayout.visibility = View.VISIBLE
            passwordEditLayout.requestFocus()
            showKeyboard(passwordEditText)
        }
        //FIN BOTONES DE EDICIÓN//


        //BOTONES DE CANCELAR EDICIÓN//
        // Funcionamiento botón cancelar edición nombre de usuario
        usernameChangeCancelButton.setOnClickListener{
            // Borra el contenido del edittext
            userEditText.text.clear()

            Toast.makeText(this, R.string.newUsernameCancelled,
                Toast.LENGTH_SHORT).show()

            // Oculta el edittext del nombre de usuario
            usernameLayout.visibility = View.VISIBLE
            usernameEditLayout.visibility = View.GONE

            hideKeyboard(userEditText)
        }

        // Funcionamiento botón cancelar edición email
        emailChangeCancelButton.setOnClickListener {
            // Borra el contenido del edittext
            emailEditText.text.clear()

            Toast.makeText(this, R.string.newEmailCancelled,
                Toast.LENGTH_SHORT).show()

            // Oculta el edittext del email
            emailLayout.visibility = View.VISIBLE
            emailEditLayout.visibility = View.GONE

            hideKeyboard(emailEditText)
        }

        // Funcionamiento botón cancelar edición contraseña
        passwordChangeCancelButton.setOnClickListener {
            // Borra el contenido del edittext
            passwordEditText.text.clear()

            Toast.makeText(this, R.string.newPasswordCancelled,
                Toast.LENGTH_SHORT).show()

            // Oculta el edittext de la contraseña
            passwordLayout.visibility = View.VISIBLE
            passwordEditLayout.visibility = View.GONE

            hideKeyboard(passwordEditText)
        }
        //FIN BOTONES DE CANCELAR EDICIÓN//


        //BOTONES DE ACEPTAR EDICIÓN//
        // Funcionamiento botón aceptar edición nombre de usuario
        usernameChangeAcceptButton.setOnClickListener{
            changeUser(userEditText.text.toString())
        }

        // Funcionamiento botón aceptar edición email
        emailChangeAcceptButton.setOnClickListener {
            changePassword(passwordEditText.text.toString())
        }

        // Funcionamiento botón aceptar edición contraseña
        passwordChangeAcceptButton.setOnClickListener {
            changePassword(passwordEditText.text.toString())
        }
        //FIN BOTONES DE ACEPTAR EDICIÓN//

        // ACEPTAR EDITTEXTS CON INTRO EN EL TECLADO //
        // permite aceptar edición de nombre de usuario pulsando intro en el teclado
        userEditText.setOnKeyListener { view, keyCode, event ->
            if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                changeUser(userEditText.text.toString())
            }
            return@setOnKeyListener false
        }

        // permite aceptar edición de email pulsando intro en el teclado
        emailEditText.setOnKeyListener { view, keyCode, event ->
            if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                changeEmail(emailEditText.text.toString())
            }
            return@setOnKeyListener false
        }

        // permite aceptar edición de contraseña pulsando intro en el teclado
        passwordEditText.setOnKeyListener { view, keyCode, event ->
            if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                changePassword(passwordEditText.text.toString())
            }
            return@setOnKeyListener false
        }
        // FIN ACEPTAR EDITTEXTS CON INTRO EN EL TECLADO //


        // Botón para eliminar usuario
        deleteUserButton.setOnClickListener {
            val alertDialog = AlertDialog.Builder(this)
            alertDialog.setMessage(R.string.deleteUserDialog)
            alertDialog.setPositiveButton(R.string.deleteUserButton) { dialog, i ->
                deleteUser()
                dialog.dismiss()
            }
            alertDialog.setNegativeButton(R.string.cancelButton) { dialog, i ->
                Toast.makeText(this, R.string.deleteUserCancelled,
                    Toast.LENGTH_SHORT
                ).show()

                dialog.dismiss()
            }
            alertDialog.show()
        }

        // Comportamiento del botón de visibilidad de la contraseña
        passwordVisibilityButton.setOnClickListener {
            if (passwordEditText.transformationMethod == PasswordTransformationMethod.getInstance()) {
                // Si la contraseña no es visible la muestra y cambia el icono del botón
                passwordEditText.transformationMethod = HideReturnsTransformationMethod.getInstance()
                passwordVisibilityButton.setImageResource(R.drawable.login_password_visible_button)
            } else {
                // Si la contraseña es visible la oculta y cambia el icono del botón
                passwordEditText.transformationMethod = PasswordTransformationMethod.getInstance()
                passwordVisibilityButton.setImageResource(R.drawable.login_password_notvisible_button)
            }
        }
        ///FIN DE FUNCIONAMIENTO DE BOTONES///

        Log.e("hola", FirebaseQueries.getCurrentUser()!!.email.toString())

        // Muestra u oculta el mensaje de verificación de email
        if(checkEmailVerified()) {
            Log.e("hola", "verificado")
            noVerifiedEmailText.setTextColor(Color.TRANSPARENT)
        } else {
            Log.e("hola", "no verificado")
            noVerifiedEmailText.setTextColor(this.getColor(R.color.red))
        }

        // Muestra los datos del usuario en los TextViews
        retrieveUserdata()

    }

    override fun onStart() {
        super.onStart()

        Log.e("hola", FirebaseQueries.getCurrentUser()!!.email.toString())

        // Muestra u oculta el mensaje de verificación de email
        if(checkEmailVerified()) {
            Log.e("hola", "verificado")
            noVerifiedEmailText.setTextColor(Color.TRANSPARENT)
        } else {
            Log.e("hola", "no verificado")
            noVerifiedEmailText.setTextColor(this.getColor(R.color.red))
        }

        // Muestra los datos del usuario en los TextViews
        retrieveUserdata()
    }

    override fun onResume() {
        super.onResume()

        Log.e("hola", FirebaseQueries.getCurrentUser()!!.email.toString())

        // Muestra u oculta el mensaje de verificación de email
        if(checkEmailVerified()) {
            Log.e("hola", "verificado")
            noVerifiedEmailText.setTextColor(Color.TRANSPARENT)
        } else {
            Log.e("hola", "no verificado")
            noVerifiedEmailText.setTextColor(this.getColor(R.color.red))
        }

        // Muestra los datos del usuario en los TextViews
        retrieveUserdata()
    }

    private fun showKeyboard(editText: EditText) {
        Handler(Looper.getMainLooper()).postDelayed({
            editText.dispatchTouchEvent(
                MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(),
                    MotionEvent.ACTION_DOWN, 0f, 0f, 0)
            )
            editText.dispatchTouchEvent(
                MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(),
                    MotionEvent.ACTION_UP, 0f, 0f, 0)
            )
        }, 200)
    }

    private fun hideKeyboard(editText: EditText) {
        val manager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        manager.hideSoftInputFromWindow(
            editText.windowToken,
            InputMethodManager.HIDE_NOT_ALWAYS
        )
    }

    private fun retrieveUserimage() {
        // Muestra la barra de progreso
        userimageProgressBar.visibility = View.VISIBLE
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
                        }
                    } else {
                        // Oculta la barra de progreso
                        userimageProgressBar.visibility = View.GONE
                        userimage.visibility = View.VISIBLE
                    }
                }
            }
    }

    private fun changeUserimage(imageUri: Uri) {
        // Muestra la barra de progreso
        userimageProgressBar.visibility = View.VISIBLE
        userimage.visibility = View.INVISIBLE

        // Almacena la imagen seleccionada en firebase cloud
        val inputStream = this.contentResolver.openInputStream(imageUri)
        val firebaseStoragePath = "usersimages" + "/" +
                                    FirebaseQueries.getCurrentUser()!!.uid + "/" +
                                    "userimage"
        val ref = Firebase.storage.reference.child(firebaseStoragePath)
        ref.putStream(inputStream!!)
            .addOnSuccessListener { task ->
                FirebaseQueries.getDocumentReferenceForUserData()
                    .update(mapOf("userimageSet" to true))
                    .addOnCompleteListener { updateTask ->
                        if (updateTask.isSuccessful) {
                            // Cambia la imagen en el imageview
                            userimage.setImageURI(imageUri)

                            Toast.makeText(this, R.string.newUserimageSuccess,
                                Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(this, R.string.newUserimageError,
                                Toast.LENGTH_SHORT).show()
                        }

                        inputStream.close()

                        // Oculta la barra de progreso
                        userimageProgressBar.visibility = View.GONE
                        userimage.visibility = View.VISIBLE
                    }
            }.addOnFailureListener {
                Toast.makeText(this, R.string.newUserimageError,
                    Toast.LENGTH_SHORT).show()

                inputStream.close()

                // Oculta la barra de progreso
                userimageProgressBar.visibility = View.GONE
                userimage.visibility = View.VISIBLE
            }
    }

    /**
     * Actualiza el texto de los TextView de nombre de usuario, email y contraseña con los datos
     * del usuario que ha iniciado sesión, usando Firebase
     */
    private fun retrieveUserdata() {
        username.text = FirebaseQueries.getCurrentUser()!!.displayName
        email.text = FirebaseQueries.getCurrentUser()!!.email.toString()
        password.text = "●●●●●●●●●●●●"
    }

    /**
     * Comprueba si el usuario ha verificado el email
     * @return Boolean
     */
    private fun checkEmailVerified() : Boolean {
        return FirebaseQueries.getCurrentUser()!!.isEmailVerified
    }

    /**
     * Cambia el nombre de usuario en firebase por el nuevo dado
     * @param newUsername
     */
    private fun changeUser(newUsername: String) {
        if (newUsername.isEmpty()) {
            userEditText.error = getString(R.string.newUsernameAlert)
        } else {
            // Muestra la barra de progreso
            Utils.isProcessing(true, usernameChangeAcceptButton, userProgressBar)

            // Comprueba que el usuario introduccido tiene un formato válido
            if (Utils.validateUsername(newUsername)) {
                // Si es correcto actualiza el nombre de usuario en firebase
                FirebaseQueries.getCurrentUser()!!.updateProfile(
                    userProfileChangeRequest {
                        displayName = newUsername
                    }
                ).addOnCompleteListener { updateTask ->
                    if (updateTask.isSuccessful) { //usuario cambiado con éxito

                        // Cambia el usuario en la base de datos firestore
                        FirebaseQueries.getDocumentReferenceForUserData()
                            .update(mapOf("username" to newUsername))
                            .addOnCompleteListener { saveUsernameTask ->
                                if (saveUsernameTask.isSuccessful) { //usuario guardado con éxito
                                    Toast.makeText(this, R.string.newUsernameSuccess,
                                        Toast.LENGTH_SHORT).show()

                                    retrieveUserdata()
                                    usernameLayout.visibility = View.VISIBLE
                                    usernameEditLayout.visibility = View.GONE
                                } else { //si el guardado de usuario falla
                                    Toast.makeText(this, R.string.newUsernameError,
                                        Toast.LENGTH_SHORT).show()
                                }
                            }
                    } else { //si el cambio de usuario falla
                        Toast.makeText(this, R.string.newUsernameError,
                            Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                userEditText.error = getString(R.string.invalidUsernameError)
            }
        }

        // Oculta la barra de progreso
        Utils.isProcessing(false, usernameChangeAcceptButton, userProgressBar)
    }

    /**
     * Cambia el email del usuario en firebase por el nuevo dado
     * @param newEmail
     */
    private fun changeEmail(newEmail: String) {
        if (newEmail.isEmpty()) {
            emailEditText.error = getString(R.string.newEmailAlert)
        } else {
            // Comprueba que el email introduccido tiene un formato válido
            if (Utils.validateEmail(newEmail)) {
                // Muestra un dialog que solicita la contraseña actual
                val alertDialog = AlertDialog.Builder(this)
                alertDialog.setMessage(R.string.passwordDialog)

                // EditText del diálogo
                val inputLayout = LayoutInflater.from(this).inflate(R.layout.general_dialog_input, null)
                alertDialog.setView(inputLayout)
                val textInputEditText: TextInputEditText = inputLayout.findViewById(R.id.textInputEditText)
                textInputEditText.transformationMethod = PasswordTransformationMethod.getInstance()
                val textInputLayout: TextInputLayout = inputLayout.findViewById(R.id.textInputLayout)
                textInputLayout.setEndIconOnClickListener {
                    if (textInputEditText.transformationMethod == PasswordTransformationMethod.getInstance()) {
                        // Si la contraseña no es visible la muestra y cambia el icono del botón
                        textInputEditText.transformationMethod = HideReturnsTransformationMethod.getInstance()
                        textInputLayout.endIconDrawable = AppCompatResources.getDrawable(this, R.drawable.login_password_visible_button)
                    } else {
                        // Si la contraseña es visible la oculta y cambia el icono del botón
                        textInputEditText.transformationMethod = PasswordTransformationMethod.getInstance()
                        textInputLayout.endIconDrawable = AppCompatResources.getDrawable(this, R.drawable.login_password_notvisible_button)
                    }
                }

                // Si se pulsa el botón aceptar
                alertDialog.setPositiveButton(R.string.acceptButton) { dialog, i ->
                    // Muestra la barra de progreso
                    Utils.isProcessing(true, emailChangeAcceptButton, emailProgressBar)

                    val currentPassword = textInputEditText.text.toString()

                    // Reautentifica al usuario para poder cambiar el email
                    // (esto es requerido por firebase)
                    val oldEmail = FirebaseQueries.getCurrentUser()!!.email.toString()
                    val credentials: AuthCredential = EmailAuthProvider
                        .getCredential(oldEmail, currentPassword)
                    FirebaseQueries.getCurrentUser()!!.reauthenticate(credentials)
                        .addOnCompleteListener(this) { reauthenticateTask ->
                            // Una vez reautentificado, se realiza el cambio de email
                            if (reauthenticateTask.isSuccessful) { //reautentificación correcta
                                FirebaseQueries.getCurrentUser()!!.verifyBeforeUpdateEmail(newEmail)
                                .addOnCompleteListener(this) { changeEmailTask ->
                                    if (changeEmailTask.isSuccessful) { //mail cambiado con éxito
                                        // Cambia el usuario en la base de datos firestore
                                        FirebaseQueries.getDocumentReferenceForUserData()
                                            .update(mapOf("email" to newEmail))
                                            .addOnCompleteListener { saveEmailTask ->
                                                if (saveEmailTask.isSuccessful) { //email guardado con éxito
                                                    Toast.makeText(this,
                                                        R.string.newEmailSuccess,
                                                        Toast.LENGTH_SHORT
                                                    ).show()

                                                    retrieveUserdata()
                                                    noVerifiedEmailText.setTextColor(this.getColor(R.color.red))
                                                    emailLayout.visibility = View.VISIBLE
                                                    emailEditLayout.visibility = View.GONE
                                                } else { //si el guardado de email falla
                                                    Toast.makeText(this,
                                                        R.string.newEmailError, Toast.LENGTH_SHORT
                                                    ).show()
                                                }
                                            }
                                    } else { //si el cambio de email falla
                                        Toast.makeText(this, R.string.newEmailError,
                                            Toast.LENGTH_SHORT).show()
                                    }
                                }
                            } else { //si la reautentificación falla
                                Toast.makeText(this, R.string.invalidPassword,
                                    Toast.LENGTH_SHORT).show()
                            }
                        }
                    dialog.dismiss() //cierra el diálogo
                }
                // Si se pulsa el botón cancelar
                alertDialog.setNegativeButton(R.string.cancelButton) { dialog, i ->
                    Toast.makeText(this, R.string.newEmailCancelled,
                        Toast.LENGTH_SHORT).show()
                    dialog.dismiss() //cierra el diálogo
                }
                alertDialog.show()

            } else {
                emailEditText.error = getString(R.string.invalidEmailError)
            }
        }

        // Oculta la barra de progreso
        Utils.isProcessing(false, emailChangeAcceptButton, emailProgressBar)
    }

    /**
     * Cambia la contraseña del usuario en firebase por la nueva dado
     * @param newPassword
     */
    private fun changePassword(newPassword: String) {
        if(newPassword.isEmpty()) {
            passwordEditText.error = getString(R.string.newPasswordAlert)
        } else {
            // Comprueba que la nueva contraseña introduccida tiene un formato válido
            if (Utils.validatePassword(newPassword)) {
                // Muestra un dialog que solicita la contraseña actual
                val alertDialog = AlertDialog.Builder(this)
                alertDialog.setMessage(R.string.passwordDialog)

                // EditText del diálogo
                val inputLayout = LayoutInflater.from(this).inflate(R.layout.general_dialog_input, null)
                alertDialog.setView(inputLayout)
                val textInputEditText: TextInputEditText = inputLayout.findViewById(R.id.textInputEditText)
                textInputEditText.transformationMethod = PasswordTransformationMethod.getInstance()
                val textInputLayout: TextInputLayout = inputLayout.findViewById(R.id.textInputLayout)
                textInputLayout.setEndIconOnClickListener {
                    if (textInputEditText.transformationMethod == PasswordTransformationMethod.getInstance()) {
                        // Si la contraseña no es visible la muestra y cambia el icono del botón
                        textInputEditText.transformationMethod = HideReturnsTransformationMethod.getInstance()
                        textInputLayout.endIconDrawable = AppCompatResources.getDrawable(this, R.drawable.login_password_visible_button)
                    } else {
                        // Si la contraseña es visible la oculta y cambia el icono del botón
                        textInputEditText.transformationMethod = PasswordTransformationMethod.getInstance()
                        textInputLayout.endIconDrawable = AppCompatResources.getDrawable(this, R.drawable.login_password_notvisible_button)
                    }
                }

                // Si se pulsa el botón aceptar
                alertDialog.setPositiveButton(R.string.acceptButton) { dialog, i ->
                    // Muestra la barra de progreso
                    Utils.isProcessing(true, passwordChangeAcceptButton, passwordProgressBar)

                    val currentPassword = textInputEditText.text.toString()

                    // Reautentifica al usuario para poder cambiar la contraseña
                    // (esto es requerido por firebase)
                    val credentials: AuthCredential = EmailAuthProvider
                        .getCredential(FirebaseQueries.getCurrentUser()!!.email.toString(), currentPassword)
                    FirebaseQueries.getCurrentUser()!!.reauthenticate(credentials)
                        .addOnCompleteListener(this) { reauthenticateTask ->
                            // Una vez reautentificado, se realiza el cambio de contraseña
                            if (reauthenticateTask.isSuccessful) { //reautentificación correcta
                                FirebaseQueries.getCurrentUser()!!.updatePassword(newPassword)
                                    .addOnCompleteListener(this) { changeEmailTask ->
                                        if (changeEmailTask.isSuccessful) { //contraseña cambiada con éxito
                                            Toast.makeText(this, R.string.newPasswordSuccess,
                                                Toast.LENGTH_SHORT).show()

                                            retrieveUserdata()
                                            passwordLayout.visibility = View.VISIBLE
                                            passwordEditLayout.visibility = View.GONE
                                        } else { //si el cambio de contraseña falla
                                            Toast.makeText(this, R.string.newPasswordError,
                                                Toast.LENGTH_SHORT).show()
                                        }
                                    }
                            } else { //si la reautentificación falla
                                Toast.makeText(this, R.string.invalidPassword,
                                    Toast.LENGTH_SHORT).show()
                            }
                        }
                    dialog.dismiss() //cierra el diálogo
                }
                // Si se pulsa el botón cancelar
                alertDialog.setNegativeButton(R.string.cancelButton) { dialog, i ->
                    Toast.makeText(this, R.string.newPasswordCancelled,
                        Toast.LENGTH_SHORT).show()
                    dialog.dismiss() //cierra el diálogo
                }
                alertDialog.show()

            } else {
                passwordEditText.error = getString(R.string.passwordLengthError)
            }
        }
        // Oculta la barra de progreso
        Utils.isProcessing(false, passwordChangeAcceptButton, passwordProgressBar)
    }

    /**
     * Borra el usuario indicado de firebase
     */
    private fun deleteUser() {
        // Muestra un dialog que solicita la contraseña actual
        val alertDialog = AlertDialog.Builder(this)
        alertDialog.setMessage(R.string.passwordDialog)

        // EditText del diálogo
        val inputLayout = LayoutInflater.from(this).inflate(R.layout.general_dialog_input, null)
        alertDialog.setView(inputLayout)
        val textInputEditText: TextInputEditText = inputLayout.findViewById(R.id.textInputEditText)
        textInputEditText.transformationMethod = PasswordTransformationMethod.getInstance()
        val textInputLayout: TextInputLayout = inputLayout.findViewById(R.id.textInputLayout)
        textInputLayout.setEndIconOnClickListener {
            if (textInputEditText.transformationMethod == PasswordTransformationMethod.getInstance()) {
                // Si la contraseña no es visible la muestra y cambia el icono del botón
                textInputEditText.transformationMethod = HideReturnsTransformationMethod.getInstance()
                textInputLayout.endIconDrawable = AppCompatResources.getDrawable(this, R.drawable.login_password_visible_button)
                //passwordVisibilityButton.setImageResource(R.drawable.login_password_visible_button)
            } else {
                // Si la contraseña es visible la oculta y cambia el icono del botón
                textInputEditText.transformationMethod = PasswordTransformationMethod.getInstance()
                textInputLayout.endIconDrawable = AppCompatResources.getDrawable(this, R.drawable.login_password_notvisible_button)
                //passwordVisibilityButton.setImageResource(R.drawable.login_password_notvisible_button)
            }
        }

        // Si se pulsa el botón aceptar
        alertDialog.setPositiveButton(R.string.acceptButton) { dialog, i ->
            // Muestra la barra de progreso HOLA

            val currentPassword = textInputEditText.text.toString()

            // Reautentifica al usuario para poder cambiar el email
            // (esto es requerido por firebase)
            val email = FirebaseQueries.getCurrentUser()!!.email.toString()
            val credentials: AuthCredential = EmailAuthProvider
                .getCredential(email, currentPassword)
            FirebaseQueries.getCurrentUser()!!.reauthenticate(credentials)
                .addOnCompleteListener(this) { reauthenticateTask ->
                    // Una vez reautentificado, se elimina el usuario
                    if (reauthenticateTask.isSuccessful) { //reautentificación correcta
                        FirebaseQueries.getDocumentReferenceForUserData().delete()
                            .addOnCompleteListener(this) { deleteData ->
                                if (deleteData.isSuccessful) {
                                    FirebaseQueries.getCurrentUser()!!.delete()
                                        .addOnCompleteListener(this) { task ->
                                            if (task.isSuccessful) {
                                                Toast.makeText(this, R.string.deleteUserSuccess,
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                                FirebaseQueries.sigoutUser(this)
                                                Transitions.goToLoginScreen(this)
                                            } else {
                                                Toast.makeText(this, R.string.deleteUserError,
                                                    Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                } else {
                                    Toast.makeText(this, R.string.deleteUserError,
                                        Toast.LENGTH_SHORT).show()
                                }
                            }
                    } else { //si la reautentificación falla
                        Toast.makeText(this, R.string.invalidPassword,
                            Toast.LENGTH_SHORT).show()
                    }
                }
            dialog.dismiss() //cierra el diálogo
        }
        // Si se pulsa el botón cancelar
        alertDialog.setNegativeButton(R.string.cancelButton) { dialog, i ->
            Toast.makeText(this, R.string.deleteUserCancelled,
                Toast.LENGTH_SHORT).show()
            dialog.dismiss() //cierra el diálogo
        }
        alertDialog.show()
    }

}