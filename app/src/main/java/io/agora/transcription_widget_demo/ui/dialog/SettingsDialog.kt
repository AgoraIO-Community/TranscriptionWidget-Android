package io.agora.transcription_widget_demo.ui.dialog

import android.content.Context
import android.widget.TextView
import com.lxj.xpopup.XPopup
import com.lxj.xpopup.core.BottomPopupView
import io.agora.transcription_widget_demo.R
import io.agora.transcription_widget_demo.agora.AppConfig
import io.agora.transcription_widget_demo.databinding.DialogSettingsBinding
import io.agora.transcription_widget_demo.utils.Utils

object SettingsDialog {
    fun showSettingsDialog(context: Context) {
        XPopup.Builder(context)
            .hasBlurBg(true)
            .autoDismiss(false)
            .popupHeight((Utils.getScreenHeight(context) * 0.9).toInt())
            .asCustom(object : BottomPopupView(context) {

                override fun getImplLayoutId(): Int {
                    return R.layout.dialog_settings;
                }

                override fun initPopupContent() {
                    super.initPopupContent()
                    val binding = DialogSettingsBinding.bind(popupImplView)

                    binding.okBtn.setOnClickListener {
                        dismiss()
                    }

                    updateView(
                        binding.transcriptLanguageValueTv,
                        binding.translateLanguageValueTv,
                        binding.serverEnvValueTv
                    )

                    binding.transcriptLanguageLayout.setOnClickListener {
                        val items = mutableListOf<String>()
                        AppConfig.getSupportLanguages().forEach {
                            items.add(it.code)
                        }

                        val selectedItems = mutableSetOf<String>()
                        AppConfig.getTranscriptLanguages().forEach {
                            selectedItems.add(it)
                        }

                        MultiSelectDialog.showMultiSelectDialog(
                            context,
                            items,
                            selectedItems,
                            object : MultiSelectDialog.MultiSelectDialogCallback {
                                override fun onConfirm(selectedItems: MutableSet<String>) {
                                    AppConfig.setTranscriptLanguages(selectedItems.toList())
                                    updateView(
                                        binding.transcriptLanguageValueTv,
                                        binding.translateLanguageValueTv,
                                        binding.serverEnvValueTv
                                    )
                                }
                            }, false
                        )
                    }

                    binding.translateLanguageLayout.setOnClickListener {
                        val items = mutableListOf<String>()
                        AppConfig.getSupportLanguages().forEach {
                            items.add(it.code)
                        }

                        val selectedItems = mutableSetOf<String>()
                        AppConfig.getTranslateLanguages().forEach {
                            selectedItems.add(it)
                        }

                        MultiSelectDialog.showMultiSelectDialog(
                            context,
                            items,
                            selectedItems,
                            object : MultiSelectDialog.MultiSelectDialogCallback {
                                override fun onConfirm(selectedItems: MutableSet<String>) {
                                    AppConfig.setTranslateLanguages(selectedItems.toList())
                                    updateView(
                                        binding.transcriptLanguageValueTv,
                                        binding.translateLanguageValueTv,
                                        binding.serverEnvValueTv
                                    )
                                }
                            }
                        )


                    }

                    binding.serverEnvLayout.setOnClickListener {
                        val items = mutableListOf<String>()
                        AppConfig.getSupportServerConfig().forEach {
                            items.add(it.name + "\n" + it.serverUrl)
                        }

                        val selectedItems = mutableSetOf<String>()
                        AppConfig.getServerConfig()?.let {
                            selectedItems.add(it.name + "\n" + it.serverUrl)
                        }

                        MultiSelectDialog.showMultiSelectDialog(
                            context,
                            items,
                            selectedItems,
                            object : MultiSelectDialog.MultiSelectDialogCallback {
                                override fun onConfirm(selectedItems: MutableSet<String>) {
                                    AppConfig.getSupportServerConfig().forEach {
                                        if (selectedItems.contains(it.name + "\n" + it.serverUrl)) {
                                            AppConfig.setServerConfig(it)
                                            updateView(
                                                binding.transcriptLanguageValueTv,
                                                binding.translateLanguageValueTv,
                                                binding.serverEnvValueTv
                                            )
                                        }
                                    }
                                }
                            }, false
                        )

                    }

                }

            })
            .show();
    }

    private fun updateView(
        transcriptLanguageValueTv: TextView,
        translateLanguageValueTv: TextView,
        serverEnvValueTv: TextView
    ) {
        transcriptLanguageValueTv.text = buildString {
            AppConfig.getTranscriptLanguages().forEach {
                append(it)
                append("/")
            }
        }.dropLast(1)

        translateLanguageValueTv.text = buildString {
            AppConfig.getTranslateLanguages().forEach {
                append(it)
                append("/")
            }
        }.dropLast(1)

        serverEnvValueTv.text = AppConfig.getServerConfig()?.name
    }
}