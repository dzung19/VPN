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
    suspend fun registerAndGetConfig(privateKey: String, publicKey: String): ServerConfig? {
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
            
            // Cloudflare returns IP v4 and v6. 
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
            Log.d(TAG, "Peer endpoint: host=${peer.endpoint.host}, v4=${peer.endpoint.v4}, v6=${peer.endpoint.v6}")
            
            // Force known IP endpoint to avoid DNS issues during handshake
            val endpointHost = peer.endpoint.host
            val resolvedEndpoint = if (endpointHost.contains("engage.cloudflareclient.com")) {
                "162.159.192.1:2408"
            } else {
                endpointHost
            }
            Log.d(TAG, "Resolved endpoint: $resolvedEndpoint")

            val config = ServerConfig(
                name = "Cloudflare WARP",
                privateKey = privateKey,
                address = addresses, 
                dns = "1.1.1.1, 2606:4700:4700::1111", 
                publicKey = peer.public_key,
                endpoint = resolvedEndpoint,
                allowedIps = "0.0.0.0/0, ::/0"
            )
            android.util.Log.d(TAG, "WARP config created successfully: address=$addresses, endpoint=$resolvedEndpoint")
            config
        } catch (e: Exception) {
            android.util.Log.e(TAG, "WARP registration FAILED: ${e.message}", e)
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
