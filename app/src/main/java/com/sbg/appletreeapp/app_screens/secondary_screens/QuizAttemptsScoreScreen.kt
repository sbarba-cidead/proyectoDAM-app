package com.sbg.appletreeapp.app_screens.secondary_screens

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.sbg.appletreeapp.R
import com.sbg.appletreeapp.view_adapters.ViewPagerAdapter

class QuizAttemptsScoreScreen : AppCompatActivity() {

    private lateinit var scoreViewPager: ViewPager2
    private lateinit var bottomNavigationBar: BottomNavigationView

    private lateinit var viewPagerAdapter: ViewPagerAdapter
    private lateinit var quizAttemptsScoreFragment: QuizAttemptsScoreFragment
    private lateinit var tasksScoreFragment: TasksScoreFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.score_quiz_attempts_score_screen)

        scoreViewPager = findViewById(R.id.scoreViewPager)
        bottomNavigationBar = findViewById(R.id.bottomNavigationBar)

        initializeFragments()

        //Funcionamiento de la barra inferior de navegación
        bottomNavigationBar.setOnItemSelectedListener {
            when(it.itemId) {
                R.id.quizzesScore -> {
                    scoreViewPager.currentItem = 0

                    true
                }
                R.id.tasksScore -> {
                    scoreViewPager.currentItem = 1

                    true
                }
                else -> false
            }
        }
        bottomNavigationBar.selectedItemId = R.id.quizzesScore
    }

    private fun initializeFragments() {
        // recibe los datos del QuizzesScoreFragment
        val quizID = intent.getStringExtra("quizID").toString()
        val quizName = intent.getStringExtra("quizName").toString()
        val totalAttempts = intent.getStringExtra("totalAttempts").toString()

        //Inicia los fragmentos para la vista de tareas
        quizAttemptsScoreFragment = QuizAttemptsScoreFragment()
        tasksScoreFragment = TasksScoreFragment()

        // envía los datos del intent al fragmento QuizAttemptsScoreFragment
        val bundle = Bundle()
        bundle.putString("quizID", quizID)
        bundle.putString("quizName", quizName)
        bundle.putString("totalAttempts", totalAttempts)
        quizAttemptsScoreFragment.arguments = bundle

        // Adaptador para los fragmentos para la vista de tareas
        viewPagerAdapter = ViewPagerAdapter(supportFragmentManager, lifecycle)
        viewPagerAdapter.addFragment(quizAttemptsScoreFragment)
        viewPagerAdapter.addFragment(tasksScoreFragment)
        scoreViewPager.orientation = ViewPager2.ORIENTATION_HORIZONTAL
        scoreViewPager.adapter = viewPagerAdapter
    }

}