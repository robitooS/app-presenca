package com.example.presenteapp.network

import com.example.presenteapp.network.model.AttendanceRequest
import com.example.presenteapp.network.model.SessionResponse
import com.example.presenteapp.network.model.StatusUpdateRequest
import com.example.presenteapp.network.model.UserProfile
import com.example.presenteapp.network.model.UserRegistration
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {

    @GET("api/usuarios/check/{uid}") //
    suspend fun getMyProfile(
        @Header("Authorization") token: String,
        @Path("uid") firebaseUid: String
    ): Response<UserProfile>

    @POST("api/professores/registrar") // Use o caminho correto da sua API
    suspend fun registerUser(@Body userData: UserRegistration): Response<Void> // Supondo que a API retorna 201 ou 204 sem corpo

    @GET("api/admin/professores/pendentes")
    suspend fun getPendingProf(
        @Header("Authorization") token: String,
    ): Response<List<UserProfile>>

    @POST("api/admin/professores/{uid}/aprovar")
    suspend fun approveProfessor(
        @Header("Authorization") token: String,
        @Path("uid") firebaseUid: String
    ): Response<UserProfile>

    @DELETE("api/admin/professores/{uid}")
    suspend fun rejectProfessor(
        @Header("Authorization") token: String,
        @Path("uid") firebaseUid: String // <-- DEVE SER String
    ): Response<UserProfile>

    @GET("api/professores/ativos")
    suspend fun getProfessoresAtivos(
        @Header("Authorization") token: String // <-- ADICIONE ESTA LINHA
    ): Response<List<UserProfile>>

    // --- NOVOS ENDPOINTS PARA ALUNOS ---

    @POST("api/students/create") // Endpoint específico para alunos
    suspend fun registerStudent(@Body studentData: UserRegistration): Response<Void>

    @GET("api/professores/alunos/pendentes")
    suspend fun getPendingStudents(
        @Header("Authorization") token: String
    ): Response<List<UserProfile>>

    @POST("api/professores/alunos/{uid}/aprovar")
    suspend fun approveStudent(
        @Header("Authorization") token: String,
        @Path("uid") firebaseUid: String
    ): Response<UserProfile>

    @DELETE("api/professores/alunos/{uid}/excluir")
    suspend fun rejectStudent(
        @Header("Authorization") token: String,
        @Path("uid") firebaseUid: String
    ): Response<UserProfile>


    // --- ENDPOINT PARA O PROFESSOR CRIAR A SESSÃO ---
    @POST("api/sessions/create")
    suspend fun createClassSession(
        @Header("Authorization") token: String
    ): Response<SessionResponse> // Espera receber um SessionResponse

    // --- ENDPOINT PARA O ALUNO REGISTRAR PRESENÇA ---
    @POST("api/attendance/register")
    suspend fun registerAttendance(
        @Header("Authorization") token: String,
        @Body attendanceRequest: AttendanceRequest
    ): Response<Void>

}