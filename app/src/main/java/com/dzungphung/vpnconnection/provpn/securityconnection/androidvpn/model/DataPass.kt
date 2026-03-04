package com.dzungphung.vpnconnection.provpn.securityconnection.androidvpn.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class DataPass(
    val id: String,
    val totalBytes: Long,
    val description: String,
    var bytesConsumed: Long = 0L
) : Parcelable {
    val bytesRemaining: Long
        get() = maxOf(0L, totalBytes - bytesConsumed)

    val isDepleted: Boolean
        get() = bytesRemaining <= 0L
}