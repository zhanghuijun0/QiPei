package com.gammainfo.qipei;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class AreaBaseActivity extends BaseActivity {

	public static final String EXTRA_PROVINCE = "result_province";
	public static final String EXTRA_CITY = "result_city";
	public static final String EXTRA_COUNTY = "result_county";
	public static final String EXTRA_LEVEL = "level";
	public static final String EXTRA_ADDRESS = "result_address";
	public static final String EXTRA_AREA_SHORT = "area_short";
	public static final String EXTRA_AUTO_LOCATIOIN = "auto_location";
	public static final int LEVEL_PROVINCE = 1;
	public static final int LEVEL_PROVINCE_CITY = 2;
	public static final int LEVEL_PROVINCE_CITY_COUNTY = 3;
	protected Intent mGetIntent;
	protected boolean mIsAreaShort;
	protected int mLevel;
	protected boolean mAutoLocation;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mGetIntent = getIntent();
		mIsAreaShort = mGetIntent.getBooleanExtra(EXTRA_AREA_SHORT, false);
		mAutoLocation = mGetIntent.getBooleanExtra(EXTRA_AUTO_LOCATIOIN, true);
		mLevel = mGetIntent
				.getIntExtra(EXTRA_LEVEL, LEVEL_PROVINCE_CITY_COUNTY);
	}

	public void onBackClick(View v) {
		finish();
	}

	@Override
	public void startActivityForResult(Intent intent, int requestCode) {
		extraIntent(intent);
		super.startActivityForResult(intent, requestCode);
	}

	private void extraIntent(Intent intent) {
		if (intent == null) {
			intent = new Intent();
		}
		intent.putExtra(EXTRA_AREA_SHORT, mIsAreaShort);
		intent.putExtra(EXTRA_LEVEL, mLevel);
	}
}
