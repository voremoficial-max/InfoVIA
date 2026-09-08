package com.alertaciudadana.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.alertaciudadana.app.data.preferences.PreferencesManager
import com.alertaciudadana.app.navigation.InfoViaNavGraph
import com.alertaciudadana.app.ui.theme.InfoViaTheme

class MainActivity : ComponentActivity() {

    private lateinit var preferencesManager: PreferencesManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        preferencesManager = PreferencesManager(applicationContext)

        setContent {
            InfoViaApp(preferencesManager)
        }
    }
}

@Composable
fun InfoViaApp(preferencesManager: PreferencesManager) {
    InfoViaTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            InfoViaNavGraph(preferencesManager = preferencesManager)
        }
    }
}
