package com.gammainfo.qipei.widget;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.gammainfo.qipei.R;

public class UploadDialog extends Dialog implements View.OnClickListener {
	private OnUploadDialogListener mButtonselcetor;
	private TextView mDescView;
	private TextView mTitleView;
	private View mRetryButton;
	private String mTitle;
	private String mDesc;

	public UploadDialog(Context context) {
		super(context);
	}

	public UploadDialog(Context context, int theme) {
		super(context, theme);
	}

	public void setOnUploadListener(OnUploadDialogListener buttonListener) {
		mButtonselcetor = buttonListener;
	}

	public static interface OnUploadDialogListener {
		void onRetry(UploadDialog targetDialog);

		void onCancel(UploadDialog targetDialog);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dialog_upload);
		mTitleView = (TextView) findViewById(R.id.dialog_upload_title);
		mDescView = (TextView) findViewById(R.id.dialog_upload_desc);
		mDescView.setText(mDesc);
		mTitleView.setText(mTitle);
		mRetryButton = findViewById(R.id.btn_dialog_upload_retry);
		mRetryButton.setOnClickListener(this);
		findViewById(R.id.btn_dialog_upload_cancel).setOnClickListener(this);
		setCanceledOnTouchOutside(false);
		setCancelable(false);
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.btn_dialog_upload_retry) {
			mButtonselcetor.onRetry(this);
		} else {
			mButtonselcetor.onCancel(this);
		}
	}

	public void showRetryButton() {
		mRetryButton.setVisibility(View.VISIBLE);
	}

	public void hideRetryButton() {
		mRetryButton.setVisibility(View.GONE);
	}

	@Override
	public void setTitle(CharSequence title) {
		setTitle(title.toString());
	}

	@Override
	public void setTitle(int titleId) {
		setTitle(getContext().getResources().getString(titleId));
	}

	public void setTitle(String title) {
		mTitle = title;
		if (mTitleView != null) {
			mTitleView.setText(mTitle);
		}
	}

	public void setDesc(String desc) {
		mDesc = desc;
		if (mDescView != null) {
			mDescView.setText(mDesc);
		}
	}

	public static UploadDialog build(Context context, String title,
			String desc, OnUploadDialogListener onButtonSelectorListener) {
		UploadDialog dialog = new UploadDialog(context, R.style.AlertDialog);
		dialog.setTitle(title);
		dialog.setDesc(desc);
		dialog.setOnUploadListener(onButtonSelectorListener);
		return dialog;
	}

	public static UploadDialog build(Context context, int resTitleId,
			int resDescId, int resCancelId,
			OnUploadDialogListener onAlertDialogListener) {
		return build(context, context.getString(resTitleId),
				context.getString(resDescId), onAlertDialogListener);
	}

}
