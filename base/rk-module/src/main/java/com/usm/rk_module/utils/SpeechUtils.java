package com.usm.rk_module.utils;

import android.content.Context;
import android.media.AudioManager;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;

import java.util.HashMap;
import java.util.Locale;


public class SpeechUtils {


    private static final String TAG = "SpeechUtils";
    private static SpeechUtils singleton;

    private TextToSpeech textToSpeech; // TTS对象

    private AudioManager audioManager;
    private AudioManager.OnAudioFocusChangeListener audioFocusChangeListener;

    private boolean isSpeaking = false;


    public static SpeechUtils getInstance(Context context) {
        Log.d(TAG, "getInstance: 0");
        if (singleton == null) {
            Log.d(TAG, "getInstance: 1");
            synchronized (SpeechUtils.class) {
                if (singleton == null) {
                    Log.d(TAG, "getInstance: 2");
                    singleton = new SpeechUtils(context);
                }
            }
        }
        return singleton;
    }

    public SpeechUtils(Context context) {

        textToSpeech = new TextToSpeech(context, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int i) {
                if (i == TextToSpeech.SUCCESS) {
                    // Toast.makeText(MainActivity.this,"成功输出语音",
                    // Toast.LENGTH_SHORT).show();
                    // Locale loc1=new Locale("us");
                    // Locale loc2=new Locale("china");

                    textToSpeech.setPitch(1.0f);//方法用来控制音调
                    textToSpeech.setSpeechRate(1.0f);//用来控制语速

                    //判断是否支持下面两种语言
                    int result1 = textToSpeech.setLanguage(Locale.US);
                    int result2 = textToSpeech.setLanguage(Locale.
                            CHINA);
                    boolean a = (result1 == TextToSpeech.LANG_MISSING_DATA || result1 == TextToSpeech.LANG_NOT_SUPPORTED);
                    boolean b = (result2 == TextToSpeech.LANG_MISSING_DATA || result2 == TextToSpeech.LANG_NOT_SUPPORTED);

                    Log.i("zhh_tts", "US支持否？--》" + a +
                            "\nzh-CN支持否》--》" + b);
                    audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
                    audioFocusChangeListener = new AudioManager.OnAudioFocusChangeListener() {
                        @Override
                        public void onAudioFocusChange(int focusChange) {
                            Log.e(TAG,"focusChange="+focusChange);
                            // 在这里处理音频焦点变化的逻辑
                            if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {
                                // 当前应用失去音频焦点，停止语音播放
                                if (isSpeaking) {
                                    audioManager.abandonAudioFocus(audioFocusChangeListener);
                                    isSpeaking = false; // 设置为非正在播放语音
                                }
                            } else if (focusChange == AudioManager.AUDIOFOCUS_GAIN) {
                                // 当前应用获得音频焦点，可以继续或重新开始语音播放
                                // 例如，重新调用textToSpeech.speak()方法
                            }
                        }
                    };

                }
            }
        });
    }

    public void speakText(String text) {

        if (textToSpeech != null) {
            int result = audioManager.requestAudioFocus(audioFocusChangeListener,
                    AudioManager.STREAM_MUSIC,
                    AudioManager.AUDIOFOCUS_GAIN);
            Log.e(TAG,"请求音频焦点成功="+result);

            if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                Log.d(TAG, "speakText: AudioManager.AUDIOFOCUS_REQUEST_GRANTED");
                HashMap<String, String> params = new HashMap<>();
                params.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "speech");
                textToSpeech.setOnUtteranceProgressListener(utteranceProgressListener);
                textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, params);
            }

        }

    }
    private UtteranceProgressListener utteranceProgressListener = new UtteranceProgressListener() {
        @Override
        public void onStart(String utteranceId) {
            // 当语音播放开始时执行的操作
        }

        @Override
        public void onDone(String utteranceId) {
            // 当语音播放完成时执行的操作
            // 在这里释放音频焦点
            audioManager.abandonAudioFocus(audioFocusChangeListener);
        }

        @Override
        public void onError(String utteranceId) {
            // 当语音播放发生错误时执行的操作
        }
    };

}
