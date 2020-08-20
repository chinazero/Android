package com.liuyc.lib.utils.broadcast

import android.content.BroadcastReceiver
import android.content.IntentFilter
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import java.lang.ref.WeakReference


/**
 * 基础广播类 需要传入 FragmentActivity
 * 暂时未测试： 目的 就是为 用户只需要在 视图 中执行 BaseBroadcastReceiver(activity).addRegisterReceiver
 * 之后就可以了，至于后续的 UnRegisterReceiver 可以通过 lifecycle 进行监听 在触发 ON_DESTROY 的时候进行销毁
 *
 *  kotlin 写弱引用 真优雅
 *
 * 刘隽
 * 0.1--8.11
 * 0.2--8.12 增加对外反注册方法
 */
abstract class BaseBroadcastReceiver(activity: FragmentActivity, filter: IntentFilter) :
    BroadcastReceiver() {
    /**
     * 弱引用 宿主 activity 消失 则一起销毁
     */
    private val mActivity: WeakReference<FragmentActivity>

    /**
     * 状态判断，广播是否已经被反注册了
     * 这种方案 比较简单
     * 当然也可以通过 packageManager 去搜索我们注册的广播
     * 然后在看看方法回来的注册 是不是有我们自己 但是这样我们还需要缓存 我注册目标 所以还不如这个实惠
     */
    private var isUnregister = false


    /**
     * 添加移除广播事件
     */
    private fun addUnRegisterReceiver() {
        mActivity.get()?.lifecycle?.addObserver(LifecycleEventObserver { source, event ->
            if (event == Lifecycle.Event.ON_DESTROY) {
                unregisterReceiver()
            }
        })
    }

    /**
     * 注册
     */
    private fun registerReceiver(filter: IntentFilter){
        mActivity.get()?.registerReceiver(this, filter)
    }

    /**
     * 一些特殊的情况下 需要用户手动 反注册广播
     */
    fun unregisterReceiver() {
        if (!isUnregister) {
            mActivity.get()?.unregisterReceiver(this@BaseBroadcastReceiver)
            isUnregister = true
        }
    }

    init {
        mActivity = WeakReference(activity)
        registerReceiver(filter)
        addUnRegisterReceiver()
    }
}