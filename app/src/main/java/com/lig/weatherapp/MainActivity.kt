package com.lig.weatherapp

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import com.google.android.gms.location.*
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener

class MainActivity : AppCompatActivity() {

    private lateinit var mFusedLocationClient : FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)


        if(!isLocationEnable()){
            Toast.makeText(this, "Please turn on your GPS", Toast.LENGTH_SHORT).show()

            val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            startActivity(intent)
        }else{
            Dexter.withContext(this).withPermissions(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            ).withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(report: MultiplePermissionsReport) {
                    if(report.areAllPermissionsGranted()){
                        requestNewLocationData()
                    }
                }
                override fun onPermissionRationaleShouldBeShown( //after user deny the permission
                        permissions: MutableList<PermissionRequest>,
                        token: PermissionToken
                ) {
                    //Toast.makeText(this@MainActivity, "You have deny the permission", Toast.LENGTH_SHORT).show()
                    showRationalDialogForPermission()
                }
            }).onSameThread().check()
            Toast.makeText(this, "Your location provider is already on", Toast.LENGTH_SHORT).show()
        }
    }

    private fun isLocationEnable(): Boolean{
        val locationManaer = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManaer.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManaer.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    //custom method for go in settings permission for this app
    private fun showRationalDialogForPermission(){
        AlertDialog.Builder(this)
                .setMessage("It looks like you have turned off permission required for this feature. It can be enable under Application settings")
                .setPositiveButton("GO TO SETTINGS")
                { _,_-> // we don't use parameters
                    try {
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                        val uri = Uri.fromParts("package", packageName, null)
                        intent.data = uri
                        startActivity(intent)
                    }catch (e: ActivityNotFoundException){
                        e.printStackTrace()
                    }
                }
                .setNegativeButton("Cancel"){ dialog, _->
                    dialog.dismiss()
                }.show()
    }

    @SuppressLint("MissingPermission")
    private fun requestNewLocationData(){
        var mLocationRequest = LocationRequest()
        mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        mLocationRequest.interval = 1000
        mLocationRequest.numUpdates = 1
        // we already check permission so we can Suppress error
        mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper())
    }

    private val mLocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            val mLastLocation: Location = locationResult.lastLocation
            val mLatitude = mLastLocation.latitude
            val mLongitude = mLastLocation.longitude
            Log.i("Current Longitude", "$mLongitude")
        }
    }

}