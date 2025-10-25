package com.dadm.localizadordadm.viewmodel

import android.annotation.SuppressLint
import android.app.Application
import android.location.Location
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.dadm.localizadordadm.api.OverpassService
import com.dadm.localizadordadm.data.OsmElement
import com.dadm.localizadordadm.data.SettingsDataStore
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.osmdroid.util.GeoPoint


data class MapState(
    // Usamos GeoPoint para la ubicación
    val userLocation: GeoPoint = GeoPoint(4.7110, -74.0721),
    val nearbyPois: List<OsmElement> = emptyList(),
    val searchRadiusKm: Double = 5.0,
    val isLoading: Boolean = false,
    val error: String? = null
)

class MapViewModel(application: Application) : AndroidViewModel(application) {

    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(application)

    private val overpassService = OverpassService()
    private val settingsDataStore = SettingsDataStore(application)

    var state by mutableStateOf(MapState())
        private set

    init {
        viewModelScope.launch {
            settingsDataStore.searchRadiusFlow.collect { radius ->
                state = state.copy(searchRadiusKm = radius)
                fetchCurrentLocationAndSearch()
            }
        }
    }

    fun fetchCurrentLocationAndSearch() {
        state = state.copy(isLoading = true, error = null)
        getCurrentLocation { location ->
            if (location != null) {
                // Conversión de Location a GeoPoint
                val geoPoint = GeoPoint(location.latitude, location.longitude)
                state = state.copy(userLocation = geoPoint)
                searchNearbyPois(geoPoint, state.searchRadiusKm)
            } else {
                state = state.copy(
                    isLoading = false,
                    error = "No se pudo obtener la ubicación. Activa el GPS."
                )
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun getCurrentLocation(onSuccess: (Location?) -> Unit) {
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? -> onSuccess(location) }
            .addOnFailureListener { onSuccess(null) }
    }

    private fun searchNearbyPois(location: GeoPoint, radiusKm: Double) {
        viewModelScope.launch {
            try {
                val response = withContext(this.coroutineContext) {
                    overpassService.searchNearbyPois(location.latitude, location.longitude, radiusKm)
                }

                state = state.copy(
                    nearbyPois = response.elements,
                    isLoading = false,
                    error = null
                )
            } catch (e: Exception) {
                state = state.copy(
                    isLoading = false,
                    error = "Error de Red/API: ${e.message ?: "Conexión fallida."}"
                )
            }
        }
    }

    fun saveSearchRadius(radius: Double) {
        viewModelScope.launch {
            settingsDataStore.saveSearchRadius(radius)
        }
    }
}