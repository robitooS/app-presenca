// Em: app/src/main/java/com/example/presenteapp/network/model/UserRegistration.kt
package com.example.presenteapp.network.model

data class UserRegistration(
    val firebaseUid: String,
    val nome: String,
    val email: String,
    val tipoUsuario: String,
    val curso: String,
    val ra: String? = null,
    val status: String
)
