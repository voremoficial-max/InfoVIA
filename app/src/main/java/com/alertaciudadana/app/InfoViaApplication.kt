package com.alertaciudadana.app

import android.app.Application
import com.google.firebase.FirebaseApp

class InfoViaApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Inicialización explícita para que Firebase quede listo antes de que
        // la pantalla de reportes intente autenticarse o acceder a Firestore.
        if (FirebaseApp.getApps(this).isEmpty()) {
            FirebaseApp.initializeApp(this)
        }
    }
}
