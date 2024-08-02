package io.agora.transcription_widget_demo.agora

import io.agora.transcription_widget_demo.constants.Constants
import io.agora.transcription_widget_demo.net.NetworkClient
import io.agora.transcription_widget_demo.utils.LogUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

object RttManager {
    private var mToken = ""
    private var mTaskId = ""
    private var mCallback: RttCallback? = null

    fun setCallback(callback: RttCallback) {
        mCallback = callback
    }

    fun initRttManager(rttCallback: RttCallback) {
        mCallback = rttCallback
        LogUtils.d("initRttManager success")
    }

    fun requestStartRttRecognize(channelId: String) {
        LogUtils.d("requestStartRttRecognize channelId:$channelId")
        if (AppConfig.getServerConfig() == null) {
            LogUtils.i("buildToken AppConfig.getServerConfig() == null")
            mCallback?.onRttError("AppConfig.getServerConfig() == null")
            return
        }
        CoroutineScope(Dispatchers.IO).launch {
            buildToken(channelId)
        }
    }

    private fun buildToken(channelId: String) {
        val url =
            AppConfig.getServerConfig()?.serverUrl + "/v1/projects/" + AppConfig.getServerConfig()?.appId + "/rtsc/speech-to-text/builderTokens"

        val headers = mapOf("Content-Type" to "application/json")

        val bodyJson = JSONObject()
        bodyJson.put("instanceId", channelId)
        bodyJson.put("devicePlatform", Constants.ANDROID)
        bodyJson.put("testIp", AppConfig.getServerConfig()?.testIp)
        bodyJson.put("testPort", AppConfig.getServerConfig()?.testPort)

        NetworkClient.sendHttpsRequest(
            url,
            headers,
            bodyJson.toString(),
            NetworkClient.Method.POST,
            object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    LogUtils.e("buildToken onFailure e:$e")
                    mCallback?.onRttError("buildToken onFailure e:$e")
                }

                override fun onResponse(call: Call, response: Response) {
                    val responseStr = response.body?.string()
                    LogUtils.d("buildToken onResponse responseStr:$responseStr")
                    if (responseStr.isNullOrEmpty()) {
                        mCallback?.onRttError("buildToken onResponse responseStr isNullOrEmpty")
                        return
                    }
                    val responseJson = JSONObject(responseStr)
                    if (responseJson.has("tokenName")) {
                        mToken = responseJson.getString("tokenName")
                        start(channelId)
                    } else {
                        mCallback?.onRttError(responseJson.getString("message"))
                    }
                }

            }
        )
    }

    private fun start(channelId: String) {
        val url =
            AppConfig.getServerConfig()?.serverUrl + "/v1/projects/" + AppConfig.getServerConfig()?.appId + "/rtsc/speech-to-text/tasks" + "?builderToken=" + mToken

        val headers = mapOf("Content-Type" to "application/json")

        val bodyJson = JSONObject()
        val languagesJSONArray = JSONArray()
        AppConfig.getTranscriptLanguages().forEach {
            languagesJSONArray.put(it)
        }
        //转写目前仅支持一种语言
        bodyJson.put("languages", languagesJSONArray)
        if (AppConfig.getTranslateLanguages().isNotEmpty()) {
            val translateConfigJson = JSONObject()
            val languagesJson = JSONObject()
            val targetJSONArray = JSONArray()
            AppConfig.getTranslateLanguages().forEach {
                targetJSONArray.put(it)
            }
            languagesJson.put("target", targetJSONArray)
            languagesJson.put("source", AppConfig.getTranscriptLanguages()[0])
            val translateConfigLanguagesJsonArray = JSONArray()
            translateConfigLanguagesJsonArray.put(languagesJson)
            translateConfigJson.put("languages", translateConfigLanguagesJsonArray)
            bodyJson.put("translateConfig", translateConfigJson)
        }

        bodyJson.put("maxIdleTime", 60)
        bodyJson.put("devicePlatform", Constants.ANDROID)
        val rtcConfigJson = JSONObject()
        rtcConfigJson.put("channelName", channelId)
        rtcConfigJson.put("subBotUid", "998")
        rtcConfigJson.put("pubBotUid", "999")
        bodyJson.put("rtcConfig", rtcConfigJson)

        NetworkClient.sendHttpsRequest(
            url,
            headers,
            bodyJson.toString(),
            NetworkClient.Method.POST,
            object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    LogUtils.e("start onFailure e:$e")
                    mCallback?.onRttError("start onFailure e:$e")
                }

                override fun onResponse(call: Call, response: Response) {
                    val responseStr = response.body?.string()
                    LogUtils.d("start onResponse responseStr:$responseStr")
                    if (responseStr.isNullOrEmpty()) {
                        mCallback?.onRttError("start onResponse responseStr isNullOrEmpty")
                        return
                    }
                    val responseJson = JSONObject(responseStr)
                    if (responseJson.has("taskId")) {
                        mTaskId = responseJson.getString("taskId")
                        mCallback?.onRttStart(mToken, mTaskId)
                    } else {
                        mCallback?.onRttError(responseJson.getString("message"))
                    }
                }

            }
        )
    }

    fun requestStopRttRecognize() {
        LogUtils.d("requestStopRttRecognize mTaskId:$mTaskId mToken:$mToken")
        val url =
            AppConfig.getServerConfig()?.serverUrl + "/v1/projects/" + AppConfig.getServerConfig()?.appId + "/rtsc/speech-to-text/tasks/$mTaskId" + "?builderToken=" + mToken

        val headers = mapOf("Content-Type" to "application/json")

        NetworkClient.sendHttpsRequest(
            url,
            headers,
            "",
            NetworkClient.Method.DELETE,
            object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    LogUtils.e("stop onFailure e:$e")
                    mCallback?.onRttError("stop onFailure e:$e")
                }

                override fun onResponse(call: Call, response: Response) {
                    val responseStr = response.body?.string()
                    LogUtils.d("stop onResponse responseStr:$responseStr")
                    if (responseStr.isNullOrEmpty()) {
                        mCallback?.onRttError("stop onResponse responseStr isNullOrEmpty")
                        return
                    }
                    mCallback?.onRttStop()
                }

            }
        )
    }

    interface RttCallback {
        fun onRttError(errorMessage: String) {

        }

        fun onRttStart(token: String, taskId: String) {

        }

        fun onRttStop() {

        }
    }

}