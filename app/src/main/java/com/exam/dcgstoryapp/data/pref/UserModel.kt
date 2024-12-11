package com.exam.dcgstoryapp.data.pref

import com.google.gson.annotations.SerializedName

data class UserModel(
    val email: String,
    val token: String,
    val isLogin: Boolean = false
)

data class Story(
    val id: String,
    val name: String,
    val photoUrl: String,
    val description: String
)

data class UserProfile(
    @SerializedName("name") val name: String,
    @SerializedName("email") val email: String
)