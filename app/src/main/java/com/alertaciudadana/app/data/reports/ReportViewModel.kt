package com.alertaciudadana.app.data.reports

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ReportFormState(
    val type: ReportType? = null,
    val level: ReportLevel? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val city: String? = null,
    val isSubmitting: Boolean = false,
    val success: Boolean = false,
    val errorMessage: String? = null
)

class ReportViewModel(
    private val repository: FirebaseReportRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ReportFormState())
    val state: StateFlow<ReportFormState> = _state.asStateFlow()

    fun setType(type: ReportType) {
        _state.value = _state.value.copy(type = type, errorMessage = null)
    }

    fun setLevel(level: ReportLevel) {
        _state.value = _state.value.copy(level = level, errorMessage = null)
    }

    fun setCity(city: String) {
        _state.value = _state.value.copy(city = city, errorMessage = null)
    }

    fun setLocation(latitude: Double, longitude: Double) {
        if (latitude !in -90.0..90.0 || longitude !in -180.0..180.0) {
            _state.value = _state.value.copy(errorMessage = "La ubicación seleccionada no es válida.")
            return
        }
        _state.value = _state.value.copy(
            latitude = latitude,
            longitude = longitude,
            errorMessage = null
        )
    }

    fun clearResult() {
        _state.value = ReportFormState(
            type = _state.value.type,
            level = _state.value.level,
            latitude = _state.value.latitude,
            longitude = _state.value.longitude,
            city = _state.value.city
        )
    }

    fun submit() {
        val current = _state.value
        val type = current.type
        val level = current.level
        val latitude = current.latitude
        val longitude = current.longitude
        val city = current.city

        if (type == null || level == null || latitude == null || longitude == null || city == null) {
            _state.value = current.copy(
                errorMessage = "Selecciona el tipo, el nivel y una ubicación antes de enviar."
            )
            return
        }

        if (latitude !in -4.3..13.6 || longitude !in -82.0..-66.8) {
            _state.value = current.copy(errorMessage = "La ubicación seleccionada está fuera del área de Colombia.")
            return
        }

        if (current.isSubmitting) return

        _state.value = current.copy(isSubmitting = true, errorMessage = null)

        viewModelScope.launch {
            runCatching {
                val now = java.time.ZonedDateTime.now()
                val date = now.toLocalDate().toString()
                val time = now.toLocalTime().withNano(0).toString()
                repository.createReport(
                    type = type,
                    level = level,
                    latitude = latitude,
                    longitude = longitude,
                    city = city,
                    date = date,
                    time = time
                )
            }.onSuccess {
                _state.value = _state.value.copy(isSubmitting = false, success = true)
            }.onFailure { error ->
                _state.value = _state.value.copy(
                    isSubmitting = false,
                    errorMessage = error.message ?: "No fue posible enviar el reporte."
                )
            }
        }
    }

    class Factory(context: Context) : ViewModelProvider.Factory {
        private val appContext = context.applicationContext

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ReportViewModel::class.java)) {
                return ReportViewModel(FirebaseReportRepository(appContext)) as T
            }
            throw IllegalArgumentException("ViewModel desconocido: ${modelClass.name}")
        }
    }
}
