package com.liuyc.lib.utils.perfp


import android.app.Application
import android.content.Context
import android.content.SharedPreferences

/**
 * PreferenceManager
 * @param spfName 对应文件名称
 * 刘隽
 */
abstract class PreferenceManager(private val spfName: String) {

    private lateinit var application: Application

    private val prefs: SharedPreferences by lazy {
        application.getSharedPreferences(spfName, Context.MODE_PRIVATE)
    }

    fun init(application: Application) {
        this.application = application
    }


    /**
     * 存储数据
     */
    internal fun putSharedPreference(key: String, value: Any) = with(prefs.edit()) {
        when (value) {
            is Long -> {
                putLong(key, value)
            }
            is Int -> {
                putInt(key, value)
            }
            is Float -> {
                putFloat(key, value)
            }
            is String -> {
                putString(key, value)
            }
            is Boolean -> {
                putBoolean(key, value)
            }
            else -> throw IllegalStateException("暂时仅支持 Long/Int/Float/String/Boolean 类型的存储")
        }.apply()
    }

    /**
     * 获取数据
     */
    internal fun <T> getSharedPreference(key: String, default: T): T = with(prefs) {
        val result: Any = when (default) {
            is Long -> {
                getLong(key, default)
            }
            is Int -> {
                getInt(key, default)
            }
            is Float -> {
                getFloat(key, default)
            }
            is Boolean -> {
                getBoolean(key, default)
            }
            is String -> {
                getString(key, default) ?: ""
            }
            else -> throw IllegalStateException("暂时仅支持 Long/Int/Float/String/Boolean 类型的存储")
        }
        return result as T
    }


}