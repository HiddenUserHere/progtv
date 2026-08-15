package dev.jvfl.progtv.domain.model

/** A single playable stream. */
data class StreamRef(
    val id: String,
    val url: String,
    val quality: String?,
    /** User-Agent proven to play this stream (sent by the player). */
    val userAgent: String? = null,
    /** Referer required to play this stream, when applicable. */
    val referrer: String? = null,
)

/** A channel feed grouping one or more streams. */
data class Feed(
    val id: String,
    val name: String,
    val isMain: Boolean,
    val streams: List<StreamRef>,
)

/** An EPG program slot (start/end are ISO-8601 strings from the API). */
data class Program(
    val title: String,
    val description: String?,
    val startsAt: String,
    val endsAt: String,
)

/** Current + next EPG program for a channel. */
data class ChannelProgram(
    val now: Program?,
    val next: Program?,
)

/** A TV channel with its feeds and (optional) current program. */
data class Channel(
    val id: String,
    val name: String,
    val logo: String?,
    val categories: List<String>,
    val program: ChannelProgram?,
    val feeds: List<Feed>,
) {
    /** Chosen stream to play, preferring the main feed's first stream. */
    val playStream: StreamRef?
        get() = (feeds.firstOrNull { it.isMain } ?: feeds.firstOrNull())
            ?.streams?.firstOrNull()
            ?: feeds.flatMap { it.streams }.firstOrNull()

    /** URL of the chosen stream. */
    val playUrl: String?
        get() = playStream?.url

    /** Current program title, or null when no EPG is available. */
    val nowTitle: String? get() = program?.now?.title
}

/** A category grouping channels. Favorites is a special, pinned category. */
data class Category(
    val id: String,
    val name: String,
    val channels: List<Channel>,
    val isFavorites: Boolean = false,
)
