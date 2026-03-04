package com.dzungphung.vpnconnection.provpn.securityconnection.androidvpn.data

import com.dzungphung.vpnconnection.provpn.securityconnection.androidvpn.model.ConnectRequest
import com.dzungphung.vpnconnection.provpn.securityconnection.androidvpn.model.RegisterRequest
import com.dzungphung.vpnconnection.provpn.securityconnection.androidvpn.model.RegisterResponse
import com.dzungphung.vpnconnection.provpn.securityconnection.androidvpn.model.ServerListResponse
import com.dzungphung.vpnconnection.provpn.securityconnection.androidvpn.model.WireGuardConfigResponse
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

    @POST("api/register")
    suspend fun registerKey(
        @Body request: RegisterRequest
    ): RegisterResponse
}
