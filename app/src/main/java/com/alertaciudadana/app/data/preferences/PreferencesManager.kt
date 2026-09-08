package com.alertaciudadana.app.data.preferences

import android.content.Context
import android.content.SharedPreferences

/**
 * Wrapper minimalista sobre SharedPreferences.
 *
 * InfoVia no maneja cuentas de usuario ni datos personales:
 * lo único que se persiste localmente en esta fase es la bandera de
 * aceptación de términos y condiciones, para no volver a mostrarlos
 * en aperturas posteriores de la app.
 */
class PreferencesManager(context: Context) {

    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    var termsAccepted: Boolean
        get() = prefs.getBoolean(KEY_TERMS_ACCEPTED, false)
        set(value) = prefs.edit().putBoolean(KEY_TERMS_ACCEPTED, value).apply()

    companion object {
        private const val PREFS_NAME = "infovia_prefs"
        private const val KEY_TERMS_ACCEPTED = "terms_accepted"
    }
}
