package com.gammainfo.qipei;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class CityChangedDialog extends Dialog implements View.OnClickListener {

	private Context mContext;
	private OnCityChangedListener mOnCityChanged;
	private TextView mCurrentCityTv;
	private TextView mTargetCityTv;
	private String mCurrentCity;
	private String mTargetCity;

	public CityChangedDialog(Context context) {
		super(context);
		this.mContext = context;
	}

	public CityChangedDialog(Context context, int theme) {
		super(context, theme);
		this.mContext = context;
	}

	public void setOnCityChangedListener(
			OnCityChangedListener onCityChangedListener) {
		mOnCityChanged = onCityChangedListener;
	}

	public void setCity(String currentCity, String targetCity) {
		mCurrentCity = currentCity;
		mTargetCity = targetCity;
		if (mCurrentCityTv != null) {
			mCurrentCityTv.setText(mCurrentCity);
			mTargetCityTv.setText(mTargetCity);
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dialog_city_changed);
		mCurrentCityTv = (TextView) findViewById(R.id.tv_city_changed_current_city);
		mTargetCityTv = (TextView) findViewById(R.id.tv_city_changed_target_city);
		mCurrentCityTv.setText(mCurrentCity);
		mTargetCityTv.setText(mTargetCity);
		findViewById(R.id.btn_city_changed_ok).setOnClickListener(this);
		findViewById(R.id.btn_city_changed_cancel).setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.btn_city_changed_ok) {
			if (mOnCityChanged != null) {
				mOnCityChanged.onOk(this, mTargetCity);
			}
		} else {
			if (mOnCityChanged != null) {
				mOnCityChanged.onCancel(this);
			}
		}
	}

	public static interface OnCityChangedListener {
		void onOk(CityChangedDialog cityChangedDialog, String targetCity);

		void onCancel(CityChangedDialog cityChangedDialog);
	}

	public static CityChangedDialog build(Context context, String currentCity,
			String targetCity, OnCityChangedListener onCityChangedListener) {
		CityChangedDialog cityChangedDialog = new CityChangedDialog(context,
				R.style.NoTitleTransparentDialog);
		cityChangedDialog.setOnCityChangedListener(onCityChangedListener);
		cityChangedDialog.setCity(currentCity, targetCity);
		return cityChangedDialog;
	}
}