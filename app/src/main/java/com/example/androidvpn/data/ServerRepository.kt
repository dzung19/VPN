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

@Singleton
class ServerRepository @Inject constructor(
    @ApplicationContext context: Context,
    private val cloudflareService: CloudflareService
) {
    private val prefs: SharedPreferences = context.getSharedPreferences("vpn_configs", Context.MODE_PRIVATE)
    private val KEY_CONFIGS = "saved_configs"
    private val KEY_CURRENT_ID = "current_config_id"

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
        prefs.edit().putString(KEY_CURRENT_ID, config.name).apply() // Simple ID by name for now
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
            allowedIps = json.optString("allowedIps")
        )
    }
    
    // CloudflareService is now injected


    // ... (existing code)

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
}
