package dev.jvfl.progtv.data.remote

import dev.jvfl.progtv.domain.repository.SettingsRepository
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Rewrites each request's scheme/host/port to the user-configured base URL,
 * so the API endpoint can be changed at runtime (Settings) without rebuilding Retrofit.
 */
@Singleton
class BaseUrlInterceptor @Inject constructor(
    private val settings: SettingsRepository,
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val configured = settings.currentBaseUrl().toHttpUrlOrNull()
            ?: return chain.proceed(request)

        val newUrl = request.url.newBuilder()
            .scheme(configured.scheme)
            .host(configured.host)
            .port(configured.port)
            .build()

        return chain.proceed(request.newBuilder().url(newUrl).build())
    }
}
