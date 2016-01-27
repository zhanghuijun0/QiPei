package com.gammainfo.qipei.widget;

import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

import com.baidu.voicerecognition.android.VoiceRecognitionClient;
import com.gammainfo.qipei.R;
import com.gammainfo.qipei.SearchActivity;
import com.gammainfo.qipei.db.Preferences;
import com.gammainfo.qipei.model.helper.KeywordHelper;
import com.gammainfo.qipei.voice.VoiceRecognitionDialog;

@SuppressLint("NewApi")
public class SearchView extends LinearLayout implements View.OnClickListener {
	public static final int SEARCH_TYPE_MANUFACTURER = 1;
	public static final int SEARCH_TYPE_AGENCY = 2;
	public static final int SEARCH_TYPE_ENDUSER = 3;
	public static final int SEARCH_TYPE_PRODUCT = 4;
	public static final int SEARCH_TYPE_ENDUSER_4SDIAN = 31;
	public static final int SEARCH_TYPE_ENDUSER_KUAIXINDIAN = 32;
	public static final int SEARCH_TYPE_ENDUSER_QIXIUCHANG = 33;
	public static final int SEARCH_TYPE_ENDUSER_MEIRONGDIAN = 34;

	private static final int CITIES_PER_ROW = 4;
	private PopupWindow mMyCityPopupWindow;
	private CheckedTextView mCurrentCityIndicator;
	private PopupWindow mSearchTypePopupWindow;
	private CheckedTextView mCurrentSearchTypeIndicator;
	private LayoutInflater mLayoutInflater;
	private Context mContext;
	private OnSearchViewListener mOnSearchViewListener;
	private EditText mKeywordEt;
	private View mCancelSerachBtn;
	private View mDeleteBtn;
	private View mSpeechBtn;
	private String mCurrentCity;
	private int mCurrentSearchType;
	private String mCurrentKeyword;

	public static interface OnSearchViewListener {
		void onCityChanged(SearchView targetView, String oldCity, String newCity);

		void onSearchTypeChanged(SearchView targetView, int oldSearchType,
				int newSearchType);

		void onSwitchCity(SearchView targetView);

		/**
		 * 点击搜索键时触发
		 * 
		 * @param targetView
		 * @param city
		 * @param searchType
		 *            {@link SearchView.SEARCH_TYPE_AGENCY}
		 * @param keyword
		 */
		void onSearch(SearchView targetView, String city, int searchType,
				String keyword);

		void onCancel(SearchView targetView, View cancelButton);

		void onKeywordChanged(SearchView targetView, String keyword);

		void onRequestLocation(SearchView targetView);
	}

	public SearchView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public SearchView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	public SearchView(Context context) {
		super(context);
		init(context);
	}

	@Override
	public void onClick(View v) {
		// TODO 通用的click handle
		switch (v.getId()) {
		case R.id.et_search_keyword:
			if (mKeywordEt.isFocusable()) {
				mCancelSerachBtn.setVisibility(View.VISIBLE);
			} else {
				// 不可获得焦点时表示要跳转到搜索页面
				startSearchActivity();
			}
			break;
		case R.id.tv_search_type:
			showSearchTypeView(v);
			break;
		case R.id.tv_search_area:
			showCityChooseView(v);
			break;
		case R.id.rl_popupwindow_switch_city:
			if (mOnSearchViewListener != null) {
				mOnSearchViewListener.onSwitchCity(this);
			}
			if (mMyCityPopupWindow != null) {
				mMyCityPopupWindow.dismiss();
			}
			break;
		case R.id.btn_search_view_cancelsearch:
			cancelSearch();
			break;
		case R.id.ibtn_search_deltext:
			setKeyword("");
			break;
		case R.id.ibtn_search_speech:
			if (mKeywordEt.isFocusable()) {
				mCancelSerachBtn.setVisibility(View.VISIBLE);
				speech();
			} else {
				// 不可获得焦点时表示要跳转到搜索页面
				startSearchActivity();
			}

			break;
		}

	}

