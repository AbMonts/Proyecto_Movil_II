package com.example.divisav2.Data.Modelo

import kotlinx.serialization.Serializable


@Serializable
data class Moneda(
    val id: Int = 0,
    val currencyCode: String,     // Código de la divisa (USD, EUR)
    val exchangeRate: Double,     // Tasa de cambio
    val baseCurrency: String,     // Moneda base (USD, por ejemplo)
    val timestamp: Long,          // Fecha de sincronización en formato Unix
    val syncDate: String          // Fecha de sincronización legible (yyyy-MM-dd)
)
