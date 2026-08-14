package dev.jvfl.progtv.domain.usecase

import dev.jvfl.progtv.domain.model.Category
import dev.jvfl.progtv.domain.model.CategoryLabels
import dev.jvfl.progtv.domain.model.Channel
import dev.jvfl.progtv.domain.repository.ChannelsRepository
import dev.jvfl.progtv.domain.repository.FavoritesRepository
import javax.inject.Inject

/** The fixed, always-present favorites category id. */
const val FAVORITES_CATEGORY_ID = "__favorites__"

/** The fixed "all channels" category id (every channel, alphabetical). */
const val ALL_CATEGORY_ID = "__all__"

/** Fetches the catalog. */
class GetChannelsUseCase @Inject constructor(
    private val repository: ChannelsRepository,
) {
    suspend operator fun invoke(): List<Channel> = repository.getChannels()
}

/** Adds the channel to favorites if absent, removes it if present. */
class ToggleFavoriteUseCase @Inject constructor(
    private val repository: FavoritesRepository,
) {
    suspend operator fun invoke(channelId: String) = repository.toggle(channelId)
}

/**
 * Builds the ordered category list from channels + favorites:
 * a pinned "Favoritos" category first, then alphabetical content categories.
 */
class BuildCategoriesUseCase @Inject constructor() {
    operator fun invoke(channels: List<Channel>, favoriteIds: Set<String>): List<Category> {
        val favorites = channels.filter { it.id in favoriteIds }.sortedBy { it.name.lowercase() }

        // Content categories: group by the channel's own categories. Channels with no
        // category (categories == []) are intentionally NOT forced into any content
        // category — they still show up under "Todos".
        val byCategory = linkedMapOf<String, MutableList<Channel>>()
        for (channel in channels) {
            for (cat in channel.categories) {
                byCategory.getOrPut(cat) { mutableListOf() }.add(channel)
            }
        }
        val content = byCategory
            .map { (id, list) ->
                Category(
                    id = "cat:$id",
                    name = CategoryLabels.localize(id),
                    channels = list.sortedBy { it.name.lowercase() },
                )
            }
            .sortedBy { it.name.lowercase() } // alphabetical by localized (pt-BR) name

        val favoritesCategory = Category(
            id = FAVORITES_CATEGORY_ID,
            name = "Favoritos",
            channels = favorites,
            isFavorites = true,
        )
        // "Todos": every channel (including those without any category), alphabetical.
        val allCategory = Category(
            id = ALL_CATEGORY_ID,
            name = "Todos",
            channels = channels.sortedBy { it.name.lowercase() },
        )

        return listOf(favoritesCategory, allCategory) + content
    }
}
