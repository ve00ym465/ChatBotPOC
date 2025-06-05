package com.poc.chatbotrecord.audio

import android.content.Context
import android.util.Log
import android.widget.TextView
import com.poc.chatbotrecord.R
import com.score.rahasak.utils.OpusEncoder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

object OpusConverter {
    // Converts a PCM file to OPUS format using native encoder
    suspend fun convert(context: Context, inputFile: File, statusView: TextView) {
        val outputFile = File(inputFile.parent, inputFile.nameWithoutExtension + ".opus")
        try {
            System.load("/data/data/${context.packageName}/lib/libsenz.so")
            Log.d("OpusConverter", "Native library loaded successfully")
        } catch (e: UnsatisfiedLinkError) {
            Log.e("OpusConverter", "Native lib load failed: ${e.message}")
        }

        val encoder = OpusEncoder().apply {
            init(48000, 1, OpusEncoder.OPUS_APPLICATION_AUDIO)
            setBitrate(64000)
            setComplexity(10)
        }

        val input = inputFile.inputStream().buffered()
        val output = outputFile.outputStream().buffered()
        val pcmBuffer = ByteArray(960 * 2)
        val opusBuffer = ByteArray(4096)

        while (true) {
            val bytesRead = input.read(pcmBuffer)
            if (bytesRead <= 0) break
            val encoded = encoder.encode(pcmBuffer, bytesRead / 2, opusBuffer)
            if (encoded > 0) output.write(opusBuffer, 0, encoded)
        }

        encoder.close()
        input.close()
        output.close()
        Log.i("OpusConverter", "OPUS file saved at: ${outputFile.absolutePath}")

        withContext(Dispatchers.Main) {
            statusView.text = context.getString(R.string.saved, outputFile.name) +
                    "\nPCM: ${inputFile.absolutePath}\nOPUS: ${outputFile.absolutePath}"
        }
    }
}