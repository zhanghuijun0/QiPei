
package com.gammainfo.qipei.voice;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;

import com.baidu.voicerecognition.android.VoiceRecognitionClient;
import com.baidu.voicerecognition.android.VoiceRecognitionConfig;

public class AudioRecordThread extends Thread {
    private final static String TAG = "AudioRecordThread";

    private Context mContext;
    private volatile boolean mStop = false;

    public AudioRecordThread(Context context) {
        super();

        mContext = context;
    }

    public void exit() {
        mStop = true;
    }

    @Override
    public void run() {
        Log.d(TAG, " audio thread start");
        int recBufSize = AudioRecord.getMinBufferSize(VoiceRecognitionConfig.SAMPLE_RATE_8K, AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT);
        AudioRecord audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, VoiceRecognitionConfig.SAMPLE_RATE_8K,
                AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, recBufSize);

        byte[] buffer = new byte[recBufSize];
        audioRecord.startRecording();
        while (!mStop) {
            int bufferReadResult = audioRecord.read(buffer, 0, recBufSize);
            Log.d(TAG, "bufferReadResult = " + bufferReadResult);
            if (bufferReadResult > 0) {
                VoiceRecognitionClient.getInstance(mContext).feedAudioBuffer(buffer, 0, bufferReadResult);
            } else {
                audioRecord.startRecording();
            }
        }
        audioRecord.stop();
        audioRecord.release();

        Log.d(TAG, " audio thread exit");
    }
}
