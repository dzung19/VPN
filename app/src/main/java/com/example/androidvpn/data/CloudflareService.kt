package com.example.androidvpn.data

import android.util.Log
import com.example.androidvpn.model.ServerConfig
import com.google.gson.GsonBuilder
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

import javax.inject.Inject

class CloudflareService @Inject constructor(
    private val api: CloudflareApi
) {
    // Cloudflare WARP endpoints - official gateway IPs across multiple ranges
    // Ports: 2408 (WARP default), 500/4500 (IPSec standard, rarely blocked)
    // IP ranges: 162.159.192-193.x (primary), 188.114.96-99.x (secondary)
    // IPv6 ranges (2606:4700:d0-d1::/48) skipped ΓÇö unreliable on many mobile networks
    private val warpEndpoints = listOf(
        // Primary range (162.159.x)
        "162.159.193.1:2408",
        "162.159.192.1:2408",
        "162.159.193.1:500",
        "162.159.192.1:500",
        "162.159.193.1:4500",
        "162.159.192.1:4500",
        // Secondary range (188.114.x) ΓÇö 4 subnets
        "188.114.96.1:2408",
        "188.114.97.1:2408",
        "188.114.98.1:2408",
        "188.114.99.1:2408",
        "188.114.96.1:500",
        "188.114.97.1:500",
        "188.114.98.1:500",
        "188.114.99.1:500",
        "188.114.96.1:4500",
        "188.114.97.1:4500",
        "188.114.98.1:4500",
        "188.114.99.1:4500"
    )
    private var endpointIndex = 0

    fun getNextEndpoint(): String {
        val endpoint = warpEndpoints[endpointIndex % warpEndpoints.size]
        endpointIndex++
        return endpoint
    }

    fun getEndpointCount() = warpEndpoints.size

    suspend fun registerAndGetConfig(
        privateKey: String,
        publicKey: String,
        forcedEndpoint: String? = null
    ): ServerConfig? {
        return try {
            val timestamp = getIso8601Date()
            val installId = java.util.UUID.randomUUID().toString()
            
            val request = RegistrationRequest(
                key = publicKey,
                install_id = installId,
                fcm_token = "${installId}:FCM",
                tos = timestamp,
                type = "Android",
                locale = "en_US"
            )
            
            Log.d(TAG, "Registering WARP with publicKey: ${publicKey.take(20)}...")
            val response = api.register(request)
            Log.d(TAG, "Registration response ID: ${response.id}")
            Log.d(TAG, "Account type: ${response.account.account_type}")

            val addressV4 = response.config.interfaceField.addresses.v4
            val addressV6 = response.config.interfaceField.addresses.v6
            Log.d(TAG, "Addresses: v4=$addressV4, v6=$addressV6")
            
            var addresses = "$addressV4/32"
            if (addressV6.isNotEmpty()) {
                addresses += ", $addressV6/128"
            }
            
            val peer = response.config.peers.firstOrNull()
            if (peer == null) {
                Log.e(TAG, "No peers returned from WARP API!")
                return null
            }
            Log.d(TAG, "Peer publicKey: ${peer.public_key.take(20)}...")

            // Use forced endpoint or rotate through available endpoints
            val resolvedEndpoint = forcedEndpoint ?: getNextEndpoint()
            Log.d(TAG, "Using endpoint: $resolvedEndpoint")

            val config = ServerConfig(
                name = "Cloudflare WARP",
                privateKey = privateKey,
                address = addresses, 
                dns = "1.1.1.1, 1.0.0.1",
                publicKey = peer.public_key,
                endpoint = resolvedEndpoint,
                allowedIps = "0.0.0.0/0, ::/0",
                mtu = 1280
            )
            Log.d(TAG, "WARP config created: endpoint=$resolvedEndpoint")
            config
        } catch (e: Exception) {
            Log.e(TAG, "WARP registration FAILED: ${e.message}", e)
            null
        }
    }

    private fun getIso8601Date(): String {
        val df = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
        df.timeZone = TimeZone.getTimeZone("UTC")
        return df.format(Date())
    }

    companion object {
        const val TAG = "CloudflareService"
    }
}
