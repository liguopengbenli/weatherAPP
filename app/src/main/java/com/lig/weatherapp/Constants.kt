package com.lig.weatherapp

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build

object Constants{

    // http://api.openweathermap.org/data/2.5/weather?q=London,uk&APPID=7b29eacf5429c8e7058298fa20e7d6fd call example
    const val APP_ID: String = "7b29eacf5429c8e7058298fa20e7d6fd"
    const val BASE_URL: String = "https://api.openweathermap.org/data/" //the part is fix
    const val METRIC_UNIT: String = "metric"
    const val PREFERENCE_NAME = "WeatherAppPreference"
    const val WEATHER_RESPONSE_DATA = "weather_response_data"

    fun isNetwrokAvailable(context: Context) : Boolean{
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            val network = connectivityManager.activeNetwork?: return false // if there is no active network return null -> return false
            val activeNetwork = connectivityManager.getNetworkCapabilities(network)?: return false

            return when {
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
                else -> false
            }

        }else{
            val networkInfo = connectivityManager.activeNetworkInfo
            return networkInfo != null && networkInfo.isConnectedOrConnecting
        }
    }


}