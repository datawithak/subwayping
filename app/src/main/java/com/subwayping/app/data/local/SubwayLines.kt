package com.subwayping.app.data.local

/** All NYC subway lines with official MTA colors */
object SubwayLines {
    val all: List<SubwayLine> = listOf(
        // IRT - Numbers
        SubwayLine("1", "1", 0xFFEE352E, 0xFFFFFFFF, "gtfs"),
        SubwayLine("2", "2", 0xFFEE352E, 0xFFFFFFFF, "gtfs"),
        SubwayLine("3", "3", 0xFFEE352E, 0xFFFFFFFF, "gtfs"),
        SubwayLine("4", "4", 0xFF00933C, 0xFFFFFFFF, "gtfs"),
        SubwayLine("5", "5", 0xFF00933C, 0xFFFFFFFF, "gtfs"),
        SubwayLine("6", "6", 0xFF00933C, 0xFFFFFFFF, "gtfs"),
        SubwayLine("7", "7", 0xFFB933AD, 0xFFFFFFFF, "gtfs"),

        // IND - A Division
        SubwayLine("A", "A", 0xFF0039A6, 0xFFFFFFFF, "gtfs-ace"),
        SubwayLine("C", "C", 0xFF0039A6, 0xFFFFFFFF, "gtfs-ace"),
        SubwayLine("E", "E", 0xFF0039A6, 0xFFFFFFFF, "gtfs-ace"),

        // IND - B Division
        SubwayLine("B", "B", 0xFFFF6319, 0xFFFFFFFF, "gtfs-bdfm"),
        SubwayLine("D", "D", 0xFFFF6319, 0xFFFFFFFF, "gtfs-bdfm"),
        SubwayLine("F", "F", 0xFFFF6319, 0xFFFFFFFF, "gtfs-bdfm"),
        SubwayLine("M", "M", 0xFFFF6319, 0xFFFFFFFF, "gtfs-bdfm"),

        // BMT
        SubwayLine("N", "N", 0xFFFCCC0A, 0xFF000000, "gtfs-nqrw"),
        SubwayLine("Q", "Q", 0xFFFCCC0A, 0xFF000000, "gtfs-nqrw"),
        SubwayLine("R", "R", 0xFFFCCC0A, 0xFF000000, "gtfs-nqrw"),
        SubwayLine("W", "W", 0xFFFCCC0A, 0xFF000000, "gtfs-nqrw"),

        // Crosstown
        SubwayLine("G", "G", 0xFF6CBE45, 0xFFFFFFFF, "gtfs-g"),

        // BMT Eastern
        SubwayLine("J", "J", 0xFF996633, 0xFFFFFFFF, "gtfs-jz"),
        SubwayLine("Z", "Z", 0xFF996633, 0xFFFFFFFF, "gtfs-jz"),

        // Canarsie
        SubwayLine("L", "L", 0xFFA7A9AC, 0xFF000000, "gtfs-l"),

        // Shuttle
        SubwayLine("S", "S", 0xFF808183, 0xFFFFFFFF, "gtfs"),

        // Staten Island Railway
        SubwayLine("SI", "SIR", 0xFF0039A6, 0xFFFFFFFF, "gtfs-si"),
    )

    fun getLine(id: String): SubwayLine? = all.find { it.id == id }

    fun getFeedGroup(lineId: String): String =
        getLine(lineId)?.feedGroup ?: "gtfs"
}
