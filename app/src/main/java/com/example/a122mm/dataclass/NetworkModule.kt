package com.example.a122mm.dataclass

import android.content.Context
import com.example.a122mm.auth.AuthApiService
import com.example.a122mm.auth.TokenStore
import com.example.a122mm.components.ApiService
import com.example.a122mm.components.MovieApiService
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object NetworkModule {
    val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl("http://restfulapi.mooo.com/api/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val apiService: ApiService by lazy {
        retrofit.create(ApiService::class.java)
    }

    val mApiService: MovieApiService by lazy {
        retrofit.create(MovieApiService::class.java)
    }
}

object ApiClient {
    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl("http://restfulapi.mooo.com/api/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    fun <T> create(serviceClass: Class<T>): T {
        return retrofit.create(serviceClass)
    }
}

object AuthNetwork {

    val publicAuthApi: AuthApiService by lazy {
        NetworkModule.retrofit.create(AuthApiService::class.java)
    }

    fun authedAuthApi(context: Context): AuthApiService {
        val tokenStore = TokenStore(context)

        val authInterceptor = okhttp3.Interceptor { chain ->
            val access = kotlinx.coroutines.runBlocking { tokenStore.access() }
            val req = chain.request().newBuilder().apply {
                if (!access.isNullOrBlank()) header("Authorization", "Bearer $access")
            }.build()
            chain.proceed(req)
        }

        // ← add logging only when app is debuggable at runtime
        val logging = okhttp3.logging.HttpLoggingInterceptor().apply {
            level = if (isDebuggable(context))
                okhttp3.logging.HttpLoggingInterceptor.Level.BODY
            else
                okhttp3.logging.HttpLoggingInterceptor.Level.NONE
        }

        val client = okhttp3.OkHttpClient.Builder()
            .addInterceptor(logging)         // raw request/response in Logcat ("OkHttp")
            .addInterceptor(authInterceptor) // attach Bearer token
            .build()

        val gson = com.google.gson.GsonBuilder()
            .setLenient()                    // tolerate minor JSON issues
            .create()

        return retrofit2.Retrofit.Builder()
            .baseUrl("http://restfulapi.mooo.com/api/")
            .addConverterFactory(NullOnEmptyConverterFactory()) // optional safety
            .addConverterFactory(retrofit2.converter.gson.GsonConverterFactory.create(gson))
            .client(client)
            .build()
            .create(AuthApiService::class.java)
    }
}


//object AuthNetwork {
//
//    // Public (no Authorization header) — use for login/signup/refresh
//    val publicAuthApi: AuthApiService by lazy {
//        // Reuse the existing Retrofit instance from NetworkModule (no changes there)
//        NetworkModule.retrofit.create(AuthApiService::class.java)
//    }
//
//    // Authed (adds Bearer token from DataStore) — use for protected endpoints
//    fun authedAuthApi(context: Context): AuthApiService {
//        val tokenStore = TokenStore(context)
//
//        val authInterceptor = object : Interceptor {
//            override fun intercept(chain: Interceptor.Chain): Response {
//                val access = runBlocking { tokenStore.access() }
//                val req = chain.request().newBuilder().apply {
//                    if (!access.isNullOrBlank()) header("Authorization", "Bearer $access")
//                }.build()
//                return chain.proceed(req)
//            }
//        }
//
//        val client = OkHttpClient.Builder()
//            .addInterceptor(authInterceptor)
//            .build()
//
//        // Use the same base URL as your existing Retrofit
//        return Retrofit.Builder()
//            .baseUrl("http://restfulapi.mooo.com/api/") // matches NetworkModule
//            .addConverterFactory(GsonConverterFactory.create())
//            .client(client)
//            .build()
//            .create(AuthApiService::class.java)
//    }
//}