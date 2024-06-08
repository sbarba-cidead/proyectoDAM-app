package com.sbg.appletreeapp.app_screens

import android.os.Bundle
import android.text.InputType
import android.view.View
import android.widget.CalendarView
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.divider.MaterialDivider
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.firestore.AggregateSource
import com.google.firebase.firestore.Query
import com.sbg.appletreeapp.R
import com.sbg.appletreeapp.app_screens.secondary_screens.CompleteTasksFragment
import com.sbg.appletreeapp.app_screens.secondary_screens.IncompleteTasksFragment
import com.sbg.appletreeapp.models.TaskDataModel
import com.sbg.appletreeapp.utils.FirebaseQueries
import com.sbg.appletreeapp.utils.Utils
import com.sbg.appletreeapp.view_adapters.TasksAdapter
import com.sbg.appletreeapp.view_adapters.ViewPagerAdapter


class TasksScreen : AppCompatActivity(), TasksAdapter.CheckboxCallback {

    private lateinit var searchView: SearchView
    private lateinit var titleTextView: TextView
    private lateinit var tasksViewPager: ViewPager2
    private lateinit var tasksRecyclerView: RecyclerView
    private lateinit var viewPagerAdapter: ViewPagerAdapter
    private lateinit var completeTasksFragment: CompleteTasksFragment
    private lateinit var incompleteTasksFragment: IncompleteTasksFragment
    private lateinit var calendarButton: FloatingActionButton
    private lateinit var bottomNavigationBar: BottomNavigationView
    private lateinit var notasksTextView: TextView
    private lateinit var calendarView: CalendarView
    private lateinit var calendarRecyclerView: RecyclerView
    private lateinit var calendarLayout: RelativeLayout
    private lateinit var notasksCalendarTextView: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var tasksAdapter: TasksAdapter
    private lateinit var divider: MaterialDivider

