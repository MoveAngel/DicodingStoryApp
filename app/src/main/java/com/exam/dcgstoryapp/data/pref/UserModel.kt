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

data class StoriesResponse(
    @SerializedName("error") val error: Boolean,
    @SerializedName("message") val message: String,
    @SerializedName("listStory") val listStory: List<Story>
)

data class MapsStory(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("description") val description: String,
    @SerializedName("photoUrl") val photoUrl: String,
    @SerializedName("createdAt") val createdAt: String,
    @SerializedName("lat") val lat: Double?,
    @SerializedName("lon") val lon: Double?
)

data class ListStoryResponse(
    @SerializedName("error") val error: Boolean,
    @SerializedName("message") val message: String,
    @SerializedName("listStory") val listStory: List<MapsStory>
)