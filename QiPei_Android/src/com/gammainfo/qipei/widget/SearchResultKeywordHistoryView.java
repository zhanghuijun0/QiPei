package com.gammainfo.qipei.widget;

import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.gammainfo.qipei.R;
import com.gammainfo.qipei.model.helper.KeywordHelper;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshListView;

@SuppressLint("NewApi")
public class SearchResultKeywordHistoryView extends LinearLayout implements
		OnItemClickListener {
	private Context mContext;
	private LayoutInflater mLayoutInflater;
	private OnKeywordHistoryListener mOnKeywordHistoryListener;
	private PullToRefreshListView mKeywordHistoryListView;
	private KeywordHistoryAdapter mKeywordHistoryAdapter;
	private ArrayList<String> mKeywordList;
	private String mKeyword;
	private View mFooterView;
	private InputMethodManager mInputMethodManager;

	public SearchResultKeywordHistoryView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public SearchResultKeywordHistoryView(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	public SearchResultKeywordHistoryView(Context context) {
		super(context);
		init(context);
	}

	public static interface OnKeywordHistoryListener {
		/**
		 * 单击某一行时执行此回调
		 * 
		 * @param target
		 * @param keyword
		 */
		void onKeywordSelected(SearchResultKeywordHistoryView target,
				String keyword);

		/**
		 * 提取某个keyword时发生
		 * 
		 * @param target
		 * @param keyword
		 */
		void onKeywordPick(SearchResultKeywordHistoryView target, String keyword);

		void onSpeech(SearchResultKeywordHistoryView target);
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int position,
			long id) {
		if (mInputMethodManager == null) {
			mInputMethodManager = (InputMethodManager) mContext
					.getSystemService(Context.INPUT_METHOD_SERVICE);
		}
		mInputMethodManager.hideSoftInputFromWindow(getWindowToken(),
				InputMethodManager.HIDE_NOT_ALWAYS);
		// ajust postion
		if (mOnKeywordHistoryListener != null) {
			position = position - 1;
			mOnKeywordHistoryListener.onKeywordSelected(this,
					mKeywordList.get(position));
		}
	}

	private void init(Context context) {
		mContext = context;
		mLayoutInflater = LayoutInflater.from(context);
		mKeyword = null;
		View viewContainer = mLayoutInflater.inflate(
				R.layout.search_result_keyword_history_layout, null);
		viewContainer.findViewById(R.id.ibtn_search_speech).setOnClickListener(
				new OnClickListener() {

					@Override
					public void onClick(View v) {
						if (mOnKeywordHistoryListener != null) {
							mOnKeywordHistoryListener
									.onSpeech(SearchResultKeywordHistoryView.this);
						}
					}
				});
		mKeywordHistoryListView = (PullToRefreshListView) viewContainer
				.findViewById(R.id.prlv_search_keyword_history);
		mKeywordHistoryListView.setMode(Mode.DISABLED);
		mKeywordHistoryListView.setOnItemClickListener(this);
		mFooterView = mLayoutInflater.inflate(
				R.layout.search_result_keyword_history_footer_layout, null);
		mFooterView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				KeywordHelper.clear();
				generateKeywordList();
			}
		});
		mKeywordHistoryListView.getRefreshableView().addFooterView(mFooterView);
		mKeywordList = new ArrayList<String>();
		mKeywordHistoryAdapter = new KeywordHistoryAdapter();
		mKeywordHistoryListView.setAdapter(mKeywordHistoryAdapter);
		addView(viewContainer);
		// TODO 数据的获取可以优化到线程中
		generateKeywordList();
	}

	private void generateKeywordList() {
		mKeywordList = KeywordHelper.select(mKeyword);
		if (mKeywordList.isEmpty()
				|| (mKeyword != null && mKeyword.length() > 0)) {
			mFooterView.setVisibility(View.GONE);
		} else {
			mFooterView.setVisibility(View.VISIBLE);
		}
		mKeywordHistoryAdapter.notifyDataSetChanged();
	}

	private class KeywordHistoryAdapter extends BaseAdapter implements
			View.OnClickListener {

		@Override
		public int getCount() {
			return mKeywordList.size();
		}

		@Override
		public Object getItem(int position) {
			return mKeywordList.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder viewHolder;
			if (convertView == null) {
				viewHolder = new ViewHolder();
				convertView = mLayoutInflater.inflate(
						R.layout.keyword_history_listitem, null);
				viewHolder.mKeyTv = (TextView) convertView
						.findViewById(R.id.tv_keyword_history_key);
				viewHolder.mPickIv = (ImageView) convertView
						.findViewById(R.id.iv_keyword_history_pick);
				viewHolder.mPickIv.setOnClickListener(this);
				convertView.setTag(viewHolder);
			} else {
				viewHolder = (ViewHolder) convertView.getTag();
			}
			String keyword = mKeywordList.get(position);
			viewHolder.mKeyTv.setText(keyword);
			viewHolder.mPickIv.setTag(keyword);
			return convertView;
		}

		private class ViewHolder {
			TextView mKeyTv;
			ImageView mPickIv;
		}

		@Override
		public void onClick(View v) {
			// on pick image click
			if (mOnKeywordHistoryListener != null) {
				String keyword = v.getTag().toString();
				mOnKeywordHistoryListener.onKeywordPick(
						SearchResultKeywordHistoryView.this, keyword);
			}
		}
	}

	public void setOnKeywordHistoryListener(
			OnKeywordHistoryListener onKeywordHistoryListener) {
		mOnKeywordHistoryListener = onKeywordHistoryListener;
	}

	public void setKeyword(String keyword) {
		mKeyword = keyword;
		generateKeywordList();
	}
}
