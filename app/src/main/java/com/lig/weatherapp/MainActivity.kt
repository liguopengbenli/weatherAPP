package com.lig.weatherapp

import android.content.Context
import android.content.Intent
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if(!isLocationEnable()){
            Toast.makeText(this, "Please turn on your GPS", Toast.LENGTH_SHORT).show()

            val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            startActivity(intent)
        }else{
            Toast.makeText(this, "Your location provider is already on", Toast.LENGTH_SHORT).show()
        }
    }

    private fun isLocationEnable(): Boolean{
        val locationManaer = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManaer.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManaer.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

}