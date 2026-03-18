package com.dzungphung.vpnconnection.provpn.securityconnection.androidvpn.di

import com.dzungphung.vpnconnection.provpn.securityconnection.androidvpn.data.TunnelManager
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface TunnelManagerEntryPoint {
    fun tunnelManager(): TunnelManager
}
