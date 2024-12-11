package com.exam.dcgstoryapp.di

import android.content.Context
import com.exam.dcgstoryapp.data.UserRepository
import com.exam.dcgstoryapp.data.api.ApiConfig
import com.exam.dcgstoryapp.data.pref.UserPreference
import com.exam.dcgstoryapp.data.pref.dataStore

object Injection {
    fun provideRepository(context: Context): UserRepository {
        val pref = UserPreference.getInstance(context.dataStore)
        val apiService = ApiConfig.getApiService()
        return UserRepository.getInstance(pref, apiService)
    }
}