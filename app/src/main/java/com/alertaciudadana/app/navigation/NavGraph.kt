package com.alertaciudadana.app.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.alertaciudadana.app.data.preferences.PreferencesManager
import com.alertaciudadana.app.ui.screens.main.MainScreen
import com.alertaciudadana.app.ui.screens.terms.TermsScreen
import com.alertaciudadana.app.ui.screens.welcome.WelcomeScreen
import com.alertaciudadana.app.util.ViewModelFactory

object Routes {
    const val WELCOME = "welcome"
    const val TERMS = "terms"
    const val MAIN = "main"
}

/**
 * Grafo de navegación principal.
 *
 * Si el usuario ya aceptó los términos y condiciones en una sesión
 * anterior, se salta directamente a la pantalla principal; de lo
 * contrario se muestra primero la bienvenida y luego los términos.
 */
@Composable
fun InfoViaNavGraph(
    preferencesManager: PreferencesManager,
    navController: NavHostController = rememberNavController()
) {
    val viewModelFactory = ViewModelFactory(preferencesManager)
    val startDestination = if (preferencesManager.termsAccepted) Routes.MAIN else Routes.WELCOME

    NavHost(navController = navController, startDestination = startDestination) {
        composable(Routes.WELCOME) {
            WelcomeScreen(
                onStartClick = { navController.navigate(Routes.TERMS) }
            )
        }
        composable(Routes.TERMS) {
            TermsScreen(
                viewModelFactory = viewModelFactory,
                onTermsAccepted = {
                    navController.navigate(Routes.MAIN) {
                        popUpTo(Routes.WELCOME) { inclusive = true }
                    }
                }
            )
        }
        composable(Routes.MAIN) {
            MainScreen()
        }
    }
}
