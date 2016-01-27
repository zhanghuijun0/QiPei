
package com.gammainfo.qipei.voice;

import com.baidu.voicerecognition.android.VoiceRecognitionConfig;
import com.baidu.voicerecognition.android.ui.BaiduASRDigitalDialog;

/**
 * 临时保存参数信息，Demo演示使用。开发者不须关注
 * 
 * @author yangliang02
 */
public class Config {

    /** 语音搜索模式 */
    public static int VOICE_TYPE = Config.VOICE_TYPE_SEARCH;

    /** 对话框样式 */
    public static int DIALOG_THEME = BaiduASRDigitalDialog.THEME_BLUE_LIGHTBG;

    /** 语音搜索模式 */
    public static final int VOICE_TYPE_SEARCH = 0;

    /** 语音输入模式 */
    public static final int VOICE_TYPE_INPUT = 1;

    /**
     * 当前识别语言
     */
    public static String CURRENT_LANGUAGE = VoiceRecognitionConfig.LANGUAGE_CHINESE;

    private static int CURRENT_LANGUAGE_INDEX = 0;

    public static String getCurrentLanguage() {
        return CURRENT_LANGUAGE;
    }

    public static int getCurrentLanguageIndex() {
        return CURRENT_LANGUAGE_INDEX;
    }

    public static void setCurrentLanguageIndex(int index) {
        switch (index) {
            case 1:
                CURRENT_LANGUAGE = VoiceRecognitionConfig.LANGUAGE_CANTONESE;
                break;
            case 2:
                CURRENT_LANGUAGE = VoiceRecognitionConfig.LANGUAGE_ENGLISH;
                break;

            default:
                CURRENT_LANGUAGE = VoiceRecognitionConfig.LANGUAGE_CHINESE;
                index = 0;
                break;
        }
        CURRENT_LANGUAGE_INDEX = index;
    }

    /**
     * 播放开始音
     */
    public static boolean PLAY_START_SOUND = true;

    /**
     * 播放结束音
     */
    public static boolean PLAY_END_SOUND = true;

    /**
     * 显示音量
     */
    public static boolean SHOW_VOL = true;

    /**
     * 是否启用语义解析
     */
    public static boolean enableNLU = false;

    /**
     * 使用麦克风录音输入源
     */
    public static boolean USE_DEFAULT_AUDIO_SOURCE = true;
}
