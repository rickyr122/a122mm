package com.example.a122mm.update

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object UpdateRepoProvider {
    private val api by lazy {
        Retrofit.Builder()
            .baseUrl("https://videos.122movies.my.id/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(UpdateApiService::class.java)
    }

    val repo by lazy { UpdateRepository(api) }
}
