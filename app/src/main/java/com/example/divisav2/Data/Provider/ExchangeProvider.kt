package com.example.divisav2.Data.Provider

import android.app.Application
import android.content.ContentProvider
import android.content.ContentValues
import android.content.UriMatcher
import android.database.Cursor
import android.net.Uri
import android.util.Log
import com.example.divisav2.Data.Repository.ExchangeRepository
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import javax.inject.Inject

class ExchangeProvider : ContentProvider() {
    companion object {
        const val AUTHORITY = "com.example.divisav2.provider"
        //val CONTENT_URI: Uri = Uri.parse("content://$AUTHORITY/exchange_rates") //este va en el cliente :D

        private const val CODE_EXCHANGE_RATE = 1
        private const val CODE_EXCHANGE_RATE_ID = 2
        private const val CODE_EXCHANGE_RATE_FILTERED = 3

        private val uriMatcher = UriMatcher(UriMatcher.NO_MATCH).apply {
            addURI(AUTHORITY, "exchange_rates", CODE_EXCHANGE_RATE)
            addURI(AUTHORITY, "exchange_rates/#", CODE_EXCHANGE_RATE_ID)
            addURI(AUTHORITY, "exchange_rates/filtered/*/*/*", CODE_EXCHANGE_RATE_FILTERED)
        }
    }

    @Inject
    lateinit var repository: ExchangeRepository

    override fun onCreate(): Boolean {
        val appContext = context?.applicationContext as? Application ?: return false
        val entryPoint = EntryPointAccessors.fromApplication(appContext, ContentProviderEntryPoint::class.java)
        repository = entryPoint.exchangeRepository()
        return true
    }


    override fun query(
        uri: Uri,
        projection: Array<String>?,
        selection: String?,
        selectionArgs: Array<String>?,
        sortOrder: String?
    ): Cursor? {
        return when (uriMatcher.match(uri)) {
            CODE_EXCHANGE_RATE -> {
                repository.getExchangeRatesCursor()?.also { cursor ->
                    Log.d("ExchangeProvider", "Columnas en el cursor: ${cursor.columnNames.joinToString()}")
                    cursor.setNotificationUri(context?.contentResolver, uri)
                }
            }
            CODE_EXCHANGE_RATE_FILTERED -> {
                val segments = uri.pathSegments
                if (segments.size < 5) { //  currency, fechaInicio, fechaFin
                    throw IllegalArgumentException("Formato de URI inválido: $uri")
                }

                val currencyCode = segments[2]
                val fechaInicio = segments[3].toLongOrNull() ?: 0L
                val fechaFin = segments[4].toLongOrNull() ?: Long.MAX_VALUE

                repository.getFilteredExchangeRatesCursor(currencyCode, fechaInicio, fechaFin)?.also { cursor ->
                    cursor.setNotificationUri(context?.contentResolver, uri)
                }
            }else -> throw IllegalArgumentException("Unknown URI: $uri")
        }
    }

    override fun getType(uri: Uri): String? {
        return when (uriMatcher.match(uri)) {
            CODE_EXCHANGE_RATE -> "vnd.android.cursor.dir/vnd.$AUTHORITY.exchange_rates"
            CODE_EXCHANGE_RATE_ID -> "vnd.android.cursor.item/vnd.$AUTHORITY.exchange_rates"
            else -> throw IllegalArgumentException("Unknown URI: $uri")
        }
    }


    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        throw UnsupportedOperationException("Insert operation is not supported.")
    }

    override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<String>?): Int {
        throw UnsupportedOperationException("Update operation is not supported.")
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?): Int {
        throw UnsupportedOperationException("Delete operation is not supported.")
    }



    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface ContentProviderEntryPoint {
        fun exchangeRepository(): ExchangeRepository
    }


}