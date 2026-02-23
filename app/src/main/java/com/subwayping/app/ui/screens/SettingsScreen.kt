package com.subwayping.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.subwayping.app.data.local.TrackingDataStore
import com.subwayping.app.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val trackingDataStore = remember { TrackingDataStore(context) }

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
