package com.exam.dcgstoryapp.view.story.profile

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.exam.dcgstoryapp.R
import com.exam.dcgstoryapp.databinding.FragmentProfileBinding
import com.exam.dcgstoryapp.view.welcome.WelcomeActivity
import com.exam.dcgstoryapp.view.ViewModelFactory

class ProfileFragment : Fragment() {

    private lateinit var binding: FragmentProfileBinding
    private lateinit var viewModel: ProfileViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentProfileBinding.inflate(inflater, container, false)

        val factory = ViewModelFactory.getInstance(requireContext())
        viewModel = ViewModelProvider(this, factory)[ProfileViewModel::class.java]

        setupObservers()
        loadUserProfile()
        setupLogout()

        return binding.root
    }

    private fun loadUserProfile() {
        val sharedPref = requireContext().getSharedPreferences("session", Context.MODE_PRIVATE)
        val token = sharedPref.getString("token", null)
        val name = sharedPref.getString("name", null)
        val email = sharedPref.getString("email", null)

        if (!name.isNullOrEmpty() && !email.isNullOrEmpty()) {
            binding.nameTextView.text = name
            binding.emailTextView.text = email
        } else if (token != null) {
            viewModel.fetchUserProfile(token)
        } else {
            binding.nameTextView.text = getString(R.string.no_name_available)
            binding.emailTextView.text = getString(R.string.no_email_available)
        }
    }

    private fun setupObservers() {
        viewModel.userProfile.observe(viewLifecycleOwner) { userProfile ->
            binding.nameTextView.text = userProfile.name
            binding.emailTextView.text = userProfile.email

            val sharedPref = requireContext().getSharedPreferences("session", Context.MODE_PRIVATE)
            with(sharedPref.edit()) {
                putString("name", userProfile.name)
                putString("email", userProfile.email)
                apply()
            }
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { errorMessage ->
            if (errorMessage != null) {
                Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupLogout() {
        binding.logoutButton.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Yes") { _, _ ->
                    viewModel.logout()
                    val sharedPref = requireContext().getSharedPreferences("session", Context.MODE_PRIVATE)
                    sharedPref.edit().remove("token").apply()

                    val isTokenDeleted = sharedPref.getString("token", null) == null
                    if (isTokenDeleted) {
                        Log.d("Logout", "Logout successful. Token has been deleted.")
                    } else {
                        Log.e("Logout", "Logout failed. Token still exists.")
                    }

                    val intent = Intent(requireContext(), WelcomeActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(intent)
                    requireActivity().finish()
                }
                .setNegativeButton("No", null)
                .show()
        }
    }
}