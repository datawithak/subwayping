package com.subwayping.app.widget

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import androidx.glance.*
import androidx.glance.action.ActionParameters
import androidx.glance.action.clickable
import androidx.glance.appwidget.*
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.*
import androidx.glance.text.*
import androidx.glance.unit.ColorProvider
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.subwayping.app.R
import com.subwayping.app.service.TrackingService

class SubwayPingWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        // Read state BEFORE provideContent — runs fresh on every update() call
        val prefs = context.getSharedPreferences("widget_state", Context.MODE_PRIVATE)
        val isTracking = prefs.getBoolean("is_tracking", false)
        val nextArrivalEpoch = prefs.getLong("next_arrival_epoch", 0L)
        val lineId = prefs.getString("line_id", "") ?: ""
        val stationName = prefs.getString("station_name", "") ?: ""

        val nowEpoch = System.currentTimeMillis() / 1000
        val nextArrival = if (nextArrivalEpoch > nowEpoch) {
            ((nextArrivalEpoch - nowEpoch + 30) / 60).toInt()
        } else -1

        provideContent {
            WidgetContent(isTracking, nextArrival, lineId, stationName)
        }
    }

    @Composable
    private fun WidgetContent(
        isTracking: Boolean,
        nextArrival: Int,
        lineId: String,
        stationName: String
    ) {
        val buttonColor = if (isTracking) ColorProvider(Color(0xFF34C759))
            else ColorProvider(Color(0xFFFF3B30))
        val buttonText = if (isTracking) "STOP" else "PING"

        // Train image background with dark scrim
        Box(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(ImageProvider(R.drawable.subway_train))
                .cornerRadius(16.dp),
            contentAlignment = Alignment.Center
        ) {
          Box(
              modifier = GlanceModifier
                  .fillMaxSize()
                  .background(ColorProvider(Color(0xAA000000)))
                  .padding(12.dp),
              contentAlignment = Alignment.Center
          ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isTracking) {
                    if (nextArrival > 0) {
                        Text(
                            text = "$nextArrival min",
                            style = TextStyle(
                                color = ColorProvider(Color.White),
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    } else {
                        Text(
                            text = "Loading...",
                            style = TextStyle(
                                color = ColorProvider(Color(0xFFB0B0B0)),
                                fontSize = 14.sp
                            )
                        )
                    }
                    if (lineId.isNotEmpty()) {
                        Spacer(modifier = GlanceModifier.height(4.dp))
                        Text(
                            text = "$lineId — $stationName",
                            style = TextStyle(
                                color = ColorProvider(Color(0xFFB0B0B0)),
                                fontSize = 11.sp
                            ),
                            maxLines = 1
                        )
                    }
                    Spacer(modifier = GlanceModifier.height(8.dp))
                }

                Box(
                    modifier = GlanceModifier
                        .size(80.dp)
                        .cornerRadius(40.dp)
                        .background(buttonColor)
                        .clickable(actionRunCallback<ToggleTrackingAction>()),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = buttonText,
                        style = TextStyle(
                            color = ColorProvider(Color.White),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }

                if (!isTracking) {
                    Spacer(modifier = GlanceModifier.height(8.dp))
                    Text(
                        text = "Tap to track",
                        style = TextStyle(
                            color = ColorProvider(Color(0xFFB0B0B0)),
                            fontSize = 12.sp
                        )
                    )
                }
            }
          }
        }
    }

    companion object {
        suspend fun refreshAll(context: Context) {
            SubwayPingWidget().updateAll(context)
        }
    }
}

class ToggleTrackingAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        val prefs = context.getSharedPreferences("widget_state", Context.MODE_PRIVATE)
        val isTracking = prefs.getBoolean("is_tracking", false)

        if (isTracking) {
            prefs.edit().putBoolean("is_tracking", false).commit()
            TrackingService.stopTracking(context)
        } else {
            prefs.edit().putBoolean("is_tracking", true).commit()
            TrackingService.startTracking(context)
        }

        // Force all widget instances to re-render
        SubwayPingWidget().updateAll(context)
    }
}

class SubwayPingWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = SubwayPingWidget()
}
