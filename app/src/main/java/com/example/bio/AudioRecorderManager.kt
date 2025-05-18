package com.example.bio // Use your package name

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import android.util.Log
import java.io.File
import java.io.IOException

class AudioRecorderManager(private val context: Context) {

    private var recorder: MediaRecorder? = null
    private var outputFile: File? = null

    fun startRecording(): String? {
        if (recorder != null) {
            Log.w("AudioRecorder", "Start recording called while recorder wasn't null. Releasing previous.")
            releaseRecorder()
        }


        outputFile = File(context.cacheDir, "audio_record_${System.currentTimeMillis()}.m4a")
        val filePath = outputFile?.absolutePath ?: run {
            Log.e("AudioRecorder", "Failed to create output file path.")
            return null
        }


        // Create a new instance every time start is called
        recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(context)
        } else {
            @Suppress("DEPRECATION")
            MediaRecorder()
        }
        Log.d("AudioRecorder", "New MediaRecorder instance created.")

        try {
            recorder?.apply {
                Log.d("AudioRecorder", "Audio source set.")
                Log.d("AudioRecorder", "Output format set.")
                Log.d("AudioRecorder", "Audio encoder set.")
                setAudioSource(MediaRecorder.AudioSource.MIC) // Or try VOICE_COMMUNICATION later
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4) // Container for AMR
                setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
                setOutputFile(filePath)
                Log.d("AudioRecorder", "Output file set: $filePath")
                prepare()
                Log.d("AudioRecorder", "Prepare successful.")
                start()
                Log.d("AudioRecorder", "Recording started successfully.")
            }
            return filePath
        } catch (e: IOException) {
            Log.e("AudioRecorder", "prepare() failed: ${e.message}", e)
            releaseRecorder() // Clean up on failure
            return null
        } catch (e: IllegalStateException) {
            // Log the specific state error during setup
            Log.e("AudioRecorder", "IllegalStateException during setup/start: ${e.message}", e)
            releaseRecorder() // Clean up on failure
            return null
        } catch (e: Exception) {
            // Catch any other potential exceptions during setup
            Log.e("AudioRecorder", "Generic Exception during setup/start: ${e.message}", e)
            releaseRecorder()
            return null
        }
    }

    fun stopRecording(): String? {
        val path = outputFile?.absolutePath
        if (recorder == null) {
            Log.w("AudioRecorder", "stopRecording called but recorder was already null.")
            return null // Nothing to stop
        }
        try {
            recorder?.stop() // Stop recording
            Log.d("AudioRecorder", "Recording stopped.")
        } catch (e: IllegalStateException) {
            Log.e("AudioRecorder", "stop() failed: ${e.message}", e)
            releaseRecorder() // Still attempt to release
            return null // Indicate failure
        } catch (e: RuntimeException) { // stop() can also throw RuntimeException
            Log.e("AudioRecorder", "stop() failed with RuntimeException: ${e.message}", e)
            releaseRecorder()
            return null
        } finally {
            releaseRecorder()
        }
        return path
    }


    fun releaseRecorder() {
        if (recorder == null) {
            return // Nothing to release
        }
        try {
            // Reset before release might help clear state thoroughly
            recorder?.reset()
            recorder?.release()
            Log.d("AudioRecorder", "Recorder reset and released.")
        } catch (e: Exception) {
            Log.e("AudioRecorder", "Error releasing recorder: ${e.message}", e)
        } finally {
            recorder = null
        }
    }
}

