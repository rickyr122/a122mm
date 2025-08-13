package com.example.a122mm.dataclass

import retrofit2.http.GET
import retrofit2.http.Query

interface ApiHomeCodesService {
    @GET("gethomecodes")
    suspend fun getHomeCodes(
        @Query("type") type: String
    ): List<Int>
}

sealed class Section {
    object Continue : Section()
    data class Category(val code: Int) : Section()
}