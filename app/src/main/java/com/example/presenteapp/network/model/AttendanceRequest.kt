package com.example.presenteapp.network.model

import com.google.gson.annotations.SerializedName

data class AttendanceRequest(
    @SerializedName("studentUid")
    val studentUid: String,

    @SerializedName("classSessionId")
    val classSessionId: String
)
