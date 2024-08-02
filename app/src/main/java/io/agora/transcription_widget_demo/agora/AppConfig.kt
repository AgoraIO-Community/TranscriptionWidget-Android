package io.agora.transcription_widget_demo.agora

import android.content.Context
import com.google.gson.Gson
import io.agora.transcription_widget_demo.model.Language
import io.agora.transcription_widget_demo.model.ServerConfig
import io.agora.transcription_widget_demo.utils.LogUtils
import io.agora.transcription_widget_demo.utils.Utils

object AppConfig {
    private var callbacks: AppConfigCallback? = null
    private var supportServerConfig = mutableListOf<ServerConfig>()
    private var serverConfig: ServerConfig? = null
    private var supportLanguages = mutableListOf<Language>()
    private var transcriptLanguages = mutableListOf<String>()
    private var translateLanguages = mutableListOf<String>()

    fun setCallback(callback: AppConfigCallback) {
        callbacks = callback
    }

    fun initAppConfig(applicationContext: Context) {
        if (supportLanguages.isNotEmpty()) {
            LogUtils.d("AppConfig initAppConfig already init")
            return
        }
        try {
            val gson = Gson()
            val supportLanguageList = gson.fromJson(
                Utils.readAssetJsonArray(applicationContext, "languages.json"),
                Array<Language>::class.java
            )
            LogUtils.d("AppConfig supportLanguageList:${supportLanguageList.toList()}")
            supportLanguages.addAll(supportLanguageList.toList())

            val supportServerConfigList = gson.fromJson(
                Utils.readAssetJsonArray(applicationContext, "server_config.json"),
                Array<ServerConfig>::class.java
            )
            LogUtils.d("AppConfig supportServerConfigList:${supportServerConfigList.toList()}")
            supportServerConfig.addAll(supportServerConfigList.toList())

            setTranscriptLanguages(listOf(supportLanguageList[0].code))
            setTranslateLanguages(mutableListOf())
            setServerConfig(supportServerConfigList[0])
        } catch (e: Exception) {
            LogUtils.e("AppConfig initAppConfig error:${e.message}")
        }
    }

    private fun clear() {
        supportServerConfig.clear()
        serverConfig = null
        supportLanguages.clear()
        transcriptLanguages.clear()
        translateLanguages.clear()
    }

    fun getSupportLanguages(): List<Language> {
        return supportLanguages
    }

    fun getSupportServerConfig(): List<ServerConfig> {
        return supportServerConfig
    }

    fun setServerConfig(config: ServerConfig) {
        serverConfig = config
        callbacks?.onConfigUpdate()
    }

    fun getServerConfig(): ServerConfig? {
        return serverConfig
    }


    fun setTranscriptLanguages(languages: List<String>) {
        transcriptLanguages.clear()
        transcriptLanguages.addAll(languages)
        callbacks?.onConfigUpdate()
    }

    fun getTranscriptLanguages(): List<String> {
        return transcriptLanguages
    }

    fun setTranslateLanguages(languages: List<String>) {
        translateLanguages.clear()
        translateLanguages.addAll(languages)
        callbacks?.onConfigUpdate()
    }

    fun getTranslateLanguages(): List<String> {
        return translateLanguages
    }

    interface AppConfigCallback {
        fun onConfigUpdate()
    }
}