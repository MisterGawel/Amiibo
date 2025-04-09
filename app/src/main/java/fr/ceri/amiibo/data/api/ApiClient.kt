package fr.ceri.amiibo.data.api

import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

/**
 * Singleton ApiClient responsable de configurer et fournir un service Retrofit
 * pour consommer l'API Amiibo.
 */
object ApiClient {
    //* URL de base de l'API Amiibo
    private const val BASE_URL = "https://amiiboapi.com/api/"

    /**
     * Instance Gson configurée pour tolérer les erreurs de formatage JSON (lenient)
     */
    private val gson by lazy {
        GsonBuilder()
            .setLenient() // Accepte des JSON mal formés
            .create()
    }

    /**
     * !TrustManager non sécurisé qui accepte tous les certificats SSL sans les vérifier.
     * !Ne doit être utilisé que dans un environnement de développement !
     */
    private val unsafeTrustManager = object : X509TrustManager {
        override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
        override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
        override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
    }

    /**
     * Création d'un contexte SSL avec le TrustManager non sécurisé.
     * Cela permet de contourner les erreurs SSL (ex: certificats auto-signés).
     */
    private val sslContext = SSLContext.getInstance("SSL").apply {
        init(null, arrayOf<TrustManager>(unsafeTrustManager), SecureRandom())
    }

    /**
     * Client OkHttp configuré avec le SSLContext non sécurisé.
     * Utilise également un vérificateur de nom d'hôte permissif.
     */
    private val unsafeOkHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .sslSocketFactory(sslContext.socketFactory, unsafeTrustManager)
            .hostnameVerifier { _, _ -> true } // Ignore les vérifications de nom d'hôte
            .build()
    }

    /**
     * Configuration de Retrofit avec :
     * - L'URL de base de l'API
     * - Le client HTTP personnalisé (non sécurisé ici)
     * - Le convertisseur JSON via Gson
     */
    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(unsafeOkHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    /**
     * Exposition du service de l'API en tant que propriété publique.
     * Ce service permet d’appeler les endpoints définis dans l’interface ApiService.
     */
    val apiService: ApiService by lazy {
        retrofit.create(ApiService::class.java)
    }
}
