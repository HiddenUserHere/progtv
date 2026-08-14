package dev.jvfl.progtv.data.remote

import okhttp3.ResponseBody
import retrofit2.http.GET

/** Retrofit endpoint for the public catalog. The host is rewritten per-request
 *  by [BaseUrlInterceptor], so the compiled base URL here is only a placeholder.
 *  Returns the raw body; decoding is done with kotlinx.serialization in the repository. */
interface ChannelsApi {
    @GET("channels")
    suspend fun getChannels(): ResponseBody
}
