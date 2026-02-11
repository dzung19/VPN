package com.example.androidvpn

import android.app.Application

import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class VpnApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // Initialize WireGuard Backend
        com.example.androidvpn.data.TunnelManager.init(this)
    }
}
