package com.sentinel.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import android.util.Log
import com.sentinel.data.local.db.dao.AppConfigDao
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.security.MessageDigest
import javax.inject.Inject

@AndroidEntryPoint
class SMSReceiver : BroadcastReceiver() {

    @Inject
    lateinit var appConfigDao: AppConfigDao

    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Telephony.Sms.Intents.SMS_RECEIVED_ACTION) {
            val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
            for (sms in messages) {
                val messageBody = sms.messageBody ?: continue
                handleIncomingMessage(context, messageBody, sms.originatingAddress)
            }
        }
    }

    private fun handleIncomingMessage(context: Context, body: String, sender: String?) {
        coroutineScope.launch {
            val config = appConfigDao.getConfig("secret_pin_hash") ?: return@launch
            val storedHash = config.value

            val parts = body.trim().split(" ")
            if (parts.size < 2) return@launch

            val providedPin = parts[0]
            val command = parts[1].uppercase()

            if (hashPin(providedPin) == storedHash) {
                Log.d("SMSReceiver", "Authenticated command: $command from $sender")
                executeCommand(context, command, sender, parts.drop(2))
            } else {
                Log.w("SMSReceiver", "Unauthorized PIN attempt from $sender")
            }
        }
    }

    private fun hashPin(pin: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(pin.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }

    private fun executeCommand(context: Context, command: String, sender: String?, args: List<String>) {
        // Command execution logic will be implemented in Task 2.1's CommandDispatcher
        val intent = Intent("com.sentinel.COMMAND_TRIGGERED").apply {
            putExtra("COMMAND", command)
            putExtra("SENDER", sender)
            putStringArrayListExtra("ARGS", ArrayList(args))
            setPackage(context.packageName)
        }
        context.sendBroadcast(intent)
    }
}
