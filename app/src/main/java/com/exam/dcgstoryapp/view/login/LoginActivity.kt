package com.exam.dcgstoryapp.view.login

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.exam.dcgstoryapp.data.LoginRequest
import com.exam.dcgstoryapp.data.LoginResponse
import com.exam.dcgstoryapp.data.api.ApiConfig
import com.exam.dcgstoryapp.data.api.ApiService
import com.exam.dcgstoryapp.data.pref.UserModel
import com.exam.dcgstoryapp.databinding.ActivityLoginBinding
import com.exam.dcgstoryapp.view.ViewModelFactory
import com.exam.dcgstoryapp.view.main.MainActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var apiService: ApiService
    private lateinit var viewModel: LoginViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        apiService = ApiConfig.getApiService()

        val factory = ViewModelFactory.getInstance(this)
        viewModel = ViewModelProvider(this, factory)[LoginViewModel::class.java]

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
        binding.loginButton.setOnClickListener {
            val email = binding.emailEditText.text.toString()
            val password = binding.passwordEditText.text.toString()

            if (email.isEmpty() || password.isEmpty()) {
                showToast("Email and password cannot be empty")
                return@setOnClickListener
            }

            if (password.length < 8) {
                binding.passwordEditTextLayout.error = "Password must be at least 8 characters"
                return@setOnClickListener
            }

            performLogin(email, password)
        }
    }

    @SuppressLint("Recycle")
    private fun playAnimation() {
        ObjectAnimator.ofFloat(binding.imageView, View.TRANSLATION_X, -30f, 30f).apply {
            duration = 6000
            repeatCount = ObjectAnimator.INFINITE
            repeatMode = ObjectAnimator.REVERSE
        }.start()
        val login = ObjectAnimator.ofFloat(binding.loginButton, View.ALPHA, 1f).setDuration(400)
        val title = ObjectAnimator.ofFloat(binding.titleTextView, View.ALPHA, 1f).setDuration(200)
        val message = ObjectAnimator.ofFloat(binding.messageTextView, View.ALPHA, 1f).setDuration(200)
        val email = ObjectAnimator.ofFloat(binding.emailTextView, View.ALPHA, 1f).setDuration(200)
        val emailedit = ObjectAnimator.ofFloat(binding.emailEditTextLayout, View.ALPHA, 1f).setDuration(200)
        val pass = ObjectAnimator.ofFloat(binding.passwordTextView, View.ALPHA, 1f).setDuration(200)
        val passedit = ObjectAnimator.ofFloat(binding.passwordEditTextLayout, View.ALPHA, 1f).setDuration(200)
        val together = AnimatorSet().apply {
            playTogether(login)
        }
        AnimatorSet().apply {
            playSequentially(title, message, email, emailedit, pass, passedit, together)
            start()
        }
    }

    private fun performLogin(email: String, password: String) {
        Log.d("LoginActivity", "Attempting login with email: $email")
        showLoading(true)
        val loginRequest = LoginRequest(email, password)

        apiService.login(loginRequest).enqueue(object : Callback<LoginResponse> {
            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                showLoading(false)
                if (response.isSuccessful) {
                    val loginResult = response.body()?.loginResult
                    if (loginResult != null && !loginResult.error) {
                        val sharedPref = getSharedPreferences("session", Context.MODE_PRIVATE)
                        val editor = sharedPref.edit()
                        val token = "Bearer ${loginResult.token}"
                        val name = loginResult.name
                        editor.putString("token", token)
                        editor.putString("name", name)
                        editor.putString("email", email)
                        editor.apply()

                        val userModel = UserModel(
                            email = email,
                            token = loginResult.token,
                            isLogin = true
                        )

                        viewModel.saveSession(userModel)

                        val intent = Intent(this@LoginActivity, MainActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                        finish()
                    } else {
                        Log.e("LoginActivity", "Login failed: ${loginResult?.message}")
                        showToast(loginResult?.message ?: "Login failed")
                    }
                } else {
                    Log.e("LoginActivity", "API Error: ${response.message()}")
                    showToast("Login failed: ${response.message()}")
                }
            }

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                showLoading(false)
                Log.e("LoginActivity", "Network error: ${t.message}")
                showToast("Network error: ${t.message}")
            }
        })
    }



    private fun showLoading(isLoading: Boolean) {
        binding.loadingOverlay.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}