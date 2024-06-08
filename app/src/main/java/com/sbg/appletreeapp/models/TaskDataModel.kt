package com.sbg.appletreeapp.models

import com.google.firebase.Timestamp

/**
 * Clase base para crear un objeto task con datos sobre la tarea
 * Se instancia en IncompleteTasksFragment
 */
class TaskDataModel(
    var taskID: String? = null,
    var content: String? = null,
    var createdDate: Timestamp? = null,
    var dueDate: Timestamp? = null,
    var completeDate: Timestamp? = null,
    var formattedDueDate: String? = null,
    var isDone: Boolean = false
) {

    //for initialization of arguments
    init {

    }

    companion object Factory {
        fun createTask(): TaskDataModel = TaskDataModel()
    }
}