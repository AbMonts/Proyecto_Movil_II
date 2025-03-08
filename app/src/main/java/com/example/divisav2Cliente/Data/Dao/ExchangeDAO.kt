package com.example.divisav2Cliente.Data.Dao


import android.database.Cursor
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.divisav2Cliente.Data.Entities.MonedaEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ExchangeDAO {

    // Insertar un nuevo tipo de cambio
    @Insert
    suspend fun insertExchangeRate(moneda: MonedaEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInfo(monedas: List<MonedaEntity>): List<Long>

    @Query("SELECT * FROM exchange_rates ORDER BY sync_date DESC")
    suspend fun getAllMonedas(): List<MonedaEntity>

    @Query("SELECT * FROM exchange_rates ORDER BY sync_date DESC")
    fun getAllFlow(): Flow<List<MonedaEntity>>



    // Eliminar todos los registros
    @Query("DELETE FROM exchange_rates")
    suspend fun deleteAllRates()


//pa content prov
    @Query("SELECT * FROM exchange_rates")
    fun getAllExchangeRatesCursor(): Cursor

    @Query("SELECT * FROM exchange_rates WHERE id = :id")
    fun getExchangeRateByIdCursor(id: Long): Cursor
}
