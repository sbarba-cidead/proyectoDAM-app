package com.sbg.appletreeapp.app_screens

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.sbg.appletreeapp.R
import com.sbg.appletreeapp.app_screens.secondary_screens.QuizzesScoreFragment
import com.sbg.appletreeapp.app_screens.secondary_screens.TasksScoreFragment
import com.sbg.appletreeapp.view_adapters.ViewPagerAdapter

class ScoreScreen : AppCompatActivity() {

    private lateinit var scoreViewPager: ViewPager2
    private lateinit var bottomNavigationBar: BottomNavigationView

    private lateinit var viewPagerAdapter: ViewPagerAdapter
    private lateinit var quizzesScoreFragment: QuizzesScoreFragment
    private lateinit var tasksScoreFragment: TasksScoreFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.score_screen)

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

        //Funcionamiento del viewpager ligado a barra de navegación
        scoreViewPager.registerOnPageChangeCallback(object: OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                if (scoreViewPager.currentItem == 0) {
                    bottomNavigationBar.selectedItemId = R.id.quizzesScore
                }

                if (scoreViewPager.currentItem == 1) {
                    bottomNavigationBar.selectedItemId = R.id.tasksScore
                }
            }
        })

    }

    private fun initializeFragments() {
        //Inicia los fragmentos para la vista de tareas
        quizzesScoreFragment = QuizzesScoreFragment()
        tasksScoreFragment = TasksScoreFragment()

        // Adaptador para los fragmentos para la vista de tareas
        viewPagerAdapter = ViewPagerAdapter(supportFragmentManager, lifecycle)
        viewPagerAdapter.addFragment(quizzesScoreFragment)
        viewPagerAdapter.addFragment(tasksScoreFragment)
        scoreViewPager.orientation = ViewPager2.ORIENTATION_HORIZONTAL
        scoreViewPager.adapter = viewPagerAdapter
    }

}