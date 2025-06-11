package com.example.presenteapp.network.model

import com.google.gson.annotations.SerializedName

data class UserProfile(
    val id: Long,
    val firebaseUid: String?,
    val nome: String,
    val email: String,

    @SerializedName("tipoUsuario")
    val role: String,

    val status: String
)