package com.udacity.project4.utils

import android.Manifest
import android.annotation.TargetApi
import android.app.Activity
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.shouldShowRequestPermissionRationale
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsResponse
import com.google.android.gms.tasks.Task
import com.google.android.material.snackbar.Snackbar
import com.udacity.project4.BuildConfig
import com.udacity.project4.R

private const val REQUEST_LOCATION_PERMISSION = 1

fun Activity.hasBaseLocationPermissions(): Boolean {
    return (ActivityCompat.checkSelfPermission(
        this,
        Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(
        this,
        Manifest.permission.ACCESS_COARSE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED)
}

fun Activity.hasAllLocationPermissions(): Boolean {
    return listOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    ).all {
        ActivityCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
    }
}

@TargetApi(Build.VERSION_CODES.Q)
private fun hasAndroidQPermissions(activity: Activity): Boolean {
    return Build.VERSION.SDK_INT < Build.VERSION_CODES.Q ||
            ActivityCompat.checkSelfPermission(
                activity,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
}

@TargetApi(Build.VERSION_CODES.R)
private fun hasAndroidRPermissions(activity: Activity): Boolean {
    return Build.VERSION.SDK_INT < Build.VERSION_CODES.R ||
            ActivityCompat.checkSelfPermission(
                activity,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            ) == PackageManager.PERMISSION_GRANTED ||
            shouldShowRequestPermissionRationale(
                activity,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            )
}

fun Activity.requestLocationPermissions() {
    val permissions = mutableListOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )

    if (Build.VERSION.SDK_INT == Build.VERSION_CODES.Q) {
        permissions.add(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
    }

    ActivityCompat.requestPermissions(
        this,
        permissions.toTypedArray(), REQUEST_LOCATION_PERMISSION
    )
}

fun Activity.showPermissionSnackBar(view: View) {
    Snackbar.make(
        view,
        R.string.permission_denied_explanation, Snackbar.LENGTH_INDEFINITE
    )
        .setAction(R.string.settings) {
            // Displays App settings screen.
            startActivity(Intent().apply {
                action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                data = Uri.fromParts("package", BuildConfig.APPLICATION_ID, null)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            })
        }.show()
}

fun Activity.getLocationSettingsTask(resolve: Boolean = true): Task<LocationSettingsResponse> {
    val locationRequest = LocationRequest.create().apply {
        priority = LocationRequest.PRIORITY_LOW_POWER
    }
    val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
    val settingsClient = LocationServices.getSettingsClient(this)
    val locationSettingsResponseTask = settingsClient.checkLocationSettings(builder.build())
    locationSettingsResponseTask.addOnFailureListener { exception ->
        if (exception is ResolvableApiException && resolve) {
            try {
                exception.startResolutionForResult(
                    this,
                    REQUEST_LOCATION_PERMISSION
                )
            } catch (sendEx: IntentSender.SendIntentException) {
                Log.d(
                    "LocSettingsResponse",
                    "Error getting location settings resolution: " + sendEx.message
                )
            }
        } else {
            getLocationSettingsTask()
        }
    }
    return locationSettingsResponseTask
}