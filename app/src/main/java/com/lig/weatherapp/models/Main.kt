package com.lig.weatherapp.models

import java.io.Serializable

data class Main (
    val temp: Double,
    val Pressure: Double,
    val humidity: Int,
    val temp_min: Double,
    val temp_max: Double,
    val sea_level: Double,
    val gmd_level: Double
): Serializable