package com.poc.chatbotrecord.audio

import android.media.*
import android.util.Log
import java.io.File

object AudioPlayer {

    // Plays a PCM file using AudioTrack
    fun playPcm(file: File) {
        Log.d("AudioPlayer", "Preparing to play PCM file: ${file.absolutePath}")
        val bufferSize = AudioTrack.getMinBufferSize(
            48000,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        )

        val audioTrack = AudioTrack(
            AudioManager.STREAM_MUSIC,
            48000,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
            bufferSize,
            AudioTrack.MODE_STREAM
        )

        Thread {
            val buffer = ByteArray(bufferSize)
            val input = file.inputStream()
            Log.d("AudioPlayer", "AudioTrack playback started")
            audioTrack.play()
            while (true) {
                val read = input.read(buffer)
                if (read <= 0) break
                audioTrack.write(buffer, 0, read)
            }
            audioTrack.stop()
            audioTrack.release()
            input.close()
            Log.d("AudioPlayer", "AudioTrack playback finished and released")
        }.start()
    }
}
