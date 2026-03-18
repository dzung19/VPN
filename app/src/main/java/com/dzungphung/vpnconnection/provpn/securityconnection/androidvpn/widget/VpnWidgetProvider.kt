package com.dzungphung.vpnconnection.provpn.securityconnection.androidvpn.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.SystemClock
import android.util.Log
import android.view.View
import android.widget.RemoteViews
import com.dzungphung.vpnconnection.provpn.securityconnection.androidvpn.R
import com.dzungphung.vpnconnection.provpn.securityconnection.androidvpn.data.TunnelManager
import com.dzungphung.vpnconnection.provpn.securityconnection.androidvpn.data.VpnActionReceiver
import com.dzungphung.vpnconnection.provpn.securityconnection.androidvpn.di.TunnelManagerEntryPoint
import com.wireguard.android.backend.Tunnel
import dagger.hilt.android.EntryPointAccessors

class VpnWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        Log.d("VpnWidgetProvider", "onReceive: ${intent.action}")
        if (intent.action == AppWidgetManager.ACTION_APPWIDGET_UPDATE ||
            intent.action == "com.dzungphung.vpnconnection.provpn.securityconnection.androidvpn.ACTION_WIDGET_REFRESH" ||
            intent.action == AppWidgetManager.ACTION_APPWIDGET_OPTIONS_CHANGED
        ) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(
                ComponentName(
                    context,
                    VpnWidgetProvider::class.java
                )
            )
            for (appWidgetId in appWidgetIds) {
                updateAppWidget(context, appWidgetManager, appWidgetId)
            }
        }
    }

    companion object {
        fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
            val views = RemoteViews(context.packageName, R.layout.widget_vpn)
            val tunnelManager = getTunnelManager(context)

            val isConnected = tunnelManager.tunnelState.value == Tunnel.State.UP


            if (isConnected) {
                Log.d("VpnWidgetProvider", "updateAppWidget: $isConnected")
                views.setTextViewText(R.id.widget_text, "Connected")
                views.setImageViewResource(R.id.widget_icon, R.drawable.ic_lock_secure)
                views.setInt(R.id.widget_container, "setBackgroundResource", R.drawable.widget_bg_connected)
                
                views.setViewVisibility(R.id.widget_timer, View.VISIBLE)
                views.setChronometer(R.id.widget_timer, tunnelManager.tunnelStartTimeMillis, null, true)
            } else {
                views.setTextViewText(R.id.widget_text, "Tap to Connect")
                views.setImageViewResource(R.id.widget_icon, R.drawable.ic_lock_unprotected)
                views.setInt(R.id.widget_container, "setBackgroundResource", R.drawable.widget_bg_disconnected)
                
                views.setViewVisibility(R.id.widget_timer, View.GONE)
                views.setChronometer(R.id.widget_timer, SystemClock.elapsedRealtime(), null, false)
            }
            
            // Intent to toggle VPN
            val intent = Intent(context, VpnActionReceiver::class.java).apply {
                action = "TOGGLE_VPN"
            }
            val pendingIntent = PendingIntent.getBroadcast(
                context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            
            views.setOnClickPendingIntent(R.id.widget_container, pendingIntent)
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }

        private fun getTunnelManager(context: Context): TunnelManager {
            return EntryPointAccessors.fromApplication(
                context.applicationContext,
                TunnelManagerEntryPoint::class.java
            ).tunnelManager()
        }
    }

}

fun Context.updateWidget() {
    val component = ComponentName(this,
        VpnWidgetProvider::class.java)
    with(AppWidgetManager.getInstance(this)) {
        val appWidgetIds = getAppWidgetIds(component)
        for (appWidgetId in appWidgetIds) {
            VpnWidgetProvider.updateAppWidget(
                context = this@updateWidget,
                appWidgetManager = this,
                appWidgetId = appWidgetId
            )
        }
    }
}
