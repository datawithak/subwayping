package com.subwayping.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
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
import com.subwayping.app.SubwayPingApp
import com.subwayping.app.data.local.Direction
import com.subwayping.app.data.local.SavedRoute
import com.subwayping.app.data.local.Station
import com.subwayping.app.data.local.SubwayLine
import com.subwayping.app.data.local.SubwayLines
import com.subwayping.app.ui.theme.*
import kotlinx.coroutines.launch

private enum class PickerStep { LINE, STATION, DIRECTION }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoutePickerScreen(isFavouriteMode: Boolean = false, onRouteSelected: () -> Unit, onBack: () -> Unit) {
    val context = LocalContext.current
    val app = SubwayPingApp.instance
    val scope = rememberCoroutineScope()

    var step by remember { mutableStateOf(PickerStep.LINE) }
    var selectedLine by remember { mutableStateOf<SubwayLine?>(null) }
    var selectedStation by remember { mutableStateOf<Station?>(null) }
    var searchQuery by remember { mutableStateOf("") }

    val modeLabel = if (isFavouriteMode) "favourite" else "route"
    val stepTitle = when (step) {
        PickerStep.LINE -> "Pick your line"
        PickerStep.STATION -> "Pick your station"
        PickerStep.DIRECTION -> "Pick direction"
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stepTitle, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = {
                        when (step) {
                            PickerStep.LINE -> onBack()
                            PickerStep.STATION -> { step = PickerStep.LINE; searchQuery = "" }
                            PickerStep.DIRECTION -> step = PickerStep.STATION
                        }
                    }) {
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
        ) {
            when (step) {
                PickerStep.LINE -> {
                    LinePicker(
                        onLineSelected = { line ->
                            selectedLine = line
                            step = PickerStep.STATION
                        }
                    )
                }

                PickerStep.STATION -> {
                    StationPicker(
                        lineId = selectedLine!!.id,
                        searchQuery = searchQuery,
                        onSearchChange = { searchQuery = it },
                        onStationSelected = { station ->
                            selectedStation = station
                            val line = selectedLine!!
                            if (line.isBus) {
                                // Bus routes: no direction step, always "Toward Ferry"
                                val route = SavedRoute(
                                    lineId = line.id,
                                    lineName = line.name,
                                    lineColor = line.color,
                                    stationId = station.stopId,
                                    stationName = station.name,
                                    direction = "",
                                    directionLabel = "Toward Ferry",
                                    feedGroup = line.feedGroup
                                )
                                scope.launch {
                                    if (isFavouriteMode) app.subwayRepository.saveFavouriteRoute(route)
                                    else app.subwayRepository.saveRoute(route)
                                    onRouteSelected()
                                }
                            } else {
                                step = PickerStep.DIRECTION
                            }
                        }
                    )
                }

                PickerStep.DIRECTION -> {
                    DirectionPicker(
                        station = selectedStation!!,
                        onDirectionSelected = { direction ->
                            val line = selectedLine!!
                            val station = selectedStation!!
                            val dirLabel = if (direction == Direction.NORTH)
                                station.northLabel else station.southLabel

                            val route = SavedRoute(
                                lineId = line.id,
                                lineName = line.name,
                                lineColor = line.color,
                                stationId = station.stopId,
                                stationName = station.name,
                                direction = direction.suffix,
                                directionLabel = dirLabel,
                                feedGroup = line.feedGroup
                            )

                            scope.launch {
                                if (isFavouriteMode) {
                                    app.subwayRepository.saveFavouriteRoute(route)
                                } else {
                                    app.subwayRepository.saveRoute(route)
                                }
                                onRouteSelected()
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun LinePicker(onLineSelected: (SubwayLine) -> Unit) {
    val subwayLines = SubwayLines.all.filter { !it.isBus && it.id != "SI" }
    val busLines = SubwayLines.all.filter { it.isBus }

    LazyColumn(contentPadding = PaddingValues(16.dp)) {
        item {
            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                modifier = Modifier.height(((subwayLines.size / 4 + 1) * 80).dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                userScrollEnabled = false
            ) {
                items(subwayLines) { line ->
                    Box(
                        modifier = Modifier
                            .aspectRatio(1f)
                            .clip(CircleShape)
                            .background(Color(line.color))
                            .clickable { onLineSelected(line) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            line.name,
                            color = Color(line.textColor),
                            fontWeight = FontWeight.Bold,
                            fontSize = 28.sp
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                "FERRY BUSES",
                color = SubwayLightGray,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp,
                modifier = Modifier.padding(bottom = 12.dp)
            )
        }

        items(busLines) { line ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
                    .clickable { onLineSelected(line) },
                colors = CardDefaults.cardColors(containerColor = SubwayDarkGray),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(line.color)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            line.name,
                            color = Color(line.textColor),
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            when (line.id) {
                                "M42" -> "42nd St Crosstown"
                                "M34+" -> "34th St SBS"
                                else -> line.id
                            },
                            color = SubwayWhite,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp
                        )
                        Text(
                            "→ NYC Ferry Terminal",
                            color = SubwayLightGray,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StationPicker(
    lineId: String,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    onStationSelected: (Station) -> Unit
) {
    val app = SubwayPingApp.instance
    val stations = remember(lineId, searchQuery) {
        app.stationRepository.searchStations(searchQuery, lineId)
    }

    Column {
        // Search bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            placeholder = { Text("Search stations...") },
            leadingIcon = { Icon(Icons.Default.Search, "Search") },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = SubwayLightGray,
                unfocusedBorderColor = SubwayMedGray,
                cursorColor = SubwayWhite,
                focusedTextColor = SubwayWhite,
                unfocusedTextColor = SubwayWhite,
                focusedPlaceholderColor = SubwayLightGray,
                unfocusedPlaceholderColor = SubwayLightGray,
                focusedLeadingIconColor = SubwayLightGray,
                unfocusedLeadingIconColor = SubwayLightGray
            ),
            shape = RoundedCornerShape(12.dp)
        )

        LazyColumn(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(stations) { station ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onStationSelected(station) },
                    colors = CardDefaults.cardColors(containerColor = SubwayDarkGray),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            station.name,
                            color = SubwayWhite,
                            fontSize = 16.sp,
                            modifier = Modifier.weight(1f)
                        )

                        // Show other lines at this station
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            station.lines.take(5).forEach { lineAtStation ->
                                val lineInfo = SubwayLines.getLine(lineAtStation)
                                if (lineInfo != null) {
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clip(CircleShape)
                                            .background(Color(lineInfo.color)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            lineInfo.name,
                                            color = Color(lineInfo.textColor),
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DirectionPicker(
    station: Station,
    onDirectionSelected: (Direction) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            station.name,
            color = SubwayWhite,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(40.dp))

        // Northbound button
        DirectionButton(
            label = station.northLabel,
            color = SubwayMedGray,
            onClick = { onDirectionSelected(Direction.NORTH) }
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text("or", color = SubwayLightGray, fontSize = 16.sp)

        Spacer(modifier = Modifier.height(16.dp))

        // Southbound button
        DirectionButton(
            label = station.southLabel,
            color = SubwayMedGray,
            onClick = { onDirectionSelected(Direction.SOUTH) }
        )
    }
}

@Composable
private fun DirectionButton(label: String, color: Color, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = color),
        shape = RoundedCornerShape(16.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                label,
                color = SubwayWhite,
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
