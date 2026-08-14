package dev.jvfl.progtv.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import dev.jvfl.progtv.domain.repository.FavoritesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FavoritesRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) : FavoritesRepository {

    override val favoriteIds: Flow<Set<String>> =
        dataStore.data.map { prefs -> prefs[KEY] ?: emptySet() }

    override suspend fun toggle(channelId: String) {
        dataStore.edit { prefs ->
            val current = prefs[KEY]?.toMutableSet() ?: mutableSetOf()
            if (!current.add(channelId)) current.remove(channelId)
            prefs[KEY] = current
        }
    }

    private companion object {
        val KEY = stringSetPreferencesKey("favorite_channel_ids")
    }
}
