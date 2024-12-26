package com.exam.dcgstoryapp.view.story.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.exam.dcgstoryapp.data.UserRepository
import com.exam.dcgstoryapp.data.pref.UserProfile
import kotlinx.coroutines.launch

class ProfileViewModel(private val userRepository: UserRepository) : ViewModel() {

    private val _userProfile = MutableLiveData<UserProfile>()
    val userProfile: LiveData<UserProfile> = _userProfile

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    fun fetchUserProfile(token: String) {
        viewModelScope.launch {
            try {
                val response = userRepository.getUserProfile(token)
                if (response.isSuccessful) {
                    response.body()?.let {
                        _userProfile.postValue(it)
                    }
                } else {
                    _errorMessage.postValue(response.message())
                }
            } catch (e: Exception) {
                _errorMessage.postValue(e.message)
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            userRepository.logout()
        }
    }
}