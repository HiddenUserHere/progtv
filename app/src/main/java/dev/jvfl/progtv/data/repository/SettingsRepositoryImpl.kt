package dev.jvfl.progtv.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import dev.jvfl.progtv.BuildConfig
import dev.jvfl.progtv.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) : SettingsRepository {

    override val baseUrl: Flow<String> =
        dataStore.data.map { prefs -> prefs[KEY]?.takeIf { it.isNotBlank() } ?: BuildConfig.DEFAULT_BASE_URL }

    // Synchronous snapshot for the OkHttp interceptor. The base URL is fixed
    // (no in-app settings), so this defaults to BuildConfig and only changes if
    // setBaseUrl is ever called.
    @Volatile
    private var cached: String = BuildConfig.DEFAULT_BASE_URL

    override suspend fun setBaseUrl(url: String) {
        val normalized = url.trim().removeSuffix("/")
        dataStore.edit { it[KEY] = normalized }
        cached = normalized
    }

    override fun currentBaseUrl(): String = cached

    override val lastChannelId: Flow<String?> =
        dataStore.data.map { prefs -> prefs[LAST_CHANNEL_KEY]?.takeIf { it.isNotBlank() } }

    override suspend fun setLastChannelId(channelId: String) {
        dataStore.edit { it[LAST_CHANNEL_KEY] = channelId }
    }

    private companion object {
        val KEY = stringPreferencesKey("base_url")
        val LAST_CHANNEL_KEY = stringPreferencesKey("last_channel_id")
    }
}
