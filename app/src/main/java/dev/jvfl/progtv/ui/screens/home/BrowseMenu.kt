package dev.jvfl.progtv.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.LiveTv
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import dev.jvfl.progtv.domain.model.Category
import dev.jvfl.progtv.domain.model.Channel
import dev.jvfl.progtv.ui.components.RadiusMd
import dev.jvfl.progtv.ui.components.RadiusSm
import dev.jvfl.progtv.ui.components.RadiusXs
import dev.jvfl.progtv.ui.components.Spinner
import dev.jvfl.progtv.ui.components.focusGlow
import dev.jvfl.progtv.ui.components.focusScale
import dev.jvfl.progtv.ui.theme.BgBlack
import dev.jvfl.progtv.ui.theme.BgElevated
import dev.jvfl.progtv.ui.theme.BrandSoft
import dev.jvfl.progtv.ui.theme.FocusStroke
import dev.jvfl.progtv.ui.theme.GlassFill
import dev.jvfl.progtv.ui.theme.GlassFillFocus
import dev.jvfl.progtv.ui.theme.GlassFillStrong
import dev.jvfl.progtv.ui.theme.GlassStroke
import dev.jvfl.progtv.ui.theme.ScrimStrong
import dev.jvfl.progtv.ui.theme.StarAmber
import dev.jvfl.progtv.ui.theme.TextMuted
import dev.jvfl.progtv.ui.theme.TextPrimary

/** Long-press threshold (ms) for hold-OK to toggle favorite. */
private const val LONG_PRESS_MS = 450L

/**
 * Browse overlay: a glass category rail on the left and a VERTICAL channel list on the
 * right, drawn over the (dimmed) player. Focus is driven by the D-pad.
 *
 * @param openToken increments when the menu opens -> move focus into the channel list.
 * @param backToCategoriesToken increments on "back" from channels -> move focus to the rail.
 */
