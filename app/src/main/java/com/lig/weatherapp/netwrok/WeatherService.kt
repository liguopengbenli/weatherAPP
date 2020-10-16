package com.lig.weatherapp.netwrok

import com.lig.weatherapp.models.WeatherResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

// https://openweathermap.org/current doc
interface WeatherService {

    @GET("2.5/weather")
    fun getWeather(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("units") units: String?, //define display format matric
        @Query("appid") appid: String?, //API Key
    ): Call<WeatherResponse>

}