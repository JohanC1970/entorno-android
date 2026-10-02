package co.edu.uniquindio.entorno.data.remote

import co.edu.uniquindio.entorno.core.util.asFlow
import co.edu.uniquindio.entorno.data.model.UserDto
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await
import jakarta.inject.Inject
import jakarta.inject.Singleton

@Singleton
class UserRemoteDataSource @Inject constructor(db: FirebaseFirestore){

    private val users = db.collection("users")

    suspend fun get(id: String): UserDto? =
        users.document(id).get().await().toObject(UserDto::class.java)

    fun observe(id: String): Flow<UserDto?> = users.document(id).asFlow<UserDto>()

    suspend fun create(id: String, dto: UserDto) {
        users.document(id).set(dto).await()
    }

    suspend fun update(id: String, fields: Map<String, Any?>) {
        users.document(id).update(fields).await()
    }

    suspend fun addPoints(id: String, points: Int) {
        users.document(id).update("points", FieldValue.increment(points.toLong())).await()
    }

    suspend fun addBadge(id: String, badgeName: String) {
        users.document(id).update("badges", FieldValue.arrayUnion(badgeName)).await()
    }

    suspend fun delete(id: String) {
        users.document(id).delete().await()
    }


}