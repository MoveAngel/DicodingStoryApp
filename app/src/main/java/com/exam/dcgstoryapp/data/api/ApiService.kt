package com.exam.dcgstoryapp.data.api

import com.exam.dcgstoryapp.data.LoginRequest
import com.exam.dcgstoryapp.data.LoginResponse
import com.exam.dcgstoryapp.data.RegisterRequest
import com.exam.dcgstoryapp.data.RegisterResponse
import com.exam.dcgstoryapp.data.pref.Story
import com.exam.dcgstoryapp.data.pref.UserProfile
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface ApiService {
    @POST("register")
    fun register(@Body registerRequest: RegisterRequest): Call<RegisterResponse>

    @POST("login")
    fun login(@Body loginRequest: LoginRequest): Call<LoginResponse>

    @Multipart
    @POST("stories")
    suspend fun addStory(
        @Header("Authorization") token: String,
        @Part photo: MultipartBody.Part,
        @Part("description") description: RequestBody
    ): Response<Story>

    @GET("login")
    suspend fun getUserProfile(@Header("Authorization") token: String): Response<UserProfile>

}