package com.exam.dcgstoryapp.view.story

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.exam.dcgstoryapp.R
import com.exam.dcgstoryapp.data.pref.Story
import com.exam.dcgstoryapp.databinding.ItemStoryBinding
import com.exam.dcgstoryapp.view.story.detail.StoryDetailFragment

class StoryAdapter : PagingDataAdapter<Story, RecyclerView.ViewHolder>(StoryComparator) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val binding = ItemStoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return StoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val story = getItem(position)
        if (story != null) {
            (holder as StoryViewHolder).bind(story)
        }
    }

    inner class StoryViewHolder(private val binding: ItemStoryBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(story: Story) {
            binding.storyName.text = story.name
            binding.storyDescription.text = story.description

            Glide.with(binding.storyImage.context)
                .load(story.photoUrl)
                .into(binding.storyImage)

            binding.root.setOnClickListener { view ->
                val context = view.context
                if (context is FragmentActivity) {
                    val detailFragment = StoryDetailFragment.newInstance(
                        story.id,
                        story.name,
                        story.description,
                        story.photoUrl
                    )

                    context.supportFragmentManager.beginTransaction()
                        .setReorderingAllowed(true)
                        .addSharedElement(binding.storyImage, "story_image_${story.id}")
                        .addSharedElement(binding.storyName, "story_name_${story.id}")
                        .replace(R.id.fragment_container, detailFragment)
                        .addToBackStack(null)
                        .commit()
                }
            }
        }
    }


    object StoryComparator : DiffUtil.ItemCallback<Story>() {
        override fun areItemsTheSame(oldItem: Story, newItem: Story): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Story, newItem: Story): Boolean {
            return oldItem == newItem
        }
    }
}