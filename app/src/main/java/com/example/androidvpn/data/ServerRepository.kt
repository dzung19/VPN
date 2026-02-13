package com.example.androidvpn.data

import android.content.Context
import android.content.SharedPreferences
import com.example.androidvpn.model.ServerConfig
import com.wireguard.crypto.KeyPair
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import androidx.core.content.edit

@Singleton
class ServerRepository @Inject constructor(
    @ApplicationContext context: Context,
    private val cloudflareService: CloudflareService,
    private val vpnApiService: VpnApiService
) {
    private val prefs: SharedPreferences = context.getSharedPreferences("vpn_configs", Context.MODE_PRIVATE)
    private val KEY_CONFIGS = "saved_configs"
    private val KEY_CURRENT_ID = "current_config_id"
    private val KEY_CLIENT_PRIVATE_KEY = "client_private_key"
    private val KEY_CLIENT_PUBLIC_KEY = "client_public_key"

    // Load all configs
    suspend fun getConfigs(): List<ServerConfig> = withContext(Dispatchers.IO) {
        val jsonString = prefs.getString(KEY_CONFIGS, "[]")
        val list = mutableListOf<ServerConfig>()
        try {
            val array = org.json.JSONArray(jsonString)
            for (i in 0 until array.length()) {
                val json = array.getJSONObject(i)
                list.add(parseConfig(json))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        list
    }

    suspend fun addConfig(config: ServerConfig) = withContext(Dispatchers.IO) {
        val currentList = getConfigs().toMutableList()
        currentList.add(config)
        saveConfigsList(currentList)
    }

    suspend fun removeConfig(config: ServerConfig) = withContext(Dispatchers.IO) {
        val currentList = getConfigs().toMutableList()
        currentList.removeIf { it.name == config.name && it.endpoint == config.endpoint }
        saveConfigsList(currentList)
    }

    suspend fun saveCurrentConfig(config: ServerConfig) = withContext(Dispatchers.IO) {
        prefs.edit { putString(KEY_CURRENT_ID, config.name) } // Simple ID by name for now
    }

    suspend fun getCurrentConfig(): ServerConfig? = withContext(Dispatchers.IO) {
        val currentName = prefs.getString(KEY_CURRENT_ID, null) ?: return@withContext null
        getConfigs().find { it.name == currentName }
    }

    private fun saveConfigsList(list: List<ServerConfig>) {
        val array = org.json.JSONArray()
        list.forEach { config ->
            val json = JSONObject().apply {
                put("name", config.name)
                put("privateKey", config.privateKey)
                put("address", config.address)
                put("dns", config.dns)
                put("publicKey", config.publicKey)
                put("endpoint", config.endpoint)
                put("allowedIps", config.allowedIps)
                put("country", config.country)
                put("flag", config.flag)
                put("city", config.city)
                put("isPremium", config.isPremium)
            }
            array.put(json)
        }
        prefs.edit().putString(KEY_CONFIGS, array.toString()).apply()
    }

    private fun parseConfig(json: JSONObject): ServerConfig {
        return ServerConfig(
            name = json.optString("name"),
            privateKey = json.optString("privateKey"),
            address = json.optString("address"),
            dns = json.optString("dns"),
            publicKey = json.optString("publicKey"),
            endpoint = json.optString("endpoint"),
            allowedIps = json.optString("allowedIps"),
            country = json.optString("country"),
            flag = json.optString("flag"),
            city = json.optString("city"),
            isPremium = json.optBoolean("isPremium")
        )
    }

    // Generate a new keypair
    fun generateKeyPair(): KeyPair {
        return KeyPair()
    }

    // Create a REAL Cloudflare config via API
    suspend fun createCloudflareConfig(): ServerConfig? {
        val keys = generateKeyPair()
        val privateKey = keys.privateKey.toBase64()
        val publicKey = keys.publicKey.toBase64()
        
        return cloudflareService.registerAndGetConfig(privateKey, publicKey)?.also { config ->
            addConfig(config)
        }
    }

    // --- New API Integration ---

    // Get list of servers from API
    suspend fun fetchServers(): List<com.example.androidvpn.model.ServerItemDto> = withContext(Dispatchers.IO) {
        try {
            val response = vpnApiService.getServers()
            response.servers
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    // Connect to a specific server
    suspend fun connectToServer(serverItem: com.example.androidvpn.model.ServerItemDto): ServerConfig? = withContext(Dispatchers.IO) {
        val keys = getOrCreateClientKeys()

        try {
            // Step 1: Register key with Worker (for KV tracking)
            val registerRequest = com.example.androidvpn.model.RegisterRequest(
                publicKey = keys.publicKey.toBase64()
            )
            try {
                val regResponse = vpnApiService.registerKey(registerRequest)
                android.util.Log.d("ServerRepository", "Key registration status: ${regResponse.status}")
            } catch (e: Exception) {
                android.util.Log.e("ServerRepository", "Key registration failed: ${e.message}")
            }

            // Step 2: Call VM peer API directly to add peer and get assigned IP
            var assignedIP = "10.10.1.2" // default fallback
            try {
                val vmUrl = java.net.URL("http://35.222.23.3:8080/add-peer")
                val conn = vmUrl.openConnection() as java.net.HttpURLConnection
                conn.requestMethod = "POST"
                conn.setRequestProperty("Content-Type", "application/json")
                conn.setRequestProperty("X-Secret", "cc0221a4d55b1d4b09bf4635e89edbf7")
                conn.connectTimeout = 2000
                conn.readTimeout = 2000
                conn.doOutput = true

                val body = org.json.JSONObject().put("publicKey", keys.publicKey.toBase64()).toString()
                conn.outputStream.write(body.toByteArray())
                conn.outputStream.flush()

                if (conn.responseCode == 200) {
                    val responseText = conn.inputStream.bufferedReader().readText()
                    val json = JSONObject(responseText)
                    assignedIP = json.optString("ip", assignedIP)
                    android.util.Log.d("ServerRepository", "VM Peer API: status=${json.optString("status")}, ip=$assignedIP")
                } else {
                    android.util.Log.e("ServerRepository", "VM Peer API failed: HTTP ${conn.responseCode}")
                }
                conn.disconnect()
            } catch (e: Exception) {
                android.util.Log.e("ServerRepository", "VM Peer API error: ${e.message}")
            }

            // Step 3: Get server config from Worker API
            val connectRequest = com.example.androidvpn.model.ConnectRequest(
                userId = "user_${keys.publicKey.toBase64().take(8)}",
                serverId = serverItem.id,
                clientPublicKey = keys.publicKey.toBase64()
            )
            val response = vpnApiService.connect(request = connectRequest)
            android.util.Log.d("ServerRepository", "Worker API: Endpoint=${response.endpoint}, WorkerIP=${response.assignedIP}, UsingIP=$assignedIP")

            val config = ServerConfig(
                name = "${serverItem.flag} ${serverItem.city}",
                privateKey = keys.privateKey.toBase64(),
                address = "$assignedIP/32", // Use IP from VM, not Worker fallback
                dns = response.dns,
                publicKey = response.serverPublicKey,
                endpoint = response.endpoint,
                allowedIps = response.allowedIPs,
                country = serverItem.country,
                flag = serverItem.flag,
                city = serverItem.city,
                isPremium = serverItem.premium
            )

            // Save as current config
            addConfig(config)
            saveCurrentConfig(config)

            return@withContext config
        } catch (e: Exception) {
            android.util.Log.e("ServerRepository", "connectToServer failed: ${e.message}")
            e.printStackTrace()
            null
        }
    }

    private fun getOrCreateClientKeys(): KeyPair {
        val priv = prefs.getString(KEY_CLIENT_PRIVATE_KEY, null)
        val pub = prefs.getString(KEY_CLIENT_PUBLIC_KEY, null)

        if (priv != null && pub != null) {
            return KeyPair(com.wireguard.crypto.Key.fromBase64(priv))
        }

        val keys = generateKeyPair()
        prefs.edit()
            .putString(KEY_CLIENT_PRIVATE_KEY, keys.privateKey.toBase64())
            .putString(KEY_CLIENT_PUBLIC_KEY, keys.publicKey.toBase64())
            .apply()
        return keys
    }
}
