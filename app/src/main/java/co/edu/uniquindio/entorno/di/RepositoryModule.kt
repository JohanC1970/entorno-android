package co.edu.uniquindio.entorno.di

import co.edu.uniquindio.entorno.data.repository.AuthRepositoryImpl
import co.edu.uniquindio.entorno.data.repository.CommentRepositoryImpl
import co.edu.uniquindio.entorno.data.repository.DuplicateDetectorImpl
import co.edu.uniquindio.entorno.data.repository.ImageRepositoryImpl
import co.edu.uniquindio.entorno.data.repository.NotificationRepositoryImpl
import co.edu.uniquindio.entorno.data.repository.ReportRepositoryImpl
import co.edu.uniquindio.entorno.data.repository.SessionRepositoryImpl
import co.edu.uniquindio.entorno.data.repository.SettingsRepositoryImpl
import co.edu.uniquindio.entorno.data.repository.UserRepositoryImpl
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
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository

    @Binds
    @Singleton
    abstract fun bindUserRepository(impl: UserRepositoryImpl): UserRepository

    @Binds
    @Singleton
    abstract fun bindReportRepository(impl: ReportRepositoryImpl): ReportRepository

    @Binds
    @Singleton
    abstract fun bindCommentRepository(impl: CommentRepositoryImpl): CommentRepository

    @Binds
    @Singleton
    abstract fun bindNotificationRepository(impl: NotificationRepositoryImpl): NotificationRepository

    @Binds
    @Singleton
    abstract fun bindSessionRepository(impl: SessionRepositoryImpl): SessionRepository

    @Binds
    @Singleton
    abstract fun bindImageRepository(impl: ImageRepositoryImpl): ImageRepository

    @Binds
    @Singleton
    abstract fun bindSettingsRepository(impl: SettingsRepositoryImpl): SettingsRepository

    @Binds
    @Singleton
    abstract fun bindDuplicateDetector(impl: DuplicateDetectorImpl): DuplicateDetector

}
