package com.example.presenteapp.network.model

import com.google.gson.annotations.SerializedName

data class SessionResponse(
    @SerializedName("classSessionId")
    val classSessionId: String
)