package com.usm.rk_module.log;

import android.os.Build;
import android.util.Log;

import com.alibaba.fastjson.JSONObject;

import java.util.ArrayList;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

/**
 * 日志服务
 * 处理日志记录和上传功能
 */
public class LogService {
    // 用于日志标记的TAG
    private final static String TAG = "LogService";

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

    /**
     * Debug级别日志
     *
     * @param tag 日志标签
     * @param message 日志信息
     */
    public static void logd(String tag, String message) {
        if (DEBUG) {
            Log.d(tag, message);
            // 发送网络日志
            sendNetworkLog("DEBUG", tag + ": " + message, null);
        }
    }

    /**
     * Error级别日志
     *
     * @param tag 日志标签
     * @param message 日志信息
     */
    public static void loge(String tag, String message) {
        Log.e(tag, message);
        // 发送网络日志
        sendNetworkLog("ERROR", tag + ": " + message, null);
    }

    /**
     * Error级别日志
     *
     * @param tag 日志标签
     * @param message 日志信息
     * @param throwable 异常
     */
    public static void loge(String tag, String message, Throwable throwable) {
        Log.e(tag, message, throwable);
        // 发送网络日志
        sendNetworkLog("ERROR", tag + ": " + message, throwable != null ? throwable.getStackTrace() : null);
    }

    /**
     * 发送网络日志
     *
     * @param logType 日志类型
     * @param message 日志信息
     * @param stackTrace 堆栈信息
     */
    private static void sendNetworkLog(String logType, String message, StackTraceElement[] stackTrace) {
        try {
            String jsonData = createJsonLog(logType, message, stackTrace);
            postLogs(jsonData);
        } catch (Exception e) {
            Log.e(TAG, "发送网络日志失败", e);
        }
    }

    /**
     * 创建日志JSON数据
     *
     * @param logType 日志类型
     * @param message 日志信息
     * @param stackTrace 堆栈信息
     * @return 日志JSON字符串
     */
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
            Log.e(TAG, "创建日志JSON失败", e);
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
        return Build.BRAND + "_" + Build.MODEL + "_" + Build.SERIAL;
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
                Log.e(TAG, "发送网络日志请求失败", e);
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