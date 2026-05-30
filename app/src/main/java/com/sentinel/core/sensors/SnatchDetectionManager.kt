package com.sentinel.core.sensors

import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.sqrt

@Singleton
class SnatchDetectionManager @Inject constructor(
    @ApplicationContext private val context: Context
) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private val gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)

    private var lastAcceleration: Float = 0f
    private var accelerationThreshold = 15f // Adjust based on calibration
    private var rotationThreshold = 5f // Adjust based on calibration

    fun startDetection() {
        accelerometer?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
        gyroscope?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    fun stopDetection() {
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent) {
        when (event.sensor.type) {
            Sensor.TYPE_ACCELEROMETER -> {
                val x = event.values[0]
                val y = event.values[1]
                val z = event.values[2]
                val acceleration = sqrt(x * x + y * y + z * z)
                
                if (acceleration > accelerationThreshold) {
                    Log.w("SnatchDetection", "High acceleration detected: $acceleration")
                    triggerSnatchAlert()
                }
            }
            Sensor.TYPE_GYROSCOPE -> {
                val x = event.values[0]
                val y = event.values[1]
                val z = event.values[2]
                val rotation = sqrt(x * x + y * y + z * z)
                
                if (rotation > rotationThreshold) {
                    Log.w("SnatchDetection", "High rotation detected: $rotation")
                    // Could be a snatch, combined with acceleration
                }
            }
        }
    }

    private fun triggerSnatchAlert() {
        val intent = Intent("com.sentinel.COMMAND_TRIGGERED").apply {
            putExtra("COMMAND", "LOCK")
            putExtra("REASON", "SNATCH_DETECTED")
            setPackage(context.packageName)
        }
        context.sendBroadcast(intent)
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}