	private OnClickListener mOnCityClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			Button btnCity = (Button) v;
			String newCity = btnCity.getText().toString();
			setCurrentCity(newCity);
			mMyCityPopupWindow.dismiss();
		}
	};

	private void init(Context context) {
		mContext = context;
		mLayoutInflater = LayoutInflater.from(context);
		View searchView = mLayoutInflater.inflate(R.layout.search_view_layout,
				null);
		mCurrentCityIndicator = (CheckedTextView) searchView
				.findViewById(R.id.tv_search_area);
		mCurrentCityIndicator.setOnClickListener(this);
		mCurrentSearchTypeIndicator = (CheckedTextView) searchView
				.findViewById(R.id.tv_search_type);
		mCurrentSearchTypeIndicator.setOnClickListener(this);
		mKeywordEt = (EditText) searchView.findViewById(R.id.et_search_keyword);
		mKeywordEt.setOnClickListener(this);
		mKeywordEt.setOnFocusChangeListener(new OnFocusChangeListener() {

			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (hasFocus
						&& mCancelSerachBtn.getVisibility() != View.VISIBLE) {
					mCancelSerachBtn.setVisibility(View.VISIBLE);
				}
			}
		});
		mKeywordEt.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {

			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {

			}

			@Override
			public void afterTextChanged(Editable s) {
				mCurrentKeyword = s.toString();
				if (!TextUtils.isEmpty(s.toString())) {
					if (isDeleteButtonHidden()) {
						showDeleteButton();
					}
				} else {
					hideDeleteButton();
				}
				if (mOnSearchViewListener != null) {
					mOnSearchViewListener.onKeywordChanged(SearchView.this, s
							.toString().trim());
				}
			}
		});

		mKeywordEt.setOnEditorActionListener(new OnEditorActionListener() {

			@Override
			public boolean onEditorAction(TextView v, int actionId,
					KeyEvent event) {
				if (actionId == EditorInfo.IME_ACTION_SEARCH
						|| actionId == EditorInfo.IME_ACTION_DONE
						|| actionId == EditorInfo.IME_ACTION_GO
						|| actionId == EditorInfo.IME_ACTION_SEND
						|| actionId == EditorInfo.IME_ACTION_UNSPECIFIED) {
					mCurrentKeyword = mKeywordEt.getText().toString().trim();
					if (TextUtils.isEmpty(mCurrentKeyword) == false) {
						KeywordHelper.insert(mCurrentKeyword);
					}
					if (mOnSearchViewListener != null) {
						mOnSearchViewListener.onSearch(SearchView.this,
								mCurrentCity, mCurrentSearchType,
								mCurrentKeyword);
					}
					return true;
				}
				return false;
			}
		});
		setCurrentCity(Preferences.getMyCity());
		mCancelSerachBtn = searchView
				.findViewById(R.id.btn_search_view_cancelsearch);
		mCancelSerachBtn.setOnClickListener(this);
		mCurrentSearchType = SEARCH_TYPE_MANUFACTURER;
		mDeleteBtn = searchView.findViewById(R.id.ibtn_search_deltext);
		mDeleteBtn.setOnClickListener(this);
		mSpeechBtn = searchView.findViewById(R.id.ibtn_search_speech);
		mSpeechBtn.setOnClickListener(this);
		addView(searchView);
	}

	private void showSearchTypeView(View anchor) {
		if (mSearchTypePopupWindow == null) {
			// create
			View popupView = mLayoutInflater.inflate(
					R.layout.popupwindow_search_type, null);

			mSearchTypePopupWindow = new PopupWindow(popupView,
					LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, true);
			mSearchTypePopupWindow.setTouchable(true);
			mSearchTypePopupWindow.setOutsideTouchable(true);
			mSearchTypePopupWindow.setBackgroundDrawable(new BitmapDrawable(
					getResources(), (Bitmap) null));
			final OnClickListener searchTypeClickListener = new OnClickListener() {

				@Override
				public void onClick(View v) {
					switch (v.getId()) {
					case R.id.btn_popupwindow_search_type_agency:
						setCurrentSearchType(SEARCH_TYPE_AGENCY);
						break;
					case R.id.btn_popupwindow_search_type_enduser:
						setCurrentSearchType(SEARCH_TYPE_ENDUSER);
						break;
					case R.id.btn_popupwindow_search_type_manufacturer:
						setCurrentSearchType(SEARCH_TYPE_MANUFACTURER);
						break;
					case R.id.btn_popupwindow_search_type_product:
						setCurrentSearchType(SEARCH_TYPE_PRODUCT);
						break;
					}
					mSearchTypePopupWindow.dismiss();
				}
			};
			popupView.findViewById(
					R.id.btn_popupwindow_search_type_manufacturer)
					.setOnClickListener(searchTypeClickListener);
			popupView.findViewById(R.id.btn_popupwindow_search_type_agency)
					.setOnClickListener(searchTypeClickListener);
			popupView.findViewById(R.id.btn_popupwindow_search_type_enduser)
					.setOnClickListener(searchTypeClickListener);
			popupView.findViewById(R.id.btn_popupwindow_search_type_product)
					.setOnClickListener(searchTypeClickListener);
			mSearchTypePopupWindow
					.setOnDismissListener(new OnDismissListener() {

						@Override
						public void onDismiss() {
							mCurrentSearchTypeIndicator.setSelected(false);
						}
					});
		}
		DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
		int offsetX = (int) TypedValue.applyDimension(
				TypedValue.COMPLEX_UNIT_DIP, 3, displayMetrics);
		int offsetY = (int) TypedValue.applyDimension(
				TypedValue.COMPLEX_UNIT_DIP, 2, displayMetrics);
		mCurrentSearchTypeIndicator.setSelected(true);
		mSearchTypePopupWindow.showAsDropDown(anchor, -offsetX, -offsetY);
	}

	private void showCityChooseView(View anchor) {
		if (mMyCityPopupWindow == null) {
			// create
			View popupView = mLayoutInflater.inflate(
					R.layout.popupwindow_city_choose, null);

			mMyCityPopupWindow = new PopupWindow(popupView,
					LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, true);
			mMyCityPopupWindow.setTouchable(true);
			mMyCityPopupWindow.setOutsideTouchable(true);
			mMyCityPopupWindow.setBackgroundDrawable(new BitmapDrawable(
					getResources(), (Bitmap) null));
			popupView.findViewById(R.id.rl_popupwindow_switch_city)
					.setOnClickListener(this);
			mMyCityPopupWindow.setOnDismissListener(new OnDismissListener() {

				@Override
				public void onDismiss() {
					mCurrentCityIndicator.setSelected(false);
				}
			});
		}
		mCurrentCityIndicator.setSelected(true);
		mMyCityPopupWindow.showAsDropDown(anchor);
		View contentView = mMyCityPopupWindow.getContentView();
		TextView tvCurrentCity = (TextView) contentView
				.findViewById(R.id.tv_popupwindow_current_city);
		tvCurrentCity.setText(String.format(
				mContext.getString(R.string.popupwindow_current_city_format),
				mCurrentCityIndicator.getText()));
		// TODO load cities
		LinearLayout llCityHistoryView = (LinearLayout) contentView
				.findViewById(R.id.ll_popupwindow_city_history_container);
		llCityHistoryView.removeAllViews();
		ArrayList<String> cityHistory = Preferences.getMyHistoryCity();
		if (cityHistory == null) {
			cityHistory = new ArrayList<String>();
		}
		LinearLayout.LayoutParams lp = new LayoutParams(0,
				LinearLayout.LayoutParams.WRAP_CONTENT);
		lp.weight = 1;
		int screenWidth = getResources().getDisplayMetrics().widthPixels;
		FrameLayout.LayoutParams contentLayoutParams = (FrameLayout.LayoutParams) contentView
				.getLayoutParams();
		int leftMargin;
		int rightMargin;
		if (contentLayoutParams != null) {
			leftMargin = contentLayoutParams.leftMargin;
			rightMargin = contentLayoutParams.rightMargin;
		} else {
			leftMargin = 0;
			rightMargin = 0;
		}
		/**
		 * 计算每两个城市之间的空隙宽度， 每个城市有左右两个padding，每4个padding占用1个城市的空间
		 */
		int spacing = (screenWidth - (leftMargin + rightMargin
				+ contentView.getPaddingLeft() + contentView.getPaddingRight()))
				/ (CITIES_PER_ROW * 9);
		lp.leftMargin = spacing;
		lp.rightMargin = spacing;
		lp.bottomMargin = spacing;
		cityHistory.add(0, "定位");
		int size = cityHistory.size();
		for (int rows = (int) Math.ceil(size / (CITIES_PER_ROW * 1.0)), r = 0; r < rows; r++) {
			LinearLayout llItem = new LinearLayout(mContext);
			llItem.setGravity(Gravity.LEFT);
			llItem.setWeightSum(CITIES_PER_ROW);
			llCityHistoryView.addView(llItem);
			for (int c = 0; c < CITIES_PER_ROW; c++) {
				int index = r * CITIES_PER_ROW + c;
				if (index < size) {
					if (index != 0) {
						Button btnCity = new Button(mContext);
						btnCity.setLayoutParams(lp);
						btnCity.setSingleLine(true);
						btnCity.setOnClickListener(mOnCityClickListener);
						btnCity.setBackgroundResource(R.drawable.popupwindow_city_item_bg_selector);
						btnCity.setText(cityHistory.get(index));
						llItem.addView(btnCity);
					} else {
						View locationView = mLayoutInflater
								.inflate(
										R.layout.popupwindow_city_choose_location,
										null);
						locationView.setLayoutParams(lp);
						locationView.setOnClickListener(new OnClickListener() {

							@Override
							public void onClick(View v) {
								if (mOnSearchViewListener != null) {
									mOnSearchViewListener
											.onRequestLocation(SearchView.this);
								}
								mMyCityPopupWindow.dismiss();
							}
						});
						llItem.addView(locationView);
					}
				} else {
					Button btnCity = new Button(mContext);
					btnCity.setLayoutParams(lp);
					btnCity.setText("");
					btnCity.setBackgroundResource(R.drawable.popupwindow_city_item_bg_selector);
					btnCity.setVisibility(View.INVISIBLE);
					llItem.addView(btnCity);
				}
			}
		}
	}

	public String getCurrentCity() {
		return mCurrentCity;
	}

	public void setCurrentCity(String city) {
		String currentCity = mCurrentCity;
		if (currentCity == null || !currentCity.equals(city)) {
			mCurrentCity = city;
			mCurrentCityIndicator.setText(mCurrentCity);
			Preferences.setMyCity(mCurrentCity);
			Preferences.putCityInHistoryPool(mCurrentCity);
			if (mOnSearchViewListener != null) {
				mOnSearchViewListener.onCityChanged(this, currentCity, city);
			}
		}
	}

	public void setOnSearchViewListener(
			OnSearchViewListener onSearchViewListener) {
		mOnSearchViewListener = onSearchViewListener;
	}

	public int getCurrentSearchType() {
		return mCurrentSearchType;
	}

	public void setKeywordEditorFocusable(boolean focusable) {
		mKeywordEt.setFocusable(focusable);
		mKeywordEt.setFocusableInTouchMode(focusable);
	}

	public void setKeyword(String keyword) {
		mCurrentKeyword = keyword.trim();
		mKeywordEt.setText(mCurrentKeyword);
		if (mCurrentKeyword != null) {
			mKeywordEt.setSelection(mCurrentKeyword.length());
			KeywordHelper.insert(mCurrentKeyword);
		}

	}

	public void cancelSearch() {
		mCancelSerachBtn.setVisibility(View.GONE);
		InputMethodManager inputMethodManager = (InputMethodManager) mContext
				.getSystemService(Context.INPUT_METHOD_SERVICE);
		inputMethodManager.hideSoftInputFromWindow(mKeywordEt.getWindowToken(),
				0);
		if (mOnSearchViewListener != null) {
			mOnSearchViewListener.onCancel(this, mCancelSerachBtn);
		}
	}

	public void search(String keyword) {
		setKeyword(keyword);
		if (mOnSearchViewListener != null) {
			mOnSearchViewListener.onSearch(this, mCurrentCity,
					mCurrentSearchType, mCurrentKeyword);
		}
	}

	public void showDeleteButton() {
		mDeleteBtn.setVisibility(View.VISIBLE);
		mSpeechBtn.setVisibility(View.INVISIBLE);
	}

	public void hideDeleteButton() {
		mDeleteBtn.setVisibility(View.INVISIBLE);
		mSpeechBtn.setVisibility(View.VISIBLE);
	}

	public boolean isDeleteButtonHidden() {
		return mDeleteBtn.getVisibility() == View.VISIBLE ? false : true;
	}

	public String getCurrentKeyword() {
		return mCurrentKeyword;
	}

	/**
	 * 设置搜索类型，并执行回调方法
	 */
	public void setCurrentSearchType(int searchType) {
		setCurrentSearchType(searchType, true);
	}

	/**
	 * 设置搜索类型
	 * 
	 * @param searchType
	 * @param execListener
	 *            是否执行回调
	 */
	public void setCurrentSearchType(int searchType, boolean execListener) {
		if (searchType == mCurrentSearchType) {
			return;
		}
		switch (searchType) {
		case SEARCH_TYPE_AGENCY: {
			mCurrentSearchTypeIndicator
					.setText(R.string.popupwindow_search_type_agency);
			int oldSearchType = mCurrentSearchType;
			mCurrentSearchType = SEARCH_TYPE_AGENCY;
			if (mOnSearchViewListener != null && execListener) {
				mOnSearchViewListener.onSearchTypeChanged(SearchView.this,
						oldSearchType, mCurrentSearchType);
			}
		}
			break;
		case SEARCH_TYPE_ENDUSER: {
			mCurrentSearchTypeIndicator
					.setText(R.string.popupwindow_search_type_enduser);
			int oldSearchType = mCurrentSearchType;
			mCurrentSearchType = SEARCH_TYPE_ENDUSER;
			if (mOnSearchViewListener != null && execListener) {
				mOnSearchViewListener.onSearchTypeChanged(SearchView.this,
						oldSearchType, mCurrentSearchType);
			}
		}
			break;
		case SEARCH_TYPE_MANUFACTURER: {
			mCurrentSearchTypeIndicator
					.setText(R.string.popupwindow_search_type_manufacturer);
			int oldSearchType = mCurrentSearchType;
			mCurrentSearchType = SEARCH_TYPE_MANUFACTURER;
			if (mOnSearchViewListener != null && execListener) {
				mOnSearchViewListener.onSearchTypeChanged(SearchView.this,
						oldSearchType, mCurrentSearchType);
			}
		}
			break;
		case SEARCH_TYPE_PRODUCT: {
			mCurrentSearchTypeIndicator
					.setText(R.string.popupwindow_search_type_product);
			int oldSearchType = mCurrentSearchType;
			mCurrentSearchType = SEARCH_TYPE_PRODUCT;
			if (mOnSearchViewListener != null && execListener) {
				mOnSearchViewListener.onSearchTypeChanged(SearchView.this,
						oldSearchType, mCurrentSearchType);
			}
		}
			break;
		}
	}

	private VoiceRecognitionDialog mVoiceRecognitionDialog;

	public void speech() {
		if (mVoiceRecognitionDialog == null) {
			mVoiceRecognitionDialog = VoiceRecognitionDialog.build(mContext,
					new VoiceRecognitionDialog.OnVoiceRecognitionListener() {

						@Override
						public void onFinish(VoiceRecognitionDialog dialog,
								String word) {
							dialog.dismiss();
							setKeyword(word);
							search(mCurrentKeyword);
						}

						@Override
						public void onError(VoiceRecognitionDialog dialog,
								String error) {
							Toast.makeText(mContext, error, Toast.LENGTH_SHORT)
									.show();
						}
					});
		}
		mVoiceRecognitionDialog.show();
	}

	public void release() {
		if (mVoiceRecognitionDialog != null) {
			mVoiceRecognitionDialog.dismiss();
		}
		VoiceRecognitionClient.releaseInstance();
	}

	public void pause() {
		if (mVoiceRecognitionDialog != null) {
			mVoiceRecognitionDialog.dismiss();
		}
	}

	private void startSearchActivity() {
		Intent intent = new Intent(mContext, SearchActivity.class);
		intent.putExtra(SearchActivity.EXTRA_CITY, Preferences.getMyCity());
		intent.putExtra(SearchActivity.EXTRA_SEARCH_TYPE, mCurrentSearchType);
		mContext.startActivity(intent);
	}
}
