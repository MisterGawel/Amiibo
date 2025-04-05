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

object ApiClient {
    private const val BASE_URL = "https://amiiboapi.com/api/"

    private val gson by lazy {
        GsonBuilder()
            .setLenient()
            .create()
    }

    // Bypass SSL pour tests uniquement
    private val unsafeTrustManager = object : X509TrustManager {
        override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
        override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
        override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
    }

    private val sslContext = SSLContext.getInstance("SSL").apply {
        init(null, arrayOf<TrustManager>(unsafeTrustManager), SecureRandom())
    }

    private val unsafeOkHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .sslSocketFactory(sslContext.socketFactory, unsafeTrustManager)
            .hostnameVerifier { _, _ -> true }
            .build()
    }

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(unsafeOkHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    val apiService: ApiService by lazy {
        retrofit.create(ApiService::class.java)
    }
}
