package io.agora.transcription_widget_demo.ui.dialog

import android.content.Context
import androidx.recyclerview.widget.LinearLayoutManager
import com.lxj.xpopup.XPopup
import com.lxj.xpopup.core.BottomPopupView
import io.agora.transcription_widget_demo.R
import io.agora.transcription_widget_demo.databinding.DialogMultiSelectBinding
import io.agora.transcription_widget_demo.ui.adapter.MultiSelectAdapter
import io.agora.transcription_widget_demo.utils.Utils

object MultiSelectDialog {
    fun showMultiSelectDialog(
        context: Context,
        items: List<String>,
        selectedItems: MutableSet<String>,
        multiSelectDialogCallback: MultiSelectDialogCallback,
        isMultiSelect: Boolean = true
    ) {
        XPopup.Builder(context)
            .popupHeight((Utils.getScreenHeight(context) * 0.8).toInt())
            .autoDismiss(false)
            .asCustom(object : BottomPopupView(context) {
                override fun getImplLayoutId(): Int {
                    return R.layout.dialog_multi_select;
                }

                override fun initPopupContent() {
                    super.initPopupContent()
                    val selectBinding = DialogMultiSelectBinding.bind(popupImplView)
                    selectBinding.recyclerView.layoutManager =
                        LinearLayoutManager(context)
                    selectBinding.recyclerView.adapter =
                        MultiSelectAdapter(items, selectedItems, isMultiSelect)
                    selectBinding.btnConfirm.setOnClickListener {
                        multiSelectDialogCallback.onConfirm(selectedItems)
                        dismiss()
                    }
                }
            })
            .show()
    }

    interface MultiSelectDialogCallback {
        fun onConfirm(selectedItems: MutableSet<String>) {

        }

    }
}