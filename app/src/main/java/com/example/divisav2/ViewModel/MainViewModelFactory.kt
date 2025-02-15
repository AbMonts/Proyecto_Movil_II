package com.example.divisav2.ViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.divisav2.Data.Dao.ExchangeDAO
import com.example.divisav2.Data.Repository.ExchangeRepository

class MainViewModelFactory(private val dao: ExchangeDAO) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            val repository = ExchangeRepository(dao)
            return MainViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
