package com.aplikasi.asanekaldadipisne.odoopos

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class AndroidVoiceToTextParser(private val context: Context) : VoiceToTextParser {

    private val _state = MutableStateFlow(VoiceToTextState())
    override val state: StateFlow<VoiceToTextState> = _state.asStateFlow()

    private val appContext = context.applicationContext
    private var recognizer: SpeechRecognizer? = null
    private var isCurrentlyListening = false

    private fun getOrCreateRecognizer(): SpeechRecognizer {
        val current = recognizer
        if (current != null) return current

        return SpeechRecognizer.createSpeechRecognizer(appContext).also { newRecognizer ->
            newRecognizer.setRecognitionListener(createRecognitionListener())
            recognizer = newRecognizer
        }
    }

    override fun startListening(languageCode: String) {
        if (!SpeechRecognizer.isRecognitionAvailable(appContext)) {
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

        Handler(Looper.getMainLooper()).post {
            // 🛑 Guard: Cegah eksekusi ganda jika dipanggil cepat bersamaan
            if (isCurrentlyListening) return@post

            try {
                isCurrentlyListening = true
                _state.update { it.copy(isSpeaking = true, error = null) }

                val speechRecognizer = getOrCreateRecognizer()
                speechRecognizer.startListening(intent)
                Log.i("AndroidVoiceToText", "Start listening executed successfully")
            } catch (e: Exception) {
                isCurrentlyListening = false
                Log.e("AndroidVoiceToText", "Failed to start listening: ${e.message}")
                resetSpeechRecognizer()
                _state.update {
                    it.copy(
                        error = "Gagal memulai perekaman: ${e.message}",
                        isSpeaking = false
                    )
                }
            }
        }
    }

    override fun stopListening() {
        Log.i("AndroidVoiceToText", "Stop listening request received")
        Handler(Looper.getMainLooper()).post {
            try {
                if (isCurrentlyListening) {
                    recognizer?.stopListening()
                } else {
                    recognizer?.cancel()
                }
            } catch (e: Exception) {
                Log.e("AndroidVoiceToText", "Error stopping listener: ${e.message}")
            } finally {
                isCurrentlyListening = false
            }
        }
    }

    override fun reset() {
        _state.update { VoiceToTextState() }
    }

    override fun dispose() {
        Handler(Looper.getMainLooper()).post {
            resetSpeechRecognizer()
        }
    }

    private fun resetSpeechRecognizer() {
        try {
            recognizer?.cancel()
        } catch (e: Exception) {
            Log.w("AndroidVoiceToText", "Cancel failed during reset: ${e.message}")
        }

        try {
            Log.i("AndroidVoiceToText", "Resetting & destroying SpeechRecognizer instance")
            recognizer?.destroy()
        } catch (e: Exception) {
            Log.e("AndroidVoiceToText", "Failed to destroy old recognizer: ${e.message}")
        } finally {
            recognizer = null
            isCurrentlyListening = false
        }
    }

    private fun createRecognitionListener() = object : RecognitionListener {
        override fun onReadyForSpeech(params: Bundle?) {}
        override fun onBeginningOfSpeech() {}
        override fun onRmsChanged(rmsdB: Float) {}
        override fun onBufferReceived(buffer: ByteArray?) {}

        override fun onEndOfSpeech() {
            isCurrentlyListening = false
            _state.update { it.copy(isSpeaking = false) }
        }

        override fun onError(error: Int) {
            isCurrentlyListening = false
            Log.e("AndroidVoiceToText", "Error during recognition, error code = $error")

            when (error) {
                SpeechRecognizer.ERROR_AUDIO,
                SpeechRecognizer.ERROR_SERVER,
                SpeechRecognizer.ERROR_RECOGNIZER_BUSY,
                SpeechRecognizer.ERROR_SERVER_DISCONNECTED -> {
                    Log.w(
                        "AndroidVoiceToText",
                        "Fatal/Binder issue (code $error). Executing self-healing reset..."
                    )
                    resetSpeechRecognizer()
                }

                SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> {
                    Log.e("AndroidVoiceToText", "Missing RECORD_AUDIO permission!")
                    try {
                        recognizer?.cancel()
                    } catch (_: Exception) {
                    }
                }

                else -> {
                    Log.w(
                        "AndroidVoiceToText",
                        "Transient error (code $error). Canceling current session, keeping instance alive."
                    )
                    try {
                        recognizer?.cancel()
                    } catch (e: Exception) {
                        Log.e("AndroidVoiceToText", "Failed to cancel recognizer: ${e.message}")
                    }
                }
            }

            val errorMessage = when (error) {
                SpeechRecognizer.ERROR_NO_MATCH -> "Suara tidak terdengar jelas"
                SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "Tidak ada suara masuk"
                SpeechRecognizer.ERROR_AUDIO -> "Masalah pada mikrofon hardware"
                SpeechRecognizer.ERROR_NETWORK, SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Koneksi internet bermasalah"
                SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Layanan suara sedang sibuk"
                SpeechRecognizer.ERROR_SERVER_DISCONNECTED -> "Layanan suara terputus, memperbarui..."
                else -> null
            }

            Log.i("AndroidVoiceToText", errorMessage ?: "else -> null")

            _state.update { it.copy(error = errorMessage, isSpeaking = false) }
        }

        override fun onResults(results: Bundle?) {
            isCurrentlyListening = false
            results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.getOrNull(0)
                ?.let { text ->
                    _state.update { it.copy(spokenText = text, isSpeaking = false) }
                }
        }

        override fun onPartialResults(partialResults: Bundle?) {
            partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                ?.getOrNull(0)?.let { text ->
                    _state.update { it.copy(spokenText = text) }
                }
        }

        override fun onEvent(eventType: Int, params: Bundle?) {}
    }
}