package com.alertaciudadana.app.util

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.alertaciudadana.app.data.preferences.PreferencesManager
import com.alertaciudadana.app.ui.screens.terms.TermsViewModel

/**
 * Factory simple para ViewModels que requieren [PreferencesManager].
 * Se evita traer un framework de inyección de dependencias completo
 * (Hilt/Koin) hasta que el proyecto realmente lo necesite, manteniendo
 * la Fase 1 lo más ligera posible.
 */
class ViewModelFactory(private val preferencesManager: PreferencesManager) :
    ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(TermsViewModel::class.java) ->
                TermsViewModel(preferencesManager) as T
            else -> throw IllegalArgumentException("ViewModel desconocido: ${modelClass.name}")
        }
    }
}
