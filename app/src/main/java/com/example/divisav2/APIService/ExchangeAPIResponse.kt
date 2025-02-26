package com.example.divisav2.APIService

import com.google.gson.annotations.SerializedName

data class ExchangeAPIResponse(
    @SerializedName("base_code") val baseCode: String,
    @SerializedName("conversion_rates") val conversionRates: Map<String, Double>,
    @SerializedName("time_last_update_unix") val timestamp: Long
)

