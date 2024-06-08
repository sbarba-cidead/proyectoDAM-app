package com.sbg.appletreeapp.utils

import android.app.AlertDialog
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.ProgressBar
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.Locale

class Utils {

    companion object {
        /**
         * Muestra u oculta la barra de proceso
         * Toma un Button como parámetro de botón
         * @param isProcessing
         * @param button
         * @param progressBar
         */
        fun isProcessing(isProcessing: Boolean, button: Button, progressBar: ProgressBar) {
            if (isProcessing) {
                button.visibility = View.GONE
                progressBar.visibility = View.VISIBLE
            } else {
                button.visibility = View.VISIBLE
                progressBar.visibility = View.GONE
            }
        }

        /**
         * Muestra u oculta la barra de proceso
         * Toma un ImageButton como parámetro de botón
         * @param isProcessing
         * @param button
         * @param progressBar
         */
        fun isProcessing(isProcessing: Boolean, button: ImageButton, progressBar: ProgressBar) {
            if (isProcessing) {
                button.visibility = View.GONE
                progressBar.visibility = View.VISIBLE
            } else {
                button.visibility = View.VISIBLE
                progressBar.visibility = View.GONE
            }
        }

        /**
         * Comprueba el formato de la dirección email dada es correcto
         * Devuelve true si es correcto
         * @param username
         * @param usernameEditText
         * @return Boolean
         */
        fun validateUsername(username: String) : Boolean {
            val emailRegex = "^[A-zÀ-ú]+\\s[A-zÀ-ú\\s]+\$"
            return username.matches(emailRegex.toRegex())
        }

        /**
         * Comprueba el formato de la dirección email dada es correcto
         * Devuelve true si es correcto
         * @param emailAdress
         * @param userEmailEditText
         * @return Boolean
         */
        fun validateEmail(emailAdress: String) : Boolean {
            val emailRegex = "^[A-Za-z0-9_.-]+@[A-Za-z0-9]+\\.[A-Za-z0-9]+\$"
            return emailAdress.matches(emailRegex.toRegex())
        }

        /**
         * Comprueba que la longitud de la contraseña es válida
         * Devuelve true si es válida
         * @param password
         * @param userPasswordEditText
         * @return Boolean
         */
        fun validatePassword(password: String) : Boolean {
            return password.length >= 6
        }

        /**
         * Crea y muestra un diálogo sencillo con un mensaje y dos botones de aceptar o rechazar
         * Devuelve la respuesta del usuario (el botón que ha pulsado)
         * @param context
         * @param message
         * @param positiveButtonText
         * @param negativeButtonText
         * @return Boolean
         */
        fun showSimpleDialog(context: Context, message: String,
                             positiveButtonText: String, negativeButtonText: String) : Boolean {
            var userAnswer = false

            val alertDialog = AlertDialog.Builder(context)

            alertDialog.setMessage(message)
            alertDialog.setPositiveButton(positiveButtonText) { dialog, i ->
                userAnswer = true
                dialog.dismiss()
            }
            alertDialog.setNegativeButton(negativeButtonText) { dialog, i ->
                userAnswer = false
                dialog.dismiss()
            }
            alertDialog.show()

            return userAnswer
        }

        /**
         * Convierte Timestamp y lo devuelve como un Date en forma de string
         * @param timestamp
         * @return String
         */
        fun convertTimestampToDateString(timestamp: Timestamp) : String {
            return SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                .format(timestamp.toDate())
        }

        /**
         * Convierte fecha en int de día, mes y año a Date en forma de string
         * @param day
         * @param month
         * @param year
         * @return String
         */
        fun convertDateIntToDateString(day: Int, month: Int, year: Int) : String {
            var formattedDay = ""
            if (day < 10) {
                formattedDay = "0$day"
            } else {
                formattedDay = "$day"
            }

            var formattedMonth = ""
            if (month+1 < 10) {
                formattedMonth = "0${month+1}"
            } else {
                formattedMonth = "${month+1}"
            }

            return "$formattedDay/$formattedMonth/$year 00:00"
        }

        fun convertSecondsToHourMinuteSecond(seconds : Long) : String {
            val hours = seconds / 3600
            val minutes = (seconds % 3600) / 60
            val remaindedSeconds = seconds % 60

            return String.format("%02d:%02d:%02d", hours, minutes, remaindedSeconds)
        }

        /**
         * Transforma ColorStateList en hexadecimal y lo devuelve como string
         * @param colorStateList
         * @return String
         */
        fun convertColorStateListToHexString(colorStateList: ColorStateList) : String {
            val color: Int = colorStateList.defaultColor
            return String.format("#%08X", 0xFFFFFFFF and color.toLong())
        }

        /**
        * Transforma una string de color hexadecimal y lo devuelve como ColorStateList
         * @param colorHexString
         * @return ColorStateList
        */
        fun convertHexStringToColorStateList(colorHexString: String) : ColorStateList {
            val colorInt: Int = Color.parseColor(colorHexString)
            return ColorStateList.valueOf(colorInt)
        }
    }
}