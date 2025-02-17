package com.example.divisav2.Data.Entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "exchange_rates")
data class MonedaEntity (
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "currency_code")val currencyCode: String,     // Código de la divisa (USD, EUR)
    @ColumnInfo(name = "exchange_rate")val exchangeRate: Double,     // Tasa de cambio val exchangeRate: Double,     // Tasa de cambio
    @ColumnInfo(name = "base_currency") val baseCurrency: String,     // Moneda base (USD, por ejemplo)
    @ColumnInfo(name = "timestamp") val timestamp: Long,          // Fecha de sincronización en formato Unix
    @ColumnInfo(name = "sync_date") val syncDate: String          // Fecha de sincronización legible (yyyy-MM-dd)

)
