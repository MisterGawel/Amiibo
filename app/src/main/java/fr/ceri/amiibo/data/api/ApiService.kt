package fr.ceri.amiibo.data.api

import fr.ceri.amiibo.data.model.AmiiboHeader
import fr.ceri.amiibo.data.model.AmiiboWrapper
import retrofit2.Response
import retrofit2.http.GET

/**
 * Interface Retrofit représentant les endpoints (points d'accès) de l'API Amiibo.
 * Chaque fonction est une requête HTTP vers l’API distante.
 */
interface ApiService {

    /**
     * Requête GET vers l'endpoint "gameseries"
     * -> Permet de récupérer toutes les GameSeries (catégories d’Amiibo).
     *
     * @return Une réponse Retrofit contenant un objet de type AmiiboHeader
     * qui encapsule la liste des GameSeries.
     */
    @GET("gameseries")
    suspend fun getGameSeries(): Response<AmiiboHeader>

    /**
     * Requête GET vers l'endpoint "amiibo"
     * -> Récupère la liste complète de tous les Amiibos disponibles.
     *
     * @return Une réponse Retrofit contenant un objet de type AmiiboWrapper
     * qui contient une liste d’Amiibo.
     */
    @GET("amiibo")
    suspend fun getAllAmiibos(): Response<AmiiboWrapper>
}
