package com.exam.dcgstoryapp

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.asFlow
import androidx.paging.AsyncPagingDataDiffer
import androidx.recyclerview.widget.ListUpdateCallback
import com.exam.dcgstoryapp.data.api.ApiService
import com.exam.dcgstoryapp.data.pref.StoriesResponse
import com.exam.dcgstoryapp.data.pref.Story
import com.exam.dcgstoryapp.view.story.StoryAdapter
import com.exam.dcgstoryapp.view.story.StoryViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import kotlinx.coroutines.test.advanceUntilIdle
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import org.mockito.Mockito.verify
import retrofit2.Response

@RunWith(MockitoJUnitRunner::class)
class StoryViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    @OptIn(ExperimentalCoroutinesApi::class)
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Mock
    private lateinit var apiService: ApiService

    private lateinit var viewModel: StoryViewModel
    private val token = "mockToken"

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        viewModel = StoryViewModel(token, apiService)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `when Get Stories Should Not Null and Return Data`() = runTest {
        val dummyStories = DummyDataTesting.generateDummyStories()

        Mockito.`when`(
            apiService.getStories(
                token = "Bearer $token",
                page = 1,
                size = 15
            )
        ).thenReturn(
            Response.success(
                StoriesResponse(
                    error = false,
                    message = "Success",
                    listStory = dummyStories
                )
            )
        )

        val differ = AsyncPagingDataDiffer(
            diffCallback = StoryAdapter.StoryComparator,
            updateCallback = noopListUpdateCallback,
            workerDispatcher = Dispatchers.Main
        )

        val job = launch(Dispatchers.Main) {
            viewModel.stories.asFlow().collect { pagingData ->
                differ.submitData(pagingData)
            }
        }

        advanceUntilIdle()

        verify(apiService).getStories("Bearer $token", 1, 15)
        assertNotNull(differ.snapshot())
        assertEquals(dummyStories.size, differ.snapshot().size)
        assertEquals(dummyStories[0].id, differ.snapshot()[0]?.id)
        assertEquals(dummyStories[0].name, differ.snapshot()[0]?.name)

        job.cancel()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `when Get Stories Failed Should Return Empty List`() = runTest {
        Mockito.`when`(
            apiService.getStories(
                token = "Bearer $token",
                page = 1,
                size = 15
            )
        ).thenReturn(
            Response.success(
                StoriesResponse(
                    error = true,
                    message = "Failed to load stories",
                    listStory = emptyList()
                )
            )
        )

        val differ = AsyncPagingDataDiffer(
            diffCallback = StoryAdapter.StoryComparator,
            updateCallback = noopListUpdateCallback,
            workerDispatcher = Dispatchers.Main
        )

        val job = launch(Dispatchers.Main) {
            viewModel.stories.asFlow().collect { pagingData ->
                differ.submitData(pagingData)
            }
        }

        advanceUntilIdle()

        verify(apiService).getStories("Bearer $token", 1, 15)
        assertTrue(differ.snapshot().isEmpty())
        assertEquals(0, differ.snapshot().size)

        job.cancel()
    }

    companion object {
        private val noopListUpdateCallback = object : ListUpdateCallback {
            override fun onInserted(position: Int, count: Int) {}
            override fun onRemoved(position: Int, count: Int) {}
            override fun onMoved(fromPosition: Int, toPosition: Int) {}
            override fun onChanged(position: Int, count: Int, payload: Any?) {}
        }
    }
}

object DummyDataTesting {
    fun generateDummyStories(): List<Story> {
        return List(10) {
            Story(
                id = "id_$it",
                name = "Author $it",
                photoUrl = "https://example.com/photo_$it.jpg",
                description = "Description $it"
            )
        }
    }
}