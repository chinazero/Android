package com.liuyc.lib.utils.perfp

import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 *  SharePreferenceProperty
 *  SharePreference 属性委托类
 *  key 可以不写 key 不写的话会直接使用 被委托属性的名称作为 key
 * 刘隽
 */
 class PreferenceProperty<T : Any>(private val key: String? = null, private val default: T) :
        ReadWriteProperty<PreferenceManager, T> {

    override fun getValue(thisRef: PreferenceManager, property: KProperty<*>): T {
        var keyName = getKeyName(property)

        return thisRef.getSharedPreference(keyName, default)
    }


    override fun setValue(thisRef: PreferenceManager, property: KProperty<*>, value: T) {
        var keyName = getKeyName(property)
        thisRef.putSharedPreference(keyName, value)
    }


    /**
     * 获取key
     */
    private fun getKeyName(property: KProperty<*>): String = key ?: property.name

}