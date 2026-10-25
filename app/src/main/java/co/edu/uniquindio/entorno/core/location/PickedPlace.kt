package co.edu.uniquindio.entorno.core.location

import co.edu.uniquindio.entorno.domain.model.Location

/** Resultado del selector de ubicación: coordenadas más ciudad y dirección resueltas. */
data class PickedPlace(
    val location: Location,
    val city: String,
    val address: String
)