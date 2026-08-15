package dev.jvfl.progtv.domain.repository

import dev.jvfl.progtv.domain.model.Channel
import kotlinx.coroutines.flow.Flow

/** Source of the channel catalog (backed by the /channels endpoint). */
interface ChannelsRepository {
    /** Fetches the current catalog. Throws on network/HTTP errors (surfaced to the UI). */
    suspend fun getChannels(): List<Channel>
}

/** Locally persisted favorite channel ids. */
interface FavoritesRepository {
    val favoriteIds: Flow<Set<String>>
    suspend fun toggle(channelId: String)
}

/** User settings (API base URL + last watched channel). */
interface SettingsRepository {
    val baseUrl: Flow<String>
    suspend fun setBaseUrl(url: String)

    /** Synchronous snapshot for the OkHttp interceptor (non-suspend). */
    fun currentBaseUrl(): String

    /** Id of the last channel the user watched (null if none yet). */
    val lastChannelId: Flow<String?>
    suspend fun setLastChannelId(channelId: String)
}
