package com.usm.rk_module.recorder;

import android.app.Application;
import android.media.AudioFormat;
import android.media.MediaMetadataRetriever;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;

import com.usm.rk_module.log.LogService;
import com.zlw.main.recorderlib.RecordManager;
import com.zlw.main.recorderlib.recorder.RecordConfig;
import com.zlw.main.recorderlib.recorder.RecordHelper;
import com.zlw.main.recorderlib.recorder.listener.RecordResultListener;
import com.zlw.main.recorderlib.recorder.listener.RecordStateListener;

/**
 * 录音管理器
 * 封装录音相关功能
 */
public class RecorderManager {
    private static final String TAG = "RecorderManager";
    private static RecorderManager instance;
    private RecordManager recordManager;
    private Handler handler;
    private Runnable stopRecordingRunnable;
    private RecordStateListener userStateListener;
    private RecordResultListener userResultListener;

    private RecorderManager() {
        recordManager = RecordManager.getInstance();
        handler = new Handler(Looper.getMainLooper());
    }

    public static synchronized RecorderManager getInstance() {
        if (instance == null) {
            instance = new RecorderManager();
        }
        return instance;
    }

    /**
     * 初始化录音管理器
     *
     * @param context 应用上下文
     */
    public void init(Application context) {
        LogService.logd(TAG, "初始化录音管理器");
        recordManager.init(context, false);
        recordManager.changeFormat(RecordConfig.RecordFormat.MP3);
        recordManager.changeRecordConfig(recordManager.getRecordConfig().setSampleRate(16000));
        recordManager.changeRecordConfig(recordManager.getRecordConfig().setChannelConfig(AudioFormat.CHANNEL_IN_MONO));
        recordManager.changeRecordConfig(recordManager.getRecordConfig().setEncodingConfig(AudioFormat.ENCODING_PCM_16BIT));
        recordManager.setSource(RecordConfig.SOURCE_MIC);
        recordManager.getRecordConfig().setRecordDir(context.getExternalFilesDir(Environment.DIRECTORY_MUSIC).getAbsolutePath());
        LogService.logd(TAG, "初始化RecordManager完成，录音保存路径：" + context.getExternalFilesDir(Environment.DIRECTORY_MUSIC).getAbsolutePath());
    }

    /**
     * 设置最大录制时长（秒）
     *
     * @param maxDuration 最大录制时长（秒），0表示不限制
     */
    public void setMaxDuration(int maxDuration) {
        LogService.logd(TAG, "设置最大录制时长：" + maxDuration + "秒");
        recordManager.changeRecordConfig(recordManager.getRecordConfig().setMaxDuration(maxDuration));
    }

    /**
     * 获取当前设置的最大录制时长（秒）
     *
     * @return 最大录制时长（秒），0表示不限制
     */
    public int getMaxDuration() {
        return recordManager.getRecordConfig().getMaxDuration();
    }

    /**
     * 开始录音
     *
     * @param stateListener 状态监听器
     * @param resultListener 结果监听器
     */
    public void start(RecordStateListener stateListener, RecordResultListener resultListener) {
        LogService.logd(TAG, "开始录音");
        
        // 保存用户设置的监听器
        this.userStateListener = stateListener;
        this.userResultListener = resultListener;
        
        // 创建包装监听器，用于处理最大时长限制
        RecordStateListener wrapperStateListener = new RecordStateListener() {
            @Override
            public void onStateChange(RecordHelper.RecordState state) {
                if (stateListener != null) {
                    stateListener.onStateChange(state);
                }
            }

            @Override
            public void onError(String error) {
                if (stateListener != null) {
                    stateListener.onError(error);
                }
            }
        };
        
        // 设置监听器
        recordManager.setRecordStateListener(wrapperStateListener);
        recordManager.setRecordResultListener(resultListener);
        
        // 启动录音
        recordManager.start();
        
        // 如果设置了最大录制时长，启动定时器
        int maxDuration = getMaxDuration();
        if (maxDuration > 0) {
            LogService.logd(TAG, "设置最大录制时长定时器：" + maxDuration + "秒");
            
            // 取消之前的定时器（如果有）
            if (stopRecordingRunnable != null) {
                handler.removeCallbacks(stopRecordingRunnable);
            }
            
            // 创建新的定时器
            stopRecordingRunnable = new Runnable() {
                @Override
                public void run() {
                    LogService.logd(TAG, "达到最大录制时长：" + maxDuration + "秒，自动停止录音");
                    stop();
                }
            };
            
            // 启动定时器
            handler.postDelayed(stopRecordingRunnable, maxDuration * 1000);
        }
    }

    /**
     * 暂停录音
     */
    public void pause() {
        LogService.logd(TAG, "暂停录音");
        recordManager.pause();
        
        // 暂停时，取消定时器
        if (stopRecordingRunnable != null) {
            handler.removeCallbacks(stopRecordingRunnable);
        }
    }

    /**
     * 恢复录音
     */
    public void resume() {
        LogService.logd(TAG, "恢复录音");
        recordManager.resume();
        
        // 恢复录音时，重新计算剩余时间并设置定时器
        int maxDuration = getMaxDuration();
        if (maxDuration > 0) {
            // TODO: 这里需要计算已录制时长，目前简化处理，直接重新开始计时
            if (stopRecordingRunnable != null) {
                handler.postDelayed(stopRecordingRunnable, maxDuration * 1000);
            }
        }
    }

    /**
     * 停止录音
     */
    public void stop() {
        LogService.logd(TAG, "停止录音");
        recordManager.stop();
        
        // 停止时，取消定时器
        if (stopRecordingRunnable != null) {
            handler.removeCallbacks(stopRecordingRunnable);
            stopRecordingRunnable = null;
        }
    }

    /**
     * 获取本地音频时长（秒）
     *
     * @param audioPath 音频文件路径
     * @return 时长（秒）
     */
    public static int getAudioDuration(String audioPath) {
        try {
            MediaMetadataRetriever mmr = new MediaMetadataRetriever();
            mmr.setDataSource(audioPath);
            int duration = Integer.parseInt(mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)) / 1000;
            return duration;
        } catch (Exception e) {
            LogService.loge(TAG, "获取音频时长失败", e);
            return 0;
        }
    }
} 