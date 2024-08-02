package io.agora.transcription_widget.internal.model

data class TranscriptText(
    var text: String,
    var isFinal: Boolean,
    var durationMs: Int,
    var startMs: Int,
    var confidence: Double,
)
