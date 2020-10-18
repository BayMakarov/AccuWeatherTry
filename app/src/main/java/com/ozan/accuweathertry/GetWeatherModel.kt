package com.ozan.accuweathertry

import com.google.gson.annotations.SerializedName

class GetWeatherModel {

    @SerializedName("WeatherIcon")
    val WeatherIcon : Int = 0

    @SerializedName("WeatherText")
    val WeatherText : String = ""

    @SerializedName("Temperature")
    val Temperature : TemperatureModel? = null

}