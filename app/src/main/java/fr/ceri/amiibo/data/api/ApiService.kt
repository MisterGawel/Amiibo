package fr.ceri.amiibo.data.api

import fr.ceri.amiibo.data.model.AmiiboHeader
import fr.ceri.amiibo.data.model.AmiiboWrapper
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {

    @GET("gameseries")
    suspend fun getGameSeries(): Response<AmiiboHeader>

    @GET("amiibo")
    suspend fun getAmiiboByGameSeries(@Query("gameseries") series: String): Response<AmiiboWrapper>
}
