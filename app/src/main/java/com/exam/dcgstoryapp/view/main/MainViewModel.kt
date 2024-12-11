package com.exam.dcgstoryapp.view.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.exam.dcgstoryapp.data.UserRepository
import com.exam.dcgstoryapp.data.pref.UserModel

class MainViewModel(private val repository: UserRepository) : ViewModel() {
    fun getSession(): LiveData<UserModel> {
        return repository.getSession().asLiveData()
    }
}