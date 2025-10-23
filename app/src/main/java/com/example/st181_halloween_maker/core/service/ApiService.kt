package com.example.st181_halloween_maker.core.service
import com.example.st181_halloween_maker.data.model.PartAPI
import retrofit2.Response
import retrofit2.http.GET
interface ApiService {
    @GET("/api/ST181_HalloweenMaker")
    suspend fun getAllData(): Response<Map<String, List<PartAPI>>>
}