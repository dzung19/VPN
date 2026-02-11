package com.example.androidvpn.data

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
    // Init block removed as API is injected

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
            
            
            val response = api.register(request)
            
            // Cloudflare returns IP v4 and v6. 
            // We should add both if available to avoid connectivity issues.
            val addressV4 = response.config.interfaceField.addresses.v4
            val addressV6 = response.config.interfaceField.addresses.v6
            
            var addresses = "$addressV4/32"
            if (addressV6.isNotEmpty()) {
                addresses += ", $addressV6/128"
            }
            
            val peer = response.config.peers.firstOrNull() ?: return null
            
            // Force known IP endpoint to avoid DNS issues during handshake
            // engaging.cloudflareclient.com -> 162.159.192.1
            val endpointHost = peer.endpoint.host
            val resolvedEndpoint = if (endpointHost.contains("engage.cloudflareclient.com")) {
                "162.159.192.1:2408"
            } else {
                endpointHost ?: "162.159.192.1:2408"
            }

            ServerConfig(
                name = "Cloudflare WARP",
                privateKey = privateKey,
                address = addresses, 
                dns = "1.1.1.1, 2606:4700:4700::1111", 
                publicKey = peer.public_key,
                endpoint = resolvedEndpoint,
                allowedIps = "0.0.0.0/0, ::/0"
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun getIso8601Date(): String {
        val df = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
        df.timeZone = TimeZone.getTimeZone("UTC")
        return df.format(Date())
    }
}
