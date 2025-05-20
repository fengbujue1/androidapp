# 一个uniapp的基座，后续均可使用此项目来进行修改打包

注意，随着HBuilder的更新，里面的sdk包可能会用不了，届时需要到DCloud上面下载最新的sdk，把他们全部替换再跑就行。
https://nativesupport.dcloud.net.cn/AppDocs/download/android.html

第一步，先在uniapp里面创建一个应用，注意，需要登录账号

第二步，创建打包key[教程地址](https://nativesupport.dcloud.net.cn/AppDocs/usesdk/appkey.html)
[开发者后台](https://dev.dcloud.net.cn/pages/app/list)
[生成keystore](https://ask.dcloud.net.cn/article/35777)

将生成的key的SHA1和SHA256填写到里面，注意，包名要写打包的模块的build.gradle里面的android.defaultConfig.applicationId一样。

生成完成后点生成的那一行数据的离线打包key，里面会有相应的key


完成后就可以打开本项目，开始改配置
1. usm.keystore
2. dcloud_control.xml里面的appid
3. AndroidManifest.xml里面的dcloud_appkey，改成在DCloud上面生成的key，步骤参考上面
4. build.gradle里面的android.defaultConfig.applicationId
5. assets/dcloud_uniplugins.json里面的hookClass和模块名称（如果没有改类名和模块名可以忽略）

## app-debug
这个是打调试基座的包，点右边gradle，找到app-debug的other，双击assembleDebug，app-debug/outputs/apk/debug/目录下会生成一个android_debug.apk，
把他复制到uniapp的unpackage/debug目录下面，然后在HBuilder里面运行到手机或模拟器，使用自定义基座运行即可开始调试。

注：如果gradle没有assembleDebug这个命令那么打开设置，最后一栏experimental，里面应该会有一个configure all gradle tasks，给他勾上再应用出来就有了。

## app-package
这个是打发行包的，也就是最终发行的包，在HBuilder里面点击发行->原生APP-本地打包->生成本地打包资源，打包出来的资源会在unpackage/resourses下面，把他复制到app-package
模块的assets/apps下面。


## 生命周期的感知以及消息发送

app-package和app-debug里面都重写了PandoraEntryActivity和PandoraEntry，PandoraEntry是程序的入口，PandoraEntryActivity是所有网页运行的activity，由于在debug的时候HBuilder
是直接启动的PandoraEntry，所以在MyApplication里面重写了onCreate方法，让他直接打开我们写的PandoraEntry，这样就实现了debug也能跳转自定义的activity，跳转到我们重写的Activity中就能获取到完整的生命周期了。
Uni里面提供了一个fireGlobalEventCallback方法可以在原生代码里面通知前端，示例如下。
```java
class Test {
    public void test() {
        // 获取到所有的示例
        List<WXSDKInstance> allInstances = WXSDKManager.getInstance().getWXRenderManager().getAllInstances();
        if (allInstances == null) {
//          Log.d("TAG", "initContext: null");
            return;
        }
        Map<String, Object> map = new HashMap<>();
        map.put("code", value);
        for (WXSDKInstance allInstance : allInstances) {
            // 向所有的示例发送消息
            allInstance.fireGlobalEventCallback("scan", map);
        }
    }
}
```



## rk-module
具体模块名称，在里面有一个RKModule类，这个类里面有所有可在uniapp里面调用的方法（需加上@UniJSMethod注解）

- UniJSMethod

标记当前方法为uniapp可调用的原生方法     

- UniJSCallback

一般来说异步的时候使用，方法传入这个参数，在uniapp侧传入一个方法达到回调的效果。

