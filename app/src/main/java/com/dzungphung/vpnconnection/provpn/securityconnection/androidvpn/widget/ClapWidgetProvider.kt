package com.dzungphung.vpnconnection.provpn.securityconnection.androidvpn.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.dzungphung.vpnconnection.provpn.securityconnection.androidvpn.R
import com.dzungphung.vpnconnection.provpn.securityconnection.androidvpn.data.ClapPreferencesRepository
import com.dzungphung.vpnconnection.provpn.securityconnection.androidvpn.service.ClapDetectionService
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ClapWidgetProvider : AppWidgetProvider() {

    companion object {
        const val ACTION_TOGGLE = "com.dzungphung.clapfinder.ACTION_TOGGLE_FINDER"

        fun updateAllWidgets(context: Context) {
            val intent = Intent(context, ClapWidgetProvider::class.java).apply {
                action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                val ids = AppWidgetManager.getInstance(context).getAppWidgetIds(
                    ComponentName(context, ClapWidgetProvider::class.java)
                )
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
            }
            context.sendBroadcast(intent)
        }
    }

    @Inject lateinit var preferencesRepository: ClapPreferencesRepository

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        val isActive = try { preferencesRepository.isDetectionActive.value } catch (e: Exception) { false }

        for (widgetId in appWidgetIds) {
            val views = RemoteViews(context.packageName, R.layout.widget_vpn)

            val toggleIntent = Intent(context, ClapWidgetProvider::class.java).apply {
                action = ACTION_TOGGLE
            }
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                0,
                toggleIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_container, pendingIntent)

            if (isActive) {
                views.setTextViewText(R.id.widget_text, context.getString(R.string.listening_active))
                views.setInt(R.id.widget_container, "setBackgroundResource", R.drawable.widget_bg_connected)
            } else {
                views.setTextViewText(R.id.widget_text, context.getString(R.string.tap_to_activate))
                views.setInt(R.id.widget_container, "setBackgroundResource", R.drawable.widget_bg_disconnected)
            }

            appWidgetManager.updateAppWidget(widgetId, views)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == ACTION_TOGGLE) {
            val isActive = try { preferencesRepository.isDetectionActive.value } catch (e: Exception) { false }
            if (isActive) {
                ClapDetectionService.stopService(context)
            } else {
                ClapDetectionService.startService(context)
            }
            updateAllWidgets(context)
        }
    }
}

