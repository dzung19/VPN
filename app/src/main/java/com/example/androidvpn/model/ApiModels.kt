package com.example.androidvpn.model

import com.google.gson.annotations.SerializedName

data class ServerListResponse(
    val servers: List<ServerItemDto>
)

data class ServerItemDto(
    val id: String,
    val country: String,
    val city: String,
    val flag: String,
    val premium: Boolean,
    @SerializedName("maxUsers") val maxUsers: Int = 0
)

data class ConnectRequest(
    val userId: String,
    val serverId: String,
    val clientPublicKey: String
)

data class WireGuardConfigResponse(
    val serverPublicKey: String,
    val endpoint: String,
    val allowedIPs: String,
    val dns: String,
    val assignedIP: String = "10.10.0.2"
)

data class RegisterRequest(
    val publicKey: String
)

data class RegisterResponse(
    val status: String
)
