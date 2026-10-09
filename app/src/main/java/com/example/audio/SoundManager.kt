package com.example.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.sin

class SoundManager {
    private val scope = CoroutineScope(Dispatchers.Default)
    var isSoundEnabled: Boolean = true

    private val sampleRate = 44100

    // Pre-rendered PCM audio buffers for instant playback
    private val selectSoundBuffer: ShortArray by lazy { generateSweep(420f, 680f, 0.05f) }
    private val dropSoundBuffer: ShortArray by lazy { generateSweep(320f, 180f, 0.07f) }
    private val invalidSoundBuffer: ShortArray by lazy { generateBuzz(160f, 0.12f) }
    private val clickSoundBuffer: ShortArray by lazy { generateSweep(850f, 950f, 0.025f) }
    private val winSoundBuffer: ShortArray by lazy { generateArpeggio() }

    fun playBallSelect() {
        if (!isSoundEnabled) return
        playSound(selectSoundBuffer)
    }

    fun playBallDrop() {
        if (!isSoundEnabled) return
        playSound(dropSoundBuffer)
    }

    fun playInvalidMove() {
        if (!isSoundEnabled) return
        playSound(invalidSoundBuffer)
    }

    fun playButtonClick() {
        if (!isSoundEnabled) return
        playSound(clickSoundBuffer)
    }

    fun playWin() {
        if (!isSoundEnabled) return
        playSound(winSoundBuffer)
    }

    private fun playSound(buffer: ShortArray) {
        scope.launch {
            try {
                val track = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_GAME)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(sampleRate)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(buffer.size * 2)
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .build()

                track.write(buffer, 0, buffer.size)
                track.play()
                // Auto release after playback
                val durationMs = (buffer.size * 1000L / sampleRate) + 50L
                kotlinx.coroutines.delay(durationMs)
                track.stop()
                track.release()
            } catch (_: Exception) {
                // Ignore audio play errors on background thread
            }
        }
    }

    private fun generateSweep(startFreq: Float, endFreq: Float, durationSec: Float): ShortArray {
        val totalSamples = (sampleRate * durationSec).toInt()
        val buffer = ShortArray(totalSamples)
        var phase = 0.0
        for (i in 0 until totalSamples) {
            val progress = i.toFloat() / totalSamples
            val freq = startFreq + (endFreq - startFreq) * progress
            val phaseInc = 2.0 * Math.PI * freq / sampleRate
            phase += phaseInc
            // Decay envelope
            val envelope = (1.0 - progress) * (1.0 - progress)
            val sample = (sin(phase) * envelope * 0.75 * Short.MAX_VALUE).toInt()
            buffer[i] = sample.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        return buffer
    }

    private fun generateBuzz(freq: Float, durationSec: Float): ShortArray {
        val totalSamples = (sampleRate * durationSec).toInt()
        val buffer = ShortArray(totalSamples)
        var phase = 0.0
        for (i in 0 until totalSamples) {
            val progress = i.toFloat() / totalSamples
            val phaseInc = 2.0 * Math.PI * freq / sampleRate
            phase += phaseInc
            // Slight square-like clipping + decaying envelope
            val raw = sin(phase)
            val square = if (raw >= 0) 0.6 else -0.6
            val envelope = 1.0 - progress
            val sample = (square * envelope * 0.5 * Short.MAX_VALUE).toInt()
            buffer[i] = sample.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        return buffer
    }

    private fun generateArpeggio(): ShortArray {
        // C5, E5, G5, C6 notes
        val notes = floatArrayOf(523.25f, 659.25f, 783.99f, 1046.50f)
        val noteDuration = 0.11f
        val noteSamples = (sampleRate * noteDuration).toInt()
        val totalSamples = noteSamples * notes.size
        val buffer = ShortArray(totalSamples)

        for ((index, freq) in notes.withIndex()) {
            val offset = index * noteSamples
            var phase = 0.0
            for (i in 0 until noteSamples) {
                val progress = i.toFloat() / noteSamples
                val phaseInc = 2.0 * Math.PI * freq / sampleRate
                phase += phaseInc
                val envelope = sin(progress * Math.PI * 0.5) * (1.0 - progress * 0.6)
                val sample = (sin(phase) * envelope * 0.7 * Short.MAX_VALUE).toInt()
                buffer[offset + i] = sample.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
            }
        }
        return buffer
    }
}
