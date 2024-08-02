package io.agora.transcription_widget.internal.model

data class ListChangeItem(val position: Int, val type: Int) {
    companion object {
        const val TYPE_UNKNOWN = -1
        const val TYPE_INSERT = 0
        const val TYPE_REMOVE = 1
        const val TYPE_CHANGE = 2
        const val TYPE_UPDATE_ALL = 3
    }
}
