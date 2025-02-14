package com.example.divisav2.ViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.divisav2.Data.Dao.ExchangeDAO
import com.example.divisav2.Data.Entities.MonedaEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MainViewModel(private val exchangeDAO: ExchangeDAO): ViewModel() {//14:00

    private val _monedas = MutableStateFlow<List<MonedaEntity>>(emptyList())
    val monedas: StateFlow<List<MonedaEntity>> get() = _monedas

    fun insertAllExchanges(monedas: List<MonedaEntity>) { // Para insertar en la base de datos
        viewModelScope.launch(Dispatchers.IO) {
            exchangeDAO.insertInfo(monedas)
        }
    }

    fun getAllExchanges() { // Para mostrar lo que se tiene actualmente en la base de datos
        viewModelScope.launch(Dispatchers.IO) {
            _monedas.value = exchangeDAO.getAllMonedas()
        }
    }
}
