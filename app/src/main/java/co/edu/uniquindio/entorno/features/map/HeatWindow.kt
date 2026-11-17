package co.edu.uniquindio.entorno.features.map

import co.edu.uniquindio.entorno.R
import co.edu.uniquindio.entorno.core.util.UiText
import co.edu.uniquindio.entorno.domain.model.Report
import java.time.Instant
import java.time.ZoneId

internal const val WINDOW_HOURS = 3

fun formatHour(hour: Int): UiText {
    val h12 = when (val h = hour % 12) { 0 -> 12 else -> h }
    return UiText(if (hour < 12) R.string.hour_am else R.string.hour_pm, h12)
}

internal fun Report.hourOfDay(): Int =
    Instant.ofEpochMilli(createdAt).atZone(ZoneId.systemDefault()).hour

internal fun inWindow(hour: Int, start: Int): Boolean {
    val offset = (hour - start + 24) % 24
    return offset < WINDOW_HOURS
}
