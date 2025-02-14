package com.example.divisav2.Application

import android.app.Application
import androidx.room.Room
import com.example.divisav2.Data.DataBase.ExchangeDB

class RoomApp: Application() {
    companion object {
        lateinit var database: ExchangeDB

    }
    override fun onCreate() {
        super.onCreate()
        database = Room.databaseBuilder(
            applicationContext,
            ExchangeDB::class.java,
            "exchange_database").build()
    }
}