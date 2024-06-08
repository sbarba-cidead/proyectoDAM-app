package com.sbg.appletreeapp.utils

import android.content.Context
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class FirebaseQueries {

    companion object {

        private val auth: FirebaseAuth = FirebaseAuth.getInstance()

        /**
         * Devuelve el usuario que tiene iniciada la sesión en Firebase
         * @return FirebaseUser?
         */
        fun getCurrentUser() : FirebaseUser? {
            return auth.currentUser
        }

        /**
         * Cierra sesión del usuario actual en Firebase
         * @param context
         */
        fun sigoutUser(context: Context) {
            auth.signOut()

            Toast.makeText(context, "Cierre de sesión correcto.",
                Toast.LENGTH_SHORT).show()
        }

        /**
         *
         */
        fun getDocumentReferenceForUserData() : DocumentReference {
            return Firebase.firestore.collection("users")
                .document(getCurrentUser()!!.uid)
        }

        fun getCollectionReferenceForClassGroups() : CollectionReference {
            return Firebase.firestore.collection("classGroups")
        }

        /**
         *
         */
        fun getCollectionReferenceForGeneralQuizzes(teacherID: String) : CollectionReference {
            return Firebase.firestore.collection("quizes")
                .document(teacherID).collection("quizes")
        }

        /**
         *
         */
        fun getCollectionReferenceForGeneralQuizzesQuestions(teacherID: String,
                                                             quizID: String) : CollectionReference {
            return Firebase.firestore.collection("quizes")
                .document(teacherID).collection("quizes")
                .document(quizID).collection("questions")
        }

        /**
         *
         */
        fun getCollectionReferenceForUserQuizzes() : CollectionReference {
            return Firebase.firestore.collection("users")
                .document(getCurrentUser()!!.uid).collection("quizes")
        }

        /**
         *
         */
        fun getCollectionReferenceForUserQuizzesQuestions(quizID: String) : CollectionReference {
            return Firebase.firestore.collection("users")
                .document(getCurrentUser()!!.uid).collection("quizes")
                .document(quizID).collection("questions")
        }

        /**
         *
         */
        fun getCollectionReferenceForNotes() : CollectionReference {
            return Firebase.firestore.collection("users")
                .document(getCurrentUser()!!.uid).collection("notes")
        }

        /**
         *
         */
        fun getCollectionReferenceForGeneralTasks(teacherID: String) : CollectionReference {
            return Firebase.firestore.collection("tasks")
                .document(teacherID).collection("tasks")
        }

        /**
         *
         */
        fun getCollectionReferenceForUserTasks() : CollectionReference {
            return Firebase.firestore.collection("users")
                .document(getCurrentUser()!!.uid).collection("tasks")
        }

        /**
         *
         */
        fun getCollectionReferenceForMessages(chatroomId: String) : CollectionReference{
            return getChatroomReference(chatroomId).collection("chats")
        }

        /**
         *
         */
        fun getChatroomReference(chatroomId: String) : DocumentReference {
            return Firebase.firestore.collection("chatrooms").document(chatroomId)
        }

        /**
         *
         */
        fun getChatroomId(userid1: String, userid2: String) : String {
            if (userid1.hashCode() < userid2.hashCode()){
                return userid1 + "_" + userid2;
            } else {
                return userid2 + "_" + userid1;
            }
        }

        /**
         *
         */
        fun getCollectionReferenceForScorequizzes() : CollectionReference {
            return Firebase.firestore.collection("users")
                .document(getCurrentUser()!!.uid).collection("scoreQuizes")
        }

        /**
         *
         */
        fun getCollectionReferenceForScorequizzesAttempts() : CollectionReference {
            return Firebase.firestore.collection("users")
                .document(getCurrentUser()!!.uid).collection("quizesAttempts")
        }



    }

}