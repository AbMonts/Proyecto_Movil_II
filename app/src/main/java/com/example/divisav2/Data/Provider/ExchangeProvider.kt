package com.example.divisav2.Data.Provider

import android.app.Application
import android.content.ContentProvider
import android.content.ContentUris
import android.content.ContentValues
import android.content.UriMatcher
import android.database.Cursor
import android.net.Uri
import com.example.divisav2.Data.Repository.ExchangeRepository
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import javax.inject.Inject

@AndroidEntryPoint
class ExchangeProvider : ContentProvider() {
    companion object {
        const val AUTHORITY = "com.example.divisav2.provider"
        val CONTENT_URI: Uri = Uri.parse("content://$AUTHORITY/exchange_rates")
    }

    @Inject
    lateinit var repository: ExchangeRepository

    override fun onCreate(): Boolean {
        val appContext = context?.applicationContext as Application
        val entryPoint = EntryPointAccessors.fromApplication(appContext, ContentProviderEntryPoint::class.java)
        repository = entryPoint.exchangeRepository()
        return true
    }

    override fun query(
        uri: Uri, projection: Array<String>?, selection: String?,
        selectionArgs: Array<String>?, sortOrder: String?
    ): Cursor? {
        return repository.getExchangeRatesCursor().apply {
            setNotificationUri(context?.contentResolver, uri)
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

    override fun getType(uri: Uri): String? {
        return "vnd.android.cursor.dir/vnd.com.example.divisav2.exchange_rates"
    }


    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface ContentProviderEntryPoint {
        fun exchangeRepository(): ExchangeRepository
    }

}