package io.agora.transcription_widget_demo.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import com.lxj.xpopup.XPopup
import com.lxj.xpopup.impl.LoadingPopupView
import io.agora.rtc2.IRtcEngineEventHandler
import io.agora.transcription_widget_demo.R
import io.agora.transcription_widget_demo.agora.RtcManager
import io.agora.transcription_widget_demo.agora.RttManager
import io.agora.transcription_widget_demo.constants.Constants
import io.agora.transcription_widget_demo.databinding.ActivityTranscriptionBinding
import io.agora.transcription_widget_demo.ui.dialog.CopyDialog
import io.agora.transcription_widget_demo.utils.KeyCenter
import io.agora.transcription_widget_demo.utils.LogUtils
import io.agora.transcription_widget_demo.utils.ToastUtils
import io.agora.transcription_widget_demo.utils.Utils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

class TranscriptionActivity : AppCompatActivity(), RtcManager.RtcCallback, RttManager.RttCallback {
    companion object {
        const val TAG: String = Constants.TAG + "-TranscriptionActivity"
    }

    private lateinit var binding: ActivityTranscriptionBinding
    private var mLoadingPopup: LoadingPopupView? = null
    private val mCoroutineScope = CoroutineScope(Dispatchers.IO)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTranscriptionBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initData()
        initView()
    }

    override fun onResume() {
        super.onResume()
        isNetworkConnected();
    }

    private fun initData() {
        mLoadingPopup = XPopup.Builder(this@TranscriptionActivity)
            .hasBlurBg(true)
            .asLoading("正在加载中")

        RtcManager.setCallback(this)
        RttManager.setCallback(this)
    }

    private fun initView() {
        handleOnBackPressed()

        binding.toolbarTitle.text =
            buildString {
                append(if (RtcManager.getClientRole() == io.agora.rtc2.Constants.CLIENT_ROLE_BROADCASTER) "Host" else "Audience")
                append("-")
                append(RtcManager.getChannelId())
                append("-")
                append(KeyCenter.getUid())
            }

        binding.toolbarBackIcon.setOnClickListener { exit() }
        binding.toolbarBack.setOnClickListener { exit() }

        binding.allTranscriptBtn.setOnClickListener {
            CopyDialog.showCopyDialog(
                this,
                binding.transcriptSubtitleView.getAllTranscriptText(),
                object : CopyDialog.DialogCallback {
                    override fun onConfirm() {
                        runOnUiThread {
                            ToastUtils.showLongToast(
                                this@TranscriptionActivity,
                                this@TranscriptionActivity.resources.getString(
                                    R.string.copied
                                )
                            )
                        }
                    }
                })


        }

        binding.allTranslateBtn.setOnClickListener {
            CopyDialog.showCopyDialog(
                this,
                binding.transcriptSubtitleView.getAllTranslateText(),
                object : CopyDialog.DialogCallback {
                    override fun onConfirm() {
                        runOnUiThread {
                            ToastUtils.showLongToast(
                                this@TranscriptionActivity,
                                this@TranscriptionActivity.resources.getString(
                                    R.string.copied
                                )
                            )
                        }
                    }
                })
        }
    }


    private fun handleOnBackPressed() {
        val onBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val xPopup = XPopup.Builder(this@TranscriptionActivity)
                    .asConfirm("返回", "返回主界面", {
                        exit()
                    }, {})
                xPopup.show()
            }
        }
        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
    }

    override fun onStop() {
        super.onStop()
        binding.transcriptSubtitleView.clear()
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    private fun isNetworkConnected(): Boolean {
        val isConnect = Utils.isNetworkConnected(this)
        if (!isConnect) {
            LogUtils.d("Network is not connected")
            ToastUtils.showLongToast(this, "请连接网络!")
        }
        return isConnect
    }

    private fun exit() {
        mLoadingPopup?.show()
        RttManager.requestStopRttRecognize()
    }

    override fun onLeaveChannel(stats: IRtcEngineEventHandler.RtcStats) {
        LogUtils.d("TranscriptionActivity onLeaveChannel")
        runOnUiThread {
            mLoadingPopup?.dismiss()
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    override fun onStreamMessage(uid: Int, streamId: Int, data: ByteArray?) {
        binding.transcriptSubtitleView.pushMessageData(data, uid)
    }


    override fun onRttError(errorMessage: String) {
        LogUtils.d("TranscriptionActivity onRttError errorMessage:$errorMessage")
        runOnUiThread { RtcManager.leaveChannel() }
    }


    override fun onRttStop() {
        LogUtils.d("TranscriptionActivity onRttStop")
        runOnUiThread { RtcManager.leaveChannel() }
    }
}