    private var selectedDate = ""
    private var searchRecheck = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.tasks_screen)

        //Enlaza los elementos del layout con variables
        searchView = findViewById(R.id.searchView)
        titleTextView = findViewById(R.id.titleTextView)
        tasksViewPager = findViewById(R.id.tasksViewPager)
        tasksRecyclerView = findViewById(R.id.tasksRecyclerView)
        calendarButton = findViewById(R.id.calendarButton)
        bottomNavigationBar = findViewById(R.id.bottomNavigationBar)
        notasksTextView = findViewById(R.id.notasksTextView)
        calendarView = findViewById(R.id.calendarView)
        calendarRecyclerView = findViewById(R.id.calendarRecyclerView)
        calendarLayout = findViewById(R.id.calendarLayout)
        notasksCalendarTextView = findViewById(R.id.notasksCalendarTextView)
        progressBar = findViewById(R.id.progressBar)
        divider = findViewById(R.id.divider)

        // SEARCH OPTION //
        searchView.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_SENTENCES

        searchView.setOnSearchClickListener {
            titleTextView.visibility = View.INVISIBLE

            tasksRecyclerView.visibility = View.VISIBLE
            tasksViewPager.visibility = View.INVISIBLE

            calendarButton.visibility = View.INVISIBLE
            divider.visibility = View.INVISIBLE
            bottomNavigationBar.visibility = View.INVISIBLE

            searchTasks("")
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

            notasksTextView.visibility = View.GONE
            tasksRecyclerView.visibility = View.INVISIBLE
            tasksViewPager.visibility = View.VISIBLE

            calendarButton.visibility = View.VISIBLE
            divider.visibility = View.VISIBLE
            bottomNavigationBar.visibility = View.VISIBLE

            searchRecheck = true

            false
        }

        var oldQuery = ""
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                searchTasks(query!!)
                searchRecheck = true

                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText!!.isEmpty()) { searchRecheck = true } // si es una nueva búsqueda
                if (newText.length < oldQuery.length) { searchRecheck = true } // si se borraron caracteres
                oldQuery = newText
                searchTasks(newText)

                return true
            }
        })

        // END OF SEARCH OPTION //

        //Funcionamiento del botón de vista de calendario
        calendarButton.setOnClickListener {
            if (tasksViewPager.visibility == View.VISIBLE) {
                tasksViewPager.visibility = View.INVISIBLE
                calendarLayout.visibility = View.VISIBLE

                retrieveTasksCalendar()

                // deselecciona todos los elementos del menú inferior
                bottomNavigationBar.menu.setGroupCheckable(0, false, false)
            } else {
                tasksViewPager.visibility = View.VISIBLE
                calendarLayout.visibility = View.INVISIBLE

                // permite volver a seleccionar los elementos del menú inferior
                bottomNavigationBar.menu.setGroupCheckable(0, true, true)
            }
        }

        //Funcionamiento del calendario al pulsar una fecha
        calendarView.setOnDateChangeListener { view, year, month, dayOfMonth ->
            selectedDate = Utils.convertDateIntToDateString(dayOfMonth, month, year)

            retrieveTasksCalendar()
        }

        //Funcionamiento de la barra inferior de navegación
        bottomNavigationBar.setOnItemSelectedListener {
            when(it.itemId) {
                R.id.incompleteTasks -> {
                    tasksViewPager.currentItem = 0

                    if (tasksViewPager.visibility == View.INVISIBLE) {
                        tasksViewPager.visibility = View.VISIBLE
                        calendarLayout.visibility = View.INVISIBLE

                        // permite volver a seleccionar los elementos del menú inferior
                        bottomNavigationBar.menu.setGroupCheckable(0, true, true)
                    }

                    true
                }
                R.id.completeTasks -> {
                    tasksViewPager.currentItem = 1

                    if (tasksViewPager.visibility == View.INVISIBLE) {
                        tasksViewPager.visibility = View.VISIBLE
                        calendarLayout.visibility = View.INVISIBLE

                        // permite volver a seleccionar los elementos del menú inferior
                        bottomNavigationBar.menu.setGroupCheckable(0, true, true)
                    }

                    true
                }
                else -> false
            }
        }
        bottomNavigationBar.selectedItemId = R.id.incompleteTasks

        //Funcionamiento del viewpager ligado a barra de navegación
        tasksViewPager.registerOnPageChangeCallback(object: ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                if (tasksViewPager.currentItem == 0) {
                    bottomNavigationBar.selectedItemId = R.id.incompleteTasks
                }

                if (tasksViewPager.currentItem == 1) {
                    bottomNavigationBar.selectedItemId = R.id.completeTasks
                }
            }
        })

        retrieveTasks()
    }

    /**
     * Implementa la interfaz del TaskAdapter para comprobar el número de tareas.
     * Esta función se ejecuta cada vez que se pulsa el checkbox de una tarea.
     */
    override fun checkTaskCount() {
    }

    /**
     * Busca nuevas tareas en firebase y las añade a la base de datos de tareas pendientes del alumno
     */
    private fun retrieveTasks() {
        // shows progressbar
        progressBar.visibility = View.VISIBLE

        FirebaseQueries.getDocumentReferenceForUserData()
            .get().addOnCompleteListener { retrieveTeacherTask ->
                if (retrieveTeacherTask.isSuccessful) {
                    val teacherID = retrieveTeacherTask.result["teacherID"].toString()

                    FirebaseQueries.getDocumentReferenceForUserData().get().addOnCompleteListener { searchTask ->
                        if (searchTask.isSuccessful) {
                            val groupID = searchTask.result["classGroupID"].toString()

                            // elimina las tareas eliminadas por el profesor
                            FirebaseQueries.getCollectionReferenceForUserTasks().get()
                                .addOnSuccessListener { userTasks ->
                                    if (userTasks.size() > 0) {
                                        val collectionReference =
                                            FirebaseQueries.getCollectionReferenceForGeneralTasks(teacherID)
                                        for (userTask in userTasks) {
                                            collectionReference.document(userTask.id).get()
                                                .addOnCompleteListener { find ->
                                                    if (find.isSuccessful) {
                                                        if (!find.result.exists()) {
                                                            FirebaseQueries.getCollectionReferenceForUserTasks()
                                                                .document(userTask.id).delete()
                                                        }
                                                    }
                                                }
                                        }
                                    }
                                }

                            // recupera las tareas de firebase
                            FirebaseQueries.getCollectionReferenceForGeneralTasks(teacherID)
                                .whereEqualTo("group", groupID)
                                .get()
                                .addOnSuccessListener { generalTasks ->
                                    if (generalTasks.size() > 0) { // si hay tareas
                                        for (task in generalTasks) { // por cada tarea
                                            // comprueba si el alumno ha completado la tarea
                                            val studentsArray = task.get("completedStudents") as ArrayList<*>
                                            if (!studentsArray
                                                    .contains(FirebaseQueries.getCurrentUser()!!.uid)) { // si no completada
                                                val taskObject = task.toObject(TaskDataModel::class.java)

                                                // añade la tarea a las tareas del alumno
                                                val documentReference =
                                                    FirebaseQueries.getCollectionReferenceForUserTasks()
                                                        .document(task.get("taskID").toString())
                                                documentReference.set(taskObject)
                                                    .addOnCompleteListener { editTask ->
                                                        if (editTask.isSuccessful) {
                                                            initializeFragments()

                                                            // hides progressbar
                                                            progressBar.visibility = View.GONE
                                                        } else {
                                                            Toast.makeText(this,
                                                                R.string.retrieveTasksError,
                                                                Toast.LENGTH_SHORT).show()

                                                            // hides progressbar
                                                            progressBar.visibility = View.GONE
                                                        }
                                                    }
                                            } else {
                                                initializeFragments()

                                                // hides progressbar
                                                progressBar.visibility = View.GONE
                                            }
                                        }
                                    } else {
                                        notasksTextView.visibility = View.VISIBLE
                                    }
                                }
                        }
                    }
                }
            }


    }

    private fun initializeFragments() {
        //Inicia los fragmentos para la vista de tareas
        completeTasksFragment = CompleteTasksFragment()
        incompleteTasksFragment = IncompleteTasksFragment()

        // Adaptador para los fragmentos para la vista de tareas
        viewPagerAdapter = ViewPagerAdapter(supportFragmentManager, lifecycle)
        viewPagerAdapter.addFragment(incompleteTasksFragment)
        viewPagerAdapter.addFragment(completeTasksFragment)
        tasksViewPager.orientation = ViewPager2.ORIENTATION_HORIZONTAL
        tasksViewPager.adapter = viewPagerAdapter
    }

     private fun retrieveTasksCalendar() {
        // shows progressbar
        progressBar.visibility = View.VISIBLE

        val query: Query = FirebaseQueries.getCollectionReferenceForUserTasks()
            .whereEqualTo("formattedDueDate", selectedDate)

        val countQuery = query.count()
        countQuery.get(AggregateSource.SERVER).addOnCompleteListener { countTask ->
            if (countTask.isSuccessful) {
                val snapshot = countTask.result

                if (snapshot.count > 0) { // si hay tareas
                    // muestra las tareas del día seleccionado en el recyclerview
                    val options: FirestoreRecyclerOptions<TaskDataModel> =
                        FirestoreRecyclerOptions.Builder<TaskDataModel>()
                            .setQuery(query, TaskDataModel::class.java)
                            .build()

                    calendarRecyclerView.layoutManager =
                        LinearLayoutManager(this)

                    tasksAdapter = TasksAdapter(this, options)
                    tasksAdapter.checkboxCallback = this
                    calendarRecyclerView.adapter = tasksAdapter

                    tasksAdapter.startListening()

                    // shows recyclerview for selected day tasks
                    calendarRecyclerView.visibility = View.VISIBLE

                    // hides textview message
                    notasksCalendarTextView.visibility = View.GONE

                    // hides progressbar
                    progressBar.visibility = View.GONE
                } else {
                    // hides recyclerview for selected day tasks
                    calendarRecyclerView.visibility = View.INVISIBLE

                    // shows textview message
                    notasksCalendarTextView.visibility = View.VISIBLE

                    // hides progressbar
                    progressBar.visibility = View.GONE
                }
            } else {
                Toast.makeText(this,
                    R.string.retrieveTasksError,
                    Toast.LENGTH_SHORT).show()

                // hides progressbar
                progressBar.visibility = View.GONE
            }
        }
    }

    /**
     * Busca el texto en la consulta de notas en firebase
     * @param text
     */
    private fun searchTasks(text: String) {
        // solo hace la búsqueda si es una nueva búsqueda o se ha borrado el último caracter
        if (searchRecheck) {
            notasksTextView.visibility = View.GONE
            progressBar.visibility = View.VISIBLE

            // cambia la primera letra de la búsqueda en mayúscula
            val capitalizedText = text.lowercase().replaceFirstChar { it.uppercase() }

            val query: Query = FirebaseQueries.getCollectionReferenceForUserTasks()
                .orderBy("content").startAt(text).endAt(text+"\uf8ff")

            val countQuery = query.count()
            countQuery.get(AggregateSource.SERVER).addOnCompleteListener { countTask ->
                if (countTask.isSuccessful) {
                    val snapshot = countTask.result

                    if (snapshot.count > 0) {
                        tasksRecyclerView.visibility = View.VISIBLE

                        val options: FirestoreRecyclerOptions<TaskDataModel> =
                            FirestoreRecyclerOptions.Builder<TaskDataModel>()
                                .setQuery(query, TaskDataModel::class.java).build()

                        tasksRecyclerView.layoutManager = LinearLayoutManager(this)

                        tasksAdapter = TasksAdapter(this, options)
                        tasksAdapter.checkboxCallback = this
                        tasksRecyclerView.adapter = tasksAdapter

                        tasksAdapter.startListening()

                        progressBar.visibility = View.GONE
                    } else {
                        // Si no hay coincidencias, se desactiva la búsqueda en firebase
                        // hasta que se borre la búsqueda completa o el último caracter.
                        // Ahorra búsquedas en firebase porque ya se sabe que no habrá resultados
                        searchRecheck = false

                        tasksRecyclerView.visibility = View.INVISIBLE
                        notasksTextView.text = getString(R.string.notFound)
                        notasksTextView.visibility = View.VISIBLE

                        progressBar.visibility = View.GONE
                    }
                }
            }

        }
    }
}