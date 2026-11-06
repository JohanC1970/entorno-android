package co.edu.uniquindio.entorno.domain.repository

import co.edu.uniquindio.entorno.domain.model.Report

interface DuplicateDetector {
    /** Devuelve, de [candidates], los reportes que describen el mismo evento que el nuevo. */
    suspend fun findDuplicates(
        title: String,
        description: String,
        candidates: List<Report>
    ): Result<List<Report>>
}