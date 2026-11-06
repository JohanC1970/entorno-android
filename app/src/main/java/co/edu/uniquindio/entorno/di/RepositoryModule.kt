package co.edu.uniquindio.entorno.di

import co.edu.uniquindio.entorno.data.repository.memory.InMemoryAuthRepository
import co.edu.uniquindio.entorno.data.repository.memory.InMemoryCommentRepository
import co.edu.uniquindio.entorno.data.repository.memory.InMemoryImageRepository
import co.edu.uniquindio.entorno.data.repository.memory.InMemoryNotificationRepository
import co.edu.uniquindio.entorno.data.repository.memory.InMemoryReportRepository
import co.edu.uniquindio.entorno.data.repository.memory.InMemorySessionRepository
import co.edu.uniquindio.entorno.data.repository.memory.InMemorySettingsRepository
import co.edu.uniquindio.entorno.data.repository.memory.InMemoryUserRepository
import co.edu.uniquindio.entorno.domain.repository.*
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import jakarta.inject.Singleton

/**
 * Fase 2: todos los repositorios viven en memoria (data/repository/memory).
 *
 * Para la Fase 3 (persistencia) basta con volver a enlazar cada interfaz con su
 * implementación de Firebase/DataStore/Cloudinary, que siguen en data/repository
 * (AuthRepositoryImpl, UserRepositoryImpl, ReportRepositoryImpl, CommentRepositoryImpl,
 * NotificationRepositoryImpl, SessionRepositoryImpl e ImageRepositoryImpl).
 * Las pantallas y ViewModels no cambian porque solo dependen de las interfaces del dominio.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(impl: InMemoryAuthRepository): AuthRepository

    @Binds
    @Singleton
    abstract fun bindUserRepository(impl: InMemoryUserRepository): UserRepository

    @Binds
    @Singleton
    abstract fun bindReportRepository(impl: InMemoryReportRepository): ReportRepository

    @Binds
    @Singleton
    abstract fun bindCommentRepository(impl: InMemoryCommentRepository): CommentRepository

    @Binds
    @Singleton
    abstract fun bindNotificationRepository(impl: InMemoryNotificationRepository): NotificationRepository

    @Binds
    @Singleton
    abstract fun bindSessionRepository(impl: InMemorySessionRepository): SessionRepository

    @Binds
    @Singleton
    abstract fun bindImageRepository(impl: InMemoryImageRepository): ImageRepository

    @Binds
    @Singleton
    abstract fun bindSettingsRepository(impl: InMemorySettingsRepository): SettingsRepository
}
