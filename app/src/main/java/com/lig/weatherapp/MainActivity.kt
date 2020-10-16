package com.lig.weatherapp

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.google.android.gms.location.*
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.lig.weatherapp.models.WeatherResponse
import com.lig.weatherapp.netwrok.WeatherService
import kotlinx.android.synthetic.main.activity_main.*
import retrofit2.*
import retrofit2.converter.gson.GsonConverterFactory

class MainActivity : AppCompatActivity() {

    private lateinit var mFusedLocationClient : FusedLocationProviderClient
    private var mProgressDialog: Dialog? = null

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


    private fun getLocationWeatherDetails(latitude: Double, longitude: Double){
        if (Constants.isNetwrokAvailable(this)){
            Toast.makeText(this, "You have connected to the internet", Toast.LENGTH_SHORT).show()
            // declaration of retrofit
            val retrofit: Retrofit = Retrofit.Builder()
                                    .baseUrl(Constants.BASE_URL)
                                    .addConverterFactory(GsonConverterFactory.create()) //use json format
                                    .build()
            // create a service of the retrofit interface
            val service: WeatherService = retrofit.create(WeatherService::class.java)

            // create a call with service of retrofit
            val listCall: Call<WeatherResponse> = service.getWeather(
                latitude, longitude, Constants.METRIC_UNIT, Constants.APP_ID
            )
            showCustomProgressDialog()

            // make a call
            listCall.enqueue(object : Callback<WeatherResponse>{
                override fun onResponse(
                    call: Call<WeatherResponse>,
                    response: Response<WeatherResponse>
                ) {
                    if(response.isSuccessful){
                        hideProgressDialog()
                        val weatherList: WeatherResponse? = response.body()
                        Log.i("response result:", "$weatherList")
                        setupUI(weatherList!!)
                    }else{
                        val rc = response.code()
                        when(rc){
                            400 -> Log.e("response result:", "Error 400 bad connection")
                            404 -> Log.e("response result:", "Error 404 Not found")
                            else -> Log.e("response result:", "Generic Error")
                        }
                    }
                }
                override fun onFailure(call: Call<WeatherResponse>, t: Throwable) {
                    hideProgressDialog()
                    Log.e("response result:", "Error!! ${t.message.toString()}")
                }
            })

        }else{
            Toast.makeText(this, "You don't have connected to the internet", Toast.LENGTH_SHORT).show()
        }

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
            getLocationWeatherDetails(mLatitude.toDouble(), mLongitude.toDouble())
        }
    }

    private fun showCustomProgressDialog(){
        mProgressDialog = Dialog(this)
        mProgressDialog!!.setContentView(R.layout.dialog_custom_progress)
        mProgressDialog!!.show()
    }

    private fun hideProgressDialog(){
        if(mProgressDialog != null){
            mProgressDialog!!.dismiss()
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun setupUI(weatherList: WeatherResponse){
        for (i in weatherList.weather.indices){
            Log.i("Weather Name", weatherList.weather.toString())

            tv_main.text = weatherList.weather[i].main
            tv_main_description.text = weatherList.weather[i].description
            tv_temp.text = weatherList.main.temp.toString() + getUnit(application.resources.configuration.locales.toString()) // to know the current location unit
        }
    }

    private fun getUnit(value:String):String?{
        var value = "°C"
        if("US" == value || "LR" == value || "MM" == value){
            value = "°F"
        }
        return value
    }

}