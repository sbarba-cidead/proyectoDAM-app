package com.sbg.appletreeapp.models

/**
 * Clase base para crear un objeto task con datos sobre la tarea
 * Se instancia en RegistrationScreen
 */
class UserDataModel(
    var userID: String? = null,
    var username: String? = null,
    var email: String? = null,
    var classGroupName: String? = null,
    var classGroupID: String? = null,
    var teacherID: String? = null,
    var userimageSet: Boolean = false
) {

    //for initialization of arguments
    init {

    }

    companion object Factory {
        fun createUser(): UserDataModel = UserDataModel()
    }

}