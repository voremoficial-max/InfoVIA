package com.alertaciudadana.app.ui.screens.main

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.drawable.BitmapDrawable
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.net.Uri
import android.net.ConnectivityManager
import android.net.Network
import android.location.Geocoder
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddLocationAlt
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.alertaciudadana.app.BuildConfig
import com.alertaciudadana.app.R
import com.alertaciudadana.app.data.reports.Report
import com.alertaciudadana.app.data.reports.ReportLevel
import com.alertaciudadana.app.data.reports.ReportType
import com.alertaciudadana.app.data.reports.ReportViewModel
import com.alertaciudadana.app.ui.theme.ReportLevelAlto
import com.alertaciudadana.app.ui.theme.ReportLevelBajo
import com.alertaciudadana.app.ui.theme.ReportLevelMedio
import com.alertaciudadana.app.ui.theme.ReportTypeAccidente
import com.alertaciudadana.app.ui.theme.ReportTypeTransito
import com.alertaciudadana.app.util.UpdateChecker
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.MapEventsOverlay
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun MainScreen(
    viewModel: MainViewModel = viewModel()
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val reportsViewModel: ActiveReportsViewModel = viewModel(factory = ActiveReportsViewModel.Factory(context.applicationContext))
    val scope = rememberCoroutineScope()
    var showReportDialog by remember { mutableStateOf(false) }
    var showCityPicker by remember { mutableStateOf(false) }
    var showMoreMenu by remember { mutableStateOf(false) }
    var showInfoDialog by remember { mutableStateOf(false) }
    var showDonateDialog by remember { mutableStateOf(false) }
    var availableUpdate by remember { mutableStateOf<UpdateChecker.UpdateInfo?>(null) }
    var selectedCity by remember { mutableStateOf(ColombianCities.all.first()) }
    var selectedReport by remember { mutableStateOf<Report?>(null) }
    val reports by reportsViewModel.reports.collectAsStateWithLifecycle()
    val reportsLoading by reportsViewModel.isLoading.collectAsStateWithLifecycle()
    val reportsError by reportsViewModel.error.collectAsStateWithLifecycle()

    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    fun hasLocationPermission(): Boolean =
        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

    var isOnline by remember { mutableStateOf(true) }
    DisposableEffect(context) {
        val connectivityManager = context.getSystemService(ConnectivityManager::class.java)
        fun refresh() {
            isOnline = connectivityManager.activeNetwork != null
        }
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) { isOnline = true }
            override fun onLost(network: Network) { refresh() }
        }
        refresh()
        connectivityManager.registerDefaultNetworkCallback(callback)
        onDispose { runCatching { connectivityManager.unregisterNetworkCallback(callback) } }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        if (results.values.any { it }) {
            viewModel.fetchCurrentLocation(fusedLocationClient, selectedCity.center.latitude, selectedCity.center.longitude)
        }
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner, selectedCity.name) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> reportsViewModel.observeCity(selectedCity.name)
                Lifecycle.Event.ON_STOP -> reportsViewModel.stopObserving()
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        if (lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
            reportsViewModel.observeCity(selectedCity.name)
        }
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            reportsViewModel.stopObserving()
        }
    }

    LaunchedEffect(selectedCity.name) {
        selectedReport = null
    }

    LaunchedEffect(isOnline) {
        if (isOnline) {
            availableUpdate = UpdateChecker.checkIfDue(context.applicationContext)
        }
    }

    LaunchedEffect(Unit) {
        if (hasLocationPermission()) {
            viewModel.fetchCurrentLocation(fusedLocationClient, selectedCity.center.latitude, selectedCity.center.longitude)
        } else {
            permissionLauncher.launch(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
            )
        }
    }

    val currentLocation = viewModel.location.value

    LaunchedEffect(currentLocation?.latitude, currentLocation?.longitude) {
        currentLocation?.let { location ->
            ColombianCities.findForLocation(location.latitude, location.longitude)?.let { city ->
                if (city.name != selectedCity.name && !location.isDefault) {
                    selectedCity = city
                }
            }
        }
    }
    val currentLocationInCity = currentLocation?.let {
        selectedCity.bounds.contains(GeoPoint(it.latitude, it.longitude))
    } == true

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("InfoVia · ${selectedCity.name}") },
                actions = {
                    Box {
                        androidx.compose.material3.IconButton(onClick = { showMoreMenu = true }) {
                            Icon(Icons.Filled.MoreVert, contentDescription = "Más opciones")
                        }
                        DropdownMenu(
                            expanded = showMoreMenu,
                            onDismissRequest = { showMoreMenu = false }
                        ) {
                            DropdownMenuItem(
                                leadingIcon = { Icon(Icons.Filled.Info, contentDescription = null) },
                                text = { Text("Información") },
                                onClick = {
                                    showMoreMenu = false
                                    showInfoDialog = true
                                }
                            )
                            DropdownMenuItem(
                                leadingIcon = { Icon(Icons.Filled.VolunteerActivism, contentDescription = null) },
                                text = { Text("Donar") },
                                onClick = {
                                    showMoreMenu = false
                                    showDonateDialog = true
                                }
                            )
                            DropdownMenuItem(
                                leadingIcon = { Icon(Icons.Filled.Refresh, contentDescription = null) },
                                text = { Text("Buscar actualización") },
                                onClick = {
                                    showMoreMenu = false
                                    scope.launch {
                                        val update = UpdateChecker.checkIfDue(context.applicationContext, force = true)
                                        availableUpdate = update
                                        if (update == null) {
                                            snackbarHostState.showSnackbar("InfoVia ya está actualizada")
                                        }
                                    }
                                }
                            )
                        }
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (!isOnline) {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
                    Row(Modifier.fillMaxWidth().padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Warning, contentDescription = null)
                        Spacer(Modifier.size(8.dp))
                        Text("Sin conexión. Los reportes pueden no actualizarse hasta recuperar Internet.", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
            reportsError?.let { error ->
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
                    Row(Modifier.fillMaxWidth().padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text("No se pudieron cargar los reportes.", fontWeight = FontWeight.SemiBold)
                            Text(error, style = MaterialTheme.typography.bodySmall)
                        }
                        TextButton(onClick = { reportsViewModel.retry(selectedCity.name) }) {
                            Icon(Icons.Filled.Refresh, contentDescription = null)
                            Spacer(Modifier.size(4.dp))
                            Text("Reintentar")
                        }
                    }
                }
            }
            Box {
                OutlinedButton(onClick = { showCityPicker = true }, modifier = Modifier.fillMaxWidth()) {
                    Text("Ciudad: ${selectedCity.name} · Colombia")
                }
                DropdownMenu(expanded = showCityPicker, onDismissRequest = { showCityPicker = false }) {
                    ColombianCities.all.forEach { city ->
                        DropdownMenuItem(
                            text = { Text(city.name) },
                            onClick = {
                                selectedCity = city
                                showCityPicker = false
                                viewModel.useDefaultLocation(city.center.latitude, city.center.longitude)
                            }
                        )
                    }
                }
            }

            Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
                OSMMap(
                    city = selectedCity,
                    reports = reports,
                    selectedReport = selectedReport,
                    onMarkerSelected = { selectedReport = it },
                    onMapLocationChanged = { _, _ -> },
                    initialLatitude = if (currentLocationInCity) currentLocation!!.latitude else selectedCity.center.latitude,
                    initialLongitude = if (currentLocationInCity) currentLocation!!.longitude else selectedCity.center.longitude
                )
                Text(
                    text = "© OpenStreetMap contributors",
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.align(Alignment.BottomEnd).padding(8.dp),
                    color = Color.DarkGray
                )
                if (reportsLoading) {
                    Card(Modifier.align(Alignment.TopStart).padding(10.dp)) {
                        Row(Modifier.padding(horizontal = 10.dp, vertical = 7.dp), verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(Modifier.size(16.dp), strokeWidth = 2.dp)
                            Spacer(Modifier.size(7.dp))
                            Text("Cargando reportes…", style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }

                selectedReport?.let { report ->
                    ReportInfoCard(
                        report = report,
                        modifier = Modifier.align(Alignment.TopCenter).padding(10.dp),
                        onClose = { selectedReport = null }
                    )
                }
            }

            LegendCard()

            Button(
                onClick = { showReportDialog = true },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(Icons.Filled.Warning, contentDescription = null)
                Spacer(Modifier.size(8.dp))
                Text(stringResource(R.string.report_button), fontWeight = FontWeight.SemiBold)
            }
        }
    }

    if (showInfoDialog) {
        AlertDialog(
            onDismissRequest = { showInfoDialog = false },
            title = { Text("Información", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("BY Vorem", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("Versión V${BuildConfig.VERSION_NAME}")
                    Divider()
                    Text("Derechos reservados Clan Krytonox.")
                }
            },
            confirmButton = { TextButton(onClick = { showInfoDialog = false }) { Text("Cerrar") } }
        )
    }

    if (showDonateDialog) {
        AlertDialog(
            onDismissRequest = { showDonateDialog = false },
            title = { Text("Donar", fontWeight = FontWeight.Bold) },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "Si quieres apoyar el proyecto, puedes hacerlo con el siguiente QR o copiando la llave.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(Modifier.height(12.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        androidx.compose.foundation.Image(
                            painter = androidx.compose.ui.res.painterResource(R.drawable.donation_qr),
                            contentDescription = "Código QR completo para donar",
                            contentScale = ContentScale.Fit,
                            modifier = Modifier.fillMaxWidth().height(300.dp).padding(10.dp)
                        )
                    }
                    Spacer(Modifier.height(10.dp))
                    Text("Llave de donación", style = MaterialTheme.typography.labelLarge)
                    Text("@bold3150736053", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = {
                            val clipboard = context.getSystemService(ClipboardManager::class.java)
                            clipboard?.setPrimaryClip(ClipData.newPlainText("Llave de donación", "@bold3150736053"))
                            scope.launch { snackbarHostState.showSnackbar("Llave copiada") }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Filled.ContentCopy, contentDescription = null)
                        Spacer(Modifier.size(8.dp))
                        Text("Copiar llave")
                    }
                }
            },
            confirmButton = { TextButton(onClick = { showDonateDialog = false }) { Text("Cerrar") } }
        )
    }

    availableUpdate?.let { update ->
        AlertDialog(
            onDismissRequest = { availableUpdate = null },
            title = { Text("Nueva actualización disponible", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Hay una nueva versión de InfoVia: v${update.versionName}")
                    if (update.notes.isNotBlank()) {
                        Text(update.notes.take(500), style = MaterialTheme.typography.bodySmall)
                    }
                    Text(
                        "Al pulsar actualizar se abrirá la descarga oficial de GitHub. Android te pedirá confirmar la instalación.",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { availableUpdate = null }) { Text("Más tarde") }
            },
            confirmButton = {
                Button(onClick = {
                    runCatching {
                        context.startActivity(
                            Intent(Intent.ACTION_VIEW, Uri.parse(update.downloadUrl)).apply {
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            }
                        )
                    }
                    availableUpdate = null
                }) {
                    Text("Actualizar ahora")
                }
            }
        )
    }

    if (showReportDialog) {
        val reportViewModel: ReportViewModel = viewModel(factory = ReportViewModel.Factory(context.applicationContext))
        val reportState by reportViewModel.state.collectAsStateWithLifecycle()

        ReportDialog(
            state = reportState,
            city = selectedCity,
            defaultLatitude = if (currentLocationInCity) currentLocation!!.latitude else selectedCity.center.latitude,
            defaultLongitude = if (currentLocationInCity) currentLocation!!.longitude else selectedCity.center.longitude,
            onDismiss = { if (!reportState.isSubmitting) { reportViewModel.clearResult(); showReportDialog = false } },
            onTypeSelected = reportViewModel::setType,
            onLevelSelected = reportViewModel::setLevel,
            onLocationSelected = reportViewModel::setLocation,
            onSubmit = onSubmit@{
                reportViewModel.setCity(selectedCity.name)
                reportViewModel.submit()
            },
            onRequestPermission = {
                if (hasLocationPermission()) {
                    viewModel.fetchCurrentLocation(
                        fusedLocationClient,
                        selectedCity.center.latitude,
                        selectedCity.center.longitude
                    )
                } else {
                    permissionLauncher.launch(
                        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
                    )
                }
            }
        )

        LaunchedEffect(reportState.success) {
            if (reportState.success) {
                reportViewModel.clearResult()
                showReportDialog = false
                scope.launch { snackbarHostState.showSnackbar(context.getString(R.string.report_sent_success)) }
            }
        }
    }
}

