package com.usm.rk_module.transcription;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.tencent.cloud.qcloudasrsdk.filerecognize.QCloudFlashRecognizer;
import com.tencent.cloud.qcloudasrsdk.filerecognize.param.QCloudFlashRecognitionParams;
import com.usm.rk_module.log.LogService;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * 腾讯云语音识别服务实现
 */
public class TencentTranscriptionService implements TranscriptionService {

    private static final String TAG = "TencentTranscriptionService";
    
    // 腾讯云语音识别相关参数
    private final String appId;
    private final String secretId;
    private final String secretKey;
    
    // 腾讯云错误码映射
    private static final Map<Integer, String> ERROR_CODE_MAP = new HashMap<>();
    
    static {
        ERROR_CODE_MAP.put(4001, "参数不合法，具体详情参考 message");
        ERROR_CODE_MAP.put(4002, "鉴权失败");
        ERROR_CODE_MAP.put(4003, "AppID 服务未开通，请在控制台开通服务");
        ERROR_CODE_MAP.put(4004, "资源包耗尽，请开通后付费或者购买资源包");
        ERROR_CODE_MAP.put(4005, "账户欠费停止服务，请及时充值");
    }
    
    /**
     * 构造方法
     * @param appId 腾讯云应用ID
     * @param secretId 腾讯云密钥ID
     * @param secretKey 腾讯云密钥
     */
    public TencentTranscriptionService(String appId, String secretId, String secretKey) {
        this.appId = appId;
        this.secretId = secretId;
        this.secretKey = secretKey;
    }
    
    @Override
    public void transcribe(File audioFile, TranscriptionCallback callback) {
        LogService.logd(TAG, "开始腾讯云语音识别，文件：" + audioFile.getAbsolutePath());
        
        // 检查文件是否存在
        if (!audioFile.exists() || !audioFile.isFile()) {
            LogService.loge(TAG, "音频文件不存在：" + audioFile.getAbsolutePath());
            callback.onError(1001, "音频文件不存在");
            return;
        }
        
        // 创建腾讯云录音文件识别实例
        QCloudFlashRecognizer fileFlashRecognizer = new QCloudFlashRecognizer(appId, secretId, secretKey);
        
        // 设置识别结果回调
        fileFlashRecognizer.setCallback((recognizer, resultStr, exception) -> {
            if (exception != null) {
                // 发生异常
                LogService.loge(TAG, "腾讯云语音识别发生错误", exception);
                callback.onError(1002, "语音识别失败：" + exception.getMessage());
                return;
            }
            
            try {
                // 解析结果
                LogService.logd(TAG, "腾讯云语音识别结果：" + resultStr);
                JSONObject jsonResult = JSON.parseObject(resultStr);
                int code = jsonResult.getIntValue("code");
                
                if (code != 0) {
                    String errorMsg = ERROR_CODE_MAP.get(code);
                    if (errorMsg == null) {
                        errorMsg = "未知错误，错误码：" + code;
                    }
                    LogService.loge(TAG, "腾讯云语音识别返回错误：" + errorMsg);
                    callback.onError(code, "语音识别失败：" + errorMsg);
                    return;
                }
                
                // 创建一个唯一的订单ID (使用时间戳+随机数作为唯一标识)
                String orderId = "tx_" + System.currentTimeMillis() + "_" + (int) (Math.random() * 10000);
                
                // 构建结果对象
                TranscriptionResult result = new TranscriptionResult();
                result.setSuccess(true);
                result.setOrderId(orderId);
                result.setAudioPath(audioFile.getAbsolutePath());
                result.setDuration(getAudioDuration(audioFile.getAbsolutePath()));
                
                // 保存原始结果
                result.setRawData(jsonResult);
                
                // 提取句子列表
                JSONArray sentenceList = jsonResult.getJSONArray("flash_result")
                        .getJSONObject(0)
                        .getJSONArray("sentence_list");
                result.setResultList(sentenceList);
                
                LogService.logd(TAG, "腾讯云语音识别成功，生成的订单ID：" + orderId);
                
                // 回调成功
                callback.onSuccess(result);
                
            } catch (Exception e) {
                LogService.loge(TAG, "解析腾讯云语音识别结果异常", e);
                callback.onError(1003, "解析识别结果失败：" + e.getMessage());
            }
        });
        
        try {
            // 设置识别参数
            QCloudFlashRecognitionParams params = (QCloudFlashRecognitionParams) QCloudFlashRecognitionParams.defaultRequestParams();
            
            // 设置录音文件路径
            params.setPath(audioFile.getAbsolutePath());
            params.setSpeakerDiarization(1);
            
            // 通过文件扩展名判断音频格式
            String fileName = audioFile.getName();
            String format = "mp3"; // 默认格式
            if (fileName.contains(".")) {
                String ext = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
                if (ext.equals("wav") || ext.equals("pcm") || ext.equals("ogg") ||
                        ext.equals("opus") || ext.equals("speex") || ext.equals("silk") ||
                        ext.equals("mp3") || ext.equals("m4a") || ext.equals("aac")) {
                    format = ext;
                }
            }
            params.setVoiceFormat(format);
            
            // 设置引擎模型类型，默认16k中文普通话通用
            params.setEngineModelType("16k_zh");
            
            // 设置超时时间
            params.setConnectTimeout(30 * 1000); // 连接超时：30秒
            params.setReadTimeout(600 * 1000);   // 读取超时：10分钟
            
            // 发起识别请求
            LogService.logd(TAG, "开始发送腾讯云语音识别请求");
            fileFlashRecognizer.recognize(params);
            
        } catch (Exception e) {
            LogService.loge(TAG, "发起腾讯云语音识别请求失败", e);
            callback.onError(1004, "发起语音识别请求失败：" + e.getMessage());
        }
    }
    
    @Override
    public void queryOrder(String orderId, TranscriptionCallback callback) {
        // 腾讯云的语音识别是一次性的，没有查询订单的需求
        // 只有返回订单ID是为了与讯飞API保持一致的接口
        LogService.logd(TAG, "腾讯云语音识别无需查询订单：" + orderId);
        callback.onError(1005, "腾讯云语音识别无需查询订单");
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