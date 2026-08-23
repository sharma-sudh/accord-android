package com.sudh.accord.network

import com.sudh.accord.auth.TokenAuthenticator
import com.sudh.accord.auth.TokenManager
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    private const val BASE_URL = "http://10.0.2.2:8080/"

    private lateinit var tokenManager: TokenManager

    // Must be called once (from AccordApplication.onCreate, before any
    // repository touches `api`) so the Authenticator has a TokenManager to
    // read/write tokens through.
    fun init(tokenManager: TokenManager) {
        this.tokenManager = tokenManager
    }

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    // Plain client, no Authenticator — used only for the refresh call itself.
    // If this client also had the Authenticator attached, a 401 on the
    // refresh call could recurse back into authenticate() indefinitely.
    private val refreshOkHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .build()

    private val refreshApi: AccordApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(refreshOkHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(AccordApi::class.java)
    }

    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .authenticator(TokenAuthenticator(tokenManager, refreshApi))
            .build()
    }

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val api: AccordApi by lazy { retrofit.create(AccordApi::class.java) }
}