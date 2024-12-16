package com.exam.dcgstoryapp.view.story

import android.Manifest
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Environment
import android.util.Log
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModelProvider
import com.exam.dcgstoryapp.R
import com.exam.dcgstoryapp.data.api.ApiConfig
import com.exam.dcgstoryapp.data.api.ApiService
import com.exam.dcgstoryapp.databinding.FragmentAddStoryBinding
import com.exam.dcgstoryapp.view.ViewModelFactory
import com.exam.dcgstoryapp.view.login.LoginActivity
import com.exam.dcgstoryapp.view.login.LoginViewModel
import com.exam.dcgstoryapp.view.main.MainActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch
import okhttp3.ResponseBody
import org.json.JSONObject
import java.io.FileOutputStream

class AddStoryFragment : Fragment() {
    private var _binding: FragmentAddStoryBinding? = null
    private val binding get() = _binding!!
    private lateinit var loginViewModel: LoginViewModel
    private lateinit var apiService: ApiService

    private var selectedUri: Uri? = null
    private var temporaryUri: Uri? = null
    private var currentToken: String = ""

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                if (currentPermissionType == PermissionType.GALLERY) {
                    pickImageFromGallery()
                } else if (currentPermissionType == PermissionType.CAMERA) {
                    openCamera()
                }
            } else {
                showPermissionDeniedMessage()
            }
        }

    private val pickImageLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val selectedImageUri = result.data?.data
                if (selectedImageUri != null) {
                    selectedUri = selectedImageUri
                    temporaryUri = selectedUri
                    updateImagePreview(selectedUri!!)
                } else {
                    Toast.makeText(requireContext(), "No image selected", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(requireContext(), "Image selection canceled", Toast.LENGTH_SHORT).show()
            }
        }

    private val takePictureLauncher =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { isSuccess ->
            if (isSuccess && cameraImageUri != null) {
                selectedUri = cameraImageUri
                temporaryUri = selectedUri
                updateImagePreview(cameraImageUri!!)
            } else {
                if (temporaryUri != null) {
                    selectedUri = temporaryUri
                    updateImagePreview(temporaryUri!!)
                } else {
                    Toast.makeText(requireContext(), "Image capture was canceled", Toast.LENGTH_SHORT).show()
                }
            }
        }


    private var cameraImageUri: Uri? = null
    private var currentPermissionType: PermissionType = PermissionType.GALLERY

    enum class PermissionType {
        GALLERY, CAMERA
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddStoryBinding.inflate(inflater, container, false)

        apiService = ApiConfig.getApiService()

        val factory = ViewModelFactory.getInstance(requireContext())
        loginViewModel = ViewModelProvider(this, factory)[LoginViewModel::class.java]

        lifecycleScope.launch {
            loginViewModel.getSession().collect { user ->
                if (!user.isLogin) {
                    redirectToLogin()
                } else {
                    currentToken = user.token
                }
            }
        }

        setupUI()
        setupOnBackPressed()

        return binding.root
    }

    private fun setupUI() {
        binding.buttonPickImage.setOnClickListener {
            currentPermissionType = PermissionType.GALLERY
            requestImagePickerPermission()
        }


        binding.buttonPickCamera.setOnClickListener {
            currentPermissionType = PermissionType.CAMERA
            requestCameraPermission()
        }

        binding.buttonSubmit.setOnClickListener {
            uploadStory()
        }

        binding.buttonSubmit.isEnabled = false
    }

    private fun setupOnBackPressed() {
        val onBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (selectedUri != null || binding.editTextDescription.text.isNotEmpty()) {
                    showDiscardChangesDialog()
                } else {
                    isEnabled = false
                    requireActivity().onBackPressedDispatcher.onBackPressed()
                }
            }
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, onBackPressedCallback)
    }

    private fun showDiscardChangesDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Discard Changes")
            .setMessage("Do you want to discard the current story?")
            .setPositiveButton("Discard") { _, _ ->
                (activity as? MainActivity)?.navigateToFragment(ListStoryFragment(), R.id.menu_home)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun requestCameraPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) ==
            PackageManager.PERMISSION_GRANTED) {
            openCamera()
        } else {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun openCamera() {
        cameraImageUri = createImageUri()
        Log.d("AddStoryFragment", "Camera URI: $cameraImageUri")

        if (cameraImageUri != null) {
            takePictureLauncher.launch(cameraImageUri)
        } else {
            Log.e("AddStoryFragment", "Failed to create URI for camera")
        }
    }


    private fun createImageUri(): Uri? {
        return try {
            val imageFileName = "JPEG_${System.currentTimeMillis()}_"
            val storageDir = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            val image = File.createTempFile(imageFileName, ".jpg", storageDir)
            Log.d("AddStoryFragment", "Image File Created: ${image.absolutePath}")
            FileProvider.getUriForFile(
                requireContext(),
                "${requireContext().packageName}.fileprovider",
                image
            )
        } catch (e: Exception) {
            Log.e("AddStoryFragment", "Error creating file URI: ${e.message}")
            null
        }
    }



    private fun requestImagePickerPermission() {
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> {
                if (ContextCompat.checkSelfPermission(
                        requireContext(),
                        Manifest.permission.READ_MEDIA_IMAGES
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    pickImageFromGallery()
                } else {
                    requestPermissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
                }
            }
            else -> {
                if (ContextCompat.checkSelfPermission(
                        requireContext(),
                        Manifest.permission.READ_EXTERNAL_STORAGE
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    pickImageFromGallery()
                } else {
                    requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                }
            }
        }
    }


    private fun updateImagePreview(uri: Uri) {
        binding.imageViewPreview.setImageURI(uri)
        binding.buttonSubmit.isEnabled = true
    }

    private fun pickImageFromGallery() {
        val intent = Intent(
            Intent.ACTION_PICK,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        )
        pickImageLauncher.launch(intent)
    }

    private fun uploadStory() {
        val description = binding.editTextDescription.text.toString().trim()

        if (description.isEmpty()) {
            binding.editTextDescription.error = "Description cannot be empty"
            return
        }

        if (selectedUri == null) {
            Toast.makeText(
                requireContext(),
                "Please select an image",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        setLoadingState(true)

        val file = prepareImageFile()

        val photoMultipart = createPhotoMultipart(file)
        val descriptionRequestBody = description.toRequestBody("text/plain".toMediaTypeOrNull())

        lifecycleScope.launch {
            try {
                val response = apiService.addStory(
                    "Bearer $currentToken",
                    photoMultipart,
                    descriptionRequestBody
                )

                if (response.isSuccessful) {
                    showUploadSuccess()
                } else {
                    val message = extractErrorMessage(response.errorBody())
                    showUploadError(message)
                }
            } catch (e: Exception) {
                showUploadError(e.message ?: "Unknown error occurred")
            } finally {
                setLoadingState(false)
            }
        }
    }

    private fun prepareImageFile(): File {
        val contentResolver = requireContext().contentResolver
        val inputStream = contentResolver.openInputStream(selectedUri!!)

        val tempFile = File(requireContext().cacheDir, "original_image.jpg")
        tempFile.outputStream().use {
            inputStream?.copyTo(it)
        }

        return compressImageFile(tempFile)
    }

    private fun compressImageFile(file: File): File {
        val bitmap = BitmapFactory.decodeFile(file.path)

        val compressedFile = File(requireContext().cacheDir, "compressed_image.jpg")

        var quality = 100
        do {
            val outputStream = FileOutputStream(compressedFile)
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
            outputStream.flush()
            outputStream.close()

            if (compressedFile.length() > 1_000_000) {
                quality -= 10
            } else {
                break
            }
        } while (quality > 0)

        return compressedFile
    }

    private fun createPhotoMultipart(file: File): MultipartBody.Part {
        val requestFile = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
        return MultipartBody.Part.createFormData(
            "photo",
            file.name,
            requestFile
        )
    }

    private fun setLoadingState(isLoading: Boolean) {
        binding.buttonSubmit.isEnabled = !isLoading
        binding.buttonPickImage.isEnabled = !isLoading
        binding.editTextDescription.isEnabled = !isLoading
    }

    private fun showUploadSuccess() {
        Toast.makeText(
            requireContext(),
            "Story uploaded successfully",
            Toast.LENGTH_SHORT
        ).show()

        (activity as? MainActivity)?.navigateToFragment(ListStoryFragment(), R.id.menu_home)
    }

    private fun showUploadError(message: String) {
        if (message.contains("duplicate", true)) {
            Toast.makeText(
                requireContext(),
                "Duplicate image detected. Please use a different image.",
                Toast.LENGTH_SHORT
            ).show()
        } else {
            Toast.makeText(
                requireContext(),
                "Failed to upload story: $message",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun showPermissionDeniedMessage() {
        Toast.makeText(
            requireContext(),
            "Storage permission is required to select an image",
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun redirectToLogin() {
        Toast.makeText(
            requireContext(),
            "Please log in again",
            Toast.LENGTH_SHORT
        ).show()

        startActivity(Intent(requireContext(), LoginActivity::class.java))
        activity?.finish()
    }

    private fun extractErrorMessage(errorBody: ResponseBody?): String {
        return try {
            val jsonObject = JSONObject(errorBody?.string() ?: "")
            jsonObject.getString("message")
        } catch (e: Exception) {
            "An error occurred"
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}