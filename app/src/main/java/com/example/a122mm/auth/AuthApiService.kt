package com.example.a122mm.auth

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface AuthApiService {
    @POST("login.php")
    suspend fun login(@Body body: LoginReq): Response<TokenRes>

    data class LoginResponse(
        val status: String,
        val access_token: String,
        val refresh_token: String,
        val token_type: String,
        val expires_in: Int
    )

    @POST("signup.php")
    suspend fun signup(@Body body: SignUpReq): Response<Map<String, Any>>

    @POST("refresh.php")
    suspend fun refresh(@Body body: Map<String, String>): Response<TokenRes>

    data class RefreshRes(
        val status: String?,
        val access_token: String,
        val refresh_token: String,
        val token_type: String?,
        val expires_in: Int?
    )

    @POST("device_upsert.php")
    suspend fun upsertDevice(
        @Body body: Map<String, String>
    ): Response<Map<String, Any>>

    @GET("profile.php")
    suspend fun profile(): Response<ProfileRes>

    @GET("get_profilepic.php")
    suspend fun getProfilePic(): Response<ProfilePicRes>

    data class ProfilePicRes(
        val pp_link: String,
        val username: String
    )

    @GET("devices.php")
    suspend fun listDevices(): Response<List<DeviceDto>>

    @GET("me.php")
    suspend fun me(): Response<Map<String, Any>>

    @GET("me.php")
    @retrofit2.http.Headers("X-No-Refresh: 1")
    suspend fun meNoRefresh(): retrofit2.Response<Map<String, Any>>

    @POST("logout_device.php")
    suspend fun logoutDevice(@Body body: Map<String, String>): Response<Map<String, Any>>

    @POST("logout_others.php")
    suspend fun logoutOthers(@Body body: Map<String, String>): Response<Map<String, Any>>

    data class DeviceDto(
        val device_id: String,
        val device_name: String,
        val device_type: String,
        val last_active: String // e.g. "Today", "Yesterday", or "2025-10-18"
    )

    data class LoginReq(
        val email: String,
        val password: String,
        val device_id: String,
        val device_name: String,
        val device_type: String,   // "phone" | "tablet" | "tv"
        val client_time: String,        // "yyyy-MM-dd HH:mm:ss" (LOCAL device time)
        val tz_offset_minutes: Int      // e.g., +420 for UTC+7 (Jakarta)
    )

    @POST("change_password.php")
    suspend fun changePassword(@Body body: ChangePasswordReq): Response<BasicRes>

    // request/response models
    data class ChangePasswordReq(
        val current_password: String,
        val new_password: String,
        val sign_out_all: Boolean
    )

    data class BasicRes(
        val status: String?,        // "ok"
        val logout: Boolean? = null // server can ask client to log out
    )
}
