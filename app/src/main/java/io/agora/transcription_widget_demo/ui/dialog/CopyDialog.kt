package io.agora.transcription_widget_demo.ui.dialog

import android.content.Context
import com.lxj.xpopup.XPopup
import com.lxj.xpopup.core.BottomPopupView
import io.agora.transcription_widget_demo.R
import io.agora.transcription_widget_demo.databinding.DialogCopyBinding
import io.agora.transcription_widget_demo.utils.Utils

object CopyDialog {
    fun showCopyDialog(
        context: Context,
        content: String,
        callback: DialogCallback
    ) {
        XPopup.Builder(context)
            .popupHeight((Utils.getScreenHeight(context) * 0.9).toInt())
            .autoDismiss(false)
            .asCustom(object : BottomPopupView(context) {
                override fun getImplLayoutId(): Int {
                    return R.layout.dialog_copy;
                }

                override fun initPopupContent() {
                    super.initPopupContent()
                    val binding = DialogCopyBinding.bind(popupImplView)
                    binding.contentTv.text = content
                    binding.copyBtn.setOnClickListener {
                        Utils.copyToClipboard(context, content)
                        callback.onConfirm()
                        dismiss()
                    }

                }
            })
            .show()
    }

    interface DialogCallback {
        fun onConfirm() {

        }

    }
}