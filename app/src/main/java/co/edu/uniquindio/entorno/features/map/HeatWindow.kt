package co.edu.uniquindio.entorno.features.map

import co.edu.uniquindio.entorno.domain.model.Report
import java.time.Instant
import java.time.ZoneId

internal const val WINDOW_HOURS = 3

fun formatHour(hour: Int): String {
    val suffix = if (hour < 12) "a. m." else "p. m."
    val h12 = when (val h = hour % 12) { 0 -> 12 else -> h }
    return "$h12 $suffix"
}

internal fun Report.hourOfDay(): Int =
    Instant.ofEpochMilli(createdAt).atZone(ZoneId.systemDefault()).hour

internal fun inWindow(hour: Int, start: Int): Boolean {
    val offset = (hour - start + 24) % 24
    return offset < WINDOW_HOURS
}
