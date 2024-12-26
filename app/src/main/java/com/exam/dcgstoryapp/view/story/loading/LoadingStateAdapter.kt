package com.exam.dcgstoryapp.view.story.loading

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.paging.LoadState
import androidx.paging.LoadStateAdapter
import androidx.recyclerview.widget.RecyclerView
import com.exam.dcgstoryapp.databinding.ItemLoadingBinding

class LoadingStateAdapter(private val retry: () -> Unit) : LoadStateAdapter<LoadingStateAdapter.LoadingStateViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        loadState: LoadState
    ): LoadingStateViewHolder {
        val binding = ItemLoadingBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return LoadingStateViewHolder(binding, retry)
    }

    override fun onBindViewHolder(holder: LoadingStateViewHolder, loadState: LoadState) {
        holder.bind(loadState)
    }

    class LoadingStateViewHolder(
        private val binding: ItemLoadingBinding,
        retry: () -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.retryButton.setOnClickListener {
                binding.retryButton.isEnabled = false
                retry()
            }
        }

        fun bind(loadState: LoadState) {
            when (loadState) {
                is LoadState.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.retryButton.visibility = View.GONE
                    binding.errorMsg.visibility = View.GONE
                }

                is LoadState.Error -> {
                    binding.progressBar.visibility = View.GONE
                    binding.retryButton.visibility = View.VISIBLE
                    binding.errorMsg.visibility = View.VISIBLE
                    binding.errorMsg.text = loadState.error.localizedMessage
                }

                else -> {
                    binding.progressBar.visibility = View.GONE
                    binding.retryButton.visibility = View.GONE
                    binding.errorMsg.visibility = View.GONE
                }
            }
        }
    }
}