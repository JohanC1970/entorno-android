package co.edu.uniquindio.entorno.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import co.edu.uniquindio.entorno.core.util.toEnumOrDefault
import co.edu.uniquindio.entorno.domain.model.UserRole
import co.edu.uniquindio.entorno.domain.model.UserSession
import co.edu.uniquindio.entorno.domain.repository.SessionRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import jakarta.inject.Inject
import jakarta.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "session")

@Singleton
class SessionRepositoryImpl @Inject constructor(
    @param:ApplicationContext private val context: Context
) : SessionRepository {

    private object Keys {
        val USER_ID = stringPreferencesKey("user_id")
        val ROLE = stringPreferencesKey("role")
    }

    override val session: Flow<UserSession?> = context.dataStore.data.map { prefs ->
        val userId = prefs[Keys.USER_ID]
        val role = prefs[Keys.ROLE]
        if (userId.isNullOrBlank() || role.isNullOrBlank()) null
        else UserSession(userId, role.toEnumOrDefault(UserRole.USER))
    }

    override suspend fun saveSession(userId: String, role: UserRole) {
        context.dataStore.edit { it[Keys.USER_ID] = userId; it[Keys.ROLE] = role.name }
    }

    override suspend fun clearSession() {
        context.dataStore.edit { it.clear() }
    }
}