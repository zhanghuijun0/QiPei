package com.gammainfo.qipei.voice;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import android.content.Context;
import android.util.Log;

import com.baidu.voicerecognition.android.VoiceRecognitionClient;

public class AudioFileThread extends Thread {
	private final static String TAG = "AudioFileThread";

	private Context mContext;
	private String mFilePath;
	private volatile boolean mStop = false;

	public AudioFileThread(Context context, String filePath) {
		super();

		mContext = context;
		mFilePath = filePath;
	}

	public void exit() {
		mStop = true;
	}

	@Override
	public void run() {
		Log.d(TAG, " audio thread start mFilePath " + mFilePath);

		FileInputStream in = null;
		try {
			in = new FileInputStream(mFilePath);
		} catch (FileNotFoundException e) {
			Log.e(TAG, " e is " + e);
			return;
		}

		int length = 1024;
		byte[] buffer = new byte[length];
		while (!mStop) {
			try {
				int byteread = in.read(buffer);
				Log.d(TAG, " byteread: " + byteread);
				if (byteread != -1) {
					VoiceRecognitionClient.getInstance(mContext)
							.feedAudioBuffer(buffer, 0, byteread);
				} else {
					for (int i = 0; i < length; i++) {
						buffer[i] = 0;
					}
					VoiceRecognitionClient.getInstance(mContext)
							.feedAudioBuffer(buffer, 0, length);
				}
			} catch (IOException e) {
				Log.e(TAG, " e is " + e);
			}
		}

		if (in != null) {
			try {
				in.close();
			} catch (IOException e) {
				Log.e(TAG, " e is " + e);
			}
		}

		Log.d(TAG, " audio thread exit");
	}
}
