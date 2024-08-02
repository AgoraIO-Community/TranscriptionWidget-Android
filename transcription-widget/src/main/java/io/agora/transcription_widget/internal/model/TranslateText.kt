package io.agora.transcription_widget.internal.model

data class TranslateText(
    var text: String,
    var isFinal: Boolean,
    val lang: String
)
