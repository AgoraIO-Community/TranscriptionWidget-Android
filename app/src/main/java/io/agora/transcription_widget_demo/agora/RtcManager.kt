package io.agora.transcription_widget_demo.agora

import android.content.Context
import io.agora.rtc2.ChannelMediaOptions
import io.agora.rtc2.Constants
import io.agora.rtc2.IAudioFrameObserver
import io.agora.rtc2.IRtcEngineEventHandler
import io.agora.rtc2.RtcEngine
import io.agora.rtc2.RtcEngineConfig
import io.agora.rtc2.audio.AudioParams
import io.agora.transcription_widget_demo.utils.KeyCenter
import io.agora.transcription_widget_demo.utils.LogUtils
import io.agora.transcription_widget_demo.utils.Utils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.text.SimpleDateFormat
import java.util.Locale

object RtcManager : IAudioFrameObserver {
    private var mRtcEngine: RtcEngine? = null
    private var mCallback: RtcCallback? = null
    private var mChannelId: String = ""
    private var mTime: String = ""
    private const val SAVE_AUDIO_RECORD_PCM = false
    private var mClientRole = Constants.CLIENT_ROLE_BROADCASTER

    fun setCallback(rtcCallback: RtcCallback) {
        mCallback = rtcCallback
    }

    fun initRtcEngine(context: Context, rtcCallback: RtcCallback) {
        mCallback = rtcCallback
        try {
            LogUtils.d("RtcEngine version:" + RtcEngine.getSdkVersion())
            val rtcEngineConfig = RtcEngineConfig()
            rtcEngineConfig.mContext = context
            rtcEngineConfig.mAppId = AppConfig.getServerConfig()?.appId
            rtcEngineConfig.mChannelProfile = Constants.CHANNEL_PROFILE_LIVE_BROADCASTING
            rtcEngineConfig.mEventHandler = object : IRtcEngineEventHandler() {
                override fun onJoinChannelSuccess(channel: String, uid: Int, elapsed: Int) {
                    LogUtils.d("onJoinChannelSuccess channel:$channel uid:$uid elapsed:$elapsed")
                    mCallback?.onJoinChannelSuccess(channel, uid, elapsed)
                }

                override fun onLeaveChannel(stats: RtcStats) {
                    LogUtils.d("onLeaveChannel")
                    mCallback?.onLeaveChannel(stats)
                }

                override fun onLocalAudioStateChanged(state: Int, error: Int) {
                    super.onLocalAudioStateChanged(state, error)
                    LogUtils.d("onLocalAudioStateChanged state:$state error:$error")
                    if (Constants.LOCAL_AUDIO_STREAM_STATE_RECORDING == state) {
                        mCallback?.onUnMuteSuccess()
                    } else if (Constants.LOCAL_AUDIO_STREAM_STATE_STOPPED == state) {
                        mCallback?.onMuteSuccess()
                    }
                }

                override fun onAudioVolumeIndication(
                    speakers: Array<out AudioVolumeInfo>?,
                    totalVolume: Int
                ) {
                    super.onAudioVolumeIndication(speakers, totalVolume)
                    mCallback?.onAudioVolumeIndication(speakers, totalVolume)
                }

                override fun onStreamMessage(uid: Int, streamId: Int, data: ByteArray?) {
                    super.onStreamMessage(uid, streamId, data)
                    //LogUtils.d("onStreamMessage uid:$uid streamId:$streamId data:${data?.size}")
                    mCallback?.onStreamMessage(uid, streamId, data)
                }
            }
            rtcEngineConfig.mAudioScenario = Constants.AUDIO_SCENARIO_CHORUS
            mRtcEngine = RtcEngine.create(rtcEngineConfig)
            LogUtils.d("mRtcEngine native handler:${mRtcEngine?.nativeHandle}")

            mRtcEngine?.setParameters("{\"rtc.enable_debug_log\":true}")

            mRtcEngine?.enableAudio()
            mRtcEngine?.disableVideo()

            mRtcEngine?.setDefaultAudioRoutetoSpeakerphone(true)

//            mRtcEngine?.setRecordingAudioFrameParameters(
//                16000,
//                1,
//                Constants.RAW_AUDIO_FRAME_OP_MODE_READ_ONLY,
//                640
//            )

            //min 50ms
//            mRtcEngine?.enableAudioVolumeIndication(
//                50,
//                3,
//                true
//            )
            LogUtils.d("initRtcEngine success")
        } catch (e: Exception) {
            e.printStackTrace()
            LogUtils.e("initRtcEngine error:" + e.message)
        }
    }

    fun joinChannel(clientRole: Int) {
        LogUtils.d("RtcManager joinChannel clientRole:$clientRole")
        try {
            mClientRole = clientRole
            if (mChannelId.isEmpty()) {
                mChannelId = Utils.getCurrentDateStr("yyyyMMddHHmmss") + Utils.getRandomString(2)
            }
            val ret = mRtcEngine?.joinChannel(
                if (AppConfig.getServerConfig()?.appCert.isNullOrEmpty()) "" else
                    KeyCenter.getRtcToken(
                        mChannelId,
                        KeyCenter.getUid(),
                        AppConfig.getServerConfig()?.appId ?: "",
                        AppConfig.getServerConfig()?.appCert ?: ""
                    ),
                mChannelId,
                KeyCenter.getUid(),
                object : ChannelMediaOptions() {
                    init {
                        publishMicrophoneTrack = true
                        autoSubscribeAudio = true
                        clientRoleType = mClientRole
                    }
                })
            LogUtils.d("joinChannel ret:$ret")
        } catch (e: Exception) {
            e.printStackTrace()
            LogUtils.e("joinChannel error:" + e.message)
        }
    }

