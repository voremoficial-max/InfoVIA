package com.alertaciudadana.app.ui.screens.main

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.alertaciudadana.app.data.reports.FirebaseReportRepository
import com.alertaciudadana.app.data.reports.Report
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class ActiveReportsViewModel(
    private val repository: FirebaseReportRepository
) : ViewModel() {
    private val _reports = MutableStateFlow<List<Report>>(emptyList())
    val reports: StateFlow<List<Report>> = _reports.asStateFlow()
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    private var observeJob: Job? = null
    private var expirationJob: Job? = null

    fun observeCity(city: String) {
        observeJob?.cancel()
        expirationJob?.cancel()
        _isLoading.value = true
        _error.value = null
        observeJob = viewModelScope.launch {
            runCatching { repository.observeActiveReports(city) }
                .onSuccess { flow ->
                    flow.collect { value ->
                        _reports.value = filterExpired(value)
                        _isLoading.value = false
                        _error.value = null
                    }
                }
                .onFailure { error ->
                    _reports.value = emptyList()
                    _isLoading.value = false
                    _error.value = error.message ?: "No se pudieron cargar los reportes."
                }
        }
        expirationJob = viewModelScope.launch {
            while (isActive) {
                delay(30_000)
                _reports.value = filterExpired(_reports.value)
            }
        }
    }

    fun retry(city: String) = observeCity(city)

    fun stopObserving() {
        observeJob?.cancel()
        expirationJob?.cancel()
        observeJob = null
        expirationJob = null
    }

    private fun filterExpired(value: List<Report>): List<Report> {
        val now = com.google.firebase.Timestamp.now()
        return value.filter { report ->
            val expiresAt = report.expiresAt
            expiresAt == null || expiresAt > now
        }
    }

    override fun onCleared() {
        observeJob?.cancel()
        expirationJob?.cancel()
        super.onCleared()
    }
    class Factory(context: Context) : ViewModelProvider.Factory {
        private val appContext = context.applicationContext

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ActiveReportsViewModel::class.java)) {
                return ActiveReportsViewModel(FirebaseReportRepository(appContext)) as T
            }
            throw IllegalArgumentException("ViewModel desconocido: ${modelClass.name}")
        }
    }
}
