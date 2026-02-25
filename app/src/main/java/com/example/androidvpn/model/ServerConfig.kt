package com.example.androidvpn.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ServerConfig(
    val name: String,
    val privateKey: String,
    val address: String, // Interface IP (e.g. 10.0.0.2/32)
    val dns: String = "1.1.1.1",
    val publicKey: String = "", // Peer Public Key
    val endpoint: String = "", // Peer Endpoint (IP:Port)
    val allowedIps: String = "0.0.0.0/0, ::/0",
    val mtu: Int = 0, // 0 = auto, 1280 recommended for WARP
    
    // UI Metadata
    val country: String = "",
    val flag: String = "",
    val city: String = "",
    val isPremium: Boolean = false
) : Parcelable
