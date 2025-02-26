package com.example.divisav2.Data.DataBase

import android.content.Context
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.Room
import com.example.divisav2.Data.Dao.ExchangeDAO
import com.example.divisav2.Data.Entities.MonedaEntity

@Database(entities = [MonedaEntity::class], version = 1, exportSchema = false)
abstract  class ExchangeDB : RoomDatabase()  {

    abstract fun exchangeDAO(): ExchangeDAO



}
