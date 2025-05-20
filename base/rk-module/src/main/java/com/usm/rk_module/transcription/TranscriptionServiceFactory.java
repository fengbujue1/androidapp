package com.usm.rk_module.transcription;

/**
 * 语音识别服务工厂类
 * 用于创建不同的语音识别服务实现
 */
public class TranscriptionServiceFactory {
    
    // 讯飞API参数
    private static final String XUNFEI_APP_ID = "e62ac45f";
    private static final String XUNFEI_SECRET_KEY = "ce3bd75a76e7b155117a7b6557fe8584";
    
    // 腾讯云API参数
    private static final String TENCENT_APP_ID = "1305076310";
    private static final String TENCENT_SECRET_ID = "AKIDaEi071hCXXKvGZjhU4QlvdDhy2iDVaXV";
    private static final String TENCENT_SECRET_KEY = "sXyCG9pPsDYmrmU2QcbMxOqKfrshM2Zs";
    
    /**
     * 创建讯飞语音识别服务
     * @return 讯飞语音识别服务实例
     */
    public static TranscriptionService createXunfeiService() {
        return new XunfeiTranscriptionService(XUNFEI_APP_ID, XUNFEI_SECRET_KEY);
    }
    
    /**
     * 创建腾讯云语音识别服务
     * @return 腾讯云语音识别服务实例
     */
    public static TranscriptionService createTencentService() {
        return new TencentTranscriptionService(TENCENT_APP_ID, TENCENT_SECRET_ID, TENCENT_SECRET_KEY);
    }
    
    /**
     * 根据指定的模式创建语音识别服务
     * @param mode 识别模式："normal"为普通模式(讯飞)，"speed"为极速语音转写模式(腾讯云)
     * @return 对应的语音识别服务实例
     */
    public static TranscriptionService createService(String mode) {
        if ("speed".equals(mode)) {
            return createTencentService();
        } else {
            return createXunfeiService();
        }
    }
} 