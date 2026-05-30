package com.sentinel.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import com.sentinel.data.local.db.dao.AppConfigDao
import com.sentinel.data.local.db.dao.TheftLogDao
import com.sentinel.core.location.LocationTracker
import com.sentinel.core.crypto.CryptoManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

import android.content.BroadcastReceiver
import android.telephony.SmsManager
import android.util.Log
import androidx.lifecycle.lifecycleScope
import com.sentinel.core.media.MediaCaptureManager
import com.sentinel.data.local.db.entities.TheftLog
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

import com.sentinel.core.sensors.SnatchDetectionManager

@AndroidEntryPoint
class SentinelCoreService : LifecycleService() {

    @Inject
    lateinit var appConfigDao: AppConfigDao

    @Inject
    lateinit var theftLogDao: TheftLogDao

    @Inject
    lateinit var locationTracker: LocationTracker

    @Inject
    lateinit var cryptoManager: CryptoManager

    @Inject
    lateinit var mediaCaptureManager: MediaCaptureManager

    @Inject
    lateinit var snatchDetectionManager: SnatchDetectionManager

    private val commandReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val command = intent.getStringExtra("COMMAND") ?: return
            val sender = intent.getStringExtra("SENDER")
            val args = intent.getStringArrayListExtra("ARGS") ?: arrayListOf()
            handleCommand(command, sender, args)
        }
    }

    override fun onCreate() {
        super.onCreate()
        startForegroundService()
        registerReceiver(commandReceiver, IntentFilter("com.sentinel.COMMAND_TRIGGERED"), Context.RECEIVER_NOT_EXPORTED)
        snatchDetectionManager.startDetection()
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(commandReceiver)
        snatchDetectionManager.stopDetection()
    }

    private fun handleCommand(command: String, sender: String?, args: List<String>) {
        lifecycleScope.launch {
            when (command) {
                "LOCATE" -> {
                    val location = locationTracker.getCurrentLocation()
                    if (location != null && sender != null) {
                        val mapsLink = "https://www.google.com/maps?q=${location.latitude},${location.longitude}"
                        sendSms(sender, "Sentinel: Device located! $mapsLink")
                    }
                }
                "PHOTO" -> {
                    val photoPath = mediaCaptureManager.takePhoto(this@SentinelCoreService)
                    if (photoPath != null && sender != null) {
                        theftLogDao.insertLog(TheftLog(
                            timestamp = System.currentTimeMillis(),
                            triggerType = "REMOTE_PHOTO",
                            latitude = null,
                            longitude = null,
                            photoPath = photoPath,
                            audioPath = null
                        ))
                        sendSms(sender, "Sentinel: Intruder photo captured and encrypted.")
                    }
                }
                "AUDIO" -> {
                    val audioPath = mediaCaptureManager.startAudioRecording()
                    if (audioPath != null && sender != null) {
                        sendSms(sender, "Sentinel: Recording 15s of audio...")
                        delay(15000)
                        mediaCaptureManager.stopAudioRecording()
                        theftLogDao.insertLog(TheftLog(
                            timestamp = System.currentTimeMillis(),
                            triggerType = "REMOTE_AUDIO",
                            latitude = null,
                            longitude = null,
                            photoPath = null,
                            audioPath = audioPath
                        ))
                        sendSms(sender, "Sentinel: Audio recording complete and encrypted.")
                    }
                }
                "STATUS" -> {
                    if (sender != null) {
                        sendSms(sender, "Sentinel Status: Active. Protection Level: High.")
                    }
                }
            }
        }
    }

    private fun sendSms(phoneNumber: String, message: String) {
        try {
            val smsManager = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                getSystemService(SmsManager::class.java)
            } else {
                @Suppress("DEPRECATION")
                SmsManager.getDefault()
            }
            smsManager.sendTextMessage(phoneNumber, null, message, null, null)
        } catch (e: Exception) {
            Log.e("SentinelService", "Failed to send SMS: ${e.message}")
        }
    }

    private fun startForegroundService() {
        val channelId = "sentinel_protection_channel"
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Sentinel Protection",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Sentinel is protecting your device."
            }
            notificationManager.createNotificationChannel(channel)
        }

        val notification: Notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Sentinel Active")
            .setContentText("Your device is protected.")
            .setSmallIcon(android.R.drawable.ic_lock_idle_lock) // Temporary icon
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .build()

        startForeground(NOTIFICATION_ID, notification)
    }

    companion object {
        private const val NOTIFICATION_ID = 1001
    }
}
