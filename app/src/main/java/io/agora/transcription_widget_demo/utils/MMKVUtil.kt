package io.agora.transcription_widget_demo.utils

import android.content.Context
import com.tencent.mmkv.MMKV

object MMKVUtil {
    private lateinit var mmkv: MMKV

    fun initialize(context: Context) {
        MMKV.initialize(context)
        mmkv = MMKV.defaultMMKV()
    }

    fun putString(key: String, value: String) {
        mmkv.encode(key, value)
    }

    fun getString(key: String, defaultValue: String = ""): String {
        return mmkv.decodeString(key, defaultValue) ?: defaultValue
    }

    fun putInt(key: String, value: Int) {
        mmkv.encode(key, value)
    }

    fun getInt(key: String, defaultValue: Int = 0): Int {
        return mmkv.decodeInt(key, defaultValue)
    }

    fun putLong(key: String, value: Long) {
        mmkv.encode(key, value)
    }

    fun getLong(key: String, defaultValue: Long = 0L): Long {
        return mmkv.decodeLong(key, defaultValue)
    }

    fun putFloat(key: String, value: Float) {
        mmkv.encode(key, value)
    }

    fun getFloat(key: String, defaultValue: Float = 0f): Float {
        return mmkv.decodeFloat(key, defaultValue)
    }

    fun putBoolean(key: String, value: Boolean) {
        mmkv.encode(key, value)
    }

    fun getBoolean(key: String, defaultValue: Boolean = false): Boolean {
        return mmkv.decodeBool(key, defaultValue)
    }

    fun remove(key: String) {
        mmkv.removeValueForKey(key)
    }

    fun clearAll() {
        mmkv.clearAll()
    }

    fun contains(key: String): Boolean {
        return mmkv.containsKey(key)
    }
}