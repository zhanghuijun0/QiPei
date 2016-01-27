package com.gammainfo.qipei.voice;

import java.util.List;
import java.util.WeakHashMap;

import android.app.Dialog;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.voicerecognition.android.Candidate;
import com.baidu.voicerecognition.android.VoiceRecognitionClient;
import com.baidu.voicerecognition.android.VoiceRecognitionClient.VoiceClientStatusChangeListener;
import com.baidu.voicerecognition.android.VoiceRecognitionConfig;
import com.gammainfo.qipei.R;

public class VoiceRecognitionDialog extends Dialog implements OnClickListener {
	private static final String API_KEY = "LK8rwZdffj2DargMDBIsTfgy";

	private static final String SECRET_KEY = "3IFpEIrHaAOurkLk8k38elM8CyGKBKxA";

	private Context mContext;
	private Button mFinishButton = null;
	private ImageView mVolumeBar = null;
	private TextView mStatusTextView = null;
	private boolean isRecognition = false;
	private Handler mHandler;
	/** 音量更新间隔 */
	private static final int POWER_UPDATE_INTERVAL = 100;
	private int currentVoiceType = Config.VOICE_TYPE_SEARCH;
	private MyVoiceRecogListener mListener = new MyVoiceRecogListener();
	private Runnable mUpdateVolume = new UpdateVolum();
	private VoiceRecognitionClient mClient;
	private AudioRecordThread mAudioRecordThread;

	private AudioFileThread mAudioFileThread;
	private View mVoiceContainer;
	private View mRetryContainer;
	private OnVoiceRecognitionListener mOnVoiceRecognitionListener;
	private WeakHashMap<Integer, Drawable> mSpeakerList;

	public VoiceRecognitionDialog(Context context) {
		super(context);
		this.mContext = context;
	}

