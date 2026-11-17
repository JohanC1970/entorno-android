package co.edu.uniquindio.entorno.core.util

import co.edu.uniquindio.entorno.R
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
 * Devuelve [UiText] para que la pantalla lo muestre en el idioma elegido.
 */
fun Long.toRelativeTime(now: Long = System.currentTimeMillis()): UiText {
    val elapsed = if (this <= 0L) 0L else now - this
    return when {
        elapsed < MINUTE_MS -> UiText(R.string.time_now)
        elapsed < HOUR_MS -> (elapsed / MINUTE_MS).toInt().let { UiText.plural(R.plurals.time_minutes_ago, it, it) }
        elapsed < DAY_MS -> (elapsed / HOUR_MS).toInt().let { UiText.plural(R.plurals.time_hours_ago, it, it) }
        else -> (elapsed / DAY_MS).toInt().let { UiText.plural(R.plurals.time_days_ago, it, it) }
    }
}

/** Fecha relativa por días: "hoy", "ayer", "hace 3 días", "hace 2 semanas"... */
fun java.time.LocalDate.toRelativeDate(today: java.time.LocalDate = java.time.LocalDate.now()): UiText {
    val days = java.time.temporal.ChronoUnit.DAYS.between(this, today).toInt().coerceAtLeast(0)
    return when {
        days == 0 -> UiText(R.string.time_today)
        days == 1 -> UiText(R.string.time_yesterday)
        days < 7 -> UiText.plural(R.plurals.time_days_ago, days, days)
        days < 30 -> (days / 7).let { UiText.plural(R.plurals.time_weeks_ago, it, it) }
        else -> (days / 30).let { UiText.plural(R.plurals.time_months_ago, it, it) }
    }
}