@Composable
private fun OSMMap(
    city: ColombianCity,
    reports: List<Report>,
    selectedReport: Report?,
    onMarkerSelected: (Report) -> Unit,
    onMapLocationChanged: (Double, Double) -> Unit,
    initialLatitude: Double,
    initialLongitude: Double
) {
    val context = LocalContext.current
    AndroidView(
        modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(16.dp)).border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(16.dp)),
        factory = {
            Configuration.getInstance().load(context, context.getSharedPreferences("osmdroid", 0))
            Configuration.getInstance().userAgentValue = context.packageName
            MapView(context).apply {
                setTileSource(TileSourceFactory.MAPNIK)
                setMultiTouchControls(true)
                setScrollableAreaLimitDouble(city.bounds)
                controller.setZoom(15.0)
                controller.setCenter(GeoPoint(initialLatitude, initialLongitude))
                minZoomLevel = 10.0
                maxZoomLevel = 18.0
            }
        },
        update = { map ->
            map.setScrollableAreaLimitDouble(city.bounds)
            val reportsKey = reports.joinToString("|") { "${it.id}:${it.latitude}:${it.longitude}:${it.level}:${it.type}" }
            val previous = map.tag as? MapRenderState ?: MapRenderState()

            if (previous.reportsKey != reportsKey) {
                map.overlays.removeAll { it is Marker }
                reports.forEach { report ->
                    val marker = Marker(map).apply {
                        position = GeoPoint(report.latitude, report.longitude)
                        title = if (report.type == ReportType.TRANSITO) "Tránsito" else "Accidente"
                        icon = createReportIcon(context, report.type, report.level)
                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                        setOnMarkerClickListener { _, _ ->
                            onMarkerSelected(report)
                            true
                        }
                    }
                    map.overlays.add(marker)
                }
            }

            var nextState = previous.copy(reportsKey = reportsKey)
            val centerKey = "center:${city.name}:$initialLatitude:$initialLongitude"
            if (selectedReport == null) {
                if (previous.centerKey != centerKey) {
                    nextState = nextState.copy(centerKey = centerKey)
                    map.controller.animateTo(GeoPoint(initialLatitude, initialLongitude))
                    map.controller.setZoom(15.0)
                }
                if (previous.selectedKey != null) {
                    nextState = nextState.copy(selectedKey = null)
                }
            }

            if (map.mapCenter.latitude !in city.bounds.latSouth..city.bounds.latNorth || map.mapCenter.longitude !in city.bounds.lonWest..city.bounds.lonEast) {
                map.controller.animateTo(city.center)
                map.controller.setZoom(15.0)
            }

            if (selectedReport != null) {
                val report = selectedReport
                val selectedKey = "${report.id}:${report.latitude}:${report.longitude}"
                if (previous.selectedKey != selectedKey) {
                    nextState = nextState.copy(selectedKey = selectedKey)
                    map.controller.animateTo(GeoPoint(report.latitude, report.longitude))
                    map.controller.setZoom(17.0)
                }
            }

            map.tag = nextState
            map.invalidate()
        }
    )
}

