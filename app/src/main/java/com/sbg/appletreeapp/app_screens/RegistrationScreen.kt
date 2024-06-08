package com.sbg.appletreeapp.app_screens

import android.content.Context
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.auth.ktx.userProfileChangeRequest
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.Query
import com.google.firebase.ktx.Firebase
import com.sbg.appletreeapp.R
import com.sbg.appletreeapp.models.UserDataModel
import com.sbg.appletreeapp.utils.FirebaseQueries
import com.sbg.appletreeapp.utils.Transitions
import com.sbg.appletreeapp.utils.Utils


class RegistrationScreen : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    private lateinit var usernameEditText: EditText
    private lateinit var userEmailEditText: EditText
    private lateinit var userPasswordEditText: EditText
    private lateinit var passwordVisibilityButton: ImageButton
    private lateinit var classGroupSpinner: Spinner
    private lateinit var registerButton: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var loginLink: TextView

    private var groupNameToTeacherIDMap = mutableMapOf<String, String>()
    private var groupNameToGroupIDMap = mutableMapOf<String, String>()
    private var classGroupNamesList = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.registration_screen)

        // Inicia la autentificación con firebase
        auth = Firebase.auth

        // Enlaza las variables con los view de la interfaz
        usernameEditText = findViewById(R.id.usernameEditText)
        userEmailEditText = findViewById(R.id.userEmailEditText)
        userPasswordEditText = findViewById(R.id.userPasswordEditText)
        passwordVisibilityButton = findViewById(R.id.passwordVisibilityButton)
        classGroupSpinner = findViewById(R.id.classGroupSpinner)
        registerButton = findViewById(R.id.registerButton)
        progressBar = findViewById(R.id.progressBar)
        loginLink = findViewById(R.id.loginLink)
        loginLink.paint?.isUnderlineText = true //texto subrayado

        prepareClassGroupsList()

        // Comportamiento del desplegable para elegir grupo
        val adapter = CustomAdapter(this, R.layout.registration_classgroupdopdown_item, classGroupNamesList, 0)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        classGroupSpinner.adapter = adapter

        classGroupSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, pos: Int, id: Long) {
                if ((parent.getChildAt(0) as TextView).text == getString(R.string.defaultClassGroupDropdown)) {
                    (parent.getChildAt(0) as TextView).setTextColor(getColor(R.color.light_grey))
                } else {
                    (parent.getChildAt(0) as TextView).setTextColor(getColor(R.color.black))
                }
            }

            override fun onNothingSelected(p0: AdapterView<*>?) { }
        }

        // Comportamiento del botón de visibilidad de la contraseña
        passwordVisibilityButton.setOnClickListener {
            if (userPasswordEditText.transformationMethod == PasswordTransformationMethod.getInstance()) {
                // Si la contraseña no es visible la muestra y cambia el icono del botón
                userPasswordEditText.transformationMethod = HideReturnsTransformationMethod.getInstance()
                passwordVisibilityButton.setImageResource(R.drawable.login_password_visible_button)
            } else {
                // Si la contraseña es visible la oculta y cambia el icono del botón
                userPasswordEditText.transformationMethod = PasswordTransformationMethod.getInstance()
                passwordVisibilityButton.setImageResource(R.drawable.login_password_notvisible_button)
            }
        }

        // Comportamiento del botón de registro de usuario
        registerButton.setOnClickListener {
            val username = usernameEditText.text.toString()
            val userEmail = userEmailEditText.text.toString()
            val userPassword = userPasswordEditText.text.toString()
            val classGroup = classGroupSpinner.selectedItem.toString()

            // Antes de crear usuario, comprueba que los datos son correctos
            if (username.isEmpty()) { //comprueba nombre de usuario
                usernameEditText.error = getString(R.string.userError)
            } else if (userEmail.isEmpty()){ //comprueba dirección email
                userEmailEditText.error = getString(R.string.emailError)
            } else if (userPassword.isEmpty()) { //comprueba contraseña
                userPasswordEditText.error = getString(R.string.passwordError)
            } else if (classGroupSpinner.selectedItem.toString() == getString(R.string.defaultClassGroupDropdown)) {
                (classGroupSpinner.selectedView as TextView).error = getString(R.string.classGroupError)
            } else {
                // Muestra la barra de progreso
                Utils.isProcessing(true, registerButton, progressBar)

                if (Utils.validateUsername(username)) {
                    if (Utils.validateEmail(userEmail)) {
                        if (Utils.validatePassword(userPassword)) {
                            // Crea nuevo usuario
                            createUser(username, userEmail, userPassword, classGroup)
                        } else {
                            userPasswordEditText.error = getString(R.string.passwordLengthError)
                        }
                    } else {
                        userEmailEditText.error = getString(R.string.invalidEmailError)
                    }
                } else {
                    usernameEditText.error = getString(R.string.invalidUsernameError)
                }

                // Oculta la barra de progreso
                Utils.isProcessing(false, registerButton, progressBar)
            }
        }

        // Comportamiento del link de cambio a inicio de sesión
        loginLink.setOnClickListener{
            // Vacía los campos del formulario
            userEmailEditText.text.clear()
            userPasswordEditText.text.clear()

            // Va a la actividad de inicio de sesión (LoginScreen)
            Transitions.goToLoginScreen(this)
        }


    }

    private fun prepareClassGroupsList() {
        val query: Query = FirebaseQueries.getCollectionReferenceForClassGroups()
            .orderBy("groupName", Query.Direction.ASCENDING)

        query.get().addOnSuccessListener { documents ->
            for (document in documents) {
                classGroupNamesList.add(document.get("groupName").toString())

                groupNameToTeacherIDMap[document.get("groupName").toString()] = document.get("teacherID").toString()
                groupNameToGroupIDMap[document.get("groupName").toString()] = document.id
            }
        }

        classGroupNamesList.add(0, getString(R.string.defaultClassGroupDropdown))
    }

    /**
     * Crea un usuario en la base de datos de firebase
     * @param username
     * @param email
     * @param password
     */
    private fun createUser(username: String, email: String, password: String, classGroup: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, R.string.newUserCreated, Toast.LENGTH_SHORT).show()

                    // Este bloque puede utilizarse para activar la verificación de email
