package io.agora.transcription_widget_demo.ui

import android.Manifest
import android.content.Intent
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import com.lxj.xpopup.XPopup
import com.lxj.xpopup.impl.LoadingPopupView
import io.agora.rtc2.IRtcEngineEventHandler
import io.agora.transcription_widget_demo.agora.AppConfig
import io.agora.transcription_widget_demo.agora.RtcManager
import io.agora.transcription_widget_demo.agora.RttManager
import io.agora.transcription_widget_demo.constants.Constants
import io.agora.transcription_widget_demo.databinding.ActivityMainBinding
import io.agora.transcription_widget_demo.ui.dialog.SettingsDialog
import io.agora.transcription_widget_demo.utils.LogUtils
import io.agora.transcription_widget_demo.utils.ToastUtils
import io.agora.transcription_widget_demo.utils.Utils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions
import kotlin.system.exitProcess


class MainActivity : AppCompatActivity(), RtcManager.RtcCallback, RttManager.RttCallback,
    AppConfig.AppConfigCallback {
    companion object {
        const val TAG: String = Constants.TAG + "-MainActivity"
        const val MY_PERMISSIONS_REQUEST_CODE = 123
    }

    private lateinit var binding: ActivityMainBinding
    private var mLoadingPopup: LoadingPopupView? = null
    private val mCoroutineScope = CoroutineScope(Dispatchers.IO)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        checkPermissions()
        initData()
        initView()
    }

    override fun onResume() {
        super.onResume()
        isNetworkConnected();
    }

    private fun checkPermissions() {
        val permissions =
            arrayOf(Manifest.permission.RECORD_AUDIO)
        if (EasyPermissions.hasPermissions(this, *permissions)) {
            // 已经获取到权限，执行相应的操作
        } else {
            EasyPermissions.requestPermissions(
                this,
                "需要录音权限",
                MY_PERMISSIONS_REQUEST_CODE,
                *permissions
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {
        // 权限被授予，执行相应的操作
        LogUtils.d(TAG, "onPermissionsGranted requestCode:$requestCode perms:$perms")
    }

    fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
        LogUtils.d(TAG, "onPermissionsDenied requestCode:$requestCode perms:$perms")
        // 权限被拒绝，显示一个提示信息
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            // 如果权限被永久拒绝，可以显示一个对话框引导用户去应用设置页面手动授权
            AppSettingsDialog.Builder(this).build().show()
        }
    }

    private fun initData() {
        mLoadingPopup = XPopup.Builder(this@MainActivity)
            .hasBlurBg(true)
            .asLoading("正在加载中")

        initAppConfig();

        RtcManager.initRtcEngine(
            applicationContext,
            this
        )

        RttManager.initRttManager(this)
    }

    private fun initAppConfig() {
        AppConfig.initAppConfig(applicationContext)
        AppConfig.setCallback(this)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        RtcManager.setCallback(this)
        RttManager.setCallback(this)
    }

    private fun initView() {
        handleOnBackPressed()

        val versionName = applicationContext.packageManager.getPackageInfo(
            applicationContext.packageName,
            0
        ).versionName
        binding.versionTv.text = buildString {
            append("Demo Version: ")
            append(versionName)
        }

        updateAppConfigView()

        binding.joinAsHostBtn.setOnClickListener {
            if (binding.channelIdEt.text.toString().isEmpty()) {
                ToastUtils.showLongToast(this, "请输入频道号!")
                return@setOnClickListener
            }
            mLoadingPopup?.show()
            Utils.hideKeyboard(this, binding.channelIdEt)
            RtcManager.setChannelId(binding.channelIdEt.text.toString())
            RtcManager.joinChannel(io.agora.rtc2.Constants.CLIENT_ROLE_BROADCASTER)
        }

        binding.joinAsAudienceBtn.setOnClickListener {
            if (binding.channelIdEt.text.toString().isEmpty()) {
                ToastUtils.showLongToast(this, "请输入频道号!")
                return@setOnClickListener
            }
            mLoadingPopup?.show()
            Utils.hideKeyboard(this, binding.channelIdEt)
            RtcManager.setChannelId(binding.channelIdEt.text.toString())
            RtcManager.joinChannel(io.agora.rtc2.Constants.CLIENT_ROLE_AUDIENCE)
        }

        binding.toolbarSetting.setOnClickListener {
            SettingsDialog.showSettingsDialog(this)
        }
    }

    private fun updateAppConfigView() {
        binding.appConfigTv.text = buildString {
            append("转写目标语言: ")
            append(AppConfig.getTranscriptLanguages())
            append("\n")
            append("翻译目标语言: ")
            append(AppConfig.getTranslateLanguages())
            append("\n")
            append("环境名称: ")
            append(AppConfig.getServerConfig()?.name)
            append("\n")
            append("环境地址: ")
            append(AppConfig.getServerConfig()?.serverUrl)
            append("\n")
            append("TestIP: ")
            append(AppConfig.getServerConfig()?.testIp)
            append("\n")
            append("TestPort: ")
            append(AppConfig.getServerConfig()?.testPort)
            append("\n")
        }
    }

    private fun handleOnBackPressed() {
        val onBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val xPopup = XPopup.Builder(this@MainActivity)
                    .asConfirm("退出", "确认退出程序", {
                        exit()
                    }, {})
                xPopup.show()
            }
        }
        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
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
        LogUtils.destroy()
        finishAffinity()
        finish()
        exitProcess(0)
    }


    override fun onJoinChannelSuccess(channel: String, uid: Int, elapsed: Int) {
        RttManager.requestStartRttRecognize(RtcManager.getChannelId())
    }

    override fun onLeaveChannel(stats: IRtcEngineEventHandler.RtcStats) {
        runOnUiThread {
            mLoadingPopup?.dismiss()

        }
    }

    override fun onRttError(errorMessage: String) {
        LogUtils.d("MainActivity onRttError errorMessage:$errorMessage")
        runOnUiThread {
            RtcManager.leaveChannel()
            ToastUtils.showLongToast(this, errorMessage)
        }
    }

    override fun onRttStart(token: String, taskId: String) {
        LogUtils.d("MainActivity onRttStart token:$token taskId:$taskId")
        runOnUiThread {
            mLoadingPopup?.dismiss()
            val intent = Intent(this, TranscriptionActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onConfigUpdate() {
        LogUtils.d("MainActivity onConfigUpdate")
        updateAppConfigView()
    }

}





