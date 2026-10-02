package co.edu.uniquindio.entorno.di

import co.edu.uniquindio.entorno.domain.repository.*
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import jakarta.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    /**
    @Binds @Singleton abstract fun bindAuth(impl: AuthRepositoryImpl): AuthRepository
    @Binds @Singleton abstract fun bindUser(impl: UserRepositoryImpl): UserRepository
    @Binds @Singleton abstract fun bindReport(impl: ReportRepositoryImpl): ReportRepository
    @Binds @Singleton abstract fun bindComment(impl: CommentRepositoryImpl): CommentRepository
    @Binds @Singleton abstract fun bindNotification(impl: NotificationRepositoryImpl): NotificationRepository
    @Binds @Singleton abstract fun bindSession(impl: SessionRepositoryImpl): SessionRepository
    @Binds @Singleton abstract fun bindImage(impl: ImageRepositoryImpl): ImageRepository
    */

}