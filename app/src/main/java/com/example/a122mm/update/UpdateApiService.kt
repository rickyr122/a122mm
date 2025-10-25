package com.example.a122mm.update

import retrofit2.http.GET

data class VersionInfo(
    val versionName: String,
    val versionCode: Long,
    val minVersionCode: Long? = null,
    val apkUrl: String,
    val size: Long? = null,
    val sha256: String? = null,
    val changelog: String? = null
)

interface UpdateApiService {
    @GET("app/version.json")
    suspend fun version(): VersionInfo
}
