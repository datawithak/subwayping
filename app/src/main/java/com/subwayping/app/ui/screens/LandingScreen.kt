package com.subwayping.app.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.subwayping.app.R
import com.subwayping.app.SubwayPingApp
import com.subwayping.app.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun LandingScreen(
    onEnterFavourite: () -> Unit,
    onCustomizeRoute: () -> Unit,
    onGoToPing: () -> Unit
) {
    val app = SubwayPingApp.instance
    val scope = rememberCoroutineScope()
    val favouriteRoute by app.subwayRepository.getFavouriteRouteFlow()
        .collectAsStateWithLifecycle(initialValue = null)

    val hasFavourite = favouriteRoute != null
    var showRouteChoiceDialog by remember { mutableStateOf(false) }

    // Helper: activate favourite then navigate
    val pingFavourite: () -> Unit = {
        scope.launch {
            app.subwayRepository.switchToFavourite()
            onGoToPing()
        }
    }

    // Route choice dialog
    if (showRouteChoiceDialog) {
        AlertDialog(
            onDismissRequest = { showRouteChoiceDialog = false },
            containerColor = SubwayMedGray,
            title = {
                Text(
                    "which route?",
                    color = SubwayWhite,
                    fontWeight = FontWeight.SemiBold
                )
            },
            text = null,
            confirmButton = {
                Button(
                    onClick = {
                        showRouteChoiceDialog = false
                        pingFavourite()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PingRed)
                ) {
                    Text(
                        "favourite route",
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        showRouteChoiceDialog = false
                        onCustomizeRoute()
                    }
                ) {
                    Text(
                        "customize route",
                        color = SubwayLightGray
                    )
                }
            }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SubwayBlack)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.weight(0.15f))

            // Title — single line gen-z style
            Text(
                text = buildAnnotatedString {
                    withStyle(SpanStyle(color = SubwayWhite, fontWeight = FontWeight.Light)) {
                        append("subway")
                    }
                    withStyle(SpanStyle(color = PingRed, fontWeight = FontWeight.Black)) {
                        append("Ping")
                    }
                },
                fontSize = 38.sp,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.weight(0.08f))

            // Subway train image
            Image(
                painter = painterResource(id = R.drawable.subway_train),
                contentDescription = "Subway train",
                modifier = Modifier
                    .size(220.dp)
                    .clip(RoundedCornerShape(24.dp)),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.weight(0.12f))

            // "Enter your favourite route" button — only if no favourite saved
            if (!hasFavourite) {
                LandingButton(
                    text = "enter your\nfavourite route",
                    isPrimary = true,
                    onClick = onEnterFavourite
                )

                Spacer(modifier = Modifier.height(16.dp))
            }

            // When favourite exists: show "start pinging" + favourite info + customize option
            if (hasFavourite) {
                // Favourite route card — tap to go to PING screen
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { pingFavourite() },
                    colors = CardDefaults.cardColors(containerColor = SubwayMedGray),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(50))
                                .background(Color(favouriteRoute!!.lineColor)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                favouriteRoute!!.lineId,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Favourite route",
                                color = SubwayLightGray,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                letterSpacing = 1.sp
                            )
                            Text(
                                "${favouriteRoute!!.stationName} — ${favouriteRoute!!.directionLabel}",
                                color = SubwayWhite,
                                fontSize = 14.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Big "Start Pinging" button — asks which route
                LandingButton(
                    text = "start pinging",
                    isPrimary = true,
                    onClick = { showRouteChoiceDialog = true }
                )

                Spacer(modifier = Modifier.height(12.dp))
            }

            // "Customize your route" button — always visible
            LandingButton(
                text = "customize your\nroute",
                isPrimary = !hasFavourite,
                onClick = onCustomizeRoute
            )

            Spacer(modifier = Modifier.weight(0.2f))
        }
    }
}

@Composable
private fun LandingButton(
    text: String,
    isPrimary: Boolean,
    onClick: () -> Unit
) {
    val bgColor = if (isPrimary) SubwayMedGray else SubwayDarkGray
    val borderGradient = if (isPrimary) {
        Brush.horizontalGradient(listOf(PingRed.copy(alpha = 0.6f), PingRed.copy(alpha = 0.2f)))
    } else {
        Brush.horizontalGradient(listOf(SubwayLightGray.copy(alpha = 0.3f), SubwayLightGray.copy(alpha = 0.1f)))
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(borderGradient)
            .padding(1.5.dp)
            .clip(RoundedCornerShape(15.dp))
            .background(bgColor)
            .clickable { onClick() }
            .padding(vertical = 20.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (isPrimary) SubwayWhite else SubwayLightGray,
            fontSize = 18.sp,
            fontWeight = if (isPrimary) FontWeight.SemiBold else FontWeight.Normal,
            textAlign = TextAlign.Center,
            lineHeight = 24.sp
        )
    }
}