	public VoiceRecognitionDialog(Context context, int theme) {
		super(context, theme);
		this.mContext = context;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dialog_voice_recognition);
		mClient = VoiceRecognitionClient.getInstance(mContext);
		mClient.setTokenApis(API_KEY, SECRET_KEY);
		mHandler = new Handler();
		mFinishButton = (Button) this
				.findViewById(R.id.btn_voice_recognitin_dialog_finish);
		mFinishButton.setOnClickListener(this);
		mVolumeBar = (ImageView) this
				.findViewById(R.id.iv_voice_recognition_speaker);
		mStatusTextView = (TextView) this
				.findViewById(R.id.tv_voice_recognition_dialog_tips);
		findViewById(R.id.ibtn_voice_recognition_dialog_close)
				.setOnClickListener(this);
		findViewById(R.id.btn_voice_recognitin_dialog_retry)
				.setOnClickListener(this);
		findViewById(R.id.btn_voice_recognitin_dialog_cancel)
				.setOnClickListener(this);
		mRetryContainer = findViewById(R.id.rl_voice_recognition_retry_container);
		mVoiceContainer = findViewById(R.id.rl_voice_recognition_voice_container);
	}

	@Override
	public void show() {
		if (isShowing()) {
			return;
		}
		super.show();
		start();
	}

	@Override
	public void dismiss() {
		mClient.stopVoiceRecognition();
		stopRecordThread();
		super.dismiss();
	}

	/**
	 * 重写用于处理语音识别回调的监听器
	 */
	class MyVoiceRecogListener implements VoiceClientStatusChangeListener {

		@Override
		public void onClientStatusChange(int status, Object obj) {
			switch (status) {
			// 语音识别实际开始，这是真正开始识别的时间点，需在界面提示用户说话。
			case VoiceRecognitionClient.CLIENT_STATUS_START_RECORDING:
				isRecognition = true;
				mFinishButton.setEnabled(true);
				mFinishButton
						.setText(R.string.voice_recognition_label_speak_finish);
				mStatusTextView
						.setText(R.string.voice_recognition_label_please_speak);
				mHandler.removeCallbacks(mUpdateVolume);
				mHandler.postDelayed(mUpdateVolume, POWER_UPDATE_INTERVAL);
				break;
			case VoiceRecognitionClient.CLIENT_STATUS_SPEECH_START: // 检测到语音起点
				mStatusTextView
						.setText(R.string.voice_recognition_label_speaking);
				break;
			case VoiceRecognitionClient.CLIENT_STATUS_AUDIO_DATA:
				// if (obj != null && obj instanceof byte[]) {
				// Log.d(TAG, "obj is " + ((byte[]) obj));
				// }
				break;
			// 已经检测到语音终点，等待网络返回
			case VoiceRecognitionClient.CLIENT_STATUS_SPEECH_END:
				stopRecordThread();
				mStatusTextView
						.setText(R.string.voice_recognition_label_in_recog);
				mFinishButton.setEnabled(false);
				break;
			// 语音识别完成，显示obj中的结果
			case VoiceRecognitionClient.CLIENT_STATUS_FINISH:
				stopRecordThread();
				isRecognition = false;
				mFinishButton.setEnabled(true);
				// TODO 识别完成，返回结果
				updateRecognitionResult(obj, true);
				break;
			// 处理连续上屏
			case VoiceRecognitionClient.CLIENT_STATUS_UPDATE_RESULTS:
				updateRecognitionResult(obj, false);
				break;
			// 用户取消
			case VoiceRecognitionClient.CLIENT_STATUS_USER_CANCELED:
				mStatusTextView
						.setText(R.string.voice_recognition_label_is_canceled);
				isRecognition = false;
				mFinishButton.setEnabled(true);
				break;
			default:
				break;
			}

		}

		@Override
		public void onError(int errorType, int errorCode) {
			isRecognition = false;
			stopRecordThread();
			// TODO 显示重试界面
			mVoiceContainer.setVisibility(View.GONE);
			mRetryContainer.setVisibility(View.VISIBLE);
		}

		@Override
		public void onNetworkStatusChange(int status, Object obj) {
			// 这里不做任何操作不影响简单识别
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.ibtn_voice_recognition_dialog_close:
		case R.id.btn_voice_recognitin_dialog_cancel:
			mClient.stopVoiceRecognition();
			stopRecordThread();
			dismiss();
			break;
		case R.id.btn_voice_recognitin_dialog_retry:
			if (isRecognition) { // 用户说完
				mClient.speakFinish();
				stopRecordThread();
			} else { // 用户重试，开始新一次语音识别
				start();
			}
			break;
		case R.id.btn_voice_recognitin_dialog_finish:
			if (isRecognition) { // 用户说完
				mClient.speakFinish();
				stopRecordThread();
			}
			break;
		default:
			break;
		}

	}

	private void start() {
		mFinishButton.setEnabled(false);
		mFinishButton.setText(R.string.voice_recognition_label_mic_reading);
		mStatusTextView.setText(R.string.voice_recognition_label_please_wait);
		mVoiceContainer.setVisibility(View.VISIBLE);
		mRetryContainer.setVisibility(View.GONE);
		// 需要开始新识别,首先设置参数
		VoiceRecognitionConfig config = new VoiceRecognitionConfig();
		currentVoiceType = Config.VOICE_TYPE;
		config.setLanguage(Config.getCurrentLanguage());
		if (currentVoiceType == Config.VOICE_TYPE_INPUT) {
			config.setSpeechMode(VoiceRecognitionConfig.SPEECHMODE_MULTIPLE_SENTENCE);
		} else {
			config.setSpeechMode(VoiceRecognitionConfig.SPEECHMODE_SINGLE_SENTENCE);
			if (Config.enableNLU) {
				config.enableNLU();
			}
		}
		config.enableVoicePower(Config.SHOW_VOL); // 音量反馈。
		if (Config.PLAY_START_SOUND) {
			config.enableBeginSoundEffect(R.raw.bdspeech_recognition_start); // 设置识别开始提示音
		}
		if (Config.PLAY_END_SOUND) {
			config.enableEndSoundEffect(R.raw.bdspeech_speech_end); // 设置识别结束提示音
		}
		config.setSampleRate(VoiceRecognitionConfig.SAMPLE_RATE_8K); // 设置采样率
		if (!Config.USE_DEFAULT_AUDIO_SOURCE) {
			config.setUseDefaultAudioSource(false);
		}
		// 下面发起识别
		int code = VoiceRecognitionClient.getInstance(mContext)
				.startVoiceRecognition(mListener, config);
		if (code == VoiceRecognitionClient.START_WORK_RESULT_WORKING) { // 能够开始识别，改变界面
			mFinishButton.setEnabled(true);
			mFinishButton
					.setText(R.string.voice_recognition_label_speak_finish);

			if (!Config.USE_DEFAULT_AUDIO_SOURCE) {
				// audio record sample
				mAudioRecordThread = new AudioRecordThread(
						mContext.getApplicationContext());
				mAudioRecordThread.start();
			}
		} else {
			Toast.makeText(
					mContext,
					mContext.getString(
							R.string.voice_recognition_toast_error_start, code),
					Toast.LENGTH_SHORT).show();
			dismiss();
		}
	}

	/**
	 * 将识别结果更新到UI上，搜索模式结果类型为List<String>,输入模式结果类型为List<List<Candidate>>
	 * 
	 * @param result
	 */
	private void updateRecognitionResult(Object result, boolean isFinish) {
		if (result != null && result instanceof List) {
			List results = (List) result;
			if (results.size() > 0) {
				if (currentVoiceType == Config.VOICE_TYPE_SEARCH) {
					if (isFinish) {
						if (mOnVoiceRecognitionListener != null) {
							mOnVoiceRecognitionListener.onFinish(this, results
									.get(0).toString());
						}
					} else {

					}
				} else if (currentVoiceType == Config.VOICE_TYPE_INPUT) {
					List<List<Candidate>> sentences = (List<List<Candidate>>) result;
					StringBuffer sb = new StringBuffer();
					for (List<Candidate> candidates : sentences) {
						if (candidates != null && candidates.size() > 0) {
							sb.append(candidates.get(0).getWord());
						}
					}
					// Toast.makeText(mContext, "识别结果：" + sb.toString(),
					// Toast.LENGTH_SHORT).show();
				}
			}
		}
	}

	private void stopRecordThread() {
		if (mAudioRecordThread != null) {
			mAudioRecordThread.exit();
			mAudioRecordThread = null;
		}
		if (mAudioFileThread != null) {
			mAudioFileThread.exit();
			mAudioFileThread = null;
		}
	}

	class UpdateVolum implements Runnable {
		public void run() {
			if (isRecognition) {
				int vol = (int) VoiceRecognitionClient.getInstance(mContext)
						.getCurrentDBLevelMeter();
				mVolumeBar.setImageDrawable(getSpeakerDrawable(vol % 100));
				mHandler.removeCallbacks(mUpdateVolume);
				mHandler.postDelayed(mUpdateVolume, POWER_UPDATE_INTERVAL);
			}
		}
	}

	public void setOnVoiceRecognitionListener(
			OnVoiceRecognitionListener onVoiceRecognitionListener) {
		mOnVoiceRecognitionListener = onVoiceRecognitionListener;
	}

	public static interface OnVoiceRecognitionListener {
		void onFinish(VoiceRecognitionDialog dialog, String word);

		void onError(VoiceRecognitionDialog dialog, String error);
	}

	public static VoiceRecognitionDialog build(Context context,
			OnVoiceRecognitionListener onVoiceRecognitionListener) {
		VoiceRecognitionDialog dialog = new VoiceRecognitionDialog(context,
				R.style.NoTitleTransparentDialog);
		dialog.setOnVoiceRecognitionListener(onVoiceRecognitionListener);
		return dialog;
	}

	public Drawable getSpeakerDrawable(int key) {
		if (mSpeakerList == null) {
			mSpeakerList = new WeakHashMap<Integer, Drawable>();
		}
		final int FACTOR = 5;
		Drawable drawable = mSpeakerList.get(key);
		if (drawable == null) {
			Resources res = mContext.getResources();
			if (key < 1 * FACTOR) {
				// < 1*FACTOR
				mSpeakerList
						.put(1,
								res.getDrawable(R.drawable.ic_voice_recognition_dialog_speaker_01));
			} else if (key < 2 * FACTOR) {
				mSpeakerList
						.put(2,
								res.getDrawable(R.drawable.ic_voice_recognition_dialog_speaker_02));
			} else if (key < 3 * FACTOR) {
				mSpeakerList
						.put(3,
								res.getDrawable(R.drawable.ic_voice_recognition_dialog_speaker_03));
			} else if (key < 4 * FACTOR) {
				mSpeakerList
						.put(4,
								res.getDrawable(R.drawable.ic_voice_recognition_dialog_speaker_04));
			} else if (key < 5 * FACTOR) {
				mSpeakerList
						.put(5,
								res.getDrawable(R.drawable.ic_voice_recognition_dialog_speaker_05));
			} else if (key < 6 * FACTOR) {
				mSpeakerList
						.put(6,
								res.getDrawable(R.drawable.ic_voice_recognition_dialog_speaker_06));
			} else if (key < 7 * FACTOR) {
				mSpeakerList
						.put(7,
								res.getDrawable(R.drawable.ic_voice_recognition_dialog_speaker_07));
			} else if (key < 8 * FACTOR) {
				mSpeakerList
						.put(8,
								res.getDrawable(R.drawable.ic_voice_recognition_dialog_speaker_08));
			} else if (key < 9 * FACTOR) {
				mSpeakerList
						.put(9,
								res.getDrawable(R.drawable.ic_voice_recognition_dialog_speaker_09));
			} else if (key < 10 * FACTOR) {
				mSpeakerList
						.put(10,
								res.getDrawable(R.drawable.ic_voice_recognition_dialog_speaker_10));
			} else if (key < 11 * FACTOR) {
				mSpeakerList
						.put(11,
								res.getDrawable(R.drawable.ic_voice_recognition_dialog_speaker_11));
			} else if (key < 12 * FACTOR) {
				mSpeakerList
						.put(12,
								res.getDrawable(R.drawable.ic_voice_recognition_dialog_speaker_12));
			} else if (key < 13 * FACTOR) {
				mSpeakerList
						.put(13,
								res.getDrawable(R.drawable.ic_voice_recognition_dialog_speaker_13));
			} else if (key < 14 * FACTOR) {
				mSpeakerList
						.put(14,
								res.getDrawable(R.drawable.ic_voice_recognition_dialog_speaker_14));
			} else if (key < 15 * FACTOR) {
				mSpeakerList
						.put(15,
								res.getDrawable(R.drawable.ic_voice_recognition_dialog_speaker_15));
			} else if (key < 16 * FACTOR) {
				mSpeakerList
						.put(16,
								res.getDrawable(R.drawable.ic_voice_recognition_dialog_speaker_16));
			} else if (key < 17 * FACTOR) {
				mSpeakerList
						.put(17,
								res.getDrawable(R.drawable.ic_voice_recognition_dialog_speaker_17));
			} else if (key < 18 * FACTOR) {
				mSpeakerList
						.put(18,
								res.getDrawable(R.drawable.ic_voice_recognition_dialog_speaker_18));
			} else if (key < 19 * FACTOR) {
				mSpeakerList
						.put(19,
								res.getDrawable(R.drawable.ic_voice_recognition_dialog_speaker_19));
			} else {
				// >= 20*FACTOR
				mSpeakerList
						.put(20,
								res.getDrawable(R.drawable.ic_voice_recognition_dialog_speaker_20));
			}

		}
		return drawable;
	}
}