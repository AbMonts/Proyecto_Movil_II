package com.example.divisav2.APIService

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.http.GET

private const val BASE_URL =
    "https://v6.exchangerate-api.com/v6/05b9a99e2b032c37f3ab837b/"

private val retrofit = Retrofit.Builder()
    .addConverterFactory(ScalarsConverterFactory.create())
    .baseUrl(BASE_URL)
    .build()

// Interfaz del servicio de la API
interface APIExchange {
    @GET("latest/MXN")  // Endpoint
    suspend fun getExchangeRates(): ExchangeAPIResponse
}


object ExchangeAPI {
    val service: APIExchange by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(APIExchange::class.java)
    }
}


