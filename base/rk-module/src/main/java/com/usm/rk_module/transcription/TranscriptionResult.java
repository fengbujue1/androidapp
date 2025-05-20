package com.usm.rk_module.transcription;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

/**
 * 语音识别结果模型类
 */
public class TranscriptionResult {
    // 是否成功
    private boolean success;
    // 订单ID
    private String orderId;
    // 音频文件路径
    private String audioPath;
    // 音频时长（秒）
    private int duration;
    // 错误消息
    private String errorMsg;
    // 识别内容（JSON格式，包含讯飞或腾讯云返回的结构化数据）
    private JSONObject rawData;
    // 识别结果列表（如腾讯云的sentence_list）
    private JSONArray resultList;

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getAudioPath() {
        return audioPath;
    }

    public void setAudioPath(String audioPath) {
        this.audioPath = audioPath;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }

    public JSONObject getRawData() {
        return rawData;
    }

    public void setRawData(JSONObject rawData) {
        this.rawData = rawData;
    }

    public JSONArray getResultList() {
        return resultList;
    }

    public void setResultList(JSONArray resultList) {
        this.resultList = resultList;
    }

    /**
     * 将结果转换为JSONObject
     */
    public JSONObject toJsonObject() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("success", success);
        jsonObject.put("orderId", orderId);
        jsonObject.put("path", audioPath);
        jsonObject.put("duration", duration);
        
        if (!success && errorMsg != null) {
            jsonObject.put("msg", errorMsg);
        }
        
        if (resultList != null) {
            jsonObject.put("sentence_list", resultList);
        }
        
        return jsonObject;
    }
} 