package com.unitbv.speedy.screens

import android.preference.PreferenceManager
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.gms.location.LocationServices
import com.unitbv.speedy.R
import com.unitbv.speedy.viewmodel.MainViewModel
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.TilesOverlay
import java.util.Calendar

val OrangePrimary = Color(0xFFFF6B00)
val DarkBg = Color(0xFF0D0D0D)

@Composable
fun MainScreen(
    onRunClick: () -> Unit = {},
    onHistoryClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    vm: MainViewModel = viewModel()
) {
    val stats by vm.stats.collectAsState()
    val userName by vm.userName.collectAsState()

    // Reîncarcă stats de fiecare dată când intri pe Main
    LaunchedEffect(Unit) {
        vm.loadStats()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
    ) {
        SpeedyMap(modifier = Modifier.fillMaxSize())

        TopGreeting(
            name = userName,
            modifier = Modifier
                .align(Alignment.TopStart)
                .statusBarsPadding()
                .padding(16.dp)
        )

        BottomMapSheet(
            modifier = Modifier.align(Alignment.BottomCenter),
            totalKm = stats.totalKm,
            totalCal = stats.totalCal,
            totalRuns = stats.totalRuns,
            onRunClick = onRunClick,
            onHistoryClick = onHistoryClick,
            onProfileClick = onProfileClick
        )
    }
}

@Composable
fun SpeedyMap(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var mapView by remember { mutableStateOf<MapView?>(null) }
    var userLocation by remember { mutableStateOf<GeoPoint?>(null) }
    var isOffCenter by remember { mutableStateOf(false) }

    val fusedClient = remember {
        LocationServices.getFusedLocationProviderClient(context)
    }

    LaunchedEffect(Unit) {
        try {
            fusedClient.lastLocation.addOnSuccessListener { location ->
                location ?: return@addOnSuccessListener
                val geoPoint = GeoPoint(location.latitude, location.longitude)
                userLocation = geoPoint
                mapView?.controller?.animateTo(geoPoint)

                val marker = Marker(mapView).apply {
                    position = geoPoint
                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    icon = ContextCompat.getDrawable(context, R.drawable.ic_location_dot)
                    title = "You are here"
                }
                mapView?.overlays?.add(marker)
                mapView?.invalidate()
            }
        } catch (e: SecurityException) { }
    }

    Box(modifier = modifier) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = {
                Configuration.getInstance().apply {
                    load(context, PreferenceManager.getDefaultSharedPreferences(context))
                    userAgentValue = context.packageName
                }
                MapView(context).apply {
                    setTileSource(TileSourceFactory.MAPNIK)
                    setMultiTouchControls(true)
                    controller.setZoom(17.0)
                    controller.setCenter(GeoPoint(45.6427, 25.5887))
                    overlayManager.tilesOverlay.setColorFilter(TilesOverlay.INVERT_COLORS)
                    zoomController.setVisibility(CustomZoomButtonsController.Visibility.NEVER)

                    // Detectează când userul mișcă harta
                    addMapListener(object : org.osmdroid.events.MapListener {
                        override fun onScroll(event: org.osmdroid.events.ScrollEvent): Boolean {
                            isOffCenter = true
                            return false
                        }
                        override fun onZoom(event: org.osmdroid.events.ZoomEvent): Boolean {
                            return false
                        }
                    })

                    mapView = this
                }
            }
        )

        // Buton recenter — apare doar când harta e deplasată
        androidx.compose.animation.AnimatedVisibility(
            visible = isOffCenter,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = 160.dp),
            enter = androidx.compose.animation.scaleIn() + androidx.compose.animation.fadeIn(),
            exit = androidx.compose.animation.scaleOut() + androidx.compose.animation.fadeOut()
        ) {
            Surface(
                shape = CircleShape,
                color = Color(0xFF1A1A1A),
                shadowElevation = 8.dp,
                modifier = Modifier
                    .size(44.dp)
                    .clickable {
                        userLocation?.let { loc ->
                            mapView?.controller?.animateTo(loc)
                            isOffCenter = false
                        }
                    }
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Outlined.MyLocation,
                        contentDescription = "Recenter",
                        tint = OrangePrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

fun getGreeting(): String {
    return when (Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) {
        in 5..11 -> "Good morning"
        in 12..17 -> "Good afternoon"
        in 18..21 -> "Good evening"
        else -> "Good night"
    }
}

@Composable
fun TopGreeting(name: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = Color(0xCC0D0D0D)
    ) {
        Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
            Text(
                text = getGreeting(),
                fontSize = 11.sp,
                color = Color.White.copy(alpha = 0.45f)
            )
            Text(
                text = name,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White
            )
        }
    }
}