    override fun onRecordAudioFrame(
        channelId: String?,
        type: Int,
        samplesPerChannel: Int,
        bytesPerSample: Int,
        channels: Int,
        samplesPerSec: Int,
        buffer: ByteBuffer?,
        renderTimeMs: Long,
        avsync_type: Int
    ): Boolean {
        CoroutineScope(Dispatchers.IO).launch {
            val length = buffer!!.remaining()
            val origin = ByteArray(length)
            buffer[origin]
            buffer.flip()
            if (SAVE_AUDIO_RECORD_PCM) {
                try {
                    val fos = FileOutputStream(
                        "${Utils.getContext().externalCacheDir?.path}/audio_" + mTime + ".pcm",
                        true
                    )
                    fos.write(origin)
                    fos.close()
                } catch (e: java.lang.Exception) {
                    e.printStackTrace()
                }
            }
        }
        return true
    }

    override fun onPlaybackAudioFrame(
        channelId: String?,
        type: Int,
        samplesPerChannel: Int,
        bytesPerSample: Int,
        channels: Int,
        samplesPerSec: Int,
        buffer: ByteBuffer?,
        renderTimeMs: Long,
        avsync_type: Int
    ): Boolean {
        return true
    }

    override fun onMixedAudioFrame(
        channelId: String?,
        type: Int,
        samplesPerChannel: Int,
        bytesPerSample: Int,
        channels: Int,
        samplesPerSec: Int,
        buffer: ByteBuffer?,
        renderTimeMs: Long,
        avsync_type: Int
    ): Boolean {
        return true
    }

    override fun onEarMonitoringAudioFrame(
        type: Int,
        samplesPerChannel: Int,
        bytesPerSample: Int,
        channels: Int,
        samplesPerSec: Int,
        buffer: ByteBuffer?,
        renderTimeMs: Long,
        avsync_type: Int
    ): Boolean {
        return true
    }

    //4.1.1.24
    override fun onPlaybackAudioFrameBeforeMixing(
        channelId: String?,
        userId: Int,
        type: Int,
        samplesPerChannel: Int,
        bytesPerSample: Int,
        channels: Int,
        samplesPerSec: Int,
        buffer: ByteBuffer?,
        renderTimeMs: Long,
        avsync_type: Int
    ): Boolean {
        return true
    }

    //4.1.1.24
    override fun onPublishAudioFrame(
        channelId: String?,
        type: Int,
        samplesPerChannel: Int,
        bytesPerSample: Int,
        channels: Int,
        samplesPerSec: Int,
        buffer: ByteBuffer?,
        renderTimeMs: Long,
        avsync_type: Int
    ): Boolean {
        return true
    }


    override fun getObservedAudioFramePosition(): Int {
        return 0
    }

    override fun getRecordAudioParams(): AudioParams {
        return AudioParams(0, 0, 0, 0)
    }

    override fun getPlaybackAudioParams(): AudioParams {
        return AudioParams(0, 0, 0, 0)
    }

    override fun getMixedAudioParams(): AudioParams {
        return AudioParams(0, 0, 0, 0)
    }

    override fun getEarMonitoringAudioParams(): AudioParams {
        return AudioParams(0, 0, 0, 0)
    }

    //4.1.1.24
    override fun getPublishAudioParams(): AudioParams {
        return AudioParams(0, 0, 0, 0)
    }


    fun mute(enable: Boolean) {
        val ret = mRtcEngine?.enableLocalAudio(!enable)
        LogUtils.d("mute enable:$enable ret:$ret")
        if (SAVE_AUDIO_RECORD_PCM) {
            if (!enable) {
                val format = SimpleDateFormat("yyyy-MM-dd-HH-mm-ss", Locale.getDefault())
                mTime = format.format(System.currentTimeMillis())
            }
        }
    }

    fun leaveChannel() {
        LogUtils.d("RtcManager leaveChannel")
        mRtcEngine?.leaveChannel()
    }

    fun destroy() {
        LogUtils.d("RtcManager destroy")
        RtcEngine.destroy()
    }

    fun setClientRole(clientRole: Int) {
        mClientRole = clientRole
        mRtcEngine?.setClientRole(mClientRole)
    }

    fun getClientRole(): Int {
        return mClientRole
    }


    fun getChannelId(): String {
        return mChannelId
    }

    fun setChannelId(channelId: String) {
        mChannelId = channelId
    }

    fun getRtcEngine(): RtcEngine? {
        return mRtcEngine
    }

    interface RtcCallback {
        fun onJoinChannelSuccess(channel: String, uid: Int, elapsed: Int) {}
        fun onLeaveChannel(stats: IRtcEngineEventHandler.RtcStats) {}
        fun onMuteSuccess() {

        }

        fun onUnMuteSuccess() {

        }

        fun onAudioVolumeIndication(
            speakers: Array<out IRtcEngineEventHandler.AudioVolumeInfo>?,
            totalVolume: Int
        ) {

        }

        fun onStreamMessage(uid: Int, streamId: Int, data: ByteArray?) {

        }
    }
}
