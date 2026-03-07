package com.subwayping.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.subwayping.app.SubwayPingApp
import com.subwayping.app.data.local.SavedRoute
import com.subwayping.app.data.local.TrackingDataStore
import com.subwayping.app.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onBack: () -> Unit, onChangeFavourite: () -> Unit = {}) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val trackingDataStore = remember { TrackingDataStore(context) }
    val app = SubwayPingApp.instance
    var favouriteRoute by remember { mutableStateOf<SavedRoute?>(null) }

    LaunchedEffect(Unit) {
        favouriteRoute = app.subwayRepository.getFavouriteRoute()
    }

    val autoStopMinutes by trackingDataStore.autoStopMinutes
        .collectAsStateWithLifecycle(initialValue = 60)
    val notificationSound by trackingDataStore.notificationSound
        .collectAsStateWithLifecycle(initialValue = true)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
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
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Favourite route
            Text(
                "FAVOURITE ROUTE",
                color = SubwayLightGray,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
            )

            Card(
                colors = CardDefaults.cardColors(containerColor = SubwayDarkGray),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (favouriteRoute != null) {
                        val isBus = favouriteRoute!!.feedGroup == "bus"
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(if (isBus) RoundedCornerShape(6.dp) else CircleShape)
                                .background(Color(favouriteRoute!!.lineColor)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                favouriteRoute!!.lineId,
                                color = if (favouriteRoute!!.lineId in listOf("N", "Q", "R", "W", "L"))
                                    Color.Black else Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = if (isBus) 11.sp else 18.sp
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                favouriteRoute!!.stationName,
                                color = SubwayWhite,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 15.sp
                            )
                            Text(
                                favouriteRoute!!.directionLabel,
                                color = SubwayLightGray,
                                fontSize = 13.sp
                            )
                        }
                    } else {
                        Text(
                            "No favourite set",
                            color = SubwayLightGray,
                            fontSize = 15.sp,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    IconButton(onClick = onChangeFavourite) {
                        Icon(Icons.Default.Edit, "Change favourite", tint = SubwayLightGray)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Auto-stop timer
            Text(
                "AUTO-STOP TIMER",
                color = SubwayLightGray,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
            )

            val timerOptions = listOf(30 to "30 minutes", 60 to "1 hour", 120 to "2 hours")
            Card(
                colors = CardDefaults.cardColors(containerColor = SubwayDarkGray),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column {
                    timerOptions.forEachIndexed { index, (minutes, label) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    scope.launch { trackingDataStore.setAutoStopMinutes(minutes) }
                                }
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(label, color = SubwayWhite, fontSize = 16.sp)
                            RadioButton(
                                selected = autoStopMinutes == minutes,
                                onClick = {
                                    scope.launch { trackingDataStore.setAutoStopMinutes(minutes) }
                                },
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = PingRed,
                                    unselectedColor = SubwayLightGray
                                )
                            )
                        }
                        if (index < timerOptions.lastIndex) {
                            Box(modifier = Modifier.fillMaxWidth().height(0.5.dp).background(SubwayMedGray))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Notification sound
            Text(
                "NOTIFICATIONS",
                color = SubwayLightGray,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
            )

            Card(
                colors = CardDefaults.cardColors(containerColor = SubwayDarkGray),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Alert sound", color = SubwayWhite, fontSize = 16.sp)
                        Text(
                            "Sound when train is ≤3 min away",
                            color = SubwayLightGray,
                            fontSize = 13.sp
                        )
                    }
                    Switch(
                        checked = notificationSound,
                        onCheckedChange = {
                            scope.launch { trackingDataStore.setNotificationSound(it) }
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = SubwayWhite,
                            checkedTrackColor = PingRed,
                            uncheckedThumbColor = SubwayLightGray,
                            uncheckedTrackColor = SubwayMedGray
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // About section
            Text(
                "SubwayPing v1.0",
                color = SubwayLightGray,
                fontSize = 13.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}
