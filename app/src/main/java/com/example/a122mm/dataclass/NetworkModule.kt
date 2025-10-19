package com.example.a122mm.dataclass

import android.content.Context
import com.example.a122mm.auth.AuthApiService
import com.example.a122mm.auth.SessionManager
import com.example.a122mm.auth.TokenStore
import com.example.a122mm.components.ApiService
import com.example.a122mm.components.MovieApiService
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

// Small helper to read the `exp` claim from a JWT (no crypto, just decode)
private fun jwtExpSeconds(jwt: String?): Long? {
    if (jwt.isNullOrBlank()) return null
    val parts = jwt.split(".")
    if (parts.size != 3) return null
    return try {
        val payload = android.util.Base64.decode(parts[1], android.util.Base64.URL_SAFE or android.util.Base64.NO_PADDING or android.util.Base64.NO_WRAP)
        val json = String(payload, Charsets.UTF_8)
        val exp = Regex(""""exp"\s*:\s*(\d+)""").find(json)?.groupValues?.get(1)?.toLong()
        exp
    } catch (_: Throwable) { null }
}

private fun isTokenExpired(access: String?): Boolean {
    val exp = jwtExpSeconds(access) ?: return false
    val now = System.currentTimeMillis() / 1000
    return exp <= now
}

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

            val res = chain.proceed(req)

            if (res.code == 401) {
                // Decide reason before clearing the token
                val reason = when {
                    isTokenExpired(access) -> com.example.a122mm.auth.LogoutReason.TOKEN_EXPIRED
                    else -> com.example.a122mm.auth.LogoutReason.REMOTE_LOGOUT
                }

                // Don’t close/consume the body here
                kotlinx.coroutines.runBlocking { tokenStore.clear() }
                com.example.a122mm.auth.SessionManager.broadcastLogout(reason)
            }

            res
        }


        val logging = okhttp3.logging.HttpLoggingInterceptor().apply {
            // use the runtime debuggable check you added earlier, or set BODY while debugging
            level = okhttp3.logging.HttpLoggingInterceptor.Level.BODY
        }

        val client = okhttp3.OkHttpClient.Builder()
            .addInterceptor(logging)         // optional, for debugging
            .addInterceptor(authInterceptor) // attaches token and handles 401
            .build()

        val gson = com.google.gson.GsonBuilder().setLenient().create()

        return retrofit2.Retrofit.Builder()
            .baseUrl("http://restfulapi.mooo.com/api/")
            .addConverterFactory(NullOnEmptyConverterFactory())
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