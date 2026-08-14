package dev.jvfl.progtv.data.repository

import dev.jvfl.progtv.data.remote.ChannelsApi
import dev.jvfl.progtv.data.remote.dto.ChannelDto
import dev.jvfl.progtv.data.remote.toDomain
import dev.jvfl.progtv.domain.model.Channel
import dev.jvfl.progtv.domain.repository.ChannelsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChannelsRepositoryImpl @Inject constructor(
    private val api: ChannelsApi,
    private val json: Json,
) : ChannelsRepository {
    override suspend fun getChannels(): List<Channel> = withContext(Dispatchers.IO) {
        val body = api.getChannels().string()
        json.decodeFromString<List<ChannelDto>>(body).map { it.toDomain() }
    }
}
