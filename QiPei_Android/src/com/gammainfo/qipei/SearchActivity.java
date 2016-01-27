package com.gammainfo.qipei;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.gammainfo.qipei.CityChangedDialog.OnCityChangedListener;
import com.gammainfo.qipei.db.Preferences;
import com.gammainfo.qipei.utils.Constant;
import com.gammainfo.qipei.utils.ToastHelper;
import com.gammainfo.qipei.widget.SearchResultKeywordHistoryView;
import com.gammainfo.qipei.widget.SearchResultKeywordHistoryView.OnKeywordHistoryListener;
import com.gammainfo.qipei.widget.SearchResultProductView;
import com.gammainfo.qipei.widget.SearchResultUserView;
import com.gammainfo.qipei.widget.SearchResultUserView.OnLocationListener;
import com.gammainfo.qipei.widget.SearchView;
import com.gammainfo.qipei.widget.SearchView.OnSearchViewListener;

public class SearchActivity extends BaseActivity {

	private static final int REQ_SWITCH_CITY = 0x01;
	private static final int REQ_SEARCH = 0x02;
	public static final String EXTRA_CITY = "city";
	public static final String EXTRA_SEARCH_TYPE = "search_type";
	public static final String EXTRA_ORDER_BY = "order_by";
	public static final String EXTRA_SHOW_KEYWORD_PANEL = "is_show_keyword_panel";

