package com.example.divisav2Cliente.Data.DataBase

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.divisav2Cliente.Data.Dao.ExchangeDAO
import com.example.divisav2Cliente.Data.Entities.MonedaEntity

@Database(entities = [MonedaEntity::class], version = 1, exportSchema = false)
abstract  class ExchangeDB : RoomDatabase()  {

    abstract fun exchangeDAO(): ExchangeDAO



}
