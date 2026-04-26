package com.unitbv.speedy.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.TilesOverlay
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import android.preference.PreferenceManager
import com.unitbv.speedy.R
import com.unitbv.speedy.viewmodel.RunTrackingViewModel

@Composable
fun RunTrackingScreen(
    onFinish: () -> Unit = {},
    vm: RunTrackingViewModel = viewModel()
) {
    val context = LocalContext.current

    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED
        )
    }
    val permLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { hasPermission = it }

    LaunchedEffect(Unit) {
        if (!hasPermission) permLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    val seconds by vm.seconds.collectAsState()
    val distanceMeters by vm.distanceMeters.collectAsState()
    val isRunning by vm.isRunning.collectAsState()
    val currentLocation by vm.currentLocation.collectAsState()
    val routePoints by vm.routePoints.collectAsState()

    var mapViewRef by remember { mutableStateOf<MapView?>(null) }
    var locationMarker by remember { mutableStateOf<org.osmdroid.views.overlay.Marker?>(null) }
    var borderPolyline by remember { mutableStateOf<org.osmdroid.views.overlay.Polyline?>(null) }
    var routePolyline by remember { mutableStateOf<org.osmdroid.views.overlay.Polyline?>(null) }

    val distanceKm = distanceMeters / 1000f
    val paceStr = if (seconds > 30 && distanceKm > 0.01f) {
        val paceMin = (seconds / 60f / distanceKm).toInt()
        val paceSec = ((seconds / 60f / distanceKm - paceMin) * 60).toInt()
        "%d:%02d".format(paceMin, paceSec)
    } else "--:--"
    val calories = (distanceKm * 70).toInt()

    // Urmărire locație + marker
    LaunchedEffect(currentLocation) {
        val loc = currentLocation ?: return@LaunchedEffect
        val map = mapViewRef ?: return@LaunchedEffect

        map.controller.animateTo(loc)

        if (locationMarker == null) {
            val marker = org.osmdroid.views.overlay.Marker(map).apply {
                icon = ContextCompat.getDrawable(context, R.drawable.location_dot)
                setAnchor(
                    org.osmdroid.views.overlay.Marker.ANCHOR_CENTER,
                    org.osmdroid.views.overlay.Marker.ANCHOR_CENTER
                )
            }
            map.overlays.add(marker)
            locationMarker = marker
        }
        locationMarker?.position = loc
        map.invalidate()
    }

    // Traseu polyline
    LaunchedEffect(routePoints) {
        val map = mapViewRef ?: return@LaunchedEffect
        if (routePoints.size < 2) return@LaunchedEffect

        // Creează polyline-urile la primul punct
        if (borderPolyline == null) {
            val border = org.osmdroid.views.overlay.Polyline(map).apply {
                outlinePaint.color = android.graphics.Color.WHITE
                outlinePaint.strokeWidth = 20f
                outlinePaint.strokeCap = android.graphics.Paint.Cap.ROUND
                outlinePaint.strokeJoin = android.graphics.Paint.Join.ROUND
                outlinePaint.alpha = 80
                outlinePaint.isAntiAlias = true
            }
            val route = org.osmdroid.views.overlay.Polyline(map).apply {
                outlinePaint.color = android.graphics.Color.parseColor("#FF6B00")
                outlinePaint.strokeWidth = 14f
                outlinePaint.strokeCap = android.graphics.Paint.Cap.ROUND
                outlinePaint.strokeJoin = android.graphics.Paint.Join.ROUND
                outlinePaint.isAntiAlias = true
            }
            map.overlays.add(0, border)
            map.overlays.add(1, route)
            borderPolyline = border
            routePolyline = route
        }

        borderPolyline?.setPoints(routePoints.toMutableList())
        routePolyline?.setPoints(routePoints.toMutableList())
        map.invalidate()
    }

    Box(modifier = Modifier.fillMaxSize().background(DarkBg)) {

        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                MapView(ctx).apply {
                    setTileSource(TileSourceFactory.MAPNIK)
                    setMultiTouchControls(true)
                    controller.setZoom(18.0)
                    overlayManager.tilesOverlay.setColorFilter(TilesOverlay.INVERT_COLORS)
                    isTilesScaledToDpi = true
                    setUseDataConnection(true)
                    mapViewRef = this
                }
            }
        )

        // Top stats bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(16.dp)
                .align(Alignment.TopCenter)
        ) {
            Surface(shape = RoundedCornerShape(16.dp), color = Color(0xEE0D0D0D)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TrackingStat(formatTime(seconds), "Time")
                    Divider(
                        modifier = Modifier.height(32.dp).width(1.dp),
                        color = Color.White.copy(alpha = 0.1f)
                    )
                    TrackingStat("%.2f".format(distanceKm), "KM")
                    Divider(
                        modifier = Modifier.height(32.dp).width(1.dp),
                        color = Color.White.copy(alpha = 0.1f)
                    )
                    TrackingStat(paceStr, "Pace")
                    Divider(
                        modifier = Modifier.height(32.dp).width(1.dp),
                        color = Color.White.copy(alpha = 0.1f)
                    )
                    TrackingStat("$calories", "Cal")
                }
            }
        }

        // Bottom controls
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter),
            shape = RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp),
            color = Color(0xF00D0D0D)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .width(36.dp)
                        .height(4.dp)
                        .background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(2.dp))
                )
                Spacer(Modifier.height(24.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ControlButton(
                        icon = if (isRunning) Icons.Outlined.Pause else Icons.Outlined.PlayArrow,
                        label = if (isRunning) "Pause" else "Resume",
                        size = 56,
                        containerColor = Color.White.copy(alpha = 0.1f),
                        onClick = { vm.togglePause() }
                    )
                    ControlButton(
                        icon = Icons.Outlined.Stop,
                        label = "Finish",
                        size = 68,
                        containerColor = Color(0xFFE53935),
                        onClick = { vm.finishRun(onFinish) }
                    )
                    ControlButton(
                        icon = Icons.Outlined.Lock,
                        label = "Lock",
                        size = 56,
                        containerColor = Color.White.copy(alpha = 0.1f),
                        onClick = {}
                    )
                }
                Spacer(Modifier.navigationBarsPadding())
            }
        }
    }
}

@Composable
fun TrackingStat(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Text(
            text = label,
            fontSize = 10.sp,
            color = Color.White.copy(alpha = 0.4f)
        )
    }
}

@Composable
fun ControlButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    size: Int,
    containerColor: Color,
    onClick: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        FloatingActionButton(
            onClick = onClick,
            shape = CircleShape,
            containerColor = containerColor,
            modifier = Modifier.size(size.dp)
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size((size * 0.42f).dp)
            )
        }
        Spacer(Modifier.height(6.dp))
        Text(label, fontSize = 11.sp, color = Color.White.copy(alpha = 0.5f))
    }
}

fun formatTime(seconds: Int): String {
    val h = seconds / 3600
    val m = (seconds % 3600) / 60
    val s = seconds % 60
    return if (h > 0) "%d:%02d:%02d".format(h, m, s)
    else "%02d:%02d".format(m, s)
}