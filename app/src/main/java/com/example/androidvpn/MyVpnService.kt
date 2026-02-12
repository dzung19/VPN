package com.example.androidvpn

import android.annotation.SuppressLint
import android.net.VpnService
import android.util.Log

@SuppressLint("VpnServicePolicy")
class MyVpnService : VpnService() {

    override fun onCreate() {
        super.onCreate()
        Log.d("MyVpnService", "Service Created")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("MyVpnService", "Service Destroyed")
    }
}
