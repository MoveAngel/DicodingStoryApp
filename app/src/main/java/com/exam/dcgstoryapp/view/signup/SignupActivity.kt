package com.exam.dcgstoryapp.view.signup

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.exam.dcgstoryapp.data.RegisterRequest
import com.exam.dcgstoryapp.data.RegisterResponse
import com.exam.dcgstoryapp.data.api.ApiConfig
import com.exam.dcgstoryapp.data.api.ApiService
import com.exam.dcgstoryapp.databinding.ActivitySignupBinding
import com.exam.dcgstoryapp.view.login.LoginActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SignupActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySignupBinding
    private lateinit var apiService: ApiService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        apiService = ApiConfig.getApiService()

        setupView()
        setupAction()
        playAnimation()
    }

    private fun setupView() {
        @Suppress("DEPRECATION")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.hide(WindowInsets.Type.statusBars())
        } else {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }
        supportActionBar?.hide()
    }

    private fun setupAction() {
        binding.signupButton.setOnClickListener {
            val name = binding.nameEditText.text.toString()
            val email = binding.emailEditText.text.toString()
            val password = binding.passwordEditText.text.toString()

            if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
                showToast("All fields are required")
                return@setOnClickListener
            }

            performRegistration(name, email, password)
        }

        binding.passwordEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val context = this@SignupActivity

                if (s != null && s.length < 8) {
                    binding.passwordEditTextLayout.error = "Password must be at least 8 characters"
                    binding.passwordEditTextLayout.setErrorTextColor(
                        ContextCompat.getColorStateList(context, android.R.color.holo_red_dark)
                    )
                    binding.signupButton.isEnabled = false
                }
                else if (s != null && s.length >= 8) {
                    binding.passwordEditTextLayout.error = null
                    binding.signupButton.isEnabled = true
                } else {
                    binding.passwordEditTextLayout.error = null
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }
        })
    }


    @SuppressLint("Recycle")
    private fun playAnimation() {
        ObjectAnimator.ofFloat(binding.imageView, View.TRANSLATION_X, -30f, 30f).apply {
            duration = 6000
            repeatCount = ObjectAnimator.INFINITE
            repeatMode = ObjectAnimator.REVERSE
        }.start()
        val signup = ObjectAnimator.ofFloat(binding.signupButton, View.ALPHA, 1f).setDuration(400)
        val title = ObjectAnimator.ofFloat(binding.titleTextView, View.ALPHA, 1f).setDuration(500)
        val nameedit = ObjectAnimator.ofFloat(binding.nameEditTextLayout, View.ALPHA, 1f).setDuration(200)
        val name = ObjectAnimator.ofFloat(binding.nameTextView, View.ALPHA, 1f).setDuration(200)
        val email = ObjectAnimator.ofFloat(binding.emailTextView, View.ALPHA, 1f).setDuration(200)
        val emailedit = ObjectAnimator.ofFloat(binding.emailEditTextLayout, View.ALPHA, 1f).setDuration(200)
        val pass = ObjectAnimator.ofFloat(binding.passwordTextView, View.ALPHA, 1f).setDuration(200)
        val passedit = ObjectAnimator.ofFloat(binding.passwordEditTextLayout, View.ALPHA, 1f).setDuration(200)
        val together = AnimatorSet().apply {
            playTogether(signup)
        }
        AnimatorSet().apply {
            playSequentially(title, nameedit, name, emailedit, email, passedit, pass, together)
            start()
        }
    }

    private fun performRegistration(name: String, email: String, password: String) {
        Log.d("SignupActivity", "Attempting registration with email: $email")
        showLoading(true)
        val registerRequest = RegisterRequest(name, email, password)

        apiService.register(registerRequest).enqueue(object : Callback<RegisterResponse> {
            override fun onResponse(call: Call<RegisterResponse>, response: Response<RegisterResponse>) {
                showLoading(false)
                if (response.isSuccessful) {
                    val registerResult = response.body()
                    if (registerResult != null && !registerResult.error) {
                        Log.d("SignupActivity", "Registration successful: ${registerResult.message}")
                        showToast("Registration successful!")
                        showSuccessDialog(email)
                    } else {
                        Log.e("SignupActivity", "Registration failed: ${registerResult?.message}")
                        showToast(registerResult?.message ?: "Registration failed")
                    }
                } else {
                    Log.e("SignupActivity", "API Error: ${response.message()}")
                    showToast("Registration failed: ${response.message()}")
                }
            }

            override fun onFailure(call: Call<RegisterResponse>, t: Throwable) {
                showLoading(false)
                Log.e("SignupActivity", "Network error: ${t.message}")
                showToast("Network error: ${t.message}")
            }
        })
    }

    private fun showSuccessDialog(email: String) {
        val dialog = AlertDialog.Builder(this)
            .setTitle("Account Created")
            .setMessage("Account created with $email. Please log in to continue.")
            .setCancelable(false)
            .create()

        dialog.show()

        Handler(Looper.getMainLooper()).postDelayed({
            dialog.dismiss()
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }, 1500)
    }

    private fun showLoading(isLoading: Boolean) {
        binding.loadingOverlay.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}