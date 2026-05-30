package com.sentinel.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.TelephonyManager
import android.util.Log
import com.sentinel.data.local.db.dao.AppConfigDao
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class HardwareMonitorReceiver : BroadcastReceiver() {

    @Inject
    lateinit var appConfigDao: AppConfigDao

    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            "android.intent.action.SIM_STATE_CHANGED" -> {
                handleSimStateChange(context)
            }
        }
    }

    private fun handleSimStateChange(context: Context) {
        coroutineScope.launch {
            val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            val currentSimId = try {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                    telephonyManager.simSerialNumber // This might be null or restricted
                } else {
                    telephonyManager.simSerialNumber
                }
            } catch (e: SecurityException) {
                null
            }

            val storedSimConfig = appConfigDao.getConfig("trusted_sim_id")
            if (storedSimConfig != null && currentSimId != null && storedSimConfig.value != currentSimId) {
                Log.w("HardwareMonitor", "Unauthorized SIM detected!")
                triggerTheftMode(context, "SIM_SWAP")
            }
        }
    }

    private fun triggerTheftMode(context: Context, reason: String) {
        val intent = Intent("com.sentinel.COMMAND_TRIGGERED").apply {
            putExtra("COMMAND", "LOCK")
            putExtra("REASON", reason)
            setPackage(context.packageName)
        }
        context.sendBroadcast(intent)
    }
}
