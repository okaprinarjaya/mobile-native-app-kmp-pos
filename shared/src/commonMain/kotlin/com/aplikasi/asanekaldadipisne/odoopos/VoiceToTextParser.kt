package com.aplikasi.asanekaldadipisne.odoopos

import kotlinx.coroutines.flow.StateFlow

interface VoiceToTextParser {
    val state: StateFlow<VoiceToTextState>
    fun startListening(languageCode: String = "id-ID")
    fun stopListening()
    fun reset()
}