package com.example.divisav2.ViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.divisav2.Data.Dao.ExchangeDAO
import com.example.divisav2.Data.Entities.MonedaEntity
import com.example.divisav2.Data.Repository.ExchangeRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MainViewModel(private val repository: ExchangeRepository): ViewModel() {//14:00

    private val _monedas = MutableStateFlow<List<MonedaEntity>>(emptyList())
    val monedas: StateFlow<List<MonedaEntity>> get() = _monedas

    fun insertAllExchanges(monedas: List<MonedaEntity>) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertAll(monedas)
        }
    }


    fun getAllExchanges() {
        viewModelScope.launch(Dispatchers.IO) {
            _monedas.value = repository.getAll()
        }
    }
}
