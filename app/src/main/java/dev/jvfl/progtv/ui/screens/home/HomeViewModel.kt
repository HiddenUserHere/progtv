package dev.jvfl.progtv.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.jvfl.progtv.domain.model.Category
import dev.jvfl.progtv.domain.model.Channel
import dev.jvfl.progtv.domain.repository.FavoritesRepository
import dev.jvfl.progtv.domain.repository.SettingsRepository
import dev.jvfl.progtv.domain.usecase.BuildCategoriesUseCase
import dev.jvfl.progtv.domain.usecase.GetChannelsUseCase
import dev.jvfl.progtv.domain.usecase.ToggleFavoriteUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.combine
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
    favoritesRepository: FavoritesRepository,
    private val settings: SettingsRepository,
) : ViewModel() {

    private val channels = MutableStateFlow<List<Channel>>(emptyList())
    private val loading = MutableStateFlow(true)
    private val error = MutableStateFlow<String?>(null)

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
