package com.gammainfo.qipei;

import java.lang.ref.WeakReference;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.widget.Toast;

import com.gammainfo.qipei.utils.Constant;
import com.umeng.analytics.MobclickAgent;

public class SplashActivity extends Activity {
	private static final long DELAY_MILLIS = 3 * 1000;
	private static final int MSG_WHAT_STARTMAIN = 1;

	private Handler mHandler;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_splash);
		if (Constant.DEBUG) {
			Toast.makeText(getApplicationContext(), Constant.API_URL_PRE,
					Toast.LENGTH_SHORT).show();
		}
		mHandler = new MyHandler(this);
		mHandler.sendEmptyMessageDelayed(MSG_WHAT_STARTMAIN, DELAY_MILLIS);
	}

	@Override
	public void onResume() {
		super.onResume();
		MobclickAgent.onResume(this);
	}

	@Override
	public void onPause() {
		super.onPause();
		MobclickAgent.onPause(this);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
			mHandler.removeMessages(MSG_WHAT_STARTMAIN);
		}
		return super.onKeyDown(keyCode, event);
	}

	private static class MyHandler extends Handler {
		WeakReference<SplashActivity> mActivity;

		MyHandler(SplashActivity activity) {
			mActivity = new WeakReference<SplashActivity>(activity);
		}

		@Override
		public void handleMessage(Message msg) {
			SplashActivity theActivity = mActivity.get();
			if (theActivity != null) {
				switch (msg.what) {
				case MSG_WHAT_STARTMAIN:
					theActivity.startActivity(new Intent(theActivity,
							MainActivity.class));
					break;
				}
			}
		}
	};
}
