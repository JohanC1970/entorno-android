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
