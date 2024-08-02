package io.agora.transcription_widget.internal.adapter

import android.graphics.Color
import android.graphics.Rect
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import io.agora.transcription_widget.R
import io.agora.transcription_widget.internal.model.TranscriptionData
import io.agora.transcription_widget.utils.Utils

class SubtitleAdapter : RecyclerView.Adapter<SubtitleAdapter.SubtitleViewHolder>() {
    private var items: MutableList<TranscriptionData>? = null

    var finalTextColor: Int = Color.BLACK
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    var nonFinalTextColor: Int = Color.GRAY
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    var textSize: Float = 15f
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    var textAreaBackgroundColor: Int = Color.argb((0.25f * 255).toInt(), 0, 0, 0)
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    var showTranscriptContent: Boolean = true
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SubtitleViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_subtitle, parent, false)
        return SubtitleViewHolder(view)
    }

    override fun onBindViewHolder(holder: SubtitleViewHolder, position: Int) {
        holder.bind(
            items?.get(position) ?: return,
            finalTextColor,
            nonFinalTextColor,
            textSize,
            textAreaBackgroundColor,
            showTranscriptContent
        )
    }

    override fun onBindViewHolder(holder: SubtitleViewHolder, position: Int, payloads: List<Any>) {
        holder.bind(
            items?.get(position) ?: return,
            finalTextColor,
            nonFinalTextColor,
            textSize,
            textAreaBackgroundColor,
            showTranscriptContent
        )
    }

    override fun getItemCount() = items?.size ?: 0

    fun setItems(items: MutableList<TranscriptionData>) {
        this.items = items
        notifyDataSetChanged()
    }

    class SubtitleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val subtitleLayout = itemView.findViewById<ViewGroup>(R.id.subtitleLayout)
        private val transcriptText: TextView = itemView.findViewById(R.id.transcriptText)

        fun bind(
            data: TranscriptionData,
            finalTextColor: Int,
            nonFinalTextColor: Int,
            textFontSize: Float,
            textAreaBackgroundColor: Int,
            showTranscriptContent: Boolean
        ) {
            //TransitionManager.beginDelayedTransition(itemView as ViewGroup)
            Utils.setRoundedBackground(subtitleLayout, textAreaBackgroundColor, 20f)

            if (data.translateTextList.isEmpty()) {
                for (i in 0 until subtitleLayout.childCount) {
                    val childView = subtitleLayout.getChildAt(i)
                    if (null != childView && childView.tag != null) {
                        subtitleLayout.removeView(childView)
                    }
                }
            }

            val isFinalTranscriptText = data.transcriptText.isFinal
            val textColor = if (isFinalTranscriptText) finalTextColor else nonFinalTextColor
            transcriptText.setTextSize(TypedValue.COMPLEX_UNIT_SP, textFontSize)
            transcriptText.setTextColor(textColor)
            transcriptText.text = data.transcriptText.text
            transcriptText.visibility = if (showTranscriptContent) View.VISIBLE else View.GONE


            for ((index, translateTextData) in data.translateTextList.withIndex()) {
                val isFinalTranslateText = translateTextData.isFinal
                val translateTextColor =
                    if (isFinalTranslateText) finalTextColor else nonFinalTextColor
                var isViewExist = false
                for (i in 0 until subtitleLayout.childCount) {
                    val childView = subtitleLayout.getChildAt(i)
                    if (null != childView && childView.tag == translateTextData.lang) {
                        isViewExist = true
                        if (childView is TextView) {
                            childView.text = translateTextData.text
                            childView.setTextSize(TypedValue.COMPLEX_UNIT_SP, textFontSize)
                            childView.setTextColor(translateTextColor)
                            break
                        }
                    }
                }

                if (!isViewExist) {
                    // 动态添加新的 TextView
                    val newTextView = TextView(itemView.context).apply {
                        layoutParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                        ).apply {
                            //topMargin = Utils.dpToPx(5, itemView.context)
                        }
                        val paddingBottomDp =
                            if (index == data.translateTextList.lastIndex) Utils.dpToPx(
                                8,
                                itemView.context
                            ) else Utils.dpToPx(
                                2,
                                itemView.context
                            )
                        setPadding(
                            Utils.dpToPx(8, itemView.context),
                            Utils.dpToPx(2, itemView.context),
                            Utils.dpToPx(8, itemView.context),
                            paddingBottomDp
                        )
                        setTextSize(TypedValue.COMPLEX_UNIT_SP, textFontSize)
                        text = translateTextData.text
                        setTextColor(translateTextColor)
                        tag = translateTextData.lang
                    }
                    subtitleLayout.addView(newTextView)
                }
            }
        }
    }

    class MyItemDecoration(private val space: Int) : RecyclerView.ItemDecoration() {

        override fun getItemOffsets(
            outRect: Rect,
            view: View,
            parent: RecyclerView,
            state: RecyclerView.State
        ) {
            outRect.left = 0
            outRect.right = 0
            outRect.bottom = 0
            outRect.top = space

            // 如果是第一个item，设置top间隔
            if (parent.getChildAdapterPosition(view) == 0) {
                outRect.top = 0
            }
        }
    }

}

