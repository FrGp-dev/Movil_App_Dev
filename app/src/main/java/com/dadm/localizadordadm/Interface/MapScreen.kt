package com.dadm.localizadordadm.Interface

import android.Manifest
import android.util.DisplayMetrics
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.permissions.*
import com.dadm.localizadordadm.viewmodel.MapViewModel
import org.osmdroid.config.Configuration
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.ScaleBarOverlay
import org.osmdroid.views.overlay.compass.CompassOverlay
import org.osmdroid.views.overlay.compass.InternalCompassOrientationProvider
import org.osmdroid.views.overlay.gestures.RotationGestureOverlay
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun MapScreen(
    viewModel: MapViewModel = viewModel(),
    onNavigateToSettings: () -> Unit
) {
    val state = viewModel.state
    val context = LocalContext.current

    val locationPermissionsState = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    )

    LaunchedEffect(locationPermissionsState.allPermissionsGranted) {
        if (locationPermissionsState.allPermissionsGranted) {
            viewModel.fetchCurrentLocationAndSearch()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Puntos interes OpenSM (${state.searchRadiusKm.toInt()} km)") },
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "Configuración")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {

            if (!locationPermissionsState.allPermissionsGranted) {
                PermissionRequestContent {
                    locationPermissionsState.launchMultiplePermissionRequest()
                }
            } else {
                val userGeoPoint = state.userLocation

                AndroidView(
                    factory = {
                        MapView(it).apply {
                            setBuiltInZoomControls(true)
                            setMultiTouchControls(true)
                            controller.setZoom(14.0)

                            // Agregar capa de ubicación del usuario
                            overlays.add(MyLocationNewOverlay(this).apply { enableMyLocation() })

                            // Agregar brújula
                            val compassOverlay = CompassOverlay(it, InternalCompassOrientationProvider(it), this)
                            compassOverlay.enableCompass()
                            overlays.add(compassOverlay)

                            // Agregar rotación
                            overlays.add(RotationGestureOverlay(this))

                            // Agregar barra de escala
                            val dm: DisplayMetrics = context.resources.displayMetrics
                            overlays.add(ScaleBarOverlay(this).apply { setCentred(true); setScaleBarOffset(dm.widthPixels / 2, 10) })
                        }
                    },
                    update = { mapView ->
                        mapView.controller.animateTo(userGeoPoint)

                        // Limpiar y re-agregar todos los overlays
                        mapView.overlays.clear()

                        // **Vuelve a agregar overlays fijos**
                        mapView.overlays.add(MyLocationNewOverlay(mapView).apply { enableMyLocation() })

                        // Brújula
                        val compassOverlay = CompassOverlay(context, InternalCompassOrientationProvider(context), mapView)
                        compassOverlay.enableCompass()
                        mapView.overlays.add(compassOverlay)

                        mapView.overlays.add(RotationGestureOverlay(mapView))

                        val dm: DisplayMetrics = context.resources.displayMetrics
                        mapView.overlays.add(ScaleBarOverlay(mapView).apply { setCentred(true); setScaleBarOffset(dm.widthPixels / 2, 10) })


                        // Agregar marcadores de POI (Points of Interest)
                        state.nearbyPois.forEach { poi ->
                            val poiGeoPoint = GeoPoint(poi.lat, poi.lon)
                            val marker = Marker(mapView)
                            marker.position = poiGeoPoint
                            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                            marker.title = poi.name
                            marker.snippet = poi.primaryType.uppercase()
                            mapView.overlays.add(marker)
                        }

                        mapView.invalidate()
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }

            if (state.isLoading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }

            state.error?.let {
                Snackbar(
                    modifier = Modifier.padding(16.dp).align(Alignment.BottomCenter),
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer,
                    action = {
                        TextButton(onClick = { viewModel.fetchCurrentLocationAndSearch() }) {
                            Text("Reintentar")
                        }
                    }
                ) { Text("Error: $it") }
            }
        }
    }
}

// Composable para solicitar permisos
@Composable
fun PermissionRequestContent(
    onRequestPermission: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "📍 Se requieren permisos de ubicación para usar el mapa y buscar POI.",
            style = MaterialTheme.typography.bodyLarge
        )
        Spacer(Modifier.height(16.dp))
        Button(onClick = onRequestPermission) {
            Text("Solicitar Permisos de Ubicación")
        }
    }
}