package com.exam.dcgstoryapp.view.story

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.exam.dcgstoryapp.data.pref.Story
import com.exam.dcgstoryapp.databinding.FragmentListStoryBinding
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

class ListStoryFragment : Fragment() {

    private lateinit var binding: FragmentListStoryBinding
    private lateinit var adapter: StoryAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentListStoryBinding.inflate(inflater, container, false)
        adapter = StoryAdapter()
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                Toast.makeText(requireContext(), "Press again to exit", Toast.LENGTH_SHORT).show()
                requireActivity().finish()
            }
        })

        fetchStories()
        return binding.root
    }

    private fun fetchStories() {
        showLoading(true)
        val preferences = requireContext().getSharedPreferences("MyApp", Context.MODE_PRIVATE)
        val token = preferences.getString("token", null)


        if (token.isNullOrBlank()) {
            Toast.makeText(requireContext(), "Authentication token is missing", Toast.LENGTH_SHORT).show()
            showLoading(false)
            return
        }

        val client = OkHttpClient()
        val request = Request.Builder()
            .url("https://story-api.dicoding.dev/v1/stories")
            .addHeader("Authorization", token)
            .build()

        client.newCall(request).enqueue(object : okhttp3.Callback {
            override fun onFailure(call: okhttp3.Call, e: IOException) {
                requireActivity().runOnUiThread {
                    showLoading(false)
                    Log.e("FetchStories", "Network request failed", e)
                    Toast.makeText(requireContext(), "Failed to load stories: ${e.message}", Toast.LENGTH_SHORT).show()
                    binding.progressBar.visibility = View.GONE
                }
            }

            override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                requireActivity().runOnUiThread {
                    showLoading(false)
                    binding.progressBar.visibility = View.GONE

                    Log.d("FetchStories", "Response Code: ${response.code}")

                    if (response.isSuccessful) {
                        try {
                            val responseBodyString = response.body?.string() ?: ""

                            val storyList = JSONObject(responseBodyString)
                                .getJSONArray("listStory")
                            val stories = parseStories(storyList)

                            if (stories.isEmpty()) {
                                binding.emptyStateText.visibility = View.VISIBLE
                            } else {
                                binding.emptyStateText.visibility = View.GONE
                                adapter.submitList(stories)
                            }
                        } catch (e: Exception) {
                            Log.e("FetchStories", "Error parsing stories", e)
                            Toast.makeText(requireContext(), "Error parsing stories: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        val errorBody = response.body?.string() ?: "No error body"
                        Log.e("FetchStories", "Failed to load stories. Response Code: ${response.code}. Error: $errorBody")
                        Toast.makeText(requireContext(), "Failed to load stories. Code: ${response.code}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }

    private fun parseStories(storyList: JSONArray): List<Story> {
        val stories = mutableListOf<Story>()
        for (i in 0 until storyList.length()) {
            val story = storyList.getJSONObject(i)
            stories.add(
                Story(
                    story.getString("id"),
                    story.getString("name"),
                    story.getString("photoUrl"),
                    story.getString("description")
                )
            )
        }
        return stories
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.recyclerView.visibility = if (isLoading) View.GONE else View.VISIBLE
    }
}