package dev.jvfl.progtv.ui.screens.home

import dev.jvfl.progtv.domain.model.Category
import dev.jvfl.progtv.domain.model.Channel

/** A resolved navigation position: which category and which channel. */
data class ChannelPos(val categoryIndex: Int, val channel: Channel)

/**
 * Moves one channel up (dir=-1) or down (dir=+1) from the given position.
 *
 * At a category boundary it jumps to the adjacent non-empty category and lands on the
 * opposite end (down -> first channel of next category; up -> last channel of previous).
 * At the global start/end it wraps around. Empty categories are skipped.
 */
fun moveChannel(
    categories: List<Category>,
    categoryIndex: Int,
    channelId: String?,
    dir: Int,
): ChannelPos? {
    if (categories.isEmpty()) return null
    val channels = categories.getOrNull(categoryIndex)?.channels ?: emptyList()
    val current = channels.indexOfFirst { it.id == channelId }
    val within = current + dir
    if (within in channels.indices) return ChannelPos(categoryIndex, channels[within])

    // Crossed the top/bottom of this category -> adjacent non-empty category (wrapping).
    var ci = categoryIndex
    repeat(categories.size) {
        ci = (ci + dir + categories.size) % categories.size
        val next = categories[ci].channels
        if (next.isNotEmpty()) {
            return ChannelPos(ci, if (dir > 0) next.first() else next.last())
        }
    }
    return if (channels.isNotEmpty()) {
        ChannelPos(categoryIndex, channels[current.coerceIn(0, channels.lastIndex)])
    } else {
        null
    }
}

/** Index of the next non-empty category from [from] in direction [dir] (wraps). */
fun nextNonEmptyCategoryIndex(categories: List<Category>, from: Int, dir: Int): Int {
    var ci = from
    repeat(categories.size) {
        ci = (ci + dir + categories.size) % categories.size
        if (categories[ci].channels.isNotEmpty()) return ci
    }
    return from
}
