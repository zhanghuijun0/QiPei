package com.gammainfo.qipei.widget;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.gammainfo.qipei.R;

public class AlertDialog extends Dialog implements View.OnClickListener {
	private OnAlertDialogListener mButtonselcetor;
	private Context mContext;
	private TextView messageView;
	private Button sureButton;
	private Button cancelButton;
	private String mMessage;
	private String mOkButtonMessage;
	private String mCancelButtonMessage;

	public AlertDialog(Context context) {
		super(context);
		this.mContext = context;
	}

	public AlertDialog(Context context, int theme) {
		super(context, theme);
		this.mContext = context;
	}

	public void setOnButtonListener(OnAlertDialogListener buttonListener) {
		mButtonselcetor = buttonListener;
	}

	public static interface OnAlertDialogListener {
		void onOk(AlertDialog alertDialogView);

		void onCancel(AlertDialog alertDialogView);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dialog_alert);
		messageView = (TextView) findViewById(R.id.dialog_textView);
		sureButton = (Button) findViewById(R.id.dialog_sure_button);
		cancelButton = (Button) findViewById(R.id.dialog_cancel_button);
		messageView.setText(mMessage);
		sureButton.setText(mOkButtonMessage);
		cancelButton.setText(mCancelButtonMessage);
		sureButton.setOnClickListener(this);
		cancelButton.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.dialog_sure_button) {
			mButtonselcetor.onOk(this);
		} else {
			mButtonselcetor.onCancel(this);
		}
	}

	public static AlertDialog build(Context context, String message,
			String sureButtonMessage, String cancelButtonMessage,
			OnAlertDialogListener onButtonSelectorListener) {
		AlertDialog alertDialogView = new AlertDialog(context,
				R.style.AlertDialog);
		alertDialogView.mMessage = message;
		alertDialogView.mOkButtonMessage = sureButtonMessage;
		alertDialogView.mCancelButtonMessage = cancelButtonMessage;
		alertDialogView.setOnButtonListener(onButtonSelectorListener);
		return alertDialogView;
	}

	public static AlertDialog build(Context context, int resMsgId, int resOkId,
			int resCancelId, OnAlertDialogListener onAlertDialogListener) {
		return build(context, context.getString(resMsgId),
				context.getString(resOkId), context.getString(resCancelId),
				onAlertDialogListener);
	}

}
