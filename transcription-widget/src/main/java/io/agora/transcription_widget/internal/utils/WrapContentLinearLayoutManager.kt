package io.agora.transcription_widget.internal.utils

import android.content.Context
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class WrapContentLinearLayoutManager(context: Context?) : LinearLayoutManager(context) {
    override fun onLayoutChildren(recycler: RecyclerView.Recycler, state: RecyclerView.State) {
        try {
            super.onLayoutChildren(recycler, state)
        } catch (e: Exception) {
            LogUtils.e("onLayoutChildren: $e")
        }
    }
}