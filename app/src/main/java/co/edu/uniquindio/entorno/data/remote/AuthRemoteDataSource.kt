package co.edu.uniquindio.entorno.data.remote

import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import jakarta.inject.Inject
import jakarta.inject.Singleton


@Singleton
class AuthRemoteDataSource @Inject constructor(private val auth: FirebaseAuth){

    val currentUserId: String? get() = auth.currentUser?.uid

    fun observeAuthState(): Flow<String?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener{ trySend(it.currentUser?.uid) }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    suspend fun register(email: String, password: String): String =
        auth.createUserWithEmailAndPassword(email,password).await().user!!.uid

    suspend fun login(email: String, password: String): String =
        auth.signInWithEmailAndPassword(email,password).await().user!!.uid

    suspend fun sendPasswordReset(email: String) {
        auth.sendPasswordResetEmail(email).await()
    }

    suspend fun reauthenticate(password: String){
        val user = auth.currentUser ?: error("No hay sesión activa")
        val credential = EmailAuthProvider.getCredential(user.email.orEmpty(), password)
        user.reauthenticate(credential).await()
    }

    suspend fun deleteCurrentUser(){
        auth.currentUser?.delete()?.await()
    }

    fun logout() = auth.signOut()

}