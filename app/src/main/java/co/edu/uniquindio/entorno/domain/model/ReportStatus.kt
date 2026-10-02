package co.edu.uniquindio.entorno.domain.model

enum class ReportStatus {
    PENDING,   // Sin verificar (estado inicial de todo reporte)
    VERIFIED,  // Verificado por un moderador
    REJECTED,  // Rechazado por un moderador (con motivo); no aparece en el feed público
    RESOLVED   // Resuelto / finalizado (por el dueño o un moderador)
}