private data class MapRenderState(
    val reportsKey: String = "",
    val centerKey: String = "",
    val selectedKey: String? = null
)

private fun createReportIcon(context: android.content.Context, type: ReportType, level: ReportLevel): BitmapDrawable {
    val size = (48 * context.resources.displayMetrics.density).toInt().coerceAtLeast(48)
    val bitmap = android.graphics.Bitmap.createBitmap(size, size, android.graphics.Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    val center = size / 2f
    val outer = size * 0.43f
    val inner = size * 0.34f
    val typeColor = if (type == ReportType.TRANSITO) android.graphics.Color.rgb(30, 100, 220) else android.graphics.Color.rgb(35, 155, 75)
    val levelColor = when (level) {
        ReportLevel.BAJO -> android.graphics.Color.rgb(245, 205, 45)
        ReportLevel.MEDIO -> android.graphics.Color.rgb(240, 130, 25)
        ReportLevel.ALTO -> android.graphics.Color.rgb(210, 45, 45)
    }
    val outerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = levelColor; style = Paint.Style.FILL }
    val innerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = typeColor; style = Paint.Style.FILL }
    canvas.drawCircle(center, center, outer, outerPaint)
    canvas.drawCircle(center, center, inner, innerPaint)
    return BitmapDrawable(context.resources, bitmap)
}

