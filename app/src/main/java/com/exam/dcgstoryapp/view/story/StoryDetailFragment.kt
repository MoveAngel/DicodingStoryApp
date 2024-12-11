package com.exam.dcgstoryapp.view.story

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.transition.TransitionInflater
import com.bumptech.glide.Glide
import com.exam.dcgstoryapp.R
import com.exam.dcgstoryapp.databinding.FragmentStoryDetailBinding
import com.exam.dcgstoryapp.view.welcome.WelcomeActivity

class StoryDetailFragment : Fragment() {

    private lateinit var binding: FragmentStoryDetailBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentStoryDetailBinding.inflate(inflater, container, false)

        val args = arguments ?: return binding.root

        val storyId = args.getString("STORY_ID") ?: return binding.root
        val storyName = args.getString("STORY_NAME") ?: ""
        val storyDescription = args.getString("STORY_DESCRIPTION") ?: ""
        val storyPhotoUrl = args.getString("STORY_PHOTO_URL") ?: ""

        sharedElementEnterTransition = TransitionInflater.from(requireContext())
            .inflateTransition(android.R.transition.fade)

        sharedElementReturnTransition = TransitionInflater.from(requireContext())
            .inflateTransition(android.R.transition.slide_top)

        binding.apply {
            storyTitleTextView.text = storyName
            storyDescriptionTextView.text = storyDescription
            storyImageView.transitionName = "story_image_$storyId"
            storyTitleTextView.transitionName = "story_title_$storyId"
            storyDescriptionTextView.transitionName = "story_description_$storyId"

            Glide.with(requireContext())
                .load(storyPhotoUrl)
                .error(R.drawable.ic_broken_image)
                .placeholder(R.drawable.ic_loading_image)
                .into(storyImageView)
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        checkAuthenticationStatus()
    }

    private fun checkAuthenticationStatus() {
        val preferences = requireContext().getSharedPreferences("MyApp", Context.MODE_PRIVATE)
        val token = preferences.getString("token", null)

        if (token.isNullOrBlank()) {
            redirectToWelcomeActivity()
        }
    }

    private fun redirectToWelcomeActivity() {
        val intent = Intent(requireContext(), WelcomeActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        requireActivity().finish()
    }

    companion object {
        fun newInstance(
            storyId: String,
            storyName: String,
            storyDescription: String,
            storyPhotoUrl: String
        ): StoryDetailFragment {
            return StoryDetailFragment().apply {
                arguments = Bundle().apply {
                    putString("STORY_ID", storyId)
                    putString("STORY_NAME", storyName)
                    putString("STORY_DESCRIPTION", storyDescription)
                    putString("STORY_PHOTO_URL", storyPhotoUrl)
                }
            }
        }
    }
}