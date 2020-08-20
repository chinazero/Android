# Android
个人开发工作中所所运用的一些控件
国内可访问：
https://gitee.com/cainiao89/LiveDataBus
https://gitee.com/cainiao89/AndroidKotlin 后续会统一放到这边

记得start哦~有啥问题也可以给我留言 或者联系 我邮箱 a875938018@qq.com

##### LiveDataBus
    该控件借鉴于：https://github.com/mrme2014/hi_jetpack 
    可替代 eventBus rxBus 
    与借鉴者 不同的是 个人认为事件是否粘性不应该由事件本身决定，而是观察者本身决定 所以在其基础上进行修改
    使用方法：
        发送事件：
        LiveDataBus.with<String>(NormalEvent::class.java).setValue(" 事件 1")
        
        非粘性事件注册
         LiveDataBus.with<String>(NormalEvent::class.java).observe(this, androidx.lifecycle.Observer {
            Toast.makeText(this@MainActivity, "Main页面的非粘性注册，触发${it}", Toast.LENGTH_SHORT).show()
        })
        粘性事件注册 ps 你传入个 false 也是非粘性
         LiveDataBus.with<String>(StickyEvent::class.java).observerSticky(this, true, Observer {
            Toast.makeText(this@TestActivity, "Test 页面的粘性注册 触发${it}", Toast.LENGTH_SHORT).show()
        })
----------------------------------------------------------------------------------------------------------

##### PreferenceManager
    主要是使用 kotlin 的属性委托来 优化简写 SharedPreferences 管理以及书写方案， 该方案仅能在 kotlin上使用 
    摒弃了我们早前使用 的get set 方案  直接调用 PreferenceManager.x = x 就是复制  PreferenceManager.x 就是取值
    使用方法：
    继承 PreferenceManager  构造中传入 SharedPreferences 文件名称 
    var test: String by PreferenceProperty(default = "默认值") 
    这样我们就建立一个  key-value 为  test-默认值 
    自定义 key
    var test1: String by PreferenceProperty("customKey",default = "自定义key默认值")
    这样我们就建立一个  key-value 为  customKey-默认值 
    
    初始化：
    需要在 application 中调用 PreferenceManager.init(application: Application) 进行初始化
----------------------------------------------------------------------------------------------------------
##### BaseBroadcastReceiver 与 BaseBroadcastReceiverJava
    这两者是一样的，一个是在 kotlin的写法一个是java的写 不过kotlin的写法更加优雅
    主要是基于 lifecycle  编写的 一个 BroadcastReceiver  内部弱引用存储了 FragmentActivity
    我们使用 广播 不在需要 关心什么时候去 unregisterReceiver 因为 new BroadcastReceiver 时候就已经增加了监听
    当宿主 触发了 ON_DESTROY 那么就会主动调用 unregisterReceiver
    正常注册位置 
    使用方法
    val intentFilter = IntentFilter()
    intentFilter.addAction(BluetoothDevice.ACTION_FOUND)
    //注册蓝牙 广播 直接注册好广播，并且添加广播事件 如果 并不需要在 onDestroy 的时候销毁广播 则可以调用：unregisterReceiver 方法进行移除
    var broadcastReceiver = object : BaseBroadcastReceiver(this, intentFilter){
       override fun onReceive(context: Context, intent: Intent) {
           val device: BluetoothDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
           Log.i("liuyc", "-------------------------------------------name:${device.name}")
       }
    }

