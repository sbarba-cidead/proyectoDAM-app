package com.sbg.appletreeapp.models

import com.google.firebase.Timestamp

class ChatMessageModel(
    var message: String? = null,
    var senderId: String? = null,
    var timestamp: Timestamp? = null,
) {

    //for initialization of arguments
    init {

    }

    companion object Factory {
        fun createChatMessage(): ChatMessageModel = ChatMessageModel()
    }

}