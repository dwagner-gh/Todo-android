package com.example.todo.repo

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import com.example.todo.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.withContext

class PrefsRepository(context: Context) {
    private val prefs = PreferenceManager.getDefaultSharedPreferences(context)
    private val webServiceUrlKey = context.getString(R.string.web_service_url_key)
    private val defaultWebServiceUrl =
        context.getString(R.string.web_service_url_default)
    private val importPeriodicKey = context.getString(R.string.import_periodic_key)

    suspend fun loadWebServiceUrl(): String = withContext(Dispatchers.IO) {
        prefs.getString(webServiceUrlKey, defaultWebServiceUrl) ?: defaultWebServiceUrl
    }

    // creates a flow that can be collected
    fun observeImportChanges() = channelFlow {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            if (importPeriodicKey == key) {
                trySend(prefs.getBoolean(importPeriodicKey, false)).isSuccess
            }
        }
        prefs.registerOnSharedPreferenceChangeListener(listener)
        awaitClose { prefs.unregisterOnSharedPreferenceChangeListener(listener) }
    }
}