//                    // Envía email de confirmación
//                    auth.currentUser!!.sendEmailVerification()

                    // Añade el nombre de usuario al perfil del usuario
                    FirebaseQueries.getCurrentUser()!!.updateProfile(
                        userProfileChangeRequest {
                            displayName = username
                        })

                    // Guarda los datos del usuario en firestore
                    val user = UserDataModel.createUser()
                    user.username = username
                    user.email = email
                    user.classGroupName = classGroup
                    user.classGroupID = groupNameToGroupIDMap[classGroup]
                    user.teacherID = groupNameToTeacherIDMap[classGroup]
                    val documentReference = FirebaseQueries.getDocumentReferenceForUserData()
                    documentReference.set(user)

                    FirebaseQueries.getCollectionReferenceForClassGroups()
                        .document(groupNameToGroupIDMap[classGroup]!!)
                        .update("students", FieldValue.arrayUnion(documentReference.id))

                    documentReference.update(mapOf("userID" to documentReference.id))
                        .addOnCompleteListener {
                            // Cierra la sesión automática iniciada por firebase
                            auth.signOut()
                        }

                    // Limpia los campos del formulario
                    usernameEditText.text.clear()
                    userEmailEditText.text.clear()
                    userPasswordEditText.text.clear()

                    // Oculta la barra de progreso
                    Utils.isProcessing(false, registerButton, progressBar)

                    // Va a la actividad de inicio de sesión (LoginScreen)
                    Transitions.goToLoginScreen(this)
                } else {
                    Toast.makeText(this, R.string.existingUserError, Toast.LENGTH_SHORT)
                        .show()

                    // Oculta la barra de progreso
                    Utils.isProcessing(false, registerButton, progressBar)
                }
            }
    }

    class CustomAdapter(context: Context, textViewResourceId: Int, itemsList: MutableList<String>,
                        private val hidingItemIndex: Int) : ArrayAdapter<String>(context, textViewResourceId, itemsList) {
        override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
            var v: View? = null
            if (position == hidingItemIndex) {
                val tv = TextView(context)
                tv.visibility = View.GONE
                v = tv
            } else {
                v = super.getDropDownView(position, null, parent)
            }
            return v!!
        }
    }
}

