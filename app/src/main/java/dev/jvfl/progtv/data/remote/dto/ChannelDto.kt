package dev.jvfl.progtv.data.remote.dto

import kotlinx.serialization.Serializable

/** Mirrors the public `/channels` JSON payload from the backend. */
@Serializable
data class ChannelDto(
    val id: String,
    val name: String,
    val logo: String? = null,
    val categories: List<String> = emptyList(),
    val program: ChannelProgramDto? = null,
    val feeds: List<FeedDto> = emptyList(),
)

@Serializable
data class ChannelProgramDto(
    val now: ProgramDto? = null,
    val next: ProgramDto? = null,
)

@Serializable
data class ProgramDto(
    val title: String,
    val description: String? = null,
    val category: String? = null,
    val startsAt: String,
    val endsAt: String,
)

@Serializable
data class FeedDto(
    val id: String,
    val name: String,
    val isMain: Boolean = false,
    val streams: List<StreamDto> = emptyList(),
)

@Serializable
data class StreamDto(
    val id: String,
    val url: String,
    val quality: String? = null,
    val status: String = "ONLINE",
)
