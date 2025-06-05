package com.poc.chatbotrecord.audio

import android.content.Context
import android.media.*
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import com.poc.chatbotrecord.R
import kotlinx.coroutines.*
import java.io.File
import java.io.FileOutputStream

class AudioRecorder(
    private val context: Context,
    private val file: File,
    private val statusView: TextView,
    private val btnRecord: Button
) {
    companion object {
        private const val SAMPLE_RATE = 48000
    }

    private val bufSize = AudioRecord.getMinBufferSize(
        SAMPLE_RATE,
        AudioFormat.CHANNEL_IN_MONO,
        AudioFormat.ENCODING_PCM_16BIT
    )

    private var recorder: AudioRecord? = null
    private var recordJob: Job? = null

    // Starts the audio recording
    fun start() {
        recorder = AudioRecord.Builder()
            .setAudioSource(MediaRecorder.AudioSource.MIC)
            .setAudioFormat(
                AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setSampleRate(SAMPLE_RATE)
                    .setChannelMask(AudioFormat.CHANNEL_IN_MONO)
                    .build()
            )
            .setBufferSizeInBytes(bufSize)
            .build().apply {
                startRecording()
            }
        Log.d("AudioRecorder", "Recording started, saving to: ${file.absolutePath}")
        recordJob = (context as ComponentActivity).lifecycleScope.launch(Dispatchers.IO) {
            FileOutputStream(file).use { out ->
                val buffer = ByteArray(bufSize)
                while (isActive) {
                    val read = recorder!!.read(buffer, 0, buffer.size)
                    if (read > 0) out.write(buffer, 0, read)
                }
            }
        }

        statusView.text = context.getString(R.string.recording)
        btnRecord.text = context.getString(R.string.stop)
    }

    // Stops the audio recording
    fun stop() {
        recordJob?.cancel()
        recorder?.apply { stop(); release() }
        Log.d("AudioRecorder", "Recording stopped and file saved at: ${file.absolutePath}")
        recorder = null
    }
}
