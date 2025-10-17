package com.example.a122mm.auth

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface AuthApiService {
    @POST("login.php")
    suspend fun login(@Body body: LoginReq): Response<TokenRes>

    @POST("signup.php")
    suspend fun signup(@Body body: SignUpReq): Response<Map<String, Any>>

    @POST("refresh.php")
    suspend fun refresh(@Body body: Map<String, String>): Response<TokenRes>

    @POST("device_upsert.php")
    suspend fun upsertDevice(
        @Body body: Map<String, String>
    ): Response<Map<String, Any>>

    @GET("profile.php")
    suspend fun profile(): Response<ProfileRes>

    @GET("get_profilepic.php")
    suspend fun getProfilePic(): retrofit2.Response<ProfilePicRes>

    data class ProfilePicRes(val pp_link: String)

}
