package com.gammainfo.qipei.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout.LayoutParams;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.gammainfo.qipei.R;

public class ToastHelper {

	private Context mContext;
	private TextView mTextTv;
	private PopupWindow mPopupWindow;

	public ToastHelper(Context context, String text) {
		mContext = context;
		View popupView = LayoutInflater.from(context).inflate(
				R.layout.dialog_toast, null);
		mPopupWindow = new PopupWindow(popupView, LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT, false);
		mPopupWindow.setOutsideTouchable(false);
		mPopupWindow.setTouchable(false);
		mPopupWindow.setBackgroundDrawable(new BitmapDrawable(context
				.getResources(), (Bitmap) null));
		mTextTv = (TextView) popupView.findViewById(R.id.tv_toast_text);
		mTextTv.setText(text);
	}

	public static ToastHelper make(Context context, String text) {
		return new ToastHelper(context, text);
	}

	public static ToastHelper make(Context context, int resId) {
		return make(context, context.getString(resId));
	}

	public ToastHelper setText(String text) {
		mTextTv.setText(text);
		return this;
	}

	public void show(View parentView, int gravity, int offsetX, int offsetY) {
		mPopupWindow.showAtLocation(parentView, gravity, offsetX, offsetY);
	}

	/**
	 * 以parentView水平居中，垂直底部对齐的方式显示Toast
	 * 
	 * @param parentView
	 */
	public void show(View parentView) {
		mPopupWindow.showAtLocation(parentView, Gravity.CENTER_HORIZONTAL
				| Gravity.BOTTOM, 0, 5);
	}

	public void dismiss() {
		if (mPopupWindow != null) {
			mPopupWindow.dismiss();
		}
	}

	public void dismiss(String text, int delayMillis) {
		if (!TextUtils.isEmpty(text)) {
			setText(text);
		}
		new Handler().postDelayed(new Runnable() {

			@Override
			public void run() {
				dismiss();
			}
		}, delayMillis);
	}

	public boolean isShowing() {
		return mPopupWindow.isShowing();
	}
}