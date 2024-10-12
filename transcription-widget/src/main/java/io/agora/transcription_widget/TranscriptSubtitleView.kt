package io.agora.transcription_widget

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.recyclerview.widget.RecyclerView
import io.agora.transcription_widget.internal.TranscriptSubtitleMachine
import io.agora.transcription_widget.internal.adapter.SubtitleAdapter
import io.agora.transcription_widget.internal.model.ListChangeItem
import io.agora.transcription_widget.internal.utils.LogUtils
import io.agora.transcription_widget.internal.utils.WrapContentLinearLayoutManager
import io.agora.transcription_widget.utils.Utils

class TranscriptSubtitleView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val recyclerView: RecyclerView
    private val adapter: SubtitleAdapter
    private val handler = Handler(Looper.getMainLooper())
    private var isDestroyed = false


    //The color of text, when the recognized state is final.
    var finalTextColor: Int = Color.BLACK
        set(value) {
            field = value
            adapter.finalTextColor = value
        }

    //The color of text, when the recognized state is non final.
    var nonFinalTextColor: Int = Color.GRAY
        set(value) {
            field = value
            adapter.nonFinalTextColor = value
        }

    //The size of text. include TranscriptContent and TranslateContent.
    var textSize: Float = resources.getDimension(R.dimen.subtitle_text_size)
        set(value) {
            field = value
            val textSizeSp = value / resources.displayMetrics.scaledDensity
            adapter.textSize = textSizeSp
        }

    //The background color of text area.
    var textAreaBackgroundColor: Int = Color.argb((0.25f * 255).toInt(), 0, 0, 0)
        set(value) {
            field = value
            adapter.textAreaBackgroundColor = value
        }

    //Show or hide the transcript content.
    var showTranscriptContent: Boolean = true
        set(value) {
            field = value
            adapter.showTranscriptContent = value
        }

    init {
        LogUtils.enableLog(context, true, true, "")

        recyclerView = RecyclerView(context).apply {
            layoutManager = WrapContentLinearLayoutManager(context)
        }

        // 初始化适配器
        adapter = SubtitleAdapter()
        adapter.setItems(
            TranscriptSubtitleMachine.getTranscriptionDataList()
        )
        recyclerView.adapter = adapter
        recyclerView.addItemDecoration(
            SubtitleAdapter.MyItemDecoration(
                Utils.dpToPx(10, context)
            )
        )

        // 将 RecyclerView 添加到 FrameLayout
        addView(
            recyclerView,
            LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
        )

        // 读取自定义属性
        context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.TranscriptSubtitleView,
            defStyleAttr,
            0
        ).apply {
            try {
                finalTextColor =
                    getColor(R.styleable.TranscriptSubtitleView_finalTextColor, finalTextColor)
                adapter.finalTextColor = finalTextColor


                nonFinalTextColor = getColor(
                    R.styleable.TranscriptSubtitleView_nonFinalTextColor,
                    nonFinalTextColor
                )
                adapter.nonFinalTextColor = nonFinalTextColor

                textSize = getDimension(R.styleable.TranscriptSubtitleView_textSize, textSize)
                val textSizeSp = textSize / resources.displayMetrics.scaledDensity
                adapter.textSize = textSizeSp

                textAreaBackgroundColor = getColor(
                    R.styleable.TranscriptSubtitleView_textAreaBackgroundColor,
                    textAreaBackgroundColor
                )
                adapter.textAreaBackgroundColor = textAreaBackgroundColor

                showTranscriptContent = getBoolean(
                    R.styleable.TranscriptSubtitleView_showTranscriptContent,
                    showTranscriptContent
                )
                adapter.showTranscriptContent = showTranscriptContent
            } finally {
                recycle()
            }
        }
    }


    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        LogUtils.i("TranscriptSubtitleView onAttachedToWindow")
        isDestroyed = false
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        LogUtils.i("TranscriptSubtitleView onDetachedFromWindow")
        TranscriptSubtitleMachine.clear()
        isDestroyed = true
    }

    /**
     * Pushes message data received from the STT (Speech-to-Text) server to processing.
     * @param data The raw data packet containing the message, received from RTC data Stream.
     */
    fun pushMessageData(data: ByteArray?) {
        if (isDestroyed) {
            LogUtils.i("TranscriptSubtitleView is destroyed, can't pushMessageData.")
            return
        }
        val item = TranscriptSubtitleMachine.handleMessageData(data)
        invalidate(item)
    }

    private fun invalidate(item: ListChangeItem) {
        LogUtils.d("TranscriptSubtitleView invalidate type:${item.type} position:${item.position}")
        if (isDestroyed) {
            LogUtils.i("TranscriptSubtitleView is destroyed, can't invalidate.")
            return
        }
        if (item.position < 0 || (item.position != 0 && item.position >= adapter.itemCount)) {
            return
        }
        handler.post {
            when (item.type) {
                ListChangeItem.TYPE_INSERT -> {
                    adapter.notifyItemInserted(item.position)
                }

                ListChangeItem.TYPE_REMOVE -> {
                    adapter.notifyItemRemoved(item.position)
                }

                ListChangeItem.TYPE_CHANGE -> {
                    adapter.notifyItemChanged(item.position, Bundle())
                }

                ListChangeItem.TYPE_UPDATE_ALL -> {
                    adapter.notifyDataSetChanged()
                }

                else -> {
                    LogUtils.i("invalidate type:${item.type} is invalid.")
                }
            }
            if (adapter.itemCount >= 1) {
                recyclerView.smoothScrollToPosition(adapter.itemCount - 1)
            }

            invalidate()  // 重绘整个视图
            //requestLayout()  // 如果尺寸发生变化，请求重新布局
        }
    }

    /**
     * @return The current all text of transcript.

     */
    fun getAllTranscriptText(): String {
        LogUtils.d("TranscriptSubtitleView getAllTranscriptText")
        return TranscriptSubtitleMachine.getAllTranscriptText()
    }


    /**
     * @return The current all text of translation.
     */
    fun getAllTranslateText(): String {
        LogUtils.d("TranscriptSubtitleView getAllTranslateText")
        return TranscriptSubtitleMachine.getAllTranslateText()
    }


    /*
    * Clear all data, and the view will be empty.
     */
    fun clear() {
        LogUtils.d("TranscriptSubtitleView clear")
        TranscriptSubtitleMachine.clear()
        invalidate(ListChangeItem(0, ListChangeItem.TYPE_UPDATE_ALL))
    }
}