package co.edu.uniquindio.entorno.di

import co.edu.uniquindio.entorno.data.repository.AuthRepositoryImpl
import co.edu.uniquindio.entorno.data.repository.CommentRepositoryImpl
import co.edu.uniquindio.entorno.data.repository.ImageRepositoryImpl
import co.edu.uniquindio.entorno.data.repository.NotificationRepositoryImpl
import co.edu.uniquindio.entorno.data.repository.ReportRepositoryImpl
import co.edu.uniquindio.entorno.data.repository.SessionRepositoryImpl
import co.edu.uniquindio.entorno.data.repository.UserRepositoryImpl
import co.edu.uniquindio.entorno.data.repository.memory.InMemorySettingsRepository
import co.edu.uniquindio.entorno.domain.repository.*
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import jakarta.inject.Singleton

/**
 * Fase 3: repositorios conectados a Firebase / DataStore / Cloudinary.
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
    abstract fun bindSettingsRepository(impl: InMemorySettingsRepository): SettingsRepository
}
