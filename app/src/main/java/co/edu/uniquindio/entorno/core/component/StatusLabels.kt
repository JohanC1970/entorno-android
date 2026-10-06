package co.edu.uniquindio.entorno.core.component

import co.edu.uniquindio.entorno.domain.model.ReportStatus
import co.edu.uniquindio.entorno.domain.model.Severity

fun ReportStatus.labelForCitizen(): String = when (this) {
    ReportStatus.PENDING -> "En verificación"
    ReportStatus.VERIFIED -> "Verificado"
    ReportStatus.REJECTED -> "Rechazado"
    ReportStatus.RESOLVED -> "Resuelto"
}

fun ReportStatus.labelForModerator(): String = when (this) {
    ReportStatus.PENDING -> "Pendiente"
    ReportStatus.VERIFIED -> "Verificado"
    ReportStatus.REJECTED -> "Rechazado"
    ReportStatus.RESOLVED -> "Resuelto"
}

fun Severity.label(): String = when (this) {
    Severity.LOW -> "Baja"
    Severity.MEDIUM -> "Media"
    Severity.HIGH, Severity.CRITICAL -> "Alta"
}
