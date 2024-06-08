package com.sbg.appletreeapp.app_screens

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.sbg.appletreeapp.R
import java.util.Locale


class SplashScreen : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.splash_screen)

        Handler(Looper.getMainLooper()).postDelayed({
            //Establece el idioma español
            val locale = Locale(getString(R.string.language), getString(R.string.country))
            Locale.setDefault(locale)
            val config: Configuration = resources.configuration
            config.setLocale(locale)
            applicationContext.createConfigurationContext(config)

            //Comprueba si el usuario ya ha iniciado sesión
            //Si no ha iniciado, le lleva a la pantalla de inicio de sesión
            val currentUser: FirebaseUser? = FirebaseAuth.getInstance().currentUser
            if (currentUser == null) {
                startActivity(Intent(this, LoginScreen::class.java))
            } else {
                startActivity(Intent(this@SplashScreen, MainMenuScreen::class.java))
            }
            finish()
        }, 3000)

    }
}