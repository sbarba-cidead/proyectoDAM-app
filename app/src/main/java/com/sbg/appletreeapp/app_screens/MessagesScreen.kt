package com.sbg.appletreeapp.app_screens

import android.os.Bundle
import android.text.InputType
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.AggregateSource
import com.google.firebase.firestore.Query
import com.sbg.appletreeapp.R
import com.sbg.appletreeapp.models.ChatMessageModel
import com.sbg.appletreeapp.models.ChatroomModel
import com.sbg.appletreeapp.utils.FirebaseQueries
import com.sbg.appletreeapp.view_adapters.MessagesAdapter

class MessagesScreen : AppCompatActivity() {

    private lateinit var messagesRecyclerView: RecyclerView
    private lateinit var searchRecyclerView: RecyclerView
    private lateinit var inputEditText: EditText
    private lateinit var sendButton: ImageButton
    private lateinit var searchView: SearchView
    private lateinit var titleTextView: TextView
    private lateinit var noMessagesTextView: TextView
    private lateinit var progressBar: ProgressBar

    private lateinit var messagesAdapter: MessagesAdapter
    private lateinit var chatroomId: String
    private var chatroomModel: ChatroomModel? = null
    private var searchRecheck = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.messages_screen)

        //Enlaza los elementos del layout con variables
        messagesRecyclerView = findViewById(R.id.messagesRecyclerView)
        searchRecyclerView = findViewById(R.id.searchRecyclerView)
        inputEditText = findViewById(R.id.inputEditText)
        sendButton = findViewById(R.id.sendButton)
        searchView = findViewById(R.id.searchView)
        titleTextView = findViewById(R.id.titleTextView)
        noMessagesTextView = findViewById(R.id.noMessagesTextView)
        progressBar = findViewById(R.id.progressBar)

        ////CHAT////
        FirebaseQueries.getDocumentReferenceForUserData().get()
            .addOnCompleteListener { retrieveTeacherTask ->
                if (retrieveTeacherTask.isSuccessful) {
                    val teacherID = retrieveTeacherTask.result["teacherID"].toString()

                    chatroomId = FirebaseQueries.getChatroomId(FirebaseQueries.getCurrentUser()!!.uid, teacherID)
                    getOrCreateChatroomModel(FirebaseQueries.getCurrentUser()!!, teacherID)

                    retrieveMessages()
                }
            }

        // muestra el teclado con la primera letra en mayúscula
        // la información debe guardarse con la primera letra en mayúscula en firebase
        // ya que el searchquery de firebase es sensible a mayúsculas
        inputEditText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_SENTENCES

        sendButton.setOnClickListener {
            val message: String = inputEditText.text.toString()
            if (message.isNotEmpty()) {
                sendMessageToUser(message, FirebaseQueries.getCurrentUser()!!)
            }
        }

        // SEARCH OPTION //
        searchView.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_SENTENCES

        searchView.setOnSearchClickListener {
            titleTextView.visibility = View.INVISIBLE

            searchMessages("")
            searchRecheck = true
        }

        val seachViewCloseButton: View =
            searchView.findViewById(androidx.appcompat.R.id.search_close_btn)
        seachViewCloseButton.setOnClickListener {
            // closes search bar
            searchView.isIconified = true
        }

        searchView.setOnCloseListener {
            titleTextView.visibility = View.VISIBLE

            noMessagesTextView.visibility = View.GONE
            messagesRecyclerView.visibility = View.VISIBLE
            searchRecyclerView.visibility = View.INVISIBLE

            searchRecheck = true

            false
        }

        var oldQuery = ""
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                messagesRecyclerView.visibility = View.INVISIBLE
                searchRecyclerView.visibility = View.VISIBLE

                searchMessages(query!!)
                searchRecheck = true

                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                messagesRecyclerView.visibility = View.INVISIBLE
                searchRecyclerView.visibility = View.VISIBLE

                if (newText!!.isEmpty()) { // si es una nueva búsqueda
                    searchRecheck = true
                    messagesRecyclerView.visibility = View.VISIBLE
                    searchRecyclerView.visibility = View.INVISIBLE
                }
                if (newText.length < oldQuery.length) { searchRecheck = true } // si se borraron caracteres
                oldQuery = newText
                searchMessages(newText!!)

                return true
            }
        })
    }

    /**
     * Actualiza los mensajes desde firebase y las muestra en el recyclerview
     * @param messagesRecyclerView
     */
    private fun retrieveMessages() {
        progressBar.visibility = View.VISIBLE

        val query: Query = FirebaseQueries.getCollectionReferenceForMessages(chatroomId)
            .orderBy("timestamp", Query.Direction.DESCENDING)
        val options: FirestoreRecyclerOptions<ChatMessageModel> =
            FirestoreRecyclerOptions.Builder<ChatMessageModel>()
                .setQuery(query, ChatMessageModel::class.java).build()

        val reverseLinearManager = LinearLayoutManager(this)
        reverseLinearManager.reverseLayout = true
        messagesRecyclerView.layoutManager = reverseLinearManager

        messagesAdapter = MessagesAdapter(this, options)
        messagesRecyclerView.adapter = messagesAdapter

        messagesAdapter.startListening()

        // mueve la vista del recyclerview para que se muestre
        // el último mensaje al pulsar enviar
        messagesAdapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                messagesRecyclerView.smoothScrollToPosition(0)
            }
        })

        progressBar.visibility = View.GONE
    }

    fun searchMessages(text: String) {
        // solo hace la búsqueda si es una nueva búsqueda o se ha borrado el último caracter
        if (searchRecheck) {
            noMessagesTextView.visibility = View.GONE
            progressBar.visibility = View.VISIBLE

            val query: Query = FirebaseQueries.getCollectionReferenceForMessages(chatroomId)
                .orderBy("message").startAt(text).endAt(text +"\uf8ff")

            val countQuery = query.count()
            countQuery.get(AggregateSource.SERVER).addOnCompleteListener { countTask ->
                if (countTask.isSuccessful) {
                    val snapshot = countTask.result

                    if (snapshot.count > 0) {
                        val options: FirestoreRecyclerOptions<ChatMessageModel> =
                            FirestoreRecyclerOptions.Builder<ChatMessageModel>()
                                .setQuery(query, ChatMessageModel::class.java).build()

                        val reverseLinearManager = LinearLayoutManager(this)
                        reverseLinearManager.reverseLayout = true
                        searchRecyclerView.layoutManager = reverseLinearManager

                        messagesAdapter = MessagesAdapter(this, options)
                        searchRecyclerView.adapter = messagesAdapter

                        messagesAdapter.startListening()

                        progressBar.visibility = View.GONE
                    } else {
                        // Si no hay coincidencias, se desactiva la búsqueda en firebase
                        // hasta que se borre la búsqueda completa o el último caracter.
                        // Ahorra búsquedas en firebase porque ya se sabe que no habrá resultados
                        searchRecheck = false

                        searchRecyclerView.visibility = View.INVISIBLE
                        noMessagesTextView.visibility = View.VISIBLE

                        progressBar.visibility = View.GONE
                    }
                }
            }
        }
    }

    /**
     *
     */
    private fun sendMessageToUser(message: String, loginUser: FirebaseUser) {
        chatroomModel!!.lastMessageTimestamp = Timestamp.now()
        chatroomModel!!.lastMessageSenderId = loginUser.uid
        FirebaseQueries.getChatroomReference(chatroomId).set(chatroomModel!!)

        val chatMessageModel = ChatMessageModel(message, loginUser.uid, Timestamp.now())
        FirebaseQueries.getCollectionReferenceForMessages(chatroomId).add(chatMessageModel)
            .addOnCompleteListener { task ->
                if (task.isSuccessful){
                    inputEditText.text.clear()
                }
            }
    }

    /**
     * Obtiene el chatroomModel a partir de los datos de Firebase.
     * Si es la primera vez que se chatea, no habrá datos en Firebase y se crean.
     *
     * @param loginUser
     * @param teacherID
     */
    private fun getOrCreateChatroomModel(loginUser: FirebaseUser, teacherID: String) {
        FirebaseQueries.getChatroomReference(chatroomId).get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                chatroomModel = task.result.toObject(ChatroomModel::class.java)
                if (chatroomModel == null) { //first time chatting
                    chatroomModel = ChatroomModel(
                        chatroomId, listOf(loginUser.uid, teacherID), Timestamp.now(), ""
                    )
                    FirebaseQueries.getChatroomReference(chatroomId).set(chatroomModel!!)
                }
            }
        }
    }
}