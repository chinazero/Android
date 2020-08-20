package com.liuyc.lib.utils.bus


import androidx.lifecycle.*
import java.util.concurrent.ConcurrentHashMap

/**
 * LiveDataBus
 *
 * 可以取代  EventBus RxBus 因为基于LiveData 所以可以感知 宿主的生命周期
 * 对于观察者只管 注册，销毁我们会根据宿主 在销毁的时候触发销毁
 * 并且 在视图非显示状态下并不发出更新通知
 * 起因：
 * LiveData 本身是事件粘性的，根据大神们的分析，粘性与否可以根据 观察者 version 来决定
 * 网络上的大神们，很早就使用了hook 方案 实现了，所以鄙人想寻找一个不实用hook的方案。于是找到
 * https://github.com/mrme2014/hi_jetpack
 * 但是这里面存在一个问题，作为事件的发送方 需要知道事件是否是粘性的
 * 1.个人认为事件是否是粘性，发送方不需要注意，应该是观察者来决定
 * 2.与日常我们使用liveData 有所差别
 * 思路：
 * 看了上面的源码，在结合自己的思考
 * 参考 LiveData 中的 wrapper 部分我们重新自己重新包装一层。
 * 然后将上层传递过来的观察者缓存住，将我们自己的观察者交付给liveData去执行。
 * LiveData还是执行LiveData内的一套。等到触发onChange的时候，这时候我们就可以根据自己控制的版本号来进行数据分发
 * 至于粘性判断，这个在我们直接观察者一开始初始化的时候就进行判断，因为不需要粘性的话，只要当前观察者版本号等于
 * 当前数据版本号即可
 *
 * 刘隽
 */
object LiveDataBus {
    /**
     * 存放事件集合
     */
    private val eventMap = ConcurrentHashMap<String, Any>()

    val DEFALUT_VERSION = 0;

    /**
     * 使用class 比 string 更不容易出现重叠情况。
     * 我们在存储的时候并不会存储 class 只会存储 一个完全 class 类名称
     * 根据经验千万不要相信你的队友跟你说 String 简单。到时候命名冲突了。鬼知道是怎么回事
     * 除非你做好这个string 的管理，但是在多模块的情况下 这就可能出岔子。所以用class 最安全
     */
    fun <T> with(clazz: Class<*>): StickyLiveData<T> {
        var eventKey = clazz.name
        var liveData = eventMap[eventKey]
        if (liveData == null) {
            liveData = StickyLiveData<T>(eventKey)
            eventMap.put(eventKey, liveData)
        }

        return liveData as StickyLiveData<T>
    }

    /**
     * 类似于 MutableLiveData
     */
    class StickyLiveData<T>(private val eventKey: String) : LiveData<T>() {
        //分发数据对应的 版本 此处增加限制不允许 包外访问
        internal var mVersion: Int = DEFALUT_VERSION

        public override fun setValue(value: T) {
            //统计我们自己的数据库版本
            mVersion++;
            super.setValue(value)
        }

        public override fun postValue(value: T) {
            mVersion++;
            super.postValue(value)
        }

        /**
         * 设置非 粘性的观察者
         */
        override fun observe(owner: LifecycleOwner, observer: Observer<in T>) {
            observerSticky(owner, false, observer)
        }

        /**
         * 设置是否粘性观察者，观察者的注册都会到这
         */
        fun observerSticky(owner: LifecycleOwner, isSticky: Boolean, observer: Observer<in T>) {
            // 增加 宿主状态监听，如果宿主已经 触发 onDestroy 后则需要移除观察者
            owner.lifecycle.addObserver(LifecycleEventObserver { source, event ->
                if (event == Lifecycle.Event.ON_DESTROY) {
                    eventMap.remove(eventKey)
                }
            })
            //这边继续走 liveData的 逻辑
            super.observe(owner, StickyObserver(this, isSticky, observer))
        }
    }

    /**
     * 我们将我们传递给 liveData Observer 重新封装一次
     * 直接使用我们自己的 version 配合 isSticky 判断 是否使用 粘性事件
     */
    class StickyObserver<T>(
            val stickyLiveData: StickyLiveData<T>,
            val isSticky: Boolean,
            val observer: Observer<in T>
    ) : Observer<T> {

        private var lastVersion: Int = DEFALUT_VERSION;

        init {
            //如果是 非粘性的观察者，则需要在一开始的时候 将 整个事件的 version 赋予观察者
            if (!isSticky) lastVersion = stickyLiveData.mVersion
        }

        /**
         * 这里会先在 LiveData considerNotify 方法中触发
         * 这时候我们可以根据 liveData 的方案来更新我们直接的 version
         */
        override fun onChanged(t: T) {
            //由于是自己来实现 version的控制，那么我们在change的时候 需要复写 version的变化比较
            if (lastVersion >= stickyLiveData.mVersion) {
                return
            }

            lastVersion = stickyLiveData.mVersion
            observer.onChanged(t)

        }
    }


}