package com.usm.rk_module;

import static android.Manifest.permission.MANAGE_EXTERNAL_STORAGE;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.os.Build;
import android.os.Environment;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.alibaba.fastjson.JSONObject;
import com.usm.rk_module.log.LogService;
import com.usm.rk_module.recorder.RecorderManager;
import com.usm.rk_module.transcription.TranscriptionResult;
import com.usm.rk_module.transcription.TranscriptionService;
import com.usm.rk_module.transcription.TranscriptionServiceFactory;
import com.zlw.main.recorderlib.RecordManager;
import com.zlw.main.recorderlib.recorder.RecordConfig;
import com.zlw.main.recorderlib.recorder.RecordHelper;
import com.zlw.main.recorderlib.recorder.listener.RecordStateListener;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.dcloud.feature.uniapp.annotation.UniJSMethod;
import io.dcloud.feature.uniapp.bridge.UniJSCallback;
import io.dcloud.feature.uniapp.common.UniModule;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

/**
 * RK模块主类
 * UniApp插件模块，提供录音和语音识别功能
 */
public class RKModule extends UniModule {

    private static Context applicationContext;
    private static RecorderManager recorderManager;
    private static final int MY_PERMISSIONS_REQUEST_RECORD_AUDIO = 1234;

    static RecordManager instance = RecordManager.getInstance();

    // 是否开启详细日志
    private static final boolean DEBUG = true;

    // 日志服务器URL
    private static final String LOG_URL = "https://biexiaozhi.cn/log";
    // 最大并发请求数
    private static final int MAX_CONCURRENT_REQUESTS = 3;
    // 日志请求队列
    private static final List<String> requestQueue = new ArrayList<>();
    // 当前待处理请求数
    private static volatile int pendingRequests = 0;

    private static final Map<Integer, String> TENCENT_ERROR_CODE_MAP = new HashMap<>();

    {
        TENCENT_ERROR_CODE_MAP.put(4001, "参数不合法，具体详情参考 message");
        TENCENT_ERROR_CODE_MAP.put(4002, "鉴权失败");
        TENCENT_ERROR_CODE_MAP.put(4003, "AppID 服务未开通，请在控制台开通服务");
        TENCENT_ERROR_CODE_MAP.put(4004, "资源包耗尽，请开通后付费或者购买资源包");
        TENCENT_ERROR_CODE_MAP.put(4005, "账户欠费停止服务，请及时充值");
    }

    /**
     * 初始化上下文
     * @param applicationContext 应用上下文
     */
    public static void initContext(Application applicationContext) {
        RKModule.applicationContext = applicationContext;
        LogService.logd("RKModule", "初始化Context");
        
        // 初始化录音管理器
        recorderManager = RecorderManager.getInstance();
        recorderManager.init(applicationContext);
        
        instance.init(applicationContext, false);
        instance.changeFormat(RecordConfig.RecordFormat.MP3);
        instance.changeRecordConfig(instance.getRecordConfig().setSampleRate(16000));
        instance.changeRecordConfig(instance.getRecordConfig().setChannelConfig(AudioFormat.CHANNEL_IN_MONO));
        instance.changeRecordConfig(instance.getRecordConfig().setEncodingConfig(AudioFormat.ENCODING_PCM_16BIT));
        instance.setSource(RecordConfig.SOURCE_MIC);
        instance.getRecordConfig().setRecordDir(applicationContext.getExternalFilesDir(Environment.DIRECTORY_MUSIC).getAbsolutePath());
        LogService.logd("RKModule", "初始化RecordManager完成，录音保存路径：" + applicationContext.getExternalFilesDir(Environment.DIRECTORY_MUSIC).getAbsolutePath());
    }

    /**
     * 检查权限
     */
    @UniJSMethod
    public void checkPer() {
        LogService.logd("RKModule", "检查权限");
        int i = ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.RECORD_AUDIO) 
                + ContextCompat.checkSelfPermission(applicationContext, WRITE_EXTERNAL_STORAGE) 
                + ContextCompat.checkSelfPermission(applicationContext, READ_EXTERNAL_STORAGE) 
                + ContextCompat.checkSelfPermission(applicationContext, MANAGE_EXTERNAL_STORAGE);
        
