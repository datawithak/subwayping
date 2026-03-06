package com.subwayping.app.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.subwayping.app.SubwayPingApp
import com.subwayping.app.data.local.TrackingDataStore
import com.subwayping.app.data.local.SavedRoute
import com.subwayping.app.service.TrackingService
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import com.subwayping.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToRoutePicker: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val app = SubwayPingApp.instance
    val trackingDataStore = remember { TrackingDataStore(context) }

    val activeRoute by app.subwayRepository.getActiveRouteFlow()
        .collectAsStateWithLifecycle(initialValue = null)
    val trackingState by trackingDataStore.trackingState
        .collectAsStateWithLifecycle(initialValue = com.subwayping.app.data.local.TrackingState())

    // Tick every second to recalculate minutes from epoch timestamps
    var nowEpoch by remember { mutableLongStateOf(System.currentTimeMillis() / 1000) }
    LaunchedEffect(trackingState.isTracking) {
        while (trackingState.isTracking) {
            nowEpoch = System.currentTimeMillis() / 1000
            kotlinx.coroutines.delay(1000L)
        }
    }
    val liveArrivals = remember(trackingState.arrivalEpochs, nowEpoch) {
        trackingState.arrivalEpochs
            .map { ((it - nowEpoch + 30) / 60).toInt() }
            .filter { it >= 0 }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "SubwayPing",
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, "Settings")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SubwayBlack,
                    titleContentColor = SubwayWhite
                )
            )
        },
        containerColor = SubwayBlack
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Route label
            if (activeRoute != null) {
                RouteLabel(
                    route = activeRoute!!,
                    onClick = onNavigateToRoutePicker
                )
            } else {
                NoRouteCard(onClick = onNavigateToRoutePicker)
            }

            Spacer(modifier = Modifier.weight(0.3f))

            // Big Red/Green Button
            PingButton(
                isTracking = trackingState.isTracking,
                enabled = activeRoute != null,
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    if (trackingState.isTracking) {
                        TrackingService.stopTracking(context)
                    } else {
                        TrackingService.startTracking(context)
                    }
                }
            )

            Spacer(modifier = Modifier.weight(0.3f))

            // Arrival cards — recalculated live every second
            if (trackingState.isTracking && liveArrivals.isNotEmpty()) {
                ArrivalCards(
                    arrivals = liveArrivals,
                    lineId = trackingState.lineId,
                    isBus = activeRoute?.feedGroup == "bus"
                )
            } else if (trackingState.isTracking) {
                Text(
                    "Fetching arrivals...",
                    color = SubwayLightGray,
                    fontSize = 16.sp
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun RouteLabel(route: SavedRoute, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = SubwayMedGray),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Line badge — circle for subway, rounded square for bus
            val isBusRoute = route.feedGroup == "bus"
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(if (isBusRoute) RoundedCornerShape(8.dp) else CircleShape)
                    .background(Color(route.lineColor)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    route.lineId,
                    color = if (route.lineId in listOf("N", "Q", "R", "W", "L"))
                        Color.Black else Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = if (isBusRoute) 13.sp else 22.sp
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    route.stationName,
                    color = SubwayWhite,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 18.sp
                )
                Text(
                    route.directionLabel,
                    color = SubwayLightGray,
                    fontSize = 14.sp
                )
            }

            Icon(
                Icons.Default.SwapHoriz,
                contentDescription = "Change route",
                tint = SubwayLightGray
            )
        }
    }
}

@Composable
private fun NoRouteCard(onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = SubwayMedGray),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Set your route",
                color = SubwayWhite,
                fontWeight = FontWeight.SemiBold,
                fontSize = 20.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "Tap to pick your line, station & direction",
                color = SubwayLightGray,
                fontSize = 14.sp
            )
        }
    }
}

@Composable
private fun PingButton(
    isTracking: Boolean,
    enabled: Boolean,
    onClick: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowAlpha"
    )

    val buttonColor = if (isTracking) StopGreen else PingRed
    val buttonText = if (isTracking) "STOP" else "PING"
    val currentScale = if (!isTracking && enabled) pulseScale else 1f

    Box(contentAlignment = Alignment.Center) {
        // Glow effect
        if (enabled && !isTracking) {
            Box(
                modifier = Modifier
                    .size((200 * currentScale + 20).dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                PingRed.copy(alpha = glowAlpha),
                                Color.Transparent
                            )
                        )
                    )
            )
        }

        // Main button
        Box(
            modifier = Modifier
                .size((200 * currentScale).dp)
                .shadow(
                    elevation = if (enabled) 16.dp else 4.dp,
                    shape = CircleShape
                )
                .clip(CircleShape)
                .background(
                    if (enabled) buttonColor
                    else buttonColor.copy(alpha = 0.3f)
                )
                .clickable(enabled = enabled) { onClick() },
            contentAlignment = Alignment.Center
        ) {
            Text(
                buttonText,
                color = Color.White,
                fontSize = 42.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 4.sp
            )
        }
    }
}

@Composable
private fun ArrivalCards(arrivals: List<Int>, lineId: String, isBus: Boolean = false) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        arrivals.take(3).forEachIndexed { index, min ->
            val isNext = index == 0
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (isNext) SubwayMedGray else SubwayDarkGray
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = if (isNext) 20.dp else 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        if (isNext) (if (isBus) "Next bus" else "Next train") else if (index == 1) "Following" else "After",
                        color = SubwayLightGray,
                        fontSize = if (isNext) 18.sp else 14.sp
                    )

                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            if (min <= 0) "Now" else "$min",
                            color = if (isNext) SubwayWhite else SubwayLightGray,
                            fontWeight = FontWeight.Bold,
                            fontSize = if (isNext) 36.sp else 22.sp
                        )
                        if (min > 0) {
                            Text(
                                " min",
                                color = SubwayLightGray,
                                fontSize = if (isNext) 18.sp else 14.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

private val EaseInOutCubic = CubicBezierEasing(0.65f, 0f, 0.35f, 1f)
