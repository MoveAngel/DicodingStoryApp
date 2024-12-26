package com.exam.dcgstoryapp.view.story.list

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.exam.dcgstoryapp.R
import com.exam.dcgstoryapp.databinding.FragmentListStoryBinding
import com.exam.dcgstoryapp.view.story.loading.LoadingStateAdapter
import com.exam.dcgstoryapp.view.story.maps.MapsFragment
import com.exam.dcgstoryapp.view.story.StoryAdapter
import com.exam.dcgstoryapp.view.story.StoryViewModel
import com.exam.dcgstoryapp.view.story.StoryViewModelFactory

class ListStoryFragment : Fragment() {

    private lateinit var binding: FragmentListStoryBinding
    private lateinit var adapter: StoryAdapter
    private lateinit var viewModel: StoryViewModel
    private lateinit var loadStateAdapter: LoadingStateAdapter
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private var backPressedOnce = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentListStoryBinding.inflate(inflater, container, false)
        swipeRefreshLayout = binding.swipeRefreshLayout
        adapter = StoryAdapter()

        loadStateAdapter = LoadingStateAdapter { retry() }

        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter.withLoadStateFooter(
            footer = loadStateAdapter
        )

        binding.swipeRefreshLayout.setOnRefreshListener {
            adapter.refresh()
        }

        val token = getToken()
        viewModel = ViewModelProvider(
            this,
            StoryViewModelFactory(token)
        )[StoryViewModel::class.java]

        viewModel.stories.observe(viewLifecycleOwner) { pagingData ->
            adapter.submitData(lifecycle, pagingData)
            swipeRefreshLayout.isRefreshing = false
        }

        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (backPressedOnce) {
                        requireActivity().finish()
                    } else {
                        backPressedOnce = true
                        Toast.makeText(requireContext(), "Press again to exit", Toast.LENGTH_SHORT).show()
                        Handler(Looper.getMainLooper()).postDelayed({ backPressedOnce = false }, 2000)
                    }
                }
            })

        setupFab()

        return binding.root
    }

    private fun retry() {
        adapter.retry()
    }

    private fun setupFab() {
        binding.fabMaps.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, MapsFragment())
                .addToBackStack(null)
                .commit()
        }
    }

    private fun getToken(): String {
        val preferences = requireContext().getSharedPreferences("session", Context.MODE_PRIVATE)
        return preferences.getString("token", "") ?: ""
    }
}