@Composable
fun BottomMapSheet(
    modifier: Modifier = Modifier,
    totalKm: Float,
    totalCal: Int,
    totalRuns: Int,
    onRunClick: () -> Unit,
    onHistoryClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp),
        color = Color(0xF00D0D0D)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 16.dp)
        ) {
            Box(
                modifier = Modifier
                    .width(36.dp)
                    .height(4.dp)
                    .background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(2.dp))
                    .align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "Today's route",
                fontSize = 11.sp,
                color = Color.White.copy(alpha = 0.45f)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = if (totalRuns == 0) "Start your first run" else "Ready to run?",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White
                    )
                    Text(
                        text = if (totalRuns == 0) "Brașov · tap Run to begin"
                        else "%.1f km total · tap Run".format(totalKm),
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.4f)
                    )
                }
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = OrangePrimary,
                    modifier = Modifier.clickable { onRunClick() }
                ) {
                    Box(
                        modifier = Modifier.padding(10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Outlined.ArrowForward,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatChip("%.1f".format(totalKm), "KM", Modifier.weight(1f))
                StatChip("$totalCal", "Cal", Modifier.weight(1f))
                StatChip("$totalRuns", "Runs", Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(8.dp))

            SpeedyBottomNav(
                onRunClick = onRunClick,
                onHistoryClick = onHistoryClick,
                onProfileClick = onProfileClick
            )

            Spacer(modifier = Modifier.navigationBarsPadding())
        }
    }
}

@Composable
fun StatChip(value: String, label: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        color = Color.White.copy(alpha = 0.06f)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(value, fontSize = 16.sp, fontWeight = FontWeight.Medium, color = Color.White)
            Text(label, fontSize = 10.sp, color = Color.White.copy(alpha = 0.4f))
        }
    }
}

@Composable
fun SpeedyBottomNav(
    onRunClick: () -> Unit,
    onHistoryClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        NavItem(icon = Icons.Outlined.Home, label = "Home", selected = true, onClick = {})
        NavItem(icon = Icons.Outlined.Place, label = "Routes", selected = false, onClick = {})

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            FloatingActionButton(
                onClick = onRunClick,
                shape = CircleShape,
                containerColor = OrangePrimary,
                modifier = Modifier.size(52.dp)
            ) {
                Icon(Icons.Outlined.PlayArrow, contentDescription = "Run", tint = Color.White)
            }
            Spacer(Modifier.height(4.dp))
            Text("Run", fontSize = 10.sp, color = OrangePrimary, fontWeight = FontWeight.Medium)
        }

        NavItem(icon = Icons.Outlined.DateRange, label = "History", selected = false, onClick = onHistoryClick)
        NavItem(icon = Icons.Outlined.Person, label = "Profile", selected = false, onClick = onProfileClick)
    }
}

@Composable
fun NavItem(icon: ImageVector, label: String, selected: Boolean, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .then(
                    if (selected) Modifier.background(
                        OrangePrimary.copy(alpha = 0.15f),
                        RoundedCornerShape(12.dp)
                    ) else Modifier
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                icon,
                contentDescription = label,
                tint = if (selected) OrangePrimary else Color.White.copy(alpha = 0.4f),
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(Modifier.height(4.dp))
        Text(
            text = label,
            fontSize = 10.sp,
            color = if (selected) OrangePrimary else Color.White.copy(alpha = 0.35f),
            fontWeight = if (selected) FontWeight.Medium else FontWeight.Normal
        )
    }
}