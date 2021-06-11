package dev.m13d.lorempicsum.api

import dev.m13d.lorempicsum.data.PicsumPhoto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface PicsumApi {

    companion object {
        const val BASE_URL = "https://picsum.photos/v2/"
    }

    @GET("list")
    suspend fun getPicsumPhotos(): Response<List<PicsumPhoto>>

    @GET("list")
    suspend fun searchNewPhotos(
        @Query("q") query: String,
        @Query("page") page: Int,
        @Query("limit") limit: Int
    ): Response<List<PicsumPhoto>>

}