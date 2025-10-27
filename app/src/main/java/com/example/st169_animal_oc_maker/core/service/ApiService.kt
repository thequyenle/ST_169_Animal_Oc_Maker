package com.example.st169_animal_oc_maker.core.service
import com.example.st169_animal_oc_maker.data.model.PartAPI
import retrofit2.Response
import retrofit2.http.GET
interface ApiService {
    @GET("/api/ST169_AnimalOcMaker")
    suspend fun getAllData(): Response<Map<String, List<PartAPI>>>
}