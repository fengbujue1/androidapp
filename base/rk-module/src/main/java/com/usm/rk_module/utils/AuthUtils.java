package com.usm.rk_module.utils;

import android.os.Build;

import com.usm.rk_module.log.LogService;

import java.net.URLEncoder;

/**
 * 身份认证工具类
 * 提供身份验证相关方法
 */
public class AuthUtils {
    private static final String TAG = "AuthUtils";

    /**
     * 生成讯飞API握手参数
     *
     * @param appId 应用ID
     * @param secretKey 密钥
     * @return 握手参数字符串
     */
    public static String getXunfeiHandShakeParams(String appId, String secretKey) {
        String ts = System.currentTimeMillis() / 1000 + "";
        String signa = "";
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                signa = EncryptUtil.HmacSHA1Encrypt(EncryptUtil.MD5(appId + ts), secretKey);
            } else {
                LogService.loge(TAG, "Android版本过低，不支持HmacSHA1加密");
            }
            return "?appId=" + appId + "&ts=" + ts + "&signa=" + URLEncoder.encode(signa, "UTF-8");
        } catch (Exception e) {
            LogService.loge(TAG, "生成握手参数失败", e);
        }

        return "";
    }
} 