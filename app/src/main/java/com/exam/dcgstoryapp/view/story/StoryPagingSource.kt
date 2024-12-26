package com.exam.dcgstoryapp.view.story

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.exam.dcgstoryapp.data.api.ApiService
import com.exam.dcgstoryapp.data.pref.Story

class StoryPagingSource(private val apiService: ApiService, private val token: String) : PagingSource<Int, Story>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Story> {
        val page = params.key ?: 1
        return try {
            val response = apiService.getStories("Bearer $token", page, params.loadSize)
            if (response.isSuccessful) {
                val stories = response.body()?.listStory ?: emptyList()
                LoadResult.Page(
                    data = stories,
                    prevKey = if (page == 1) null else page - 1,
                    nextKey = if (stories.isEmpty()) null else page + 1
                )
            } else {
                LoadResult.Error(Exception("Failed to load stories"))
            }
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }


    override fun getRefreshKey(state: PagingState<Int, Story>): Int? {
        return state.anchorPosition
    }
}