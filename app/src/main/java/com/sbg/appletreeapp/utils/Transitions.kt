package com.sbg.appletreeapp.utils

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import com.sbg.appletreeapp.app_screens.LoginScreen
import com.sbg.appletreeapp.app_screens.MainMenuScreen
import com.sbg.appletreeapp.app_screens.MessagesScreen
import com.sbg.appletreeapp.app_screens.NotesScreen
import com.sbg.appletreeapp.app_screens.QuizzesScreen
import com.sbg.appletreeapp.app_screens.RegistrationScreen
import com.sbg.appletreeapp.app_screens.ScoreScreen
import com.sbg.appletreeapp.app_screens.TasksScreen
import com.sbg.appletreeapp.app_screens.UserConfigScreen
import com.sbg.appletreeapp.app_screens.secondary_screens.CreateEditNoteScreen

class Transitions {

    companion object {
        /**
         * Finaliza la actividad actual y cambia a la actividad RegistrationScreen
         * @param activity
         */
        fun goToRegisterScreen(activity: AppCompatActivity) {
            activity.startActivity(Intent(activity, RegistrationScreen::class.java))
        }

        /**
         * Finaliza la actividad actual y cambia a la actividad LoginScreen
         * @param activity
         */
        fun goToLoginScreen(activity: AppCompatActivity) {
            activity.startActivity(Intent(activity, LoginScreen::class.java))
            activity.finish()
        }

        /**
         * Finaliza la actividad actual y cambia a la actividad MainMenuScreen
         * @param activity
         */
        fun goToMainMenuScreen(activity: AppCompatActivity) {
            activity.startActivity(Intent(activity, MainMenuScreen::class.java))
            activity.finish()
        }

        /**
         * Cambia a la actividad UserConfigScreen
         * @param activity
         */
        fun goToUserConfigScreen(activity: AppCompatActivity) {
            activity.startActivity(Intent(activity, UserConfigScreen::class.java))
        }

        /**
         * Cambia a la actividad QuizzesScreen
         * @param activity
         */
        fun goToQuizesScreen(activity: AppCompatActivity) {
            activity.startActivity(Intent(activity, QuizzesScreen::class.java))
        }

        /**
         * Cambia a la actividad NotesScreen
         * @param activity
         */
        fun goToNotesScreen(activity: AppCompatActivity) {
            activity.startActivity(Intent(activity, NotesScreen::class.java))
        }

        /**
         * Cambia a la actividad TasksScreen
         * @param activity
         */
        fun goToTasksScreen(activity: AppCompatActivity) {
            activity.startActivity(Intent(activity, TasksScreen::class.java))
        }

        /**
         * Cambia a la actividad MessagesScreen
         * @param activity
         */
        fun goToMessagesScreen(activity: AppCompatActivity) {
            activity.startActivity(Intent(activity, MessagesScreen::class.java))
        }

        /**
         * Cambia a la actividad CreateEditNoteScreen
         * @param activity
         */
        fun goToCreateEditNote(activity: AppCompatActivity) {
            activity.startActivity(Intent(activity, CreateEditNoteScreen::class.java))
        }

        /**
         * Cambia a la actividad ScoreScreen
         * @param activity
         */
        fun goToScoreScreen(activity: AppCompatActivity) {
            activity.startActivity(Intent(activity, ScoreScreen::class.java))
        }
    }
}