	public static final int SEARCH_TYPE_MANUFACTURER = SearchView.SEARCH_TYPE_MANUFACTURER;
	public static final int SEARCH_TYPE_AGENCY = SearchView.SEARCH_TYPE_AGENCY;
	public static final int SEARCH_TYPE_ENDUSER = SearchView.SEARCH_TYPE_ENDUSER;
	public static final int SEARCH_TYPE_PRODUCT = SearchView.SEARCH_TYPE_PRODUCT;
	public static final int SEARCH_TYPE_ENDUSER_4SDIAN = SearchView.SEARCH_TYPE_ENDUSER_4SDIAN;
	public static final int SEARCH_TYPE_ENDUSER_KUAIXINDIAN = SearchView.SEARCH_TYPE_ENDUSER_KUAIXINDIAN;
	public static final int SEARCH_TYPE_ENDUSER_QIXIUCHANG = SearchView.SEARCH_TYPE_ENDUSER_QIXIUCHANG;
	public static final int SEARCH_TYPE_ENDUSER_MEIRONGDIAN = SearchView.SEARCH_TYPE_ENDUSER_MEIRONGDIAN;
	public static final int ORDER_BY_HOTNUM = 1;
	public static final int ORDER_BY_LOCATION = 2;
	private SearchView mSearchView;
	private SearchResultKeywordHistoryView mSearchResultKeywordHistoryView;
	private SearchResultProductView mSearchResultProductView;
	private SearchResultUserView mSearchResultUserView;
	private GpsLocationChangedBroadcastReceiver mGpsLocationChangedReceiver;
	private ToastHelper mToastHelper;
	private boolean mCanCreateToastHelper;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_search);
		mSearchView = (SearchView) findViewById(R.id.search_view_search);
		mSearchView.setOnSearchViewListener(mOnSearchViewListener);
		mSearchView.setKeywordEditorFocusable(true);
		mSearchResultKeywordHistoryView = (SearchResultKeywordHistoryView) findViewById(R.id.search_result_keyword_history);
		mSearchResultProductView = (SearchResultProductView) findViewById(R.id.search_result_product);
		mSearchResultUserView = (SearchResultUserView) findViewById(R.id.search_result_user);
		mSearchResultKeywordHistoryView
				.setOnKeywordHistoryListener(mOnKeywordHistoryListener);
		mSearchResultUserView.setOnLocationListener(mOnLocationListener);
		handleIntent();
	}

	private OnLocationListener mOnLocationListener = new OnLocationListener() {

		@Override
		public void onReuqest(SearchResultUserView targetView) {
			if (mCanCreateToastHelper) {
				requestLocation();
			} else {
				new Handler().postDelayed(new Runnable() {

					@Override
					public void run() {
						do {
							if (mCanCreateToastHelper) {
								requestLocation();
								break;
							}
							try {
								Thread.sleep(50);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
						} while (true);
					}
				}, 100);
			}
		}

	};
	private OnKeywordHistoryListener mOnKeywordHistoryListener = new OnKeywordHistoryListener() {

		@Override
		public void onKeywordSelected(SearchResultKeywordHistoryView target,
				String keyword) {
			mSearchView.setKeyword(keyword);
			if (mSearchView.getCurrentSearchType() == SearchView.SEARCH_TYPE_PRODUCT) {
				showListView(mSearchResultProductView);
				mSearchResultProductView.reset();
				mSearchResultProductView.search();
			} else {
				showListView(mSearchResultUserView);
				mSearchResultUserView.reset();
				mSearchResultUserView.search();
			}
			
		}

		@Override
		public void onKeywordPick(SearchResultKeywordHistoryView target,
				String keyword) {
			mSearchView.setKeyword(keyword);
		}

		@Override
		public void onSpeech(SearchResultKeywordHistoryView target) {
			mSearchView.speech();
		}

	};
	private OnSearchViewListener mOnSearchViewListener = new OnSearchViewListener() {

		@Override
		public void onSwitchCity(SearchView targetView) {
			Intent intent = new Intent(SearchActivity.this,
					ProvinceActivity.class);
			intent.putExtra(AreaBaseActivity.EXTRA_LEVEL,
					AreaBaseActivity.LEVEL_PROVINCE_CITY);
			intent.putExtra(AreaBaseActivity.EXTRA_AREA_SHORT, true);
			startActivityForResult(intent, REQ_SWITCH_CITY);
		}

		@Override
		public void onSearchTypeChanged(SearchView targetView,
				int oldSearchType, int newSearchType) {
			if (newSearchType == SearchView.SEARCH_TYPE_PRODUCT) {
				showListView(mSearchResultProductView);
			} else {
				showListView(mSearchResultUserView);
			}
			if (newSearchType == SearchView.SEARCH_TYPE_PRODUCT) {
				mSearchResultProductView.reset();
				mSearchResultProductView.search();
			} else {
				mSearchResultUserView.reset();
				mSearchResultUserView.search();
			}
		}

		@Override
		public void onCityChanged(SearchView targetView, String oldCity,
				String newCity) {
			if (mSearchView.getCurrentSearchType() == SearchView.SEARCH_TYPE_PRODUCT) {
				showListView(mSearchResultProductView);
				mSearchResultProductView.reset();
				mSearchResultProductView.search();
			} else {
				showListView(mSearchResultUserView);
				mSearchResultUserView.reset();
				mSearchResultUserView.search();
			}
		}

		@Override
		public void onSearch(SearchView targetView, String city,
				int searchType, String keyword) {
			if (searchType == SearchView.SEARCH_TYPE_PRODUCT) {
				showListView(mSearchResultProductView);
				mSearchResultProductView.reset();
				mSearchResultProductView.search();
			} else {
				showListView(mSearchResultUserView);
				mSearchResultUserView.reset();
				mSearchResultUserView.search();
			}
		}

		@Override
		public void onCancel(SearchView targetView, View cancelButton) {
			finish();
		}

		@Override
		public void onKeywordChanged(SearchView targetView, String keyword) {
			if (TextUtils.isEmpty(keyword)) {
				if (mSearchView.getCurrentSearchType() != SearchView.SEARCH_TYPE_PRODUCT) {
					showListView(mSearchResultUserView);
				} else {
					showListView(mSearchResultProductView);
				}
			} else {
				if (mSearchResultKeywordHistoryView.getVisibility() != View.VISIBLE) {
					showListView(mSearchResultKeywordHistoryView);
				}
				mSearchResultKeywordHistoryView.setKeyword(keyword);
			}
		}

		@Override
		public void onRequestLocation(SearchView targetView) {
			requestLocation();
		}

	};

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == REQ_SWITCH_CITY) {
			if (resultCode == Activity.RESULT_OK) {
				String newCity = data
						.getStringExtra(AreaBaseActivity.EXTRA_CITY);
				if (!mSearchView.getCurrentCity().equals(newCity)) {
					mSearchView.setCurrentCity(newCity);
				}
			} else if (requestCode == REQ_SEARCH) {

			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public void onPause() {
		if (mGpsLocationChangedReceiver != null) {
			unregisterReceiver(mGpsLocationChangedReceiver);
			mGpsLocationChangedReceiver = null;
			if (mToastHelper != null) {
				mToastHelper.dismiss();
			}
		}
		if (mSearchView != null) {
			mSearchView.pause();
		}
		super.onPause();
	}

	@Override
	public void onResume() {
		super.onResume();
		if (mCanCreateToastHelper == false) {
			mCanCreateToastHelper = true;
		}
	}

	private void handleIntent() {
		Intent getIntent = getIntent();
		int searchType = getIntent.getIntExtra(EXTRA_SEARCH_TYPE,
				SEARCH_TYPE_MANUFACTURER);
		mSearchView.setCurrentSearchType(searchType, false);
		mSearchResultUserView.setSearchView(mSearchView);
		mSearchResultProductView.setSearchView(mSearchView);
		boolean isShowKeywordPanel = getIntent.getBooleanExtra(
				EXTRA_SHOW_KEYWORD_PANEL, true);
		if (isShowKeywordPanel) {
			mSearchResultProductView.setVisibility(View.GONE);
			mSearchResultUserView.setVisibility(View.GONE);
			mSearchResultKeywordHistoryView.setVisibility(View.VISIBLE);
		} else {
			if (searchType == SEARCH_TYPE_PRODUCT) {
				mSearchResultProductView.setVisibility(View.VISIBLE);
				mSearchResultUserView.setVisibility(View.GONE);
				mSearchResultKeywordHistoryView.setVisibility(View.GONE);
			} else {
				mSearchResultProductView.setVisibility(View.GONE);
				mSearchResultUserView.setVisibility(View.VISIBLE);
				mSearchResultKeywordHistoryView.setVisibility(View.GONE);
				int orderByHotnum = getIntent.getIntExtra(EXTRA_ORDER_BY,
						ORDER_BY_HOTNUM);
				if (orderByHotnum == ORDER_BY_LOCATION) {
					mSearchResultUserView.setOrderBy(true);
					getWindow().setSoftInputMode(
							WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);// 隐藏输入法
				} else {
					mSearchResultUserView.setOrderBy(false);
				}
			}
		}
	}

	private void showListView(View listView) {
		if (listView == mSearchResultKeywordHistoryView) {
			mSearchResultKeywordHistoryView.setVisibility(View.VISIBLE);
			mSearchResultUserView.setVisibility(View.GONE);
			mSearchResultProductView.setVisibility(View.GONE);
		} else if (listView == mSearchResultProductView) {
			mSearchResultKeywordHistoryView.setVisibility(View.GONE);
			mSearchResultUserView.setVisibility(View.GONE);
			mSearchResultProductView.setVisibility(View.VISIBLE);
		} else {
			mSearchResultKeywordHistoryView.setVisibility(View.GONE);
			mSearchResultUserView.setVisibility(View.VISIBLE);
			mSearchResultProductView.setVisibility(View.GONE);
		}
	}

	private class GpsLocationChangedBroadcastReceiver extends BroadcastReceiver {
		public boolean mIsReceived;

		public void onReceive(Context context, Intent intent) {
			mIsReceived = true;
			if (mToastHelper != null) {
				mToastHelper.dismiss(
						getString(R.string.common_toast_location_complete),
						1000);
			}

			if (mSearchResultUserView.isRequestLocation()
					&& mSearchResultUserView.isSearchNearby()) {
				mSearchResultUserView.search();
			} else {
				String targetCity = intent
						.getStringExtra(GpsService.EXTRA_CITY);
				String currentCity = mSearchView.getCurrentCity();
				if (targetCity != null) {
					if (!mSearchView.isShown()) {
						return;
					}
					if (!targetCity.equals(currentCity)) {
						CityChangedDialog cityChangedDialog = CityChangedDialog
								.build(SearchActivity.this, currentCity,
										targetCity,
										new OnCityChangedListener() {

											@Override
											public void onOk(
													CityChangedDialog cityChangedDialog,
													String targetCity) {
												Preferences
														.setMyCity(targetCity);
												mSearchView
														.setCurrentCity(targetCity);
												cityChangedDialog.dismiss();
											}

											@Override
											public void onCancel(
													CityChangedDialog cityChangedDialog) {
												cityChangedDialog.dismiss();
											}
										});
						cityChangedDialog.show();
					}
				} else {
					Toast.makeText(SearchActivity.this,
							R.string.common_toast_location_failed,
							Toast.LENGTH_SHORT).show();
				}
			}

		}
	}

	@Override
	public void onDestroy() {
		if (mToastHelper != null) {
			mToastHelper.dismiss();
		}
		if (mSearchView != null) {
			mSearchView.release();
		}
		super.onDestroy();
	}

	private void requestLocation() {
		if (mGpsLocationChangedReceiver != null
				&& mGpsLocationChangedReceiver.mIsReceived == false) {
			return;
		}

		if (mToastHelper == null) {
			mToastHelper = ToastHelper.make(SearchActivity.this,
					R.string.common_toast_location_loading);
		} else {
			mToastHelper
					.setText(getString(R.string.common_toast_location_loading));
		}
		mToastHelper.show(mSearchView);

		if (mGpsLocationChangedReceiver == null) {
			mGpsLocationChangedReceiver = new GpsLocationChangedBroadcastReceiver();
		}
		mGpsLocationChangedReceiver.mIsReceived = false;
		registerReceiver(mGpsLocationChangedReceiver, new IntentFilter(
				Constant.ACTION_GPS_LOCATION_CHANGED_RECEIVER));
		sendBroadcast(new Intent(Constant.ACTION_GPS_SERVICE_RECEIVER));
	}
}
