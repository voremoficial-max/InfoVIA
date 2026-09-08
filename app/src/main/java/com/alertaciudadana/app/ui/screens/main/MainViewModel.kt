package com.alertaciudadana.app.ui.screens.main

import android.annotation.SuppressLint
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.Priority
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.State

data class MapLocation(
    val latitude: Double,
    val longitude: Double,
    val isDefault: Boolean
)

/**
 * ViewModel de la pantalla principal (mapa).
 *
 * En esta fase no existe todavía integración con el SDK de mapas ni con
 * Firebase: se limita a resolver la ubicación que debe usarse para centrar
 * el futuro mapa, respetando si el usuario concedió o no el permiso.
 */
class MainViewModel : ViewModel() {

    private var _location = mutableStateOf<MapLocation?>(null)
    val location: State<MapLocation?> get() = _location

    private var _permissionRequested = mutableStateOf(false)
    val permissionRequested: State<Boolean> get() = _permissionRequested

    fun useDefaultLocation(defaultLat: Double, defaultLng: Double) {
        _permissionRequested.value = true
        _location.value = MapLocation(defaultLat, defaultLng, isDefault = true)
    }

    @SuppressLint("MissingPermission")
    fun fetchCurrentLocation(
        client: FusedLocationProviderClient,
        defaultLat: Double,
        defaultLng: Double
    ) {
        _permissionRequested.value = true
        viewModelScope.launch {
            runCatching {
                client.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, null).await()
            }.onSuccess { loc ->
                _location.value = if (loc != null) {
                    MapLocation(loc.latitude, loc.longitude, isDefault = false)
                } else {
                    MapLocation(defaultLat, defaultLng, isDefault = true)
                }
            }.onFailure {
                _location.value = MapLocation(defaultLat, defaultLng, isDefault = true)
            }
        }
    }
}
