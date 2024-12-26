package com.exam.dcgstoryapp.view.story

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.exam.dcgstoryapp.data.api.ApiService
import com.exam.dcgstoryapp.data.pref.Story

class StoryViewModel(private val token: String, private val apiService: ApiService) : ViewModel() {

    private val pager = Pager(
        config = PagingConfig(
            pageSize = 5,
            enablePlaceholders = false
        ),
        pagingSourceFactory = { StoryPagingSource(apiService, token) }
    )

    val stories: LiveData<PagingData<Story>> = pager.flow
        .cachedIn(viewModelScope)
        .asLiveData()
}