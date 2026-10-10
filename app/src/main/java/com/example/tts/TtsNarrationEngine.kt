package com.example.tts

import android.content.Context
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import com.example.model.CaptionSegment
import com.example.model.CaptionWord
import java.io.File
import java.util.Locale
import java.util.UUID

class TtsNarrationEngine(private val context: Context) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = null
    var isInitialized: Boolean = false
        private set

    init {
        try {
            tts = TextToSpeech(context.applicationContext, this)
        } catch (e: Exception) {
            Log.e("TtsNarrationEngine", "Error initializing TTS", e)
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts?.setLanguage(Locale.US)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                tts?.setLanguage(Locale.getDefault())
            }
            isInitialized = true
            Log.d("TtsNarrationEngine", "TTS initialized successfully")
        } else {
            Log.e("TtsNarrationEngine", "TTS initialization failed with code: $status")
        }
    }

    fun speak(
        text: String,
        speechRate: Float = 1.1f,
        pitch: Float = 1.0f,
        onStart: () -> Unit = {},
        onDone: () -> Unit = {},
        onError: () -> Unit = {}
    ) {
        val ttsInstance = tts ?: return
        val utteranceId = UUID.randomUUID().toString()

        ttsInstance.setSpeechRate(speechRate.coerceIn(0.5f, 2.0f))
        ttsInstance.setPitch(pitch.coerceIn(0.5f, 2.0f))

        ttsInstance.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(id: String?) {
                if (id == utteranceId) onStart()
            }

            override fun onDone(id: String?) {
                if (id == utteranceId) onDone()
            }

            @Deprecated("Deprecated in Java")
            override fun onError(id: String?) {
                if (id == utteranceId) onError()
            }

            override fun onError(id: String?, errorCode: Int) {
                if (id == utteranceId) onError()
            }
        })

        val params = Bundle().apply {
            putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, utteranceId)
        }
        ttsInstance.speak(text, TextToSpeech.QUEUE_FLUSH, params, utteranceId)
    }

    fun stop() {
        tts?.stop()
    }

    fun synthesizeToFile(
        text: String,
        outputFile: File,
        speechRate: Float = 1.1f,
        pitch: Float = 1.0f,
        onComplete: (Boolean) -> Unit
    ) {
        val ttsInstance = tts
        if (ttsInstance == null || !isInitialized) {
            onComplete(false)
            return
        }

        val utteranceId = "synth_" + UUID.randomUUID().toString()
        ttsInstance.setSpeechRate(speechRate.coerceIn(0.5f, 2.0f))
        ttsInstance.setPitch(pitch.coerceIn(0.5f, 2.0f))

        ttsInstance.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(id: String?) {}

            override fun onDone(id: String?) {
                if (id == utteranceId) {
                    onComplete(true)
                }
            }

            @Deprecated("Deprecated in Java")
            override fun onError(id: String?) {
                if (id == utteranceId) {
                    onComplete(false)
                }
            }
        })

        val params = Bundle().apply {
            putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, utteranceId)
        }
        val result = ttsInstance.synthesizeToFile(text, params, outputFile, utteranceId)
        if (result != TextToSpeech.SUCCESS) {
            onComplete(false)
        }
    }

    fun shutdown() {
        try {
            tts?.stop()
            tts?.shutdown()
        } catch (e: Exception) {
            Log.e("TtsNarrationEngine", "Error shutting down TTS", e)
        }
    }

    companion object {
        /**
         * Calculates high-precision caption timing for synchronized karaoke rendering
         * based on natural spoken pacing, word syllable length, punctuation, and speech rate.
         */
        fun buildCaptionTimeline(
            scriptText: String,
            speechRate: Float = 1.1f
        ): Pair<List<CaptionSegment>, Float> {
            val cleanScript = scriptText.replace("\r", " ").trim()
            if (cleanScript.isBlank()) return Pair(emptyList(), 0f)

            // Split into sentences / punchy clauses
            val rawSentences = cleanScript.split(Regex("(?<=[.!?])\\s+"))
                .map { it.trim() }
                .filter { it.isNotBlank() }

            // Base rate: at 1.0x, ~2.5 words per sec -> 400ms per word
            val baseSecondsPerWord = 0.40f / speechRate.coerceIn(0.7f, 1.8f)

            var currentTime = 0.3f // small initial delay before audio starts
            val segments = mutableListOf<CaptionSegment>()

            rawSentences.forEachIndexed { sIndex, sentence ->
                val words = sentence.split(Regex("\\s+")).filter { it.isNotBlank() }
                if (words.isEmpty()) return@forEachIndexed

                val segStart = currentTime
                val wordSegments = mutableListOf<CaptionWord>()

                for (w in words) {
                    val wClean = w.replace(Regex("[^a-zA-Z0-9]"), "")
                    // Word duration depends on character length
                    val charFactor = (wClean.length.coerceIn(2, 12) / 5f)
                    var wDuration = (baseSecondsPerWord * (0.7f + 0.3f * charFactor)).coerceIn(0.18f, 0.95f)

                    // Extra pause for punctuation
                    if (w.endsWith(",") || w.endsWith(";") || w.endsWith(":")) {
                        wDuration += 0.22f / speechRate
                    } else if (w.endsWith(".") || w.endsWith("!") || w.endsWith("?")) {
                        wDuration += 0.40f / speechRate
                    }

                    val wStart = currentTime
                    val wEnd = wStart + wDuration
                    wordSegments.add(CaptionWord(word = w, startSec = wStart, endSec = wEnd))
                    currentTime = wEnd
                }

                // Sentence pause
                currentTime += (0.15f / speechRate)
                val segEnd = currentTime

                segments.add(
                    CaptionSegment(
                        index = sIndex,
                        fullText = sentence,
                        startSec = segStart,
                        endSec = segEnd,
                        words = wordSegments
                    )
                )
            }

            val totalDuration = currentTime + 0.5f
            return Pair(segments, totalDuration)
        }
    }
}
