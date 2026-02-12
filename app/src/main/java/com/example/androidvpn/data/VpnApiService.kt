package com.example.androidvpn.data

import com.example.androidvpn.model.ConnectRequest
import com.example.androidvpn.model.ServerListResponse
import com.example.androidvpn.model.WireGuardConfigResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface VpnApiService {
    @GET("api/servers")
    suspend fun getServers(@Header("X-API-Key") apiKey: String = ""): ServerListResponse
    
    @POST("api/connect")
    suspend fun connect(
        @Header("X-API-Key") apiKey: String = "",
        @Body request: ConnectRequest
    ): WireGuardConfigResponse
}
