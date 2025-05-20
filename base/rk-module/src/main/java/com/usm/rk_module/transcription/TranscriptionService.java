package com.usm.rk_module.transcription;

import java.io.File;

/**
 * 语音识别服务接口
 * 定义语音识别功能
 */
public interface TranscriptionService {
    
    /**
     * 语音识别结果回调接口
     */
    interface TranscriptionCallback {
        /**
         * 识别成功
         * @param result 识别结果
         */
        void onSuccess(TranscriptionResult result);
        
        /**
         * 识别失败
         * @param errorCode 错误码
         * @param errorMsg 错误信息
         */
        void onError(int errorCode, String errorMsg);
    }
    
    /**
     * 开始语音识别
     * @param audioFile 音频文件
     * @param callback 回调
     */
    void transcribe(File audioFile, TranscriptionCallback callback);
    
    /**
     * 查询订单状态
     * @param orderId 订单ID
     * @param callback 回调
     */
    void queryOrder(String orderId, TranscriptionCallback callback);
} 