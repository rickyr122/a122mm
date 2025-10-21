package com.example.a122mm.dataclass

import android.content.Context
import com.example.a122mm.auth.AuthApiService
import com.example.a122mm.auth.LogoutReason
import com.example.a122mm.auth.SessionManager.broadcastLogout
import com.example.a122mm.auth.TokenStore
import com.example.a122mm.components.ApiService
import com.example.a122mm.components.MovieApiService
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
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

    // Reuse your existing public Retrofit for refresh calls (no auth header)
    val publicAuthApi: AuthApiService by lazy {
        NetworkModule.retrofit.create(AuthApiService::class.java)
    }

    // A mutex to prevent multiple simultaneous refresh calls
    private val refreshMutex = Mutex()

    // Marker header to avoid infinite retry loops
    private const val HDR_REFRESH_ATTEMPTED = "X-Refresh-Attempted"

    fun authedAuthApi(context: Context): AuthApiService {
        val tokenStore = TokenStore(context)

        // Build a client with our auth+refresh interceptor (and optional logging)
        val logging = okhttp3.logging.HttpLoggingInterceptor().apply {
            level = okhttp3.logging.HttpLoggingInterceptor.Level.BODY // or NONE in release
        }

        val authAndRefreshInterceptor = okhttp3.Interceptor { chain ->
            // 1) Attach current access token (if any)
            val access = runBlocking { tokenStore.access() }
            val originalReq = chain.request()

            // Detect if this request should skip refresh logic
            val noRefresh = originalReq.header("X-No-Refresh") == "1"

            val reqWithAuth = originalReq.newBuilder().apply {
                if (!access.isNullOrBlank()) header("Authorization", "Bearer $access")
            }.build()

            // 2) Fire the request
            var res = chain.proceed(reqWithAuth)

            // ✅ Skip refresh entirely for no-refresh requests
            if (res.code == 401 && noRefresh) {
                broadcastLogout(LogoutReason.REMOTE_LOGOUT)
                return@Interceptor res
            }

            // 3) If unauthorized AND we haven’t retried this call yet, try refresh once
            // 3) If unauthorized AND we haven’t retried this call yet, try refresh once
            val alreadyTried = originalReq.header(HDR_REFRESH_ATTEMPTED) == "true"
            if (res.code == 401 && !alreadyTried) {

                // 🔁 Do NOT close 'res' yet. We might return it if refresh fails.

                val newAccess = runBlocking {
                    refreshMutex.lock()
                    try {
                        val latestAccess = tokenStore.access()
                        if (!latestAccess.isNullOrBlank() && latestAccess != access) {
                            latestAccess
                        } else {
                            val refreshTok = tokenStore.refresh()
                            if (refreshTok.isNullOrBlank()) null else try {
                                val r = publicAuthApi.refresh(mapOf("refresh_token" to refreshTok))
                                if (r.isSuccessful && r.body() != null) {
                                    val b = r.body()!!
                                    tokenStore.save(b.access_token, b.refresh_token)
                                    b.access_token
                                } else null
                            } catch (_: Throwable) { null }
                        }
                    } finally {
                        refreshMutex.unlock()
                    }
                }

                if (!newAccess.isNullOrBlank()) {
                    // ✅ Now we are going to retry -> it's safe to close the first response
                    res.close()

                    val retried = originalReq.newBuilder()
                        .header("Authorization", "Bearer $newAccess")
                        .header(HDR_REFRESH_ATTEMPTED, "true")
                        .build()
                    res = chain.proceed(retried)
                } else {
                    // ❌ Refresh failed -> clear + broadcast, then return the ORIGINAL (still-open) 401
                    runBlocking { tokenStore.clear() }
                    //broadcastLogout(LogoutReason.TOKEN_EXPIRED)
                    broadcastLogout(LogoutReason.REMOTE_LOGOUT)
                    return@Interceptor res   // safe: we did NOT close it
                }
            }


            res
        }

        val client = okhttp3.OkHttpClient.Builder()
            .addInterceptor(logging)                 // optional
            .addInterceptor(authAndRefreshInterceptor)
            .build()

        val gson = com.google.gson.GsonBuilder()
            .setLenient()
            .create()

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