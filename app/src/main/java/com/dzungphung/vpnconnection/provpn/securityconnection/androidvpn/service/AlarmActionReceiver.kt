package com.dzungphung.vpnconnection.provpn.securityconnection.androidvpn.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.dzungphung.vpnconnection.provpn.securityconnection.androidvpn.alert.AlarmController
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class AlarmActionReceiver : BroadcastReceiver() {

    @Inject lateinit var alarmController: AlarmController

    override fun onReceive(context: Context?, intent: Intent?) {
        val action = intent?.action ?: return
        when (action) {
            "ACTION_STOP_ALARM" -> {
                alarmController.stopAlarm()
            }
            "ACTION_STOP_SERVICE" -> {
                alarmController.stopAlarm()
                context?.let { ClapDetectionService.stopService(it) }
            }
        }
    }
}

