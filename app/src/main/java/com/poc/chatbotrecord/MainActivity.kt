package com.poc.chatbotrecord

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Environment
import android.widget.Button
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.poc.chatbotrecord.audio.AudioPlayer
import com.poc.chatbotrecord.audio.AudioRecorder
import com.poc.chatbotrecord.audio.OpusConverter
import kotlinx.coroutines.launch
import java.io.File

class MainActivity : ComponentActivity() {

    private var audioRecorder: AudioRecorder? = null
    private lateinit var outputFile: File

    private val micPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (!granted) finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            micPermission.launch(Manifest.permission.RECORD_AUDIO)
        }

        findViewById<Button>(R.id.btnRecord).setOnClickListener { toggleRecording() }
        findViewById<Button>(R.id.btnPlay).setOnClickListener {
            if (::outputFile.isInitialized && outputFile.exists()) {
                AudioPlayer.playPcm(outputFile)
            } else {
                findViewById<TextView>(R.id.tvStatus).text = "No recording available."
            }
        }
    }

    // Starts or stops audio recording
    private fun toggleRecording() {
        if (audioRecorder == null) startRecording() else stopRecording()
    }

    // Starts recording using AudioRecorder
    private fun startRecording() {
        val btn = findViewById<Button>(R.id.btnRecord)
        val status = findViewById<TextView>(R.id.tvStatus)

        outputFile = File(getExternalFilesDir(Environment.DIRECTORY_MUSIC), "rec_${System.currentTimeMillis()}.pcm")

        audioRecorder = AudioRecorder(this, outputFile, status, btn).apply {
            start()
        }
    }

    // Stops recording and starts OPUS conversion
    private fun stopRecording() {
        val btn = findViewById<Button>(R.id.btnRecord)
        val status = findViewById<TextView>(R.id.tvStatus)

        audioRecorder?.stop()
        audioRecorder = null

        status.text = getString(R.string.converting)
        btn.text = getString(R.string.start)

        lifecycleScope.launch {
            OpusConverter.convert(this@MainActivity, outputFile, status)
        }
    }
}
