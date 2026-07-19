package com.aplikasi.asanekaldadipisne.odoopos

data class VoiceToTextState(
    val spokenText: String = "",
    val isSpeaking: Boolean = false,
    val error: String? = null
)
