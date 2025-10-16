package com.example.a122mm.auth

data class LoginReq(val email: String, val password: String)
data class SignUpReq(val email: String, val name: String, val password: String)

data class TokenRes(
    val status: String,
    val access_token: String,
    val refresh_token: String,
    val token_type: String,
    val expires_in: Int
)

data class Profile(val id: Int, val email: String, val username: String, val created_at: String)
data class ProfileRes(val status: String, val profile: Profile)
