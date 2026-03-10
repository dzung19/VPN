package com.dzungphung.vpnconnection.provpn.securityconnection.androidvpn.data

import android.util.Log
import com.dzungphung.vpnconnection.provpn.securityconnection.androidvpn.model.ServerConfig
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.UUID

import javax.inject.Inject

class CloudflareService @Inject constructor(
    private val api: CloudflareApi
) {
    suspend fun registerAndGetConfig(
        privateKey: String,
        publicKey: String,
        forcedEndpoint: String? = null
    ): ServerConfig? {
        return try {
            val timestamp = getIso8601Date()
            val installId = UUID.randomUUID().toString()
            
            val request = RegistrationRequest(
                key = publicKey,
                install_id = installId,
                fcm_token = "${installId}:FCM",
                tos = timestamp,
                type = "Android",
                locale = Locale.getDefault().toString()
            )
            
            val response = api.register(request)

            val addressV4 = response.config.interfaceField.addresses.v4
            val addressV6 = response.config.interfaceField.addresses.v6

            var addresses = "$addressV4/32"
            if (addressV6.isNotEmpty()) {
                addresses += ", $addressV6/128"
            }
            
            val peer = response.config.peers.firstOrNull()
            if (peer == null) {
                Log.e(TAG, "No peers returned from WARP API!")
                return null
            }
            // Extract assigned endpoint from Cloudflare's own API response!
            // If the API forgets to specify an exact IP, we'll fall back securely to the official Anycast DNS.

            // Note: peer.endpoint properties are defined in CloudflareApi.kt (Endpoint data class)
            var assignedEndpointStr = peer.endpoint?.v4 ?: peer.endpoint?.v6 ?: peer.endpoint?.host ?: STANDARD_FALLBACK

            // Cloudflare API sometimes returns the endpoint IP with a dummy port 0 (e.g. "162.159.192.7:0")
            if (assignedEndpointStr.endsWith(":0")) {
                assignedEndpointStr = assignedEndpointStr.substringBeforeLast(":0")
            }

            // Ensure there is a port specified correctly (WARP uses 2408)
            val finalEndpointStr = if (assignedEndpointStr.contains("]:") || (assignedEndpointStr.contains(":") && assignedEndpointStr.count { it == ':' } == 1)) {
                // Already has a port (either [ipv6]:port or ipv4:port)
                assignedEndpointStr
            } else if (assignedEndpointStr.contains(":") && !assignedEndpointStr.contains("[")) {
                // Raw IPv6 address without port or brackets
                "[$assignedEndpointStr]:2408"
            } else {
                // Raw IPv4 or hostname without port
                "$assignedEndpointStr:2408"
            }

            val resolvedEndpoint = forcedEndpoint ?: finalEndpointStr

            val config = ServerConfig(
                name = "Cloudflare WARP",
                privateKey = privateKey,
                address = addresses,
                dns = "1.1.1.1, 1.0.0.1, 2606:4700:4700:1111, 2606:4700:4700:1001",
                publicKey = peer.public_key,
                endpoint = resolvedEndpoint,
                allowedIps = "0.0.0.0/0, ::/0",
                mtu = 1280
            )
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
        const val STANDARD_FALLBACK = "engage.cloudflareclient.com:2408"
    }
}
