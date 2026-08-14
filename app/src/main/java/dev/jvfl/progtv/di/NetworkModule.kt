package dev.jvfl.progtv.di

import dev.jvfl.progtv.BuildConfig
import dev.jvfl.progtv.data.remote.BaseUrlInterceptor
import dev.jvfl.progtv.data.remote.ChannelsApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
    }

    @Provides
    @Singleton
    fun provideOkHttp(baseUrlInterceptor: BaseUrlInterceptor): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BASIC
            else HttpLoggingInterceptor.Level.NONE
        }
        return OkHttpClient.Builder()
            .addInterceptor(baseUrlInterceptor)
            .addInterceptor(logging)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(client: OkHttpClient): Retrofit = Retrofit.Builder()
        // Placeholder host — every request's host is rewritten by BaseUrlInterceptor.
        // No converter factory: the API returns ResponseBody and we decode with kotlinx.
        .baseUrl("http://localhost/")
        .client(client)
        .build()

    @Provides
    @Singleton
    fun provideChannelsApi(retrofit: Retrofit): ChannelsApi = retrofit.create(ChannelsApi::class.java)
}
