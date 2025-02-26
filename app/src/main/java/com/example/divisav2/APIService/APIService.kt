package com.example.divisav2.APIService

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.http.GET

private const val BASE_URL =
    "https://v6.exchangerate-api.com/v6/a518eed84723c3fb05ab9e0e/"

private val retrofit = Retrofit.Builder()
    .addConverterFactory(ScalarsConverterFactory.create())
    .baseUrl(BASE_URL)
    .build()

// Interfaz del servicio de la API
interface APIExchange {
    @GET("latest/USD")  // Endpoint correcto
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


