package dev.jvfl.progtv.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.jvfl.progtv.domain.model.Category
import dev.jvfl.progtv.domain.model.Channel
import dev.jvfl.progtv.domain.repository.FavoritesRepository
import dev.jvfl.progtv.domain.repository.SettingsRepository
import dev.jvfl.progtv.domain.usecase.ALL_CATEGORY_ID
import dev.jvfl.progtv.domain.usecase.BuildCategoriesUseCase
import dev.jvfl.progtv.domain.usecase.GetChannelsUseCase
import dev.jvfl.progtv.domain.usecase.ToggleFavoriteUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.inject.Inject

data class HomeUiState(
    val loading: Boolean = true,
    val error: String? = null,
    val categories: List<Category> = emptyList(),
    val favoriteIds: Set<String> = emptySet(),
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getChannels: GetChannelsUseCase,
    private val buildCategories: BuildCategoriesUseCase,
    private val toggleFavorite: ToggleFavoriteUseCase,
    private val favoritesRepository: FavoritesRepository,
    private val settings: SettingsRepository,
) : ViewModel() {

    private val channels = MutableStateFlow<List<Channel>>(emptyList())
    private val loading = MutableStateFlow(true)
    private val error = MutableStateFlow<String?>(null)

    // The channel to start on once the catalog loads: last watched (if still present),
    // else first favorite, else first channel in "Todos". Emitted once, then consumed
    // by the screen. Null while unresolved.
    private val _startChannelId = MutableStateFlow<String?>(null)
    val startChannelId: StateFlow<String?> = _startChannelId
    private var startResolved = false

    val uiState: StateFlow<HomeUiState> = combine(
        channels,
        favoritesRepository.favoriteIds,
        loading,
        error,
    ) { channelList, favorites, isLoading, err ->
        HomeUiState(
            loading = isLoading,
            error = err,
            categories = buildCategories(channelList, favorites),
            favoriteIds = favorites,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState())

    init {
        load()
        // Silent periodic refresh of the catalog every 30 minutes (no loading spinner;
        // keeps the current data on transient failures so playback isn't disrupted).
        viewModelScope.launch {
            while (true) {
                delay(REFRESH_INTERVAL_MS)
                runCatching { channels.value = getChannels() }
            }
        }
    }

    fun load() {
        viewModelScope.launch {
            loading.value = true
            error.value = null
            try {
                channels.value = getChannels()
                resolveStartChannel(channels.value)
            } catch (e: Exception) {
                error.value = describe(e)
            } finally {
                loading.value = false
            }
        }
    }

    fun onToggleFavorite(channelId: String) {
        viewModelScope.launch { toggleFavorite(channelId) }
    }

    /** Persists the currently watched channel so it can be resumed on the next launch. */
    fun onChannelWatched(channelId: String) {
        viewModelScope.launch { settings.setLastChannelId(channelId) }
    }

    /**
     * Resolves the channel to open first, once, after the catalog is available:
     * 1) last watched channel if still in the catalog,
     * 2) otherwise the first favorite,
     * 3) otherwise the first channel in "Todos".
     */
    private suspend fun resolveStartChannel(channelList: List<Channel>) {
        if (startResolved || channelList.isEmpty()) return
        val favorites = favoritesRepository.favoriteIds.first()
        val categories = buildCategories(channelList, favorites)
        val lastId = settings.lastChannelId.first()

        val resolved = lastId?.takeIf { id -> channelList.any { it.id == id } }
            ?: categories.firstOrNull { it.isFavorites }?.channels?.firstOrNull()?.id
            ?: categories.firstOrNull { it.id == ALL_CATEGORY_ID }?.channels?.firstOrNull()?.id

        _startChannelId.value = resolved
        startResolved = true
    }

    /** Builds a detailed, user-facing message so a backend outage is diagnosable on screen. */
    private fun describe(e: Throwable): String {
        val base = settings.currentBaseUrl()
        return when (e) {
            is HttpException -> "HTTP ${e.code()} ao acessar\n$base/channels"
            is UnknownHostException -> "Host não encontrado:\n$base"
            is ConnectException -> "Conexão recusada:\n$base"
            is SocketTimeoutException -> "Tempo esgotado ao conectar em\n$base"
            else -> "${e.javaClass.simpleName}: ${e.message ?: "erro desconhecido"}\n$base/channels"
        }
    }

    private companion object {
        const val REFRESH_INTERVAL_MS = 30L * 60L * 1000L // 30 minutes
    }
}
