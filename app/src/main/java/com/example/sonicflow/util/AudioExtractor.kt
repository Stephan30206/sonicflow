package com.example.sonicflow.util

import android.media.MediaExtractor
import android.media.MediaFormat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.nio.ByteBuffer
import kotlin.math.abs

object AudioExtractor {

    suspend fun extractWaveformData(
        path: String,
        samplesPerBar: Int = Constants.WAVEFORM_SAMPLES_PER_BAR
    ): List<Float> = withContext(Dispatchers.IO) {
        val extractor = MediaExtractor()

        try {
            extractor.setDataSource(path)

            var audioTrackIndex = -1
            for (i in 0 until extractor.trackCount) {
                val format = extractor.getTrackFormat(i)
                val mime = format.getString(MediaFormat.KEY_MIME)
                if (mime?.startsWith("audio/") == true) {
                    audioTrackIndex = i
                    break
                }
            }

            if (audioTrackIndex == -1) {
                return@withContext generateDefaultWaveform()
            }

            extractor.selectTrack(audioTrackIndex)

            val amplitudes = mutableListOf<Float>()
            val buffer = ByteBuffer.allocate(256 * 1024)

            var sum = 0f
            var count = 0

            while (true) {
                val sampleSize = extractor.readSampleData(buffer, 0)
                if (sampleSize < 0) break

                buffer.position(0)

                for (i in 0 until sampleSize step 2) {
                    if (i + 1 < sampleSize) {
                        val sample = (buffer.get(i).toInt() or
                                (buffer.get(i + 1).toInt() shl 8)).toShort()
                        sum += abs(sample.toFloat())
                        count++

                        if (count >= samplesPerBar) {
                            val avgAmplitude = (sum / count) / Short.MAX_VALUE
                            amplitudes.add(avgAmplitude.coerceIn(0f, 1f))
                            sum = 0f
                            count = 0
                        }
                    }
                }

                extractor.advance()
                buffer.clear()
            }

            if (count > 0) {
                val avgAmplitude = (sum / count) / Short.MAX_VALUE
                amplitudes.add(avgAmplitude.coerceIn(0f, 1f))
            }

            normalizeAmplitudes(amplitudes)

        } catch (e: Exception) {
            e.printStackTrace()
            generateDefaultWaveform()
        } finally {
            extractor.release()
        }
    }

    private fun normalizeAmplitudes(amplitudes: List<Float>): List<Float> {
        if (amplitudes.isEmpty()) return generateDefaultWaveform()

        val maxAmplitude = amplitudes.maxOrNull() ?: 1f
        return amplitudes.map { (it / maxAmplitude * 0.8f) + 0.1f }
    }

    private fun generateDefaultWaveform(): List<Float> {
        return List(50) { i -> (i % 10) / 10f * 0.5f + 0.3f }
    }
}