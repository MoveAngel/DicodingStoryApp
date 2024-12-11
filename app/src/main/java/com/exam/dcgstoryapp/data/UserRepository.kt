package com.exam.dcgstoryapp.data

import com.exam.dcgstoryapp.data.api.ApiService
import com.exam.dcgstoryapp.data.pref.UserModel
import com.exam.dcgstoryapp.data.pref.UserPreference
import com.exam.dcgstoryapp.data.pref.UserProfile
import kotlinx.coroutines.flow.Flow
import retrofit2.Response

class UserRepository private constructor(
    private val userPreference: UserPreference,
    private val apiService: ApiService
) {

    suspend fun saveSession(user: UserModel) {
        userPreference.saveSession(user)
    }

    fun getSession(): Flow<UserModel> {
        return userPreference.getSession()
    }

    suspend fun logout() {
        userPreference.logout()
    }

    suspend fun getUserProfile(token: String): Response<UserProfile> {
        return apiService.getUserProfile("Bearer $token")
    }

    companion object {
        @Volatile
        private var instance: UserRepository? = null
        fun getInstance(
            userPreference: UserPreference,
            apiService: ApiService
        ): UserRepository =
            instance ?: synchronized(this) {
                instance ?: UserRepository(userPreference, apiService)
            }.also { instance = it }
    }
}