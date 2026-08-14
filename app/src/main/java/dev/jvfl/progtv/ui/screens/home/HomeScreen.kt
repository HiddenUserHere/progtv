package dev.jvfl.progtv.ui.screens.home

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.jvfl.progtv.ui.screens.error.ErrorContent
import dev.jvfl.progtv.ui.screens.player.PlayerSurface

/** Which column of the browse menu currently owns focus (drives the back behavior). */
private enum class NavLevel { CATEGORIES, CHANNELS }

/**
 * Root screen: fullscreen player with a browse overlay ("menu") on top.
 *
 * Back behavior: CHANNELS -> CATEGORIES -> CLOSE MENU -> exit app.
 * UP/DOWN zap to the previous/next channel — crossing category boundaries (opposite
 * end) and wrapping around — both on the fullscreen player and inside the channel list.
 */
@Composable
fun HomeScreen() {
    val viewModel: HomeViewModel = hiltViewModel()
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var menuOpen by remember { mutableStateOf(true) }
    var navLevel by remember { mutableStateOf(NavLevel.CHANNELS) }
    var currentChannelId by remember { mutableStateOf<String?>(null) }
    // Start on "Todos" (index 1: Favoritos=0, Todos=1) so channels show on first open.
    var selectedCategory by remember { mutableIntStateOf(1) }
    var menuFocusChannelId by remember { mutableStateOf<String?>(null) }
    var openToken by remember { mutableIntStateOf(0) }
    var backToCategoriesToken by remember { mutableIntStateOf(0) }
    var zapToken by remember { mutableIntStateOf(0) }

    val currentChannel = remember(state.categories, currentChannelId) {
        state.categories.flatMap { it.channels }.firstOrNull { it.id == currentChannelId }
    }

    // Zap to prev/next channel (used on the fullscreen player and at list boundaries).
    fun zap(dir: Int) {
        moveChannel(state.categories, selectedCategory, currentChannelId, dir)?.let { pos ->
            selectedCategory = pos.categoryIndex
            currentChannelId = pos.channel.id
            menuFocusChannelId = pos.channel.id
            zapToken++ // re-assert focus on the overlay after the reload
        }
    }

    // Cross to the adjacent non-empty category inside the open menu (opposite end).
    fun crossCategory(dir: Int) {
        val newIdx = nextNonEmptyCategoryIndex(state.categories, selectedCategory, dir)
        if (newIdx != selectedCategory) {
            val channels = state.categories[newIdx].channels
            menuFocusChannelId = if (dir > 0) channels.first().id else channels.last().id
            selectedCategory = newIdx
            openToken++
        }
    }

    // Focus target that captures OK/MENU/UP/DOWN while playing fullscreen. Re-assert on
    // every zap so channel switching never leaves the overlay without focus.
    val closedFocus = remember { FocusRequester() }
    LaunchedEffect(menuOpen, zapToken) {
        if (!menuOpen) runCatching { closedFocus.requestFocus() }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        PlayerSurface(url = currentChannel?.playUrl)

        if (!menuOpen) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .focusRequester(closedFocus)
                    .focusable()
                    .onKeyEvent { event ->
                        val openKey = event.key == Key.DirectionCenter ||
                            event.key == Key.Enter || event.key == Key.Menu
                        when {
                            event.type == KeyEventType.KeyUp && openKey -> {
                                menuFocusChannelId = currentChannelId
                                menuOpen = true
                                navLevel = NavLevel.CHANNELS
                                openToken++
                                true
                            }
                            event.type == KeyEventType.KeyDown && event.key == Key.DirectionUp -> {
                                zap(-1); true
                            }
                            event.type == KeyEventType.KeyDown && event.key == Key.DirectionDown -> {
                                zap(1); true
                            }
                            else -> false
                        }
                    },
            )
        }

        if (menuOpen) {
            if (state.error != null) {
                ErrorContent(
                    message = state.error!!,
                    onRetry = { viewModel.load() },
                )
            } else {
                BrowseMenu(
                    categories = state.categories,
                    loading = state.loading,
                    selectedCategory = selectedCategory,
                    favoriteIds = state.favoriteIds,
                    openToken = openToken,
                    backToCategoriesToken = backToCategoriesToken,
                    focusChannelId = menuFocusChannelId,
                    onCategoryFocused = { index ->
                        selectedCategory = index
                        navLevel = NavLevel.CATEGORIES
                    },
                    onChannelsFocused = { navLevel = NavLevel.CHANNELS },
                    onOpenChannel = { channel ->
                        currentChannelId = channel.id
                        menuFocusChannelId = channel.id
                        menuOpen = false
                    },
                    onToggleFavorite = { channel -> viewModel.onToggleFavorite(channel.id) },
                    onCrossCategory = { dir -> crossCategory(dir) },
                )
            }
        }
    }

    BackHandler(enabled = true) {
        when {
            !menuOpen -> (context as? Activity)?.finish()
            navLevel == NavLevel.CHANNELS -> {
                navLevel = NavLevel.CATEGORIES
                backToCategoriesToken++
            }
            else -> {
                if (currentChannelId != null) menuOpen = false
                else (context as? Activity)?.finish()
            }
        }
    }
}
