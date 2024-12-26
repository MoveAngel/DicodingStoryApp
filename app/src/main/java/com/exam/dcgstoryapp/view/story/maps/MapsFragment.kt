package com.exam.dcgstoryapp.view.story.maps

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.exam.dcgstoryapp.R
import com.exam.dcgstoryapp.data.api.ApiConfig
import com.exam.dcgstoryapp.databinding.FragmentMapsBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.coroutines.launch

class MapsFragment : Fragment(), OnMapReadyCallback {

    private lateinit var googleMap: GoogleMap
    private var _binding: FragmentMapsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMapsBinding.inflate(inflater, container, false)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        setupFabActions()
        return binding.root
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        val indonesia = LatLng(-2.548926, 118.0148634)
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(indonesia, 4f))
        fetchStoriesWithLocation()
    }

    private fun setupFabActions() {
        binding.fabTerrain.setOnClickListener {
            showTerrainOptions()
        }

        binding.fabZoomIn.setOnClickListener {
            googleMap.animateCamera(CameraUpdateFactory.zoomIn())
        }

        binding.fabZoomOut.setOnClickListener {
            googleMap.animateCamera(CameraUpdateFactory.zoomOut())
        }
    }

    private fun showTerrainOptions() {
        val terrainTypes = arrayOf("Normal", "Satellite", "Terrain", "Hybrid")
        AlertDialog.Builder(requireContext())
            .setTitle("Select Map Type")
            .setItems(terrainTypes) { _, which ->
                when (which) {
                    0 -> googleMap.mapType = GoogleMap.MAP_TYPE_NORMAL
                    1 -> googleMap.mapType = GoogleMap.MAP_TYPE_SATELLITE
                    2 -> googleMap.mapType = GoogleMap.MAP_TYPE_TERRAIN
                    3 -> googleMap.mapType = GoogleMap.MAP_TYPE_HYBRID
                }
            }
            .show()
    }

    private fun fetchStoriesWithLocation() {
        val token = requireContext().getSharedPreferences("session", Context.MODE_PRIVATE)
            .getString("token", null)

        if (token != null) {
            val apiService = ApiConfig.getApiService()
            lifecycleScope.launch {
                try {
                    val response = apiService.getStoriesWithLocation("Bearer $token")
                    if (response.isSuccessful) {
                        response.body()?.listStory?.forEach { story ->
                            val position = story.lat?.let { story.lon?.let { it1 ->
                                LatLng(it,
                                    it1
                                )
                            } }
                            position?.let {
                                MarkerOptions()
                                    .position(it)
                                    .title(story.name)
                                    .snippet(story.description)
                            }?.let {
                                googleMap.addMarker(
                                    it
                                )
                            }
                        }
                    } else {
                        Log.e("MapsFragment", "Failed to fetch stories: ${response.message()}")
                    }
                } catch (e: Exception) {
                    Log.e("MapsFragment", "Error: ${e.message}")
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}