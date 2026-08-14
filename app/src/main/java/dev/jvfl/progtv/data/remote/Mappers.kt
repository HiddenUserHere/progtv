package dev.jvfl.progtv.data.remote

import dev.jvfl.progtv.data.remote.dto.ChannelDto
import dev.jvfl.progtv.data.remote.dto.ChannelProgramDto
import dev.jvfl.progtv.data.remote.dto.FeedDto
import dev.jvfl.progtv.data.remote.dto.ProgramDto
import dev.jvfl.progtv.data.remote.dto.StreamDto
import dev.jvfl.progtv.domain.model.Channel
import dev.jvfl.progtv.domain.model.ChannelProgram
import dev.jvfl.progtv.domain.model.Feed
import dev.jvfl.progtv.domain.model.Program
import dev.jvfl.progtv.domain.model.StreamRef

/** DTO -> domain mapping. */
fun ChannelDto.toDomain(): Channel = Channel(
    id = id,
    name = name,
    logo = logo,
    categories = categories,
    program = program?.toDomain(),
    feeds = feeds.map { it.toDomain() },
)

private fun ChannelProgramDto.toDomain() = ChannelProgram(
    now = now?.toDomain(),
    next = next?.toDomain(),
)

private fun ProgramDto.toDomain() = Program(
    title = title,
    description = description,
    startsAt = startsAt,
    endsAt = endsAt,
)

private fun FeedDto.toDomain() = Feed(
    id = id,
    name = name,
    isMain = isMain,
    streams = streams.map { it.toDomain() },
)

private fun StreamDto.toDomain() = StreamRef(
    id = id,
    url = url,
    quality = quality,
)
