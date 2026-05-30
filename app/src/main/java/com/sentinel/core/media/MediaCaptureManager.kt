package com.sentinel.core.media

import android.annotation.SuppressLint
import android.content.Context
import android.media.MediaRecorder
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.sentinel.core.crypto.CryptoManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import kotlin.coroutines.resume

class MediaCaptureManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val cryptoManager: CryptoManager
) {
    private var mediaRecorder: MediaRecorder? = null

    @SuppressLint("MissingPermission")
    suspend fun takePhoto(lifecycleOwner: LifecycleOwner): String? {
        val cameraProvider = suspendCancellableCoroutine<ProcessCameraProvider> { continuation ->
            ProcessCameraProvider.getInstance(context).addListener({
                continuation.resume(ProcessCameraProvider.getInstance(context).get())
            }, ContextCompat.getMainExecutor(context))
        }

        val imageCapture = ImageCapture.Builder().build()
        val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA

        return suspendCancellableCoroutine { continuation ->
            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, imageCapture)

                val tempFile = File(context.cacheDir, "temp_intruder.jpg")
                val outputOptions = ImageCapture.OutputFileOptions.Builder(tempFile).build()

                imageCapture.takePicture(
                    outputOptions,
                    ContextCompat.getMainExecutor(context),
                    object : ImageCapture.OnImageSavedCallback {
                        override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                            val encryptedFile = File(context.filesDir, "intruder_${System.currentTimeMillis()}.enc")
                            cryptoManager.encrypt(tempFile.readBytes(), FileOutputStream(encryptedFile))
                            tempFile.delete()
                            continuation.resume(encryptedFile.absolutePath)
                        }

                        override fun onError(exception: ImageCaptureException) {
                            Log.e("MediaCaptureManager", "Photo capture failed: ${exception.message}")
                            continuation.resume(null)
                        }
                    }
                )
            } catch (e: Exception) {
                Log.e("MediaCaptureManager", "Binding failed: ${e.message}")
                continuation.resume(null)
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun startAudioRecording(): String? {
        val outputFile = File(context.filesDir, "audio_${System.currentTimeMillis()}.m4a")
        mediaRecorder = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            MediaRecorder(context)
        } else {
            @Suppress("DEPRECATION")
            MediaRecorder()
        }.apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setOutputFile(outputFile.absolutePath)
            try {
                prepare()
                start()
            } catch (e: Exception) {
                Log.e("MediaCaptureManager", "Audio recording failed: ${e.message}")
                return null
            }
        }
        return outputFile.absolutePath
    }

    fun stopAudioRecording() {
        mediaRecorder?.apply {
            try {
                stop()
                release()
            } catch (e: Exception) {
                Log.e("MediaCaptureManager", "Stop recording failed: ${e.message}")
            }
        }
        mediaRecorder = null
    }
}
