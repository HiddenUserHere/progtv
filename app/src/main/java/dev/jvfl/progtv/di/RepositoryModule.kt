package dev.jvfl.progtv.di

import dev.jvfl.progtv.data.repository.ChannelsRepositoryImpl
import dev.jvfl.progtv.data.repository.FavoritesRepositoryImpl
import dev.jvfl.progtv.data.repository.SettingsRepositoryImpl
import dev.jvfl.progtv.domain.repository.ChannelsRepository
import dev.jvfl.progtv.domain.repository.FavoritesRepository
import dev.jvfl.progtv.domain.repository.SettingsRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindChannelsRepository(impl: ChannelsRepositoryImpl): ChannelsRepository

    @Binds
    @Singleton
    abstract fun bindFavoritesRepository(impl: FavoritesRepositoryImpl): FavoritesRepository

    @Binds
    @Singleton
    abstract fun bindSettingsRepository(impl: SettingsRepositoryImpl): SettingsRepository
}
