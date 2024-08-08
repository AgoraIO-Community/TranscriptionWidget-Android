package io.agora.transcription_widget.internal

import io.agora.transcription_widget.internal.constants.Constants
import io.agora.transcription_widget.internal.model.ListChangeItem
import io.agora.transcription_widget.internal.model.TranscriptText
import io.agora.transcription_widget.internal.model.TranscriptionData
import io.agora.transcription_widget.internal.model.TranslateText
import io.agora.transcription_widget.internal.utils.LogUtils
import io.agora.transcription_widget.speech2text.AgoraSpeech2TextProtoBuffer

object TranscriptSubtitleMachine {
    private val transcriptionDataList = mutableListOf<TranscriptionData>()

    fun handleMessageData(data: ByteArray?, uid: Int): ListChangeItem {
        val agoraSpeech2TextProtoBuffer = AgoraSpeech2TextProtoBuffer.Text.parseFrom(data)
        LogUtils.d("handleMessageData uid: ${agoraSpeech2TextProtoBuffer.uid} type: ${agoraSpeech2TextProtoBuffer.dataType}  textTs: ${agoraSpeech2TextProtoBuffer.textTs}")

        var positionIndex = transcriptionDataList.size - 1
        var transcriptionData: TranscriptionData? = null
        if (transcriptionDataList.isNotEmpty()) {
            for (i in transcriptionDataList.indices.reversed()) {
                if (Constants.MESSAGE_DATA_TYPE_TRANSCRIBE == agoraSpeech2TextProtoBuffer.dataType) {
                    if (transcriptionDataList[i].uid == uid) {
                        if (!transcriptionDataList[i].transcriptText.isFinal) {
                            transcriptionData = transcriptionDataList[i]
                        }
                        break
                    }
                } else if (Constants.MESSAGE_DATA_TYPE_TRANSLATE == agoraSpeech2TextProtoBuffer.dataType) {
                    if (agoraSpeech2TextProtoBuffer.textTs >= transcriptionDataList[i].startTs && agoraSpeech2TextProtoBuffer.textTs <= transcriptionDataList[i].endTs) {
                        transcriptionData = transcriptionDataList[i]
                        break
                    }
                }
                positionIndex--
            }
        }

        when (agoraSpeech2TextProtoBuffer.dataType) {
            Constants.MESSAGE_DATA_TYPE_TRANSCRIBE -> {
                agoraSpeech2TextProtoBuffer.wordsList.forEach {
                    LogUtils.d("handleMessageData text: ${it.text} isFinal: ${it.isFinal} confidence: ${it.confidence} startMs: ${it.startMs} durationMs: ${it.durationMs}")

                    if (null != transcriptionData) {
                        transcriptionData.transcriptText.text = it.text
                        transcriptionData.transcriptText.isFinal = it.isFinal
                        transcriptionData.transcriptText.durationMs = it.durationMs
                        transcriptionData.transcriptText.startMs = it.startMs
                        transcriptionData.transcriptText.confidence = it.confidence
                        transcriptionData.endTs = agoraSpeech2TextProtoBuffer.textTs

                        return ListChangeItem(positionIndex, ListChangeItem.TYPE_CHANGE)
                    } else {
                        if (it.text.isEmpty()) {
                            return ListChangeItem(0, ListChangeItem.TYPE_UNKNOWN)
                        }
                        val transcriptText = TranscriptText(
                            it.text,
                            it.isFinal,
                            it.durationMs,
                            it.startMs,
                            it.confidence
                        )
                        val newTranscriptionData = TranscriptionData(
                            uid,
                            agoraSpeech2TextProtoBuffer.textTs,
                            agoraSpeech2TextProtoBuffer.textTs,
                            transcriptText,
                            mutableListOf()
                        )
                        transcriptionDataList.add(newTranscriptionData)
                        return ListChangeItem(
                            transcriptionDataList.size - 1,
                            ListChangeItem.TYPE_INSERT
                        )
                    }
                }
            }

            Constants.MESSAGE_DATA_TYPE_TRANSLATE -> {
                agoraSpeech2TextProtoBuffer.transList.forEach {
                    var translateText =
                        buildString { it.textsList.forEach { item -> append(item) } }
                    LogUtils.d("handleMessageData transList texts:${translateText} isFinal: ${it.isFinal}  lang:${it.lang}")

                    if (it.lang.startsWith("ar-")) {
                        translateText = "\u200E$translateText"
                    }
                    if (null != transcriptionData) {
                        transcriptionData.translateTextList.forEach { existTranslateText ->
                            if (existTranslateText.lang == it.lang) {
                                existTranslateText.text = translateText
                                existTranslateText.isFinal = it.isFinal
                                return ListChangeItem(positionIndex, ListChangeItem.TYPE_CHANGE)
                            }
                        }
                        transcriptionData.translateTextList.add(
                            TranslateText(
                                translateText,
                                it.isFinal,
                                it.lang
                            )
                        )
                        transcriptionData.translateTextList.sortBy { translateTextItem -> translateTextItem.lang }

                        return ListChangeItem(positionIndex, ListChangeItem.TYPE_CHANGE)
                    } else {
                        LogUtils.e("handleMessageData type error")
                    }
                }
            }

            else -> {
                LogUtils.e("handleMessageData type error")
            }

        }


        return ListChangeItem(0, ListChangeItem.TYPE_UNKNOWN)
    }

    fun getTranscriptionDataList(): MutableList<TranscriptionData> {
        return transcriptionDataList
    }

    fun getAllTranscriptText(): String {
        return transcriptionDataList.joinToString(separator = "\n\n") { it.transcriptText.text }

    }

    fun getAllTranslateText(): String {
        val translateTextMap = mutableMapOf<String, String>()
        transcriptionDataList.forEach { transcriptionData ->
            transcriptionData.translateTextList.forEach { translateText ->
                if (translateTextMap.containsKey(translateText.lang)) {
                    translateTextMap[translateText.lang] =
                        translateTextMap[translateText.lang] + translateText.text
                } else {
                    translateTextMap[translateText.lang] = translateText.text
                }
            }
        }

        return translateTextMap.toSortedMap().entries.joinToString(separator = "\n\n") { it.value }
    }

    fun clear() {
        transcriptionDataList.clear()
    }
}