        if (i != PackageManager.PERMISSION_GRANTED) {
            LogService.logd("RKModule", "权限未授予，请求权限");
            Activity activity = (Activity) mWXSDKInstance.getContext();
            ActivityCompat.requestPermissions(activity, 
                    new String[]{Manifest.permission.RECORD_AUDIO, WRITE_EXTERNAL_STORAGE, READ_EXTERNAL_STORAGE, MANAGE_EXTERNAL_STORAGE}, 
                    MY_PERMISSIONS_REQUEST_RECORD_AUDIO);
        } else {
            LogService.logd("RKModule", "权限已授予");
        }
    }

    /**
     * 开始录音（默认使用极速模式）
     * @param recordEndCallback 录音结束回调
     */
    @UniJSMethod()
    public void startRecorder(UniJSCallback recordEndCallback) {
        startRecorderWithMode("speed", recordEndCallback);
    }

    /**
     * 开始录音，使用指定的识别模式
     * @param mode 识别模式："normal"为普通模式，"speed"为极速语音转写模式
     * @param recordEndCallback 录音结束回调
     */
    @UniJSMethod()
    public void startRecorderWithMode(String mode, UniJSCallback recordEndCallback) {
        LogService.logd("RKModule", "开始录音，识别模式：" + mode);
        
        // 如果是speed模式，设置最大录制时长为2分钟
        if ("speed".equals(mode)) {
            LogService.logd("RKModule", "使用腾讯云急速识别，最大录制两小时");
            recorderManager.setMaxDuration(7200);
        } else {
            // 普通模式不限制录制时长
            recorderManager.setMaxDuration(0);
        }
        
        // 设置录音状态监听
        RecordStateListener stateListener = new RecordStateListener() {
            @Override
            public void onStateChange(RecordHelper.RecordState state) {
                LogService.logd("RKModule", "录音状态变化：" + state.name());
            }

            @Override
            public void onError(String error) {
                LogService.loge("RKModule", "录音错误：" + error);
            }
        };
        
        // 开始录音
        recorderManager.start(stateListener, result -> {
            LogService.logd("RKModule", "录音完成，文件路径：" + result.getAbsolutePath() + "，大小：" + String.format("%.2f", result.length() / 1024.0 / 1024.0) + "MB");
            
            // 创建相应的语音识别服务
            TranscriptionService transcriptionService = TranscriptionServiceFactory.createService(mode);
            
            // 开始语音识别
            transcriptionService.transcribe(result, new TranscriptionService.TranscriptionCallback() {
                @Override
                public void onSuccess(TranscriptionResult transcriptionResult) {
                    // 返回识别结果
                    recordEndCallback.invoke(transcriptionResult.toJsonObject());
                }

                @Override
                public void onError(int errorCode, String errorMsg) {
                    // 返回错误结果
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("success", false);
                    jsonObject.put("msg", errorMsg);
                    recordEndCallback.invoke(jsonObject);
                }
            });
        });
    }

    /**
     * 查询订单状态
     * @param orderId 订单ID
     * @param callback 回调
     */
    @UniJSMethod
    public void queryOrder(String orderId, UniJSCallback callback, String filePath) {
        LogService.logd("RKModule", "查询订单状态，orderId：" + orderId);
        
        // 判断订单ID类型
        TranscriptionService service;
        if (orderId.startsWith("tx_")) {
            // 腾讯云订单
            service = TranscriptionServiceFactory.createTencentService();

            // 开始语音识别
            service.transcribe(new File(filePath), new TranscriptionService.TranscriptionCallback() {
                @Override
                public void onSuccess(TranscriptionResult transcriptionResult) {
                    // 返回识别结果
                    callback.invoke(transcriptionResult.toJsonObject());
                }

                @Override
                public void onError(int errorCode, String errorMsg) {
                    // 返回错误结果
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("success", false);
                    jsonObject.put("msg", errorMsg);
                    callback.invoke(jsonObject);
                }
            });
            return;
        } else {
            // 讯飞订单
            service = TranscriptionServiceFactory.createXunfeiService();
        }
        
        // 查询订单
        service.queryOrder(orderId, new TranscriptionService.TranscriptionCallback() {
            @Override
            public void onSuccess(TranscriptionResult result) {
                if (result.getResultList() != null) {
                    callback.invoke(result.getResultList());
                } else {
                    List<JSONObject> emptyList = new ArrayList<>();
                    callback.invoke(emptyList);
                }
            }

            @Override
            public void onError(int errorCode, String errorMsg) {
                JSONObject errorResult = new JSONObject();
                errorResult.put("success", false);
                errorResult.put("msg", errorMsg);
                List<JSONObject> errorList = new ArrayList<>();
                errorList.add(errorResult);
                callback.invoke(errorList);
            }
        });
    }

    /**
     * 暂停录音
     */
    @UniJSMethod
    public void pauseRecording() {
        LogService.logd("RKModule", "暂停录音");
        recorderManager.pause();
    }

    /**
     * 恢复录音
     */
    @UniJSMethod
    public void resumeRecording() {
        LogService.logd("RKModule", "恢复录音");
        recorderManager.resume();
    }

    /**
     * 停止录音
     */
    @UniJSMethod
    public void stopRecording() {
        LogService.logd("RKModule", "停止录音");
        recorderManager.stop();
    }

    /**
     * Debug级别日志
     *
     * @param message 日志信息
     */
    private static void logd(String message) {
        if (DEBUG) {
            Log.d("RKModule", message);
            // 发送网络日志
            sendNetworkLog("DEBUG", message, null);
        }
    }

    /**
     * Error级别日志
     *
     * @param message 日志信息
     */
    private static void loge(String message) {
        Log.e("RKModule", message);
        // 发送网络日志
        sendNetworkLog("ERROR", message, null);
    }

    /**
     * Error级别日志
     *
     * @param message   日志信息
     * @param throwable 异常
     */
    private static void loge(String message, Throwable throwable) {
        Log.e("RKModule", message, throwable);
        // 发送网络日志
        sendNetworkLog("ERROR", message, throwable != null ? throwable.getStackTrace() : null);
    }

    /**
     * 发送网络日志
     *
     * @param logType    日志类型
     * @param message    日志信息
     * @param stackTrace 堆栈信息
     */
    private static void sendNetworkLog(String logType, String message, StackTraceElement[] stackTrace) {
        try {
            String jsonData = createJsonLog(logType, message, stackTrace);
            postLogs(jsonData);
        } catch (Exception e) {
            Log.e("RKModule", "发送网络日志失败", e);
        }
    }

    /**
     * 创建日志JSON数据
     *
     * @param logType    日志类型
     * @param message    日志信息
     * @param stackTrace 堆栈信息
     * @return 日志JSON字符串
     */
    @SuppressLint("SimpleDateFormat")
    private static String createJsonLog(String logType, String message, StackTraceElement[] stackTrace) {
        // 从错误堆栈中提取文件名和行号
        String fileInfo = "";
        if (stackTrace != null && stackTrace.length > 0) {
            StackTraceElement caller = stackTrace[0];
            String fileName = caller.getFileName();
            int lineNumber = caller.getLineNumber();
            if (fileName != null) {
                fileInfo = "(" + fileName + ":" + lineNumber + ") ";
            }
        }

        // 创建日志对象
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("clientId", getClientId());
            jsonObject.put("logType", logType);
            jsonObject.put("timestamp", new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date()));
            jsonObject.put("threadId", Thread.currentThread().getName());
            jsonObject.put("message", fileInfo + message);
        } catch (Exception e) {
            Log.e("RKModule", "创建日志JSON失败", e);
        }

        return jsonObject.toJSONString();
    }

    /**
     * 获取客户端ID
     *
     * @return 客户端唯一标识
     */
    private static String getClientId() {
        // 可以使用设备ID、安装ID或其他唯一标识
        return Build.BRAND + "_" + Build.MODEL;
    }

    /**
     * 发送日志到服务器
     *
     * @param jsonData 日志JSON数据
     */
    private static void postLogs(final String jsonData) {
        // 如果当前请求数达到最大，加入队列
        synchronized (requestQueue) {
            if (pendingRequests >= MAX_CONCURRENT_REQUESTS) {
                requestQueue.add(jsonData);
                return;
            }
            pendingRequests++;
        }

        // 使用OkHttp发送POST请求
        new Thread(() -> {
            OkHttpClient client = new OkHttpClient();
            RequestBody requestBody = RequestBody.create(MediaType.parse("application/json"), jsonData);
            Request request = new Request.Builder()
                    .url(LOG_URL)
                    .post(requestBody)
                    .build();

            try {
                client.newCall(request).execute();
            } catch (Exception e) {
                // 发送失败仅打印错误，不重试，避免循环调用
                Log.e("RKModule", "发送网络日志请求失败", e);
            } finally {
                synchronized (requestQueue) {
                    pendingRequests--;
                    // 如果队列中有等待的请求，继续处理
                    if (!requestQueue.isEmpty()) {
                        String nextData = requestQueue.remove(0);
                        postLogs(nextData);
                    }
                }
            }
        }).start();
    }

}
