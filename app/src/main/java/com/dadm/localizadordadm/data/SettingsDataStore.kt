package com.dadm.localizadordadm.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsDataStore(context: Context) {
    private val dataStore = context.dataStore

    private val SEARCH_RADIUS_KEY = doublePreferencesKey("search_radius")
    private val DEFAULT_RADIUS = 5.0

    val searchRadiusFlow: Flow<Double> = dataStore.data
        .map { preferences ->
            preferences[SEARCH_RADIUS_KEY] ?: DEFAULT_RADIUS
        }

    suspend fun saveSearchRadius(radius: Double) {
        dataStore.edit { preferences ->
            preferences[SEARCH_RADIUS_KEY] = radius.coerceAtLeast(1.0).coerceAtMost(50.0)
        }
    }
}