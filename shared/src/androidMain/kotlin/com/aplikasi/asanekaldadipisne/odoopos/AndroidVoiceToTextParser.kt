package com.aplikasi.asanekaldadipisne.odoopos

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class AndroidVoiceToTextParser(private val context: Context) : VoiceToTextParser {

    private val _state = MutableStateFlow(VoiceToTextState())

    override val state: StateFlow<VoiceToTextState> = _state.asStateFlow()

    private val recognizer: SpeechRecognizer by lazy {
        SpeechRecognizer.createSpeechRecognizer(context)
    }

    override fun startListening(languageCode: String) {
        // Reset state teks sebelumnya begitu mulai rekaman baru
        _state.update { VoiceToTextState(isSpeaking = true) }

        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            _state.update {
                it.copy(
                    error = "Speech recognition tidak tersedia di perangkat ini",
                    isSpeaking = false
                )
            }
            return
        }

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH
            )
            putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, true)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, languageCode)
            putExtra(
                RecognizerIntent.EXTRA_PARTIAL_RESULTS,
                true
            )
        }

        recognizer.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {}
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}

            override fun onEndOfSpeech() {
                _state.update { it.copy(isSpeaking = false) }
            }

            override fun onError(error: Int) {
                // Kode error 7 biasanya terjadi jika user diam saja saat menekan tombol
                _state.update { it.copy(error = "Error code: $error", isSpeaking = false) }
            }

            override fun onResults(results: Bundle?) {
                // Dipicu saat tombol DILEPAS dan pemrosesan suara final selesai
                results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.getOrNull(0)
                    ?.let { text ->
                        _state.update { it.copy(spokenText = text, isSpeaking = false) }
                    }
            }

            override fun onPartialResults(partialResults: Bundle?) {
                // Dipicu secara real-time SELAMA tombol masih ditahan dan user sedang bicara
                partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    ?.getOrNull(0)?.let { text ->
                        _state.update { it.copy(spokenText = text) }
                    }
            }

            override fun onEvent(eventType: Int, params: Bundle?) {}
        })

        recognizer.startListening(intent)
    }

    override fun stopListening() {
        recognizer.stopListening()
    }

    override fun reset() {
        _state.update { VoiceToTextState() }
    }
}