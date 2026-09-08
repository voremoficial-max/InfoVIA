package com.alertaciudadana.app.ui.screens.terms

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.alertaciudadana.app.R
import com.alertaciudadana.app.ui.theme.BrandPrimary
import com.alertaciudadana.app.util.ViewModelFactory

private data class TermsSection(
    val number: String,
    val title: String,
    val body: String
)

@Composable
fun TermsScreen(
    viewModelFactory: ViewModelFactory,
    onTermsAccepted: () -> Unit
) {
    val viewModel: TermsViewModel = viewModel(factory = viewModelFactory)

    val sections = listOf(
        TermsSection("1", "Aceptación", "Al seleccionar “Acepto los términos y condiciones” y continuar utilizando InfoVia, confirmas que has leído, comprendido y aceptado estas condiciones. Si no estás de acuerdo, no debes utilizar la aplicación."),
        TermsSection("2", "Finalidad del servicio", "InfoVia es una herramienta de información comunitaria que permite visualizar y enviar reportes relacionados principalmente con tránsito y accidentes. Su finalidad es informativa y de apoyo a la consulta comunitaria."),
        TermsSection("3", "Naturaleza de los reportes", "Los reportes son enviados por usuarios y no constituyen información oficial. InfoVia no garantiza que un reporte sea exacto, completo, actualizado o que represente fielmente la situación existente en el lugar indicado. Un reporte puede contener errores, retrasos o información que ya no corresponda con la situación real."),
        TermsSection("4", "Responsabilidad del usuario", "Cada usuario es responsable de la información que envía. No se deben crear reportes falsos, malintencionados, engañosos, repetitivos o destinados a provocar alarma, confusión o perjuicio a terceros. InfoVia podrá aplicar medidas técnicas o de moderación frente al uso indebido del servicio."),
        TermsSection("5", "Seguridad vial", "Nunca utilices InfoVia mientras conduces, manejas maquinaria o realizas una actividad que requiera atención continua. Si necesitas crear un reporte, hazlo únicamente cuando te encuentres en un lugar seguro y puedas prestar toda tu atención a la tarea."),
        TermsSection("6", "Emergencias y fuentes oficiales", "InfoVia no es un servicio de emergencias y no sustituye a la Policía, bomberos, servicios médicos, autoridades de tránsito ni a ninguna otra entidad oficial. Ante una emergencia o una situación que requiera asistencia inmediata, utiliza los canales oficiales de atención disponibles en tu zona."),
        TermsSection("7", "Ubicación", "Cuando otorgues permiso de ubicación, InfoVia podrá utilizar la ubicación del dispositivo para centrar la experiencia y facilitar la creación de reportes. Para crear un reporte también podrás seleccionar o ajustar manualmente una ubicación. El funcionamiento puede ser limitado si no autorizas el acceso a la ubicación."),
        TermsSection("8", "Datos y privacidad", "InfoVia está diseñada para funcionar sin registro tradicional. No se solicita nombre, correo electrónico, contraseña ni número telefónico para comenzar a utilizarla. Para permitir el funcionamiento técnico de los reportes, ciertos datos del reporte —como tipo, nivel, coordenadas, fecha, hora, estado e identificadores técnicos— pueden almacenarse en los servicios utilizados por la aplicación. No se pretende recopilar información personal innecesaria."),
        TermsSection("9", "Disponibilidad y exactitud", "El servicio puede presentar interrupciones, errores, retrasos, limitaciones de red o indisponibilidad de terceros. La existencia de un reporte en InfoVia no significa que la situación continúe vigente. Verifica siempre la información mediante fuentes oficiales cuando la decisión sea relevante para tu seguridad."),
        TermsSection("10", "Uso indebido", "Queda prohibido intentar vulnerar la aplicación, manipular deliberadamente los datos, automatizar envíos abusivos, interferir con el funcionamiento del servicio o utilizarlo para acosar, amenazar, engañar o perjudicar a otras personas."),
        TermsSection("11", "Cambios", "InfoVia podrá actualizar estas condiciones cuando sea necesario para reflejar cambios funcionales, técnicos, legales o de seguridad. Las nuevas condiciones serán presentadas dentro de la aplicación cuando corresponda."),
        TermsSection("12", "Aceptación final", "Al aceptar estos términos declaras que comprendes que InfoVia es una herramienta comunitaria e informativa y que utilizarás el servicio de forma responsable, segura y conforme a estas condiciones.")
    )

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Términos y condiciones", fontWeight = FontWeight.SemiBold) })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(padding)
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 18.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Row(
                        modifier = Modifier.padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            modifier = Modifier.size(50.dp),
                            shape = RoundedCornerShape(15.dp),
                            color = MaterialTheme.colorScheme.primary
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Filled.Description, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary)
                            }
                        }
                        Spacer(Modifier.size(14.dp))
                        Column {
                            Text("Antes de continuar", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text(
                                "Lee estas condiciones para conocer el uso responsable de InfoVia.",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(top = 3.dp)
                            )
                            Text(
                                "Última actualización: 7 de septiembre de 2026",
                                style = MaterialTheme.typography.labelMedium,
                                modifier = Modifier.padding(top = 7.dp)
                            )
                        }
                    }
                }

                sections.forEach { section ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Row(modifier = Modifier.padding(17.dp), verticalAlignment = Alignment.Top) {
                            Surface(
                                modifier = Modifier.size(34.dp),
                                shape = RoundedCornerShape(10.dp),
                                color = BrandPrimary.copy(alpha = 0.12f)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(section.number, color = BrandPrimary, fontWeight = FontWeight.Bold)
                                }
                            }
                            Spacer(Modifier.size(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(section.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                Text(
                                    section.body,
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.padding(top = 6.dp)
                                )
                            }
                        }
                    }
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Security, contentDescription = null, tint = BrandPrimary)
                        Spacer(Modifier.size(10.dp))
                        Text(
                            "Al continuar, aceptas utilizar InfoVia de forma responsable y segura.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            Surface(shadowElevation = 8.dp) {
                Button(
                    onClick = {
                        viewModel.acceptTerms()
                        onTermsAccepted()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 14.dp)
                        .height(54.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Filled.CheckCircle, contentDescription = null)
                    Spacer(Modifier.size(8.dp))
                    Text(stringResource(R.string.terms_accept), fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}
