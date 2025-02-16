package com.example.divisav2.ViewModel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.work.WorkManager
import com.example.divisav2.Data.Dao.ExchangeDAO
import com.example.divisav2.Data.Repository.ExchangeRepository

class MainViewModelFactory(private val context: Context, private val dao: ExchangeDAO) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            val repository = ExchangeRepository(dao)
            val workManager = WorkManager.getInstance(context) // Obtiene WorkManager
            return MainViewModel(repository, workManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
