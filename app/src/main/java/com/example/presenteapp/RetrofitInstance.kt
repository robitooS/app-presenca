// No arquivo: app/src/main/java/com/example/presenteapp/network/RetrofitInstance.kt

package com.example.presenteapp

import com.example.presenteapp.network.ApiService
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {

    // --- AQUI ESTÁ O LINK DA API ---
    private const val BASE_URL = "http://10.0.2.2:8080/"

    val api: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL) // O link é usado aqui
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}