@Composable
fun BrowseMenu(
    categories: List<Category>,
    loading: Boolean,
    selectedCategory: Int,
    favoriteIds: Set<String>,
    openToken: Int,
    backToCategoriesToken: Int,
    focusChannelId: String?,
    onCategoryFocused: (Int) -> Unit,
    onChannelsFocused: () -> Unit,
    onOpenChannel: (Channel) -> Unit,
    onToggleFavorite: (Channel) -> Unit,
    onCrossCategory: (Int) -> Unit,
) {
    val railFocus = remember { FocusRequester() }
    val listFocus = remember { FocusRequester() }
    val listState = rememberLazyListState()
    val category = categories.getOrNull(selectedCategory)
    val channels = category?.channels.orEmpty()

    // Row to focus when the menu (re)opens: the currently-playing channel, else the first.
    val targetIndex = remember(channels, focusChannelId) {
        channels.indexOfFirst { it.id == focusChannelId }.takeIf { it >= 0 } ?: 0
    }

    // On (re)open, scroll the target row into view and focus it (keeps the current
    // channel selected instead of jumping to the top of the list).
    LaunchedEffect(openToken, channels.isNotEmpty()) {
        if (channels.isNotEmpty()) {
            runCatching { listState.scrollToItem(targetIndex) }
            runCatching { listFocus.requestFocus() }
        }
    }
    // Move focus back to the rail when returning from the channel level.
    LaunchedEffect(backToCategoriesToken) {
        if (backToCategoriesToken > 0) runCatching { railFocus.requestFocus() }
    }

    // The menu covers the LEFT 50% of the screen: 15% categories + 35% channels
    // (weights 15 + 35 inside a 50%-wide container). The right 50% shows the player.
    Row(
        modifier = Modifier
            .fillMaxHeight()
            .fillMaxWidth(0.5f),
    ) {
        CategoryRail(
            categories = categories,
            selectedIndex = selectedCategory,
            railFocus = railFocus,
            onCategoryFocused = onCategoryFocused,
            onCategoryClick = { runCatching { listFocus.requestFocus() } },
            modifier = Modifier.weight(15f).fillMaxHeight(),
        )

        Column(
            modifier = Modifier
                .weight(35f)
                .fillMaxHeight()
                .background(ScrimStrong)
                .padding(horizontal = 20.dp, vertical = 22.dp),
        ) {
            // Header: selected category name + channel count.
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = category?.name ?: "",
                    style = MaterialTheme.typography.headlineMedium,
                    color = TextPrimary,
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    text = if (channels.isEmpty()) "" else "${channels.size} canais",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted,
                    modifier = Modifier.padding(bottom = 4.dp),
                )
            }
            Spacer(Modifier.height(16.dp))

            Box(modifier = Modifier.fillMaxSize()) {
                when {
                    loading && channels.isEmpty() -> Spinner(modifier = Modifier.align(Alignment.Center))
                    channels.isEmpty() -> Text(
                        text = "Nenhum canal nesta categoria",
                        color = TextMuted,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.align(Alignment.Center),
                    )
                    else -> LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 24.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        itemsIndexed(channels, key = { _, c -> c.id }) { index, channel ->
                            ChannelRow(
                                channel = channel,
                                isFavorite = channel.id in favoriteIds,
                                modifier = if (index == targetIndex) Modifier.focusRequester(listFocus) else Modifier,
                                isFirst = index == 0,
                                isLast = index == channels.lastIndex,
                                onFocused = onChannelsFocused,
                                onOpen = { onOpenChannel(channel) },
                                onToggleFavorite = { onToggleFavorite(channel) },
                                onExitLeft = { runCatching { railFocus.requestFocus() } },
                                onCrossPrev = { onCrossCategory(-1) },
                                onCrossNext = { onCrossCategory(1) },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryRail(
    categories: List<Category>,
    selectedIndex: Int,
    railFocus: FocusRequester,
    onCategoryFocused: (Int) -> Unit,
    onCategoryClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .background(GlassFillStrong)
            .border(1.dp, GlassStroke)
            .padding(vertical = 22.dp),
    ) {
        Text(
            text = "ProgTV",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
        )
        Spacer(Modifier.height(8.dp))
        LazyColumn(
            modifier = Modifier.fillMaxWidth().fillMaxHeight(),
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            itemsIndexed(categories, key = { _, c -> c.id }) { index, category ->
                CategoryRow(
                    category = category,
                    selected = index == selectedIndex,
                    modifier = if (index == selectedIndex) Modifier.focusRequester(railFocus) else Modifier,
                    onFocused = { onCategoryFocused(index) },
                    onClick = onCategoryClick,
                    onEnterRight = onCategoryClick, // RIGHT enters the channel list
                )
            }
        }
    }
}

@Composable
private fun CategoryRow(
    category: Category,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onFocused: () -> Unit,
    onClick: () -> Unit,
    onEnterRight: () -> Unit,
) {
    var focused by remember { mutableStateOf(false) }
    val shape = RoundedCornerShape(RadiusSm)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .focusGlow(focused, shape)
            .clip(shape)
            .background(
                when {
                    focused -> GlassFillFocus
                    selected -> GlassFill
                    else -> androidx.compose.ui.graphics.Color.Transparent
                },
            )
            .border(
                width = if (focused) 1.5.dp else 1.dp,
                color = if (focused) FocusStroke else androidx.compose.ui.graphics.Color.Transparent,
                shape = shape,
            )
            .onFocusChanged { focused = it.isFocused; if (it.isFocused) onFocused() }
            // RIGHT always enters the channel list at the remembered position (avoids
            // spatial focus landing on a vertically-aligned channel).
            .onKeyEvent { event ->
                if (event.key == Key.DirectionRight) {
                    if (event.type == KeyEventType.KeyDown) onEnterRight()
                    true
                } else {
                    false
                }
            }
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(7.dp),
    ) {
        if (category.isFavorites) {
            Icon(Icons.Rounded.Star, contentDescription = null, tint = StarAmber, modifier = Modifier.size(15.dp))
        }
        Text(
            text = category.name,
            style = MaterialTheme.typography.titleMedium.copy(fontSize = 14.sp, letterSpacing = 0.sp),
            color = if (focused || selected) TextPrimary else TextMuted,
            maxLines = 1,
            softWrap = false,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun ChannelRow(
    channel: Channel,
    isFavorite: Boolean,
    modifier: Modifier = Modifier,
    isFirst: Boolean,
    isLast: Boolean,
    onFocused: () -> Unit,
    onOpen: () -> Unit,
    onToggleFavorite: () -> Unit,
    onExitLeft: () -> Unit,
    onCrossPrev: () -> Unit,
    onCrossNext: () -> Unit,
) {
    var focused by remember { mutableStateOf(false) }
    var pressStart by remember { mutableLongStateOf(0L) }
    var longConsumed by remember { mutableStateOf(false) }
    val scale = focusScale(focused, target = 1.02f)
    val shape = RoundedCornerShape(RadiusMd)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .focusGlow(focused, shape)
            .clip(shape)
            .background(if (focused) GlassFillFocus else GlassFill)
            .border(
                width = if (focused) 1.5.dp else 1.dp,
                color = if (focused) FocusStroke else GlassStroke,
                shape = shape,
            )
            .onFocusChanged { focused = it.isFocused; if (it.isFocused) onFocused() }
            // Short press (OK) = play; hold OK = toggle favorite. Detected by both the
            // native long-press flag AND press duration (combinedClickable's onLongClick
            // is unreliable on Android TV).
            .onKeyEvent { event ->
                when (event.key) {
                    Key.DirectionCenter, Key.Enter -> {
                        when (event.type) {
                            KeyEventType.KeyDown -> {
                                if (pressStart == 0L) pressStart = System.currentTimeMillis()
                                if (event.nativeKeyEvent.isLongPress && !longConsumed) {
                                    longConsumed = true
                                    onToggleFavorite()
                                }
                                true
                            }
                            KeyEventType.KeyUp -> {
                                val held = System.currentTimeMillis() - pressStart
                                pressStart = 0L
                                when {
                                    longConsumed -> longConsumed = false
                                    held >= LONG_PRESS_MS -> onToggleFavorite()
                                    else -> onOpen()
                                }
                                true
                            }
                            else -> false
                        }
                    }
                    // LEFT always returns to the SELECTED category (not a vertically
                    // aligned one), so the rail selection doesn't scroll with the list.
                    Key.DirectionLeft -> {
                        if (event.type == KeyEventType.KeyDown) onExitLeft()
                        true
                    }
                    // At the top/bottom of the list, UP/DOWN cross to the adjacent
                    // category (opposite end); middle rows fall through to normal focus.
                    Key.DirectionUp -> {
                        if (isFirst) {
                            if (event.type == KeyEventType.KeyDown) onCrossPrev()
                            true
                        } else {
                            false
                        }
                    }
                    Key.DirectionDown -> {
                        if (isLast) {
                            if (event.type == KeyEventType.KeyDown) onCrossNext()
                            true
                        } else {
                            false
                        }
                    }
                    else -> false
                }
            }
            .focusable()
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        ChannelLogo(logo = channel.logo, name = channel.name)
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = channel.name,
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = channel.nowTitle ?: "Ao vivo",
                style = MaterialTheme.typography.bodySmall,
                color = if (channel.nowTitle != null) BrandSoft else TextMuted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        if (isFavorite) {
            Icon(
                imageVector = Icons.Rounded.Star,
                contentDescription = "Favorito",
                tint = StarAmber,
                modifier = Modifier.size(22.dp),
            )
        }
    }
}

@Composable
private fun ChannelLogo(logo: String?, name: String) {
    Box(
        modifier = Modifier
            .size(56.dp)
            .clip(RoundedCornerShape(RadiusXs))
            .background(BgElevated),
        contentAlignment = Alignment.Center,
    ) {
        if (!logo.isNullOrBlank()) {
            AsyncImage(
                model = logo,
                contentDescription = name,
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize().padding(6.dp),
            )
        } else {
            Icon(Icons.Rounded.LiveTv, contentDescription = null, tint = TextMuted, modifier = Modifier.size(26.dp))
        }
    }
}
