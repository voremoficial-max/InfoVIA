package com.alertaciudadana.app.ui.screens.terms

import androidx.lifecycle.ViewModel
import com.alertaciudadana.app.data.preferences.PreferencesManager

class TermsViewModel(private val preferencesManager: PreferencesManager) : ViewModel() {

    fun acceptTerms() {
        preferencesManager.termsAccepted = true
    }
}
