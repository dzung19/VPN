package com.example.androidvpn.data

import com.google.gson.annotations.SerializedName
import retrofit2.http.Body
import retrofit2.http.POST

interface CloudflareApi {
    @POST("v0a1922/reg")
    suspend fun register(@Body body: RegistrationRequest): RegistrationResponse
}

data class RegistrationRequest(
    val key: String,
    val install_id: String = "",
    val fcm_token: String = "",
    val tos: String,
    val type: String = "Android",
    val model: String = android.os.Build.MODEL,
    val locale: String = java.util.Locale.getDefault().toString()
)

data class RegistrationResponse(
    val id: String,
    val token: String,
    val config: WireGuardConfigResponse,
    val account: AccountResponse
)

data class AccountResponse(
    val license: String,
    val account_type: String
)

data class WireGuardConfigResponse(
    @SerializedName("interface") // interface is a keyword in Kotlin
    val interfaceField: InterfaceData,
    val peers: List<PeerData>
)

data class InterfaceData(
    val addresses: Addresses
)

data class Addresses(
    val v4: String,
    val v6: String
)

data class PeerData(
    val public_key: String,
    val endpoint: Endpoint
)

data class Endpoint(
    val v4: String,
    val v6: String,
    val host: String
)
