package com.exam.dcgstoryapp.view.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.exam.dcgstoryapp.data.UserRepository
import com.exam.dcgstoryapp.data.pref.UserModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class LoginViewModel(private val repository: UserRepository) : ViewModel() {
    fun saveSession(user: UserModel) {
        viewModelScope.launch {
            repository.saveSession(user)
        }
    }

    fun getSession(): Flow<UserModel> {
        return repository.getSession()
    }
}