package com.example.divisav2Cliente.ui.Screens


import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.util.Log
import com.example.divisav2Cliente.Data.Modelo.Moneda

const val AUTHORITY = "com.example.divisav2.provider"
val CONTENT_URI: Uri = Uri.parse("content://$AUTHORITY/exchange_rates")
val FILTERED_CONTENT_URI: Uri = Uri.parse("content://com.example.divisav2.provider/exchange_rates/filtered")

fun getAllExchanges(context: Context): List<Moneda> {
    val list = mutableListOf<Moneda>()

    try {
        val projection = arrayOf(
            "id",
            "currency_code",
            "exchange_rate",
            "base_currency",
            "timestamp",
            "sync_date"
        )

        Log.d("ExchangeData", "Consultando ContentProvider en URI: $CONTENT_URI")

        val cursor: Cursor? = context.contentResolver.query(
            CONTENT_URI,
            projection,
            null,
            null,
            "timestamp DESC"
        )

        cursor?.use {
            Log.d("ExchangeData", "Numero de registros obtenidos: ${it.count}")

            val idIndex = it.getColumnIndex("id")
            val codeIndex = it.getColumnIndex("currency_code")
            val rateIndex = it.getColumnIndex("exchange_rate")
            val baseIndex = it.getColumnIndex("base_currency")
            val timestampIndex = it.getColumnIndex("timestamp")
            val syncDateIndex = it.getColumnIndex("sync_date")

            if (listOf(idIndex, codeIndex, rateIndex, baseIndex, timestampIndex, syncDateIndex).any { it == -1 }) {
                Log.w("ExchangeData", "Advertencia: Algunas columnas pueden estar ausentes en el cursor.")
            }

            while (it.moveToNext()) {
                val moneda = Moneda(
                    id = it.getLong(idIndex).toInt(),
                    currencyCode = it.getString(codeIndex),
                    exchangeRate = it.getDouble(rateIndex),
                    baseCurrency = it.getString(baseIndex),
                    timestamp = it.getLong(timestampIndex),
                    syncDate = it.getString(syncDateIndex)
                )
                list.add(moneda)

                Log.d("ExchangeData", "Moneda obtenida: $moneda")
            }
        } ?: Log.e("ExchangeData", "Cursor es nulo, posible problema con el ContentProvider")

    } catch (e: Exception) {
        Log.e("ExchangeData", "Error al consultar ContentProvider: ${e.message}", e)
    }

    return list.sortedByDescending { it.syncDate }
}


fun getFilteredExchanges(context: Context, currencyCode: String, fechaInicio: Long, fechaFin: Long): List<Moneda> {
    val list = mutableListOf<Moneda>()

    try {
        val fechaInicioSeg = fechaInicio / 1000
        val fechaFinSeg = fechaFin / 1000

        Log.d("getFilteredExchanges", "Filtrando con: currencyCode=$currencyCode, fechaInicio=$fechaInicioSeg, fechaFin=$fechaFinSeg")

        Log.d("ExchangeProvider", "Consulta recibida - currency: $currencyCode, fechaInicio: $fechaInicioSeg, fechaFin: $fechaFinSeg")

        val uri = FILTERED_CONTENT_URI.buildUpon()
            .appendPath(currencyCode)
            .appendPath(fechaInicioSeg.toString())
            .appendPath(fechaFinSeg.toString())
            .build()

        Log.d("getFilteredExchanges", "Consultando URI: $uri")

        val cursor: Cursor? = context.contentResolver.query(
            uri,
            arrayOf("id", "currency_code", "exchange_rate", "base_currency", "timestamp", "sync_date"),
            null,
            null,
            "timestamp DESC"
        )

        cursor?.use {
            Log.d("getFilteredExchanges", "Numero de registros obtenidos: ${it.count}")

            while (it.moveToNext()) {
                val moneda = Moneda(
                    id = it.getLong(it.getColumnIndexOrThrow("id")).toInt(),
                    currencyCode = it.getString(it.getColumnIndexOrThrow("currency_code")),
                    exchangeRate = it.getDouble(it.getColumnIndexOrThrow("exchange_rate")),
                    baseCurrency = it.getString(it.getColumnIndexOrThrow("base_currency")),
                    timestamp = it.getLong(it.getColumnIndexOrThrow("timestamp")),
                    syncDate = it.getString(it.getColumnIndexOrThrow("sync_date"))
                )
                list.add(moneda)
            }
        } ?: Log.e("ExchangeData", "Cursor es nulo, posible problema con el ContentProvider")

    } catch (e: Exception) {
        Log.e("ExchangeData", "Error al consultar ContentProvider: ${e.message}", e)
    }

    return list.sortedByDescending { it.syncDate }
}


