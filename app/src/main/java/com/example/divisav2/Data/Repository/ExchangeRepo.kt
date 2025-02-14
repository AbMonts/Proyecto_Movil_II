package com.example.divisav2.Data.Repository

import com.example.divisav2.Data.Entities.MonedaEntity
import kotlinx.coroutines.flow.Flow

interface ExchangeRepository {

    // Insertar un tipo de cambio
    suspend fun insertExchangeRate(rate: MonedaEntity)

    // Obtener tipos de cambio por fecha como Flow
    fun getRatesByDate(date: String): Flow<List<MonedaEntity>>

    // Obtener tipos de cambio desde una fecha hasta ahora
    fun getRatesFromDate(startDate: String): Flow<List<MonedaEntity>>

    // Obtener todos los tipos de cambio
    fun getAllRates(): Flow<List<MonedaEntity>>

    // Eliminar tipos de cambio por una fecha específica
    suspend fun deleteRatesByDate(date: String)

    // Eliminar todos los tipos de cambio
    suspend fun deleteAllRates()
}
