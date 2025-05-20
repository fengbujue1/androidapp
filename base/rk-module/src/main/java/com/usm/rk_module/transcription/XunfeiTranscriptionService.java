package com.usm.rk_module.transcription;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.usm.rk_module.log.LogService;
import com.usm.rk_module.utils.AuthUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * 讯飞语音识别服务实现
 */
public class XunfeiTranscriptionService implements TranscriptionService {

    private static final String TAG = "XunfeiTranscriptionService";
    
    // 讯飞语音识别相关参数
    private final String appId;
    private final String secretKey;
    
    private final OkHttpClient okHttpClient = new OkHttpClient();
    
    /**
     * 构造方法
     * @param appId 讯飞应用ID
     * @param secretKey 讯飞密钥
     */
    public XunfeiTranscriptionService(String appId, String secretKey) {
        this.appId = appId;
        this.secretKey = secretKey;
    }
    
    @Override
    public void transcribe(File audioFile, TranscriptionCallback callback) {
        LogService.logd(TAG, "开始讯飞语音识别，文件：" + audioFile.getAbsolutePath());
        
        // 检查文件是否存在
        if (!audioFile.exists() || !audioFile.isFile()) {
            LogService.loge(TAG, "音频文件不存在：" + audioFile.getAbsolutePath());
            callback.onError(1001, "音频文件不存在");
            return;
        }
        
        // 获取文件大小
        long fileSize = audioFile.length();
        
        // 创建请求体
        RequestBody requestBody = RequestBody.create(MediaType.parse("application/octet-stream"), audioFile);
        
        // 构建API URL
        String url = "https://raasr.xfyun.cn/v2/api/upload" + 
                AuthUtils.getXunfeiHandShakeParams(appId, secretKey) + 
                "&fileSize=" + fileSize + 
                "&roleType=1" + 
                "&fileName=" + audioFile.getName() + 
                "&duration=1";
        
        // 构建请求
        Request request = new Request.Builder()
                .url(url)
                .post(requestBody)
                .build();
        
        // 在新线程中执行请求
        new Thread(() -> {
            try {
                LogService.logd(TAG, "开始上传文件：" + audioFile.getName());
                Response response = okHttpClient.newCall(request).execute();
                
                if (!response.isSuccessful()) {
                    LogService.loge(TAG, "上传文件失败，HTTP状态码：" + response.code());
                    callback.onError(response.code(), "上传文件失败，HTTP状态码：" + response.code());
                    return;
                }
                
                // 解析响应
                String responseBody = response.body().string();
                LogService.logd(TAG, "上传文件成功，响应：" + responseBody);
                
                JSONObject jsonResponse = JSON.parseObject(responseBody);
                
                // 检查是否成功
                if (jsonResponse.getIntValue("code") != 0) {
                    String message = jsonResponse.getString("message");
                    LogService.loge(TAG, "讯飞服务返回错误：" + message);
                    callback.onError(jsonResponse.getIntValue("code"), message);
                    return;
                }
                
                // 获取订单ID
                JSONObject content = jsonResponse.getJSONObject("content");
                String orderId = content.getString("orderId");
                
                // 构建结果对象
                TranscriptionResult result = new TranscriptionResult();
                result.setSuccess(true);
                result.setOrderId(orderId);
                result.setAudioPath(audioFile.getAbsolutePath());
                result.setDuration(getAudioDuration(audioFile.getAbsolutePath()));
                
                // 回调成功
                callback.onSuccess(result);
                
            } catch (IOException e) {
                LogService.loge(TAG, "上传文件发生异常", e);
                callback.onError(1002, "上传文件失败：" + e.getMessage());
            }
        }).start();
    }
    
