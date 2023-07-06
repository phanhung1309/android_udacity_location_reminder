package com.udacity.project4.locationreminders.savereminder.selectreminderlocation

import android.annotation.SuppressLint
import android.content.res.Resources
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.findNavController
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.databinding.FragmentSelectLocationBinding
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.utils.EspressoIdlingResource.wrapEspressoIdlingResource
import com.udacity.project4.utils.hasAllLocationPermissions
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import com.udacity.project4.utils.showPermissionSnackBar
import org.koin.android.ext.android.inject
import java.util.*

class SelectLocationFragment : BaseFragment(), OnMapReadyCallback {

    private val TAG = "SelectLocationFragment"

    override val _viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSelectLocationBinding

    private lateinit var map: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private lateinit var selectedMarker: Marker
    private lateinit var selectedPointOfInterest: PointOfInterest

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val layoutId = R.layout.fragment_select_location
        binding = DataBindingUtil.inflate(inflater, layoutId, container, false)

        binding.viewModel = _viewModel
        binding.lifecycleOwner = this

        setHasOptionsMenu(true)
        setDisplayHomeAsUpEnabled(true)

        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        binding.saveLocationButton.setOnClickListener {
            onLocationSelected()
        }
        return binding.root
    }

    private fun onLocationSelected() {
        if (this::selectedPointOfInterest.isInitialized) {
            _viewModel.selectedPOI.value = selectedPointOfInterest
            _viewModel.reminderSelectedLocationStr.value = selectedPointOfInterest.name
            _viewModel.longitude.value = selectedPointOfInterest.latLng.longitude
            _viewModel.latitude.value = selectedPointOfInterest.latLng.latitude
        }
        findNavController().popBackStack()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.map_options, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.normal_map -> {
            map.mapType = GoogleMap.MAP_TYPE_NORMAL
            true
        }
        R.id.hybrid_map -> {
            map.mapType = GoogleMap.MAP_TYPE_HYBRID
            true
        }
        R.id.satellite_map -> {
            map.mapType = GoogleMap.MAP_TYPE_SATELLITE
            true
        }
        R.id.terrain_map -> {
            map.mapType = GoogleMap.MAP_TYPE_TERRAIN
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    override fun onMapReady(p0: GoogleMap) {
        map = p0
        setMapStyle(map)
        setPoiClick(map)
        setMapLongClick(map)
        setMyLocation()
    }

    @SuppressLint("MissingPermission")
    private fun setMyLocation() {
        if (requireActivity().hasAllLocationPermissions()) {
            map.isMyLocationEnabled = true
            fusedLocationClient.lastLocation?.addOnSuccessListener { location ->
                if (location != null) {
                    // Handle the case when the last known location is available
                    val snippet = getString(
                        R.string.lat_long_snippet,
                        location.latitude,
                        location.longitude
                    )
                    val myLatLng = LatLng(location.latitude, location.longitude)

                    selectedPointOfInterest =
                        PointOfInterest(myLatLng, snippet, "My current location")

                    selectedMarker = map.addMarker(
                        MarkerOptions()
                            .snippet(snippet)
                            .position(myLatLng)
                            .title(getString(R.string.reminder_location))
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                    )!!

                    val zoomLevel = 18f
                    map.moveCamera(CameraUpdateFactory.newLatLngZoom(myLatLng, zoomLevel))
                    selectedMarker.showInfoWindow()
                } else {
                    // Request location updates
                    locationCallback = object : LocationCallback() {
                        override fun onLocationResult(locationResult: LocationResult) {
                            for (location in locationResult.locations) {
                                // Handle the case when a new location is available
                                val snippet = getString(
                                    R.string.lat_long_snippet,
                                    location.latitude,
                                    location.longitude
                                )
                                val myLatLng = LatLng(location.latitude, location.longitude)

                                selectedPointOfInterest =
                                    PointOfInterest(myLatLng, snippet, "My current location")

                                selectedMarker = map.addMarker(
                                    MarkerOptions()
                                        .snippet(snippet)
                                        .position(myLatLng)
                                        .title(getString(R.string.reminder_location))
                                        .icon(
                                            BitmapDescriptorFactory.defaultMarker(
                                                BitmapDescriptorFactory.HUE_GREEN
                                            )
                                        )
                                )!!

                                val zoomLevel = 18f
                                map.moveCamera(
                                    CameraUpdateFactory.newLatLngZoom(
                                        myLatLng,
                                        zoomLevel
                                    )
                                )
                                selectedMarker.showInfoWindow()

                                // Stop receiving location updates
                                fusedLocationClient.removeLocationUpdates(locationCallback)
                            }
                        }
                    }

                    fusedLocationClient.requestLocationUpdates(
                        LocationRequest(),
                        locationCallback,
                        null
                    )
                }
            }
        } else {
            requireActivity().showPermissionSnackBar(binding.root)
        }
    }

    private fun setMapLongClick(map: GoogleMap) {
        map.setOnMapLongClickListener { latLng ->
            if (this::selectedMarker.isInitialized) {
                selectedMarker.remove()
            }

            val snippet = getString(
                R.string.lat_long_snippet,
                latLng.latitude,
                latLng.longitude
            )

            selectedPointOfInterest = PointOfInterest(latLng, snippet, snippet)

            selectedMarker = map.addMarker(
                MarkerOptions()
                    .position(latLng)
                    .title(getString(R.string.reminder_location))
                    .snippet(snippet)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
            )!!

            selectedMarker.showInfoWindow()
            wrapEspressoIdlingResource {
                _viewModel.locationSelected.postValue(true)
            }
        }
    }

    private fun setPoiClick(map: GoogleMap) {
        map.setOnPoiClickListener { poi ->
            if (this::selectedMarker.isInitialized) {
                selectedMarker.remove()
            }

            selectedMarker = map.addMarker(
                MarkerOptions()
                    .position(poi.latLng)
                    .title(poi.name)
            )!!

            selectedPointOfInterest = poi
            selectedMarker.showInfoWindow()
            wrapEspressoIdlingResource {
                _viewModel.locationSelected.postValue(true)
            }
        }
    }

    private fun setMapStyle(map: GoogleMap) {
        try {
            val styleOptions = context?.let {
                MapStyleOptions.loadRawResourceStyle(it, R.raw.map_style)
            }
            val success = styleOptions?.let {
                map.setMapStyle(it)
            }
            if (!success!!) {
                Log.e(TAG, "Style parsing failed.")
            }
        } catch (e: Resources.NotFoundException) {
            Log.e(TAG, "Can't find style. Error: ", e)
        }
    }
}