@Composable
private fun ReportInfoCard(report: Report, modifier: Modifier, onClose: () -> Unit) {
    val context = LocalContext.current
    var approximateAddress by remember(report.id) { mutableStateOf("Buscando dirección aproximada…") }

    LaunchedEffect(report.id, report.latitude, report.longitude) {
        approximateAddress = withContext(Dispatchers.IO) {
            runCatching {
                @Suppress("DEPRECATION")
                val address = Geocoder(context, Locale("es", "CO"))
                    .getFromLocation(report.latitude, report.longitude, 1)
                    ?.firstOrNull()
                address?.getAddressLine(0)?.takeIf { it.isNotBlank() }
                    ?: listOfNotNull(
                        address?.thoroughfare,
                        address?.subThoroughfare,
                        address?.subLocality,
                        address?.locality
                    ).joinToString(", ").takeIf { it.isNotBlank() }
            }.getOrNull() ?: "${report.latitude.formatCoord()}, ${report.longitude.formatCoord()}"
        }
    }

    Card(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text(if (report.type == ReportType.TRANSITO) "Tránsito" else "Accidente", fontWeight = FontWeight.Bold)
                TextButton(onClick = onClose) { Text("Cerrar") }
            }
            Text("Nivel: ${report.level.name.lowercase().replaceFirstChar { it.uppercase() }}")
            Text("Ciudad: ${report.city}, Colombia")
            Text("Dirección aproximada: $approximateAddress", style = MaterialTheme.typography.bodySmall)
            Text("Fecha: ${report.date} · ${report.time}")
            Text("Reportado ${elapsedText(report)}")
            ReportDetailMap(report)
        }
    }
}

@Composable
private fun ReportDetailMap(report: Report) {
    val context = LocalContext.current
    AndroidView(
        modifier = Modifier.fillMaxWidth().height(145.dp).clip(RoundedCornerShape(12.dp)),
        factory = {
            Configuration.getInstance().load(it, it.getSharedPreferences("osmdroid", 0))
            Configuration.getInstance().userAgentValue = it.packageName
            MapView(it).apply {
                setTileSource(TileSourceFactory.MAPNIK)
                setMultiTouchControls(true)
                minZoomLevel = 15.0
                maxZoomLevel = 19.0
                controller.setZoom(17.5)
                controller.setCenter(GeoPoint(report.latitude, report.longitude))
            }
        },
        update = { map ->
            val point = GeoPoint(report.latitude, report.longitude)
            map.controller.setCenter(point)
            map.controller.setZoom(17.5)
            map.overlays.removeAll { it is Marker }
            map.overlays.add(Marker(map).apply {
                position = point
                icon = createReportIcon(context, report.type, report.level)
                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            })
            map.invalidate()
        }
    )
}

private fun elapsedText(report: Report): String {
    val timestamp = report.serverTimestamp?.toDate()?.time ?: runCatching {
        SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).parse("${report.date} ${report.time}")?.time
    }.getOrNull() ?: return "recientemente"
    val minutes = ((System.currentTimeMillis() - timestamp) / 60000L).coerceAtLeast(0L)
    return when {
        minutes < 1 -> "hace menos de un minuto"
        minutes == 1L -> "hace 1 minuto"
        minutes < 60 -> "hace $minutes minutos"
        else -> "hace ${minutes / 60} h"
    }
}

@Composable
private fun ReportDialog(
    state: com.alertaciudadana.app.data.reports.ReportFormState,
    city: ColombianCity,
    defaultLatitude: Double,
    defaultLongitude: Double,
    onDismiss: () -> Unit,
    onTypeSelected: (ReportType) -> Unit,
    onLevelSelected: (ReportLevel) -> Unit,
    onLocationSelected: (Double, Double) -> Unit,
    onSubmit: () -> Unit,
    onRequestPermission: () -> Unit
) {
    var pickerLatitude by remember(defaultLatitude) { mutableStateOf(defaultLatitude) }
    var pickerLongitude by remember(defaultLongitude) { mutableStateOf(defaultLongitude) }

    LaunchedEffect(defaultLatitude, defaultLongitude) {
        pickerLatitude = defaultLatitude
        pickerLongitude = defaultLongitude
        onLocationSelected(defaultLatitude, defaultLongitude)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Row(verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Filled.AddLocationAlt, null); Spacer(Modifier.size(8.dp)); Text("Nuevo reporte") } },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Ciudad: ${city.name}, Colombia", fontWeight = FontWeight.Bold)
                Text("Tipo de reporte", fontWeight = FontWeight.Bold)
                SelectionRow(ReportType.entries, state.type, { if (it == ReportType.TRANSITO) "Tránsito" else "Accidente" }, onTypeSelected)
                Text("Nivel de importancia", fontWeight = FontWeight.Bold)
                SelectionRow(ReportLevel.entries, state.level, {
                    when (it) { ReportLevel.BAJO -> "Bajo"; ReportLevel.MEDIO -> "Medio"; ReportLevel.ALTO -> "Alto" }
                }, onLevelSelected)
                Divider()
                Text("Ubicación dentro de ${city.name}", fontWeight = FontWeight.Bold)
                LocationSelector(city, pickerLatitude, pickerLongitude) { lat, lon ->
                    pickerLatitude = lat; pickerLongitude = lon; onLocationSelected(lat, lon)
                }
                OutlinedButton(onClick = onRequestPermission, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Filled.LocationOn, null); Spacer(Modifier.size(8.dp)); Text("Usar mi ubicación")
                }
                if (state.errorMessage != null) Text(state.errorMessage, color = MaterialTheme.colorScheme.error)
            }
        },
        confirmButton = { Button(onClick = onSubmit, enabled = !state.isSubmitting) { if (state.isSubmitting) CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp) else Text("Enviar reporte") } },
        dismissButton = { TextButton(onClick = onDismiss, enabled = !state.isSubmitting) { Text("Cancelar") } }
    )
}

@Composable
private fun <T> SelectionRow(options: List<T>, selected: T?, label: @Composable (T) -> String, onSelected: (T) -> Unit) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        options.forEach { option -> AssistChip(onClick = { onSelected(option) }, label = { Text(label(option)) }, leadingIcon = if (selected == option) ({ Icon(Icons.Filled.Warning, null, Modifier.size(16.dp)) }) else null) }
    }
}

@Composable
private fun LocationSelector(
    city: ColombianCity,
    latitude: Double,
    longitude: Double,
    onLocationChanged: (Double, Double) -> Unit
) {
    val context = LocalContext.current
    var lastCityName by remember { mutableStateOf(city.name) }
    var lastUserSelectedLatitude by remember { mutableStateOf<Double?>(null) }
    var lastUserSelectedLongitude by remember { mutableStateOf<Double?>(null) }

    AndroidView(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .clip(RoundedCornerShape(14.dp))
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(14.dp)),
        factory = {
            Configuration.getInstance().load(it, it.getSharedPreferences("osmdroid", 0))
            Configuration.getInstance().userAgentValue = it.packageName
            MapView(it).apply {
                setTileSource(TileSourceFactory.MAPNIK)
                setMultiTouchControls(true)
                setScrollableAreaLimitDouble(city.bounds)
                minZoomLevel = 13.0
                maxZoomLevel = 19.0
                controller.setZoom(16.0)
                controller.setCenter(GeoPoint(latitude, longitude))

                val marker = Marker(this).apply {
                    position = GeoPoint(latitude, longitude)
                    icon = createReportIcon(context, ReportType.TRANSITO, ReportLevel.ALTO)
                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    isDraggable = true
                    setOnMarkerDragListener(object : Marker.OnMarkerDragListener {
                        override fun onMarkerDrag(marker: Marker) = Unit
                        override fun onMarkerDragEnd(marker: Marker) {
                            val p = marker.position
                            if (city.bounds.contains(p)) {
                                lastUserSelectedLatitude = p.latitude
                                lastUserSelectedLongitude = p.longitude
                                onLocationChanged(p.latitude, p.longitude)
                            } else {
                                marker.position = GeoPoint(latitude, longitude)
                            }
                            invalidate()
                        }
                        override fun onMarkerDragStart(marker: Marker) = Unit
                    })
                }
                overlays.add(marker)
                overlays.add(MapEventsOverlay(object : MapEventsReceiver {
                    override fun singleTapConfirmedHelper(p: GeoPoint): Boolean {
                        if (city.bounds.contains(p)) {
                            lastUserSelectedLatitude = p.latitude
                            lastUserSelectedLongitude = p.longitude
                            marker.position = p
                            onLocationChanged(p.latitude, p.longitude)
                            invalidate()
                        }
                        return true
                    }
                    override fun longPressHelper(p: GeoPoint): Boolean = singleTapConfirmedHelper(p)
                }))
            }
        },
        update = { map ->
            map.setScrollableAreaLimitDouble(city.bounds)
            val marker = map.overlays.filterIsInstance<Marker>().firstOrNull()
            if (city.name != lastCityName) {
                lastCityName = city.name
                lastUserSelectedLatitude = null
                lastUserSelectedLongitude = null
                marker?.position = GeoPoint(latitude, longitude)
                map.controller.animateTo(GeoPoint(latitude, longitude))
                map.controller.setZoom(16.0)
            } else if (marker != null) {
                val target = GeoPoint(latitude, longitude)
                val isTheSameAsManualSelection =
                    lastUserSelectedLatitude?.let { kotlin.math.abs(it - latitude) < 0.00001 } == true &&
                    lastUserSelectedLongitude?.let { kotlin.math.abs(it - longitude) < 0.00001 } == true
                if (!isTheSameAsManualSelection && marker.position.distanceToAsDouble(target) > 2.0) {
                    marker.position = target
                    map.controller.animateTo(target)
                }
            }
            map.invalidate()
        }
    )

    Text(
        "Toca otro punto o arrastra el marcador para cambiar la ubicación.",
        style = MaterialTheme.typography.labelSmall,
        modifier = Modifier.padding(top = 4.dp)
    )
}

private fun Double.formatCoord(): String = String.format(Locale.US, "%.5f", this)

@Composable
private fun LegendCard() {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                LegendDot(ReportTypeTransito, "Tránsito")
                LegendDot(ReportTypeAccidente, "Accidente")
                LegendDot(ReportLevelBajo, "Bajo", RoundedCornerShape(4.dp))
                LegendDot(ReportLevelMedio, "Medio", RoundedCornerShape(4.dp))
                LegendDot(ReportLevelAlto, "Alto", RoundedCornerShape(4.dp))
            }
            Text("El borde indica el nivel; el centro indica el tipo. Los reportes duran 2 horas.", style = MaterialTheme.typography.labelSmall)
        }
    }
}

@Composable
private fun LegendDot(color: Color, label: String, shape: androidx.compose.ui.graphics.Shape = CircleShape) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(12.dp).background(color, shape))
        Text(" $label", style = MaterialTheme.typography.labelSmall)
    }
}
