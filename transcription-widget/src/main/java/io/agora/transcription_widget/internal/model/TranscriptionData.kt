package io.agora.transcription_widget.internal.model

data class TranscriptionData(
    val uid: Long,
    val startTs: Long,
    var endTs: Long,
    var transcriptText: TranscriptText,
    var translateTextList: MutableList<TranslateText>
)
