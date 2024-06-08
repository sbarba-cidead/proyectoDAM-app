package com.sbg.appletreeapp.models

import com.google.firebase.Timestamp

class ChatroomModel(
    var chatroomId: String? = null,
    var userIds: List<String>? = null,
    var lastMessageTimestamp: Timestamp? = null,
    var lastMessageSenderId: String? = null
) {

    //for initialization of arguments
    init {

    }

    companion object Factory {
        fun createChatroom(): ChatroomModel = ChatroomModel()
    }

}