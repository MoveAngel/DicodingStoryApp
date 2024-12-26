package com.exam.dcgstoryapp.view.story

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.exam.dcgstoryapp.data.api.ApiConfig

class StoryViewModelFactory(private val token: String) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(StoryViewModel::class.java)) {
            val apiService = ApiConfig.getApiService()
            return StoryViewModel(token, apiService) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

