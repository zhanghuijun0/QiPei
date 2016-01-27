package com.gammainfo.qipei.utils;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager.LayoutParams;

import com.gammainfo.qipei.R;

public class PhotoChoose extends Dialog implements OnClickListener {

	private OnPhotoChooseListener mOnPhotoChooseListener;

	public PhotoChoose(Context context) {
		super(context);
	}

	public PhotoChoose(Context context, int theme) {
		super(context, theme);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dialog_photo_choose);
		findViewById(R.id.btn_photo_choose_album).setOnClickListener(this);
		findViewById(R.id.btn_photo_choose_camera).setOnClickListener(this);
		findViewById(R.id.btn_photo_choose_cancel).setOnClickListener(this);
		Window win = getWindow();
		LayoutParams params = win.getAttributes();
		params.gravity = Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM;
		params.width = getLayoutInflater().getContext().getResources()
				.getDisplayMetrics().widthPixels;
		win.setAttributes(params);
		setCanceledOnTouchOutside(true);// 设置点击Dialog外部任意区域关闭Dialog
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_photo_choose_album:
			if (mOnPhotoChooseListener != null) {
				mOnPhotoChooseListener.album(this);
			}
			break;
		case R.id.btn_photo_choose_camera:
			if (mOnPhotoChooseListener != null) {
				mOnPhotoChooseListener.camera(this);
			}
			break;
		case R.id.btn_photo_choose_cancel:
			if (mOnPhotoChooseListener != null) {
				mOnPhotoChooseListener.cancel(this);
			}
			break;
		}
	}

	public void setOnPhotoChooseListener(
			OnPhotoChooseListener onPhotoChooseListener) {
		mOnPhotoChooseListener = onPhotoChooseListener;
	}

	public static interface OnPhotoChooseListener {
		void camera(PhotoChoose target);

		void album(PhotoChoose target);

		void cancel(PhotoChoose target);
	}

	public static PhotoChoose build(Context context,
			OnPhotoChooseListener onPhotoChooseListener) {
		PhotoChoose pc = new PhotoChoose(context, R.style.AlertDialog);
		pc.setOnPhotoChooseListener(onPhotoChooseListener);
		return pc;
	}
}
