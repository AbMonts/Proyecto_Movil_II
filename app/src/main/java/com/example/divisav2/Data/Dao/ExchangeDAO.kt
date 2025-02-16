package com.example.divisav2.Data.Dao


import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.divisav2.Data.Entities.MonedaEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ExchangeDAO {

    // Insertar un nuevo tipo de cambio
    @Insert
    suspend fun insertExchangeRate(moneda: MonedaEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInfo(monedas: List<MonedaEntity>): List<Long>

    @Query("SELECT * FROM exchange_rates")
    suspend fun getAllMonedas(): List<MonedaEntity>

    @Query("SELECT * FROM exchange_rates")
    fun getAllFlow(): Flow<List<MonedaEntity>>

    // Eliminar todos los registros
    @Query("DELETE FROM exchange_rates")
    suspend fun deleteAllRates()
}
