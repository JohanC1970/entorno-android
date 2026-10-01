package co.edu.uniquindio.entorno.core.util

sealed class RequestResult {
    data object Loading : RequestResult()
    data class Success(val message: String) : RequestResult()
    data class Failure(val errorMessage: String) : RequestResult()
}