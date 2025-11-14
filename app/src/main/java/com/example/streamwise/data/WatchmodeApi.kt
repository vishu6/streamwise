package com.example.streamwise.data

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

// --- Data Classes for Watchmode API Response ---

@Serializable
data class WatchmodeSearchResponse(
    @SerialName("title_results")
    val titleResults: List<WatchmodeTitle>
)

@Serializable
data class WatchmodeTitle(
    val id: Int,
    val name: String,
    @SerialName("year")
    val releaseYear: Int? = null,
    @SerialName("image_url")
    val imageUrl: String? = null,
    @SerialName("type")
    val resultType: String,
    // This will be populated by a second API call
    var sources: List<WatchmodeSource> = emptyList()
)

@Serializable
data class WatchmodeSource(
    @SerialName("source_id")
    val sourceId: Int,
    val name: String,
    val type: String, // e.g., "sub", "rent", "buy"
    @SerialName("web_url")
    val webUrl: String
)

// --- Retrofit Service Definition ---

private const val BASE_URL = "https://api.watchmode.com/v1/"
private const val API_KEY = "6Xdushxs6eRyxui14G1gCSZChRirHpUlsKrdAD2E"

private val loggingInterceptor = HttpLoggingInterceptor().apply {
    level = HttpLoggingInterceptor.Level.BODY
}

private val okHttpClient = OkHttpClient.Builder()
    .addInterceptor { chain ->
        val original = chain.request()
        val url = original.url.newBuilder()
            .addQueryParameter("apiKey", API_KEY)
            .build()
        val requestBuilder = original.newBuilder().url(url)
        val request = requestBuilder.build()
        chain.proceed(request)
    }
    .addInterceptor(loggingInterceptor)
    .build()

private val json = Json {
    ignoreUnknownKeys = true
    coerceInputValues = true
}

private val retrofit = Retrofit.Builder()
    .baseUrl(BASE_URL)
    .client(okHttpClient)
    .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
    .build()

interface WatchmodeApiService {
    @GET("search/")
    suspend fun search(
        @Query("search_field") searchField: String = "name",
        @Query("search_value") searchValue: String
    ): WatchmodeSearchResponse

    // New function to get sources for a specific title
    @GET("title/{title_id}/sources/")
    suspend fun getTitleSources(
        @Path("title_id") titleId: Int
    ): List<WatchmodeSource>
}

object WatchmodeApi {
    val service: WatchmodeApiService by lazy {
        retrofit.create(WatchmodeApiService::class.java)
    }
}
