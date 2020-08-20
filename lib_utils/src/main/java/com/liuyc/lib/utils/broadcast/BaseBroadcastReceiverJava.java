package com.liuyc.lib.utils.broadcast;

import android.content.BroadcastReceiver;
import android.content.IntentFilter;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleEventObserver;
import androidx.lifecycle.LifecycleOwner;

import java.lang.ref.WeakReference;

public abstract class BaseBroadcastReceiverJava extends BroadcastReceiver {

    /**
     * 弱引用 宿主 activity 消失 则一起销毁
     */
    private WeakReference<FragmentActivity> mActivity;
    /**
     * 状态判断，广播是否已经被反注册了
     */
    private boolean isUnregister = false;

    public BaseBroadcastReceiverJava(FragmentActivity activity, IntentFilter intentFilter) {
        mActivity = new WeakReference<>(activity);
        registerReceiver(intentFilter);
        addUnRegisterReceiver();
    }

    /**
     * 执行注册
     * @param intentFilter
     */
    private void registerReceiver(IntentFilter intentFilter) {
        if (mActivity.get() != null) {
            mActivity.get().registerReceiver(this, intentFilter);
        }
    }

    /**
     * 添加反注册监听
     */
    private void addUnRegisterReceiver() {
        if (mActivity.get() != null) {
            mActivity.get().getLifecycle().addObserver(new LifecycleEventObserver() {
                @Override
                public void onStateChanged(@NonNull LifecycleOwner source, @NonNull Lifecycle.Event event) {
                    if (event == Lifecycle.Event.ON_DESTROY) {
                        unregisterReceiver();
                    }
                }
            });
        }
    }

    /**
     * 反注册
     * 除非有特殊需要 手动反注册，否则不需要调用
     */
    public void unregisterReceiver() {
        if (!isUnregister && mActivity.get() != null) {
            mActivity.get().unregisterReceiver(this);
            isUnregister = true;
        }
    }


}
