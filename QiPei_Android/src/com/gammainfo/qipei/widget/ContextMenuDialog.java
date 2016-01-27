package com.gammainfo.qipei.widget;

import java.util.ArrayList;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager.LayoutParams;
import android.widget.Button;
import android.widget.LinearLayout;

import com.gammainfo.qipei.R;

public class ContextMenuDialog extends Dialog implements OnClickListener {

	private OnContextMenuListener mOnContextMenuListener;
	private ArrayList<String> mDataSource;
	private LayoutInflater mLayoutInflater;
	private LinearLayout mContentView;

	public ContextMenuDialog(Context context) {
		super(context);
	}

	public ContextMenuDialog(Context context, int theme) {
		super(context, theme);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mLayoutInflater = getLayoutInflater();
		mContentView = (LinearLayout) mLayoutInflater.inflate(
				R.layout.dialog_context_menu, null);
		setContentView(mContentView);
		Window win = getWindow();
		LayoutParams params = win.getAttributes();
		params.gravity = Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM;
		params.width = getLayoutInflater().getContext().getResources()
				.getDisplayMetrics().widthPixels;
		win.setAttributes(params);
		setCanceledOnTouchOutside(true);// 设置点击Dialog外部任意区域关闭Dialog
		buildView();
	}

	private void buildView() {
		int spacing = (int) TypedValue.applyDimension(
				TypedValue.COMPLEX_UNIT_DIP, 10, getContext().getResources()
						.getDisplayMetrics());
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.MATCH_PARENT,
				LinearLayout.LayoutParams.WRAP_CONTENT);
		params.bottomMargin = spacing;
		int size = mDataSource.size();
		for (int i = 0; i < size; i++) {
			Button btn = (Button) mLayoutInflater.inflate(
					R.layout.context_menu_normal_item, null);
			btn.setText(mDataSource.get(i));
			btn.setId(i);
			btn.setOnClickListener(this);
			btn.setLayoutParams(params);
			mContentView.addView(btn);
		}
		// add cancel item
		View cancelView = mLayoutInflater.inflate(
				R.layout.context_menu_cancel_item, null);
		cancelView.setOnClickListener(this);
		LinearLayout.LayoutParams cancelParams = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.MATCH_PARENT,
				LinearLayout.LayoutParams.WRAP_CONTENT);
		cancelParams.bottomMargin = spacing;
		cancelParams.topMargin = spacing / 2;
		cancelView.setLayoutParams(cancelParams);
		cancelView.setId(size);
		mContentView.addView(cancelView);
	}

	@Override
	public void onClick(View v) {
		int index = v.getId();
		if (index == mDataSource.size()) {
			// cancel item
			if (mOnContextMenuListener != null) {
				mOnContextMenuListener.onCancelClick(this);
			}
		} else {
			// normal item
			if (mOnContextMenuListener != null) {
				mOnContextMenuListener.onItemClick(this, index);
			}
		}
	}

	public void setOnContextMenuListener(
			OnContextMenuListener onContextMenuListener) {
		mOnContextMenuListener = onContextMenuListener;
	}

	public void setDataSource(ArrayList<String> dataSource) {
		mDataSource = dataSource;
		if (mContentView != null) {
			mContentView.removeAllViews();
			buildView();
		}
	}

	public static interface OnContextMenuListener {

		void onItemClick(ContextMenuDialog target, int index);

		void onCancelClick(ContextMenuDialog target);
	}

	public static ContextMenuDialog build(Context context,
			OnContextMenuListener onContextMenuListener,
			ArrayList<String> dataSource) {
		ContextMenuDialog pc = new ContextMenuDialog(context,
				R.style.AlertDialog);
		pc.setOnContextMenuListener(onContextMenuListener);
		pc.setDataSource(dataSource);
		return pc;
	}
}
