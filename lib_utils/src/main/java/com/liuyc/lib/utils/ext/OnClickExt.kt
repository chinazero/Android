package com.liuyc.test.app.ext

import android.view.View

/**
 * OnClick 的扩展方法
 *
 * 刘隽
 */


/**
 *  替代 普通 的点击事件
 */
fun <T : View> T.click(block: (T) -> Unit) = setOnClickListener {
    block(it as T)
}