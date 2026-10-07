package co.edu.uniquindio.entorno.core.util

import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.Query
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

/** Como runCatching, pero no se traga la cancelación de corrutinas. */
suspend inline fun <T> safeCall(block: () -> T): Result<T> =
    try {
        Result.success(block())
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Result.failure(e)
    }

/** Escucha una consulta de Firestore en tiempo real como Flow. */
inline fun <reified T : Any> Query.asFlow(): Flow<List<T>> = callbackFlow {
    val registration = addSnapshotListener { snap, error ->
        if (error != null) {
            close(error)
            return@addSnapshotListener
        }
        trySend(snap?.toObjects(T::class.java).orEmpty())
    }
    awaitClose { registration.remove() }
}

/** Escucha un documento de Firestore en tiempo real como Flow. */
inline fun <reified T : Any> DocumentReference.asFlow(): Flow<T?> = callbackFlow {
    val registration = addSnapshotListener { snap, error ->
        if (error != null) {
            close(error)
            return@addSnapshotListener
        }
        trySend(snap?.toObject(T::class.java))
    }
    awaitClose { registration.remove() }
}

inline fun <reified E : Enum<E>> String?.toEnumOrDefault(default: E): E =
    enumValues<E>().firstOrNull { it.name == this } ?: default

private const val MINUTE_MS = 60_000L
private const val HOUR_MS = 60 * MINUTE_MS
private const val DAY_MS = 24 * HOUR_MS

/**
 * Formatea un epoch en milisegundos como tiempo relativo: "ahora", "hace 45 min",
 * "hace 3 h", "hace 2 días". Un valor sin asignar (<= 0) o a futuro (p. ej. un
 * timestamp de servidor que aún no llega) se muestra como "ahora".
 */
fun Long.toRelativeTime(now: Long = System.currentTimeMillis()): String {
    val elapsed = if (this <= 0L) 0L else now - this
    return when {
        elapsed < MINUTE_MS -> "ahora"
        elapsed < HOUR_MS -> "hace ${elapsed / MINUTE_MS} min"
        elapsed < DAY_MS -> "hace ${elapsed / HOUR_MS} h"
        else -> (elapsed / DAY_MS).let { days -> if (days == 1L) "hace 1 día" else "hace $days días" }
    }
}
