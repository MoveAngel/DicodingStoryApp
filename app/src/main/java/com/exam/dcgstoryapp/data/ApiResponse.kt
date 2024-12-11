package com.exam.dcgstoryapp.data

import com.google.gson.annotations.SerializedName

data class RegisterRequest(
    val name: String,
    val email: String,
    val password: String
)

data class LoginRequest(
    val email: String,
    val password: String
)

data class LoginResponse(
    @SerializedName("error") val error: Boolean,
    @SerializedName("message") val message: String,
    @SerializedName("loginResult") val loginResult: LoginResult?
)

data class LoginResult(
    @SerializedName("error") val error: Boolean,
    @SerializedName("userId") val userId: String,
    @SerializedName("name") val name: String,
    @SerializedName("message") val message: String,
    @SerializedName("token") val token: String
)

data class RegisterResponse(
    @SerializedName("error") val error: Boolean,
    @SerializedName("message") val message: String
)