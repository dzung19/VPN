package com.dzungphung.vpnconnection.provpn.securityconnection.androidvpn.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class TimePass(
    val id: String,
    val durationHours: Int,
    val description: String,
    var activationTimeMs: Long? = null
) : Parcelable {
    val expiresAtMs: Long?
        get() = activationTimeMs?.plus(durationHours * 60L * 60L * 1000L)

    fun isExpired(currentTimeMs: Long): Boolean {
        val exp = expiresAtMs ?: return false
        return currentTimeMs >= exp
    }
}