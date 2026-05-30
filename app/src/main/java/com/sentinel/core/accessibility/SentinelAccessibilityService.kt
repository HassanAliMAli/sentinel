package com.sentinel.core.accessibility

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.view.accessibility.AccessibilityEvent

class SentinelAccessibilityService : AccessibilityService() {

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        if (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            val packageName = event.packageName?.toString()
            val className = event.className?.toString()
            
            // Logic to detect power menu and intercept it
            if (packageName == "com.android.systemui" && className?.contains("GlobalActionsDialog", ignoreCase = true) == true) {
                // Dismiss the power menu
                performGlobalAction(GLOBAL_ACTION_BACK)
                
                // Trigger Fake Shutdown UI
                val intent = Intent("com.sentinel.TRIGGER_FAKE_SHUTDOWN").apply {
                    setPackage(packageName)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                sendBroadcast(intent)
            }
        }
    }

    override fun onInterrupt() {}
}