    @Override
    public void queryOrder(String orderId, TranscriptionCallback callback) {
        LogService.logd(TAG, "查询讯飞订单状态，orderId：" + orderId);
        
        // 构建API URL
        String url = "https://raasr.xfyun.cn/v2/api/getResult" + 
                AuthUtils.getXunfeiHandShakeParams(appId, secretKey) + 
                "&orderId=" + orderId;
        
        // 构建请求
        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();
        
        // 在新线程中执行请求
        new Thread(() -> {
            try {
                LogService.logd(TAG, "发送查询请求：" + url);
                Response response = okHttpClient.newCall(request).execute();
                
                if (!response.isSuccessful()) {
                    LogService.loge(TAG, "查询失败，HTTP状态码：" + response.code());
                    callback.onError(response.code(), "查询失败，HTTP状态码：" + response.code());
                    return;
                }
                
                // 解析响应
                String responseBody = response.body().string();
                LogService.logd(TAG, "查询成功，响应：" + responseBody);
                
                // 解析识别结果
                List<JSONObject> resultList = parseTranscriptionResult(responseBody);
                
                if (resultList == null || resultList.isEmpty()) {
                    LogService.logd(TAG, "订单未完成或无识别结果");
                    callback.onError(1003, "订单未完成或无识别结果");
                    return;
                }
                
                // 构建结果对象
                TranscriptionResult result = new TranscriptionResult();
                result.setSuccess(true);
                result.setOrderId(orderId);
                
                // 将结果列表转换为JSONArray
                JSONArray jsonArray = new JSONArray();
                jsonArray.addAll(resultList);
                result.setResultList(jsonArray);
                
                // 回调成功
                callback.onSuccess(result);
                
            } catch (IOException e) {
                LogService.loge(TAG, "查询订单发生异常", e);
                callback.onError(1004, "查询订单失败：" + e.getMessage());
            }
        }).start();
    }
    
    /**
     * 解析讯飞语音识别结果
     * 
     * @param responseJson 响应JSON字符串
     * @return 解析后的结果列表
     */
    private List<JSONObject> parseTranscriptionResult(String responseJson) {
        List<JSONObject> resultList = new ArrayList<>();
        try {
            JSONObject messageObj = JSON.parseObject(responseJson);
            JSONObject content = messageObj.getJSONObject("content");
            JSONObject orderInfo = content.getJSONObject("orderInfo");
            Integer status = orderInfo.getInteger("status");
            
            // 状态为4表示订单已完成
            if (status != 4) {
                return null;
            }
            
            JSONObject orderResult = content.getJSONObject("orderResult");
            JSONArray latticeArr = orderResult.getJSONArray("lattice");
            
            for (int i = 0; i < latticeArr.size(); i++) {
                StringBuilder resultBuilder = new StringBuilder();
                JSONObject data = new JSONObject();
                JSONObject st = latticeArr.getJSONObject(i).getJSONObject("json_1best").getJSONObject("st");
                
                String rl = st.getString("rl");
                data.put("rl", rl);
                data.put("bg", st.getString("bg"));
                data.put("ed", st.getString("ed"));
                
                JSONArray rtArr = st.getJSONArray("rt");
                for (int j = 0; j < rtArr.size(); j++) {
                    JSONObject rtArrObj = rtArr.getJSONObject(j);
                    JSONArray wsArr = rtArrObj.getJSONArray("ws");
                    for (int k = 0; k < wsArr.size(); k++) {
                        JSONObject wsArrObj = wsArr.getJSONObject(k);
                        JSONArray cwArr = wsArrObj.getJSONArray("cw");
                        for (int c = 0; c < cwArr.size(); c++) {
                            JSONObject cwArrObj = cwArr.getJSONObject(c);
                            String wStr = cwArrObj.getString("w");
                            resultBuilder.append(wStr);
                        }
                    }
                }
                
                data.put("text", resultBuilder.toString());
                resultList.add(data);
            }
        } catch (Exception e) {
            LogService.loge(TAG, "解析讯飞识别结果异常", e);
        }
        
        return resultList;
    }
    
    /**
     * 获取音频时长
     * 
     * @param audioPath 音频文件路径
     * @return 时长（秒）
     */
    private int getAudioDuration(String audioPath) {
        try {
            android.media.MediaMetadataRetriever mmr = new android.media.MediaMetadataRetriever();
            mmr.setDataSource(audioPath);
            int duration = Integer.parseInt(mmr.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_DURATION)) / 1000;
            return duration;
        } catch (Exception e) {
            LogService.loge(TAG, "获取音频时长失败", e);
            return 0;
        }
    }
} 