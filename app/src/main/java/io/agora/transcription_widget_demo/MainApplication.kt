package io.agora.transcription_widget_demo

import android.app.Application
import android.content.Context
import android.os.Process
import io.agora.transcription_widget_demo.utils.LogUtils
import io.agora.transcription_widget_demo.utils.MMKVUtil

class MainApplication : Application(), Thread.UncaughtExceptionHandler {
    override fun onCreate() {
        super.onCreate()
        init()
    }

    private fun init() {
        registerUncaughtExceptionHandler()
        MMKVUtil.initialize(this)
        LogUtils.enableLog(this, true, true)
    }

    private fun registerUncaughtExceptionHandler() {
        Thread.setDefaultUncaughtExceptionHandler(this)
    }

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
    }

    override fun uncaughtException(t: Thread, e: Throwable) {
        e.printStackTrace()
        System.exit(1)
        Process.killProcess(Process.myPid())
    }

}
