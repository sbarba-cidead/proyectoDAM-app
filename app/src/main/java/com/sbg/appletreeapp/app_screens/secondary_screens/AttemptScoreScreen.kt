package com.sbg.appletreeapp.app_screens.secondary_screens

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.sbg.appletreeapp.R
import com.sbg.appletreeapp.view_adapters.ViewPagerAdapter

class AttemptScoreScreen : AppCompatActivity() {

    private lateinit var scoreViewPager: ViewPager2
    private lateinit var bottomNavigationBar: BottomNavigationView

    private lateinit var viewPagerAdapter: ViewPagerAdapter
    private lateinit var attemptScoreFragment: AttemptScoreFragment
    private lateinit var tasksScoreFragment: TasksScoreFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.score_attempt_score_screen)

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
        // recibe los datos del QuizAttemptsScoreFragment
        val quizID = intent.getStringExtra("quizID").toString()
        val quizName = intent.getStringExtra("quizName").toString()
        val attemptID = intent.getStringExtra("attemptID").toString()
        val attemptNumber = intent.getStringExtra("attemptNumber").toString()
        val attemptDate = intent.getStringExtra("attemptDate").toString()

        //Inicia los fragmentos para la vista de tareas
        attemptScoreFragment = AttemptScoreFragment()
        tasksScoreFragment = TasksScoreFragment()

        // envía los datos del intent al fragmento AttemptScoreFragment
        val bundle = Bundle()
        bundle.putString("quizID", quizID)
        bundle.putString("quizName", quizName)
        bundle.putString("attemptID", attemptID)
        bundle.putString("attemptNumber", attemptNumber)
        bundle.putString("attemptDate", attemptDate)
        attemptScoreFragment.arguments = bundle

        // Adaptador para los fragmentos para la vista de tareas
        viewPagerAdapter = ViewPagerAdapter(supportFragmentManager, lifecycle)
        viewPagerAdapter.addFragment(attemptScoreFragment)
        viewPagerAdapter.addFragment(tasksScoreFragment)
        scoreViewPager.orientation = ViewPager2.ORIENTATION_HORIZONTAL
        scoreViewPager.adapter = viewPagerAdapter
    }

}