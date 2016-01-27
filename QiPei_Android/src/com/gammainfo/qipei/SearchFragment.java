package com.gammainfo.qipei;

import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.format.DateUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import cn.amose.vpi.CirclePageIndicator;
import cn.amose.vpi.PageIndicator;

import com.gammainfo.qipei.CityChangedDialog.OnCityChangedListener;
import com.gammainfo.qipei.MainActivity.OnTabChangedListener;
import com.gammainfo.qipei.adapter.UserAdapter;
import com.gammainfo.qipei.convert.HomeBannerJSONConvert;
import com.gammainfo.qipei.convert.UserJSONConvert;
import com.gammainfo.qipei.db.Preferences;
import com.gammainfo.qipei.http.AsyncHttpResponseHandler;
import com.gammainfo.qipei.http.RequestParams;
import com.gammainfo.qipei.http.RestClient;
import com.gammainfo.qipei.model.HomeBanner;
import com.gammainfo.qipei.model.User;
import com.gammainfo.qipei.model.helper.HomeBannerHelper;
import com.gammainfo.qipei.model.helper.HotUserHelper;
import com.gammainfo.qipei.utils.Constant;
import com.gammainfo.qipei.utils.ToastHelper;
import com.gammainfo.qipei.widget.SearchView;
import com.gammainfo.qipei.widget.SearchView.OnSearchViewListener;
import com.gammainfo.qipei.widget.UninterceptableViewPager;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.image.SmartImageView;

public class SearchFragment extends Fragment implements OnItemClickListener,
		PullToRefreshBase.OnRefreshListener2<ListView>, OnTabChangedListener {

	private static final int REQ_SWITCH_CITY = 0x01;
	private static final int REQ_SEARCH = 0x02;
	private static final int REQ_USER_DETAILS = 0x03;
	private LayoutInflater mLayoutInflater;
	private MainActivity mContext;
	private PullToRefreshListView mPullToRefreshListView;
	private UserAdapter mHotUserAdapter;
	private HomeBannerAdapter mHomeBannerAdapter;
	private SearchView mSearchView;
	private BroadcastReceiver mGpsLocationChangedReceiver;
	private ToastHelper mToastHelper;
	private UninterceptableViewPager mBannerPager;
	private AsyncHttpClient mAsyncHttpClient;

	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View messageLayout = inflater.inflate(R.layout.fragment_search,
				container, false);
		return messageLayout;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		mContext = (MainActivity) getActivity();
		mContext.registerOnTabChangedListener(this);
		initViews();
		loadHomeBanner();
		loadHotUser();
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == REQ_SWITCH_CITY) {
			if (resultCode == Activity.RESULT_OK) {
				String newCity = data
						.getStringExtra(AreaBaseActivity.EXTRA_CITY);
				if (!mSearchView.getCurrentCity().equals(newCity)) {
					mSearchView.setCurrentCity(newCity);
				}
			}
		} else if (requestCode == REQ_USER_DETAILS
				&& resultCode == Activity.RESULT_OK) {
			int index = data.getIntExtra("selectIndex", -1);
			if (index > -1) {
				ArrayList<User> userList = mHotUserAdapter.getDataSource();
				User selectUser = data.getParcelableExtra("selectUser");
				userList.set(index, selectUser);// 更改数据源，将更新后的User加入数据源
				mHotUserAdapter.notifyDataSetChanged();
			}
		} else if (requestCode == REQ_SEARCH) {

		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public void onChanged(MainActivity mainActivity, int selectedTabId) {
		if (selectedTabId == R.id.tab_search) {
			mBannerPager.enableFlip();
			syncCityIfNeed();
			if (mHotUserAdapter.getPageNumber() == 1) {
				getHotUser();
			}
		} else {
			mBannerPager.disableFlip();
			if (mAsyncHttpClient != null) {
				mAsyncHttpClient.cancelRequests(mContext, true);
			}
		}
	}

	private void syncCityIfNeed() {
		String myCity = Preferences.getMyCity();
		if (!mSearchView.getCurrentCity().equals(myCity)) {
			mSearchView.setCurrentCity(myCity);
		}
	}

	private void initViews() {
		mLayoutInflater = mContext.getLayoutInflater();
		mSearchView = (SearchView) mContext.findViewById(R.id.search_view);
		mPullToRefreshListView = (PullToRefreshListView) mContext
				.findViewById(R.id.prlv_search_hotuser);
		View headerView = mLayoutInflater.inflate(
				R.layout.search_listview_header_layout, null);
		mPullToRefreshListView.getRefreshableView().addHeaderView(headerView);
		mBannerPager = (UninterceptableViewPager) headerView
				.findViewById(R.id.vp_search_pager);
		mHomeBannerAdapter = new HomeBannerAdapter(new ArrayList<HomeBanner>());
		mBannerPager.setAdapter(mHomeBannerAdapter);
		PageIndicator indicator = (CirclePageIndicator) headerView
				.findViewById(R.id.cpi_search_pager_indcator);
		indicator.setViewPager(mBannerPager);
		mPullToRefreshListView.setOnRefreshListener(this);
		mPullToRefreshListView.setOnItemClickListener(this);
		mPullToRefreshListView.setMode(Mode.BOTH);
		mHotUserAdapter = new UserAdapter(new ArrayList<User>(), mContext);
		mPullToRefreshListView.setAdapter(mHotUserAdapter);
		mContext.findViewById(R.id.ll_search_agency).setOnClickListener(
				mOnNearByItemViewClickListener);
		mContext.findViewById(R.id.ll_search_enduser).setOnClickListener(
				mOnNearByItemViewClickListener);
		mContext.findViewById(R.id.ll_search_manufacturer).setOnClickListener(
				mOnNearByItemViewClickListener);
		mSearchView.setOnSearchViewListener(new OnSearchViewListener() {
			@Override
			public void onSwitchCity(SearchView targetView) {
				Intent intent = new Intent(mContext, ProvinceActivity.class);
				intent.putExtra(AreaBaseActivity.EXTRA_LEVEL,
						AreaBaseActivity.LEVEL_PROVINCE_CITY);
				intent.putExtra(AreaBaseActivity.EXTRA_AREA_SHORT, true);
				startActivityForResult(intent, REQ_SWITCH_CITY);
			}

			@Override
			public void onSearchTypeChanged(SearchView targetView,
					int oldSearchType, int newSearchType) {
			}

			@Override
			public void onCityChanged(SearchView targetView, String oldCity,
					String newCity) {
				getHomeBanner();
				mHotUserAdapter.setPageNumber(1);
				getHotUser();
			}

			@Override
			public void onSearch(SearchView targetView, String city,
					int searchType, String keyword) {

			}

			@Override
			public void onCancel(SearchView targetView, View cancelButton) {

			}

			@Override
			public void onKeywordChanged(SearchView targetView, String keyword) {

			}

			@Override
			public void onRequestLocation(SearchView targetView) {
				if (mToastHelper == null) {
					mToastHelper = ToastHelper.make(mContext,
							R.string.common_toast_location_loading);
				} else {
					mToastHelper
							.setText(getString(R.string.common_toast_location_loading));
				}
				int offsetY = ((MainActivity) mContext).getTabbarHeight();
				mToastHelper.show(mSearchView, Gravity.CENTER_HORIZONTAL
						| Gravity.BOTTOM, 0, offsetY + 5);
				if (mGpsLocationChangedReceiver == null) {
					mGpsLocationChangedReceiver = new GpsSerivceBroadcastReceiver();
				}
				mContext.registerReceiver(mGpsLocationChangedReceiver,
						new IntentFilter(
								Constant.ACTION_GPS_LOCATION_CHANGED_RECEIVER));
				mContext.sendBroadcast(new Intent(
						Constant.ACTION_GPS_SERVICE_RECEIVER));
			}

		});
	}

	View.OnClickListener mOnNearByItemViewClickListener = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			Intent intent = new Intent(mContext, SearchActivity.class);
			intent.putExtra(SearchActivity.EXTRA_ORDER_BY,
					SearchActivity.ORDER_BY_LOCATION);
			intent.putExtra(SearchActivity.EXTRA_SHOW_KEYWORD_PANEL, false);
			switch (v.getId()) {
			case R.id.ll_search_agency:
				intent.putExtra(SearchActivity.EXTRA_SEARCH_TYPE,
						SearchActivity.SEARCH_TYPE_AGENCY);
				break;
			case R.id.ll_search_enduser:
				intent.putExtra(SearchActivity.EXTRA_SEARCH_TYPE,
						SearchActivity.SEARCH_TYPE_ENDUSER);
				break;
			case R.id.ll_search_manufacturer:
				intent.putExtra(SearchActivity.EXTRA_SEARCH_TYPE,
						SearchActivity.SEARCH_TYPE_MANUFACTURER);
				break;
			}
			mContext.startActivity(intent);
		}
	};

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int position,
			long id) {
		position = position - 2;
		ArrayList<User> userList = mHotUserAdapter.getDataSource();
		User selectUser = userList.get(position);
		Intent companyActivityIntent = new Intent(mContext,
				CompanyInfoActivity.class);
		Bundle bundle = new Bundle();
		bundle.putInt("selectIndex", position);
		companyActivityIntent.putExtra("selectUser", selectUser);
		companyActivityIntent.putExtras(bundle);
		startActivityForResult(companyActivityIntent, REQ_USER_DETAILS);
	}

	@Override
	public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
		String label = DateUtils.formatDateTime(mContext,
				System.currentTimeMillis(), DateUtils.FORMAT_SHOW_TIME
						| DateUtils.FORMAT_SHOW_DATE
						| DateUtils.FORMAT_ABBREV_ALL);

		// Update the LastUpdatedLabel
		refreshView.getLoadingLayoutProxy().setLastUpdatedLabel(label);

		// Do work to refresh the list here.
		getHomeBanner();
		mHotUserAdapter.setPageNumber(1);
		getHotUser();
	}

	private void loadHomeBanner() {
		// TODO load local data
		mHomeBannerAdapter.setDataSource(HomeBannerHelper.select());
		// get server data
		getHomeBanner();
	}

	private void loadHotUser() {
		// TODO load local data
		mHotUserAdapter.setDataSource(HotUserHelper.select());
		// get server data
		mHotUserAdapter.setPageNumber(1);
		getHotUser();
	}

	@Override
	public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
		// String label = DateUtils.formatDateTime(mContext,
		// System.currentTimeMillis(), DateUtils.FORMAT_SHOW_TIME
		// | DateUtils.FORMAT_SHOW_DATE
		// | DateUtils.FORMAT_ABBREV_ALL);
		// // Update the LastUpdatedLabel
		// refreshView.getLoadingLayoutProxy().setLastUpdatedLabel(label);
		// Do work to refresh the list here.
		getHotUser();
	}

	private class HomeBannerAdapter extends PagerAdapter implements
			View.OnClickListener {
		private ArrayList<HomeBanner> mBannerList;

		public HomeBannerAdapter(ArrayList<HomeBanner> bannerList) {
			mBannerList = bannerList;
		}

		@Override
		public boolean isViewFromObject(View arg0, Object arg1) {
			return arg0 == arg1;
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			((ViewPager) container).removeView((View) object);
		}

		@Override
		public Object instantiateItem(ViewGroup container, int position) {
			View view = mLayoutInflater.inflate(
					R.layout.search_listitem_banner, null);
			TextView tvBannerTitle = (TextView) view
					.findViewById(R.id.tv_search_banner_title);
			SmartImageView ivBannerImage = (SmartImageView) view
					.findViewById(R.id.siv_search_banner_image);
			HomeBanner mBanner = mBannerList.get(position);
			tvBannerTitle.setText(mBanner.getTitle());
			ivBannerImage.setImageUrl(mBanner.getImgUrl());
			view.setClickable(true);
			view.setOnClickListener(this);
			view.setId(position);
			container.addView(view);
			return view;
		}

		@Override
		public int getCount() {
			return mBannerList.size();
		}

		@Override
		public void onClick(View v) {
			int position = v.getId();
			if (mBannerList.size() < position) {
				// 在切换城市的时候有可能不会刷新view
				mHomeBannerAdapter.notifyDataSetChanged();
				return;
			}
			HomeBanner homeBanner = mBannerList.get(position);
			Intent intent;
			switch (homeBanner.getType()) {
			case HomeBanner.TYPE_USER:
				intent = new Intent(mContext, CompanyInfoActivity.class);
				intent.putExtra(CompanyInfoActivity.EXTRA_USER_ID,
						homeBanner.getTypeId());
				break;
			case HomeBanner.TYPE_PRODUCT:
				intent = new Intent(mContext, ProductInfoActivity.class);
				intent.putExtra(ProductInfoActivity.EXTRA_PRODUCT_ID,
						homeBanner.getTypeId());
				break;
			case HomeBanner.TYPE_URL:
			default:
				intent = new Intent(mContext, WebBrowserActivity.class);
				intent.putExtra(WebBrowserActivity.ACTION_URL_TEXT,
						homeBanner.getClickUrl());
			}
			mContext.startActivity(intent);
		}

		public void setDataSource(ArrayList<HomeBanner> bannerList) {
			mBannerList = bannerList;
			notifyDataSetChanged();
		}
	}

	private void getHotUser() {
		RequestParams params = new RequestParams();
		params.put("user_id", String.valueOf(Preferences.getAccountUserId()));
		params.put(Constant.JSON_KEY_AREA, Preferences.getMyCity());
		params.put("page_index",
				String.valueOf(mHotUserAdapter.getPageNumber()));
		params.put("page_size", String.valueOf(mHotUserAdapter.getPageSize()));
		if (mAsyncHttpClient == null) {
			mAsyncHttpClient = new AsyncHttpClient();
		} else {
			mAsyncHttpClient.cancelRequests(mContext, true);
		}
		mAsyncHttpClient.get(mContext, Constant.API_GET_HOT_USER, params,
				new AsyncHttpResponseHandler(mContext,
						new JsonHttpResponseHandler() {

							@Override
							public void onSuccess(JSONObject rlt) {
								try {
									if (rlt.getInt(Constant.JSON_KEY_CODE) == Constant.CODE_SUCCESS) {
										JSONObject infoJsonObject = rlt
												.getJSONObject(Constant.JSON_KEY_INFO);
										ArrayList<User> userList = UserJSONConvert
												.convertJsonArrayToItemList(infoJsonObject
														.getJSONArray(Constant.JSON_KEY_LIST));
										if (mHotUserAdapter.incPageNumber() == 2) {
											HotUserHelper.asyncInsert(mContext,
													userList);
											mHotUserAdapter
													.setDataSource(userList);
										} else {
											if (userList.isEmpty()) {
												Toast.makeText(
														mContext,
														R.string.common_label_last_page,
														Toast.LENGTH_SHORT)
														.show();
											}
											mHotUserAdapter
													.appendDataSource(userList);
										}
									} else {
										Toast.makeText(
												mContext,
												rlt.getString(Constant.JSON_MESSAGE),
												Toast.LENGTH_SHORT).show();
									}
								} catch (JSONException e) {
									e.printStackTrace();
								}
							}

							@Override
							public void onFinish() {
								mPullToRefreshListView.onRefreshComplete();
								super.onFinish();
							}

						}));
	}

	private void getHomeBanner() {
		RequestParams params = new RequestParams();
		params.put("area", Preferences.getMyCity());
		RestClient.get(Constant.API_GET_HOME_BANNER, params,
				new AsyncHttpResponseHandler(mContext,
						new JsonHttpResponseHandler() {

							@Override
							public void onSuccess(JSONObject rlt) {
								try {
									if (rlt.getInt(Constant.JSON_KEY_CODE) == Constant.CODE_SUCCESS) {
										JSONObject infoJsonObject = rlt
												.getJSONObject(Constant.JSON_KEY_INFO);
										ArrayList<HomeBanner> homeBannerList = HomeBannerJSONConvert
												.convertJsonArrayToItemList(infoJsonObject
														.getJSONArray(Constant.JSON_KEY_LIST));
										mHomeBannerAdapter
												.setDataSource(homeBannerList);
										HomeBannerHelper.asyncInsert(mContext,
												homeBannerList);
									} else {
										Toast.makeText(
												mContext,
												rlt.getString(Constant.JSON_MESSAGE),
												Toast.LENGTH_SHORT).show();
									}
								} catch (JSONException e) {
									e.printStackTrace();
								}
							}

							@Override
							public void onFinish() {
								mPullToRefreshListView.onRefreshComplete();
								super.onFinish();
							}

						}));
	}

	@Override
	public void onResume() {
		super.onResume();
		syncCityIfNeed();
	}

	@Override
	public void onPause() {
		if (mGpsLocationChangedReceiver != null) {
			mContext.unregisterReceiver(mGpsLocationChangedReceiver);
			mGpsLocationChangedReceiver = null;
			if (mToastHelper != null) {
				mToastHelper.dismiss();
			}
		}
		super.onPause();
	}

	@Override
	public void onDestroy() {
		if (mToastHelper != null) {
			mToastHelper.dismiss();
		}
		super.onDestroy();
	}

	private class GpsSerivceBroadcastReceiver extends BroadcastReceiver {

		public void onReceive(Context context, Intent intent) {
			if (mToastHelper != null) {
				mToastHelper.dismiss(
						getString(R.string.common_toast_location_complete),
						1000);
			}
			if (!mSearchView.isShown()) {
				return;
			}
			String targetCity = intent.getStringExtra(GpsService.EXTRA_CITY);
			String currentCity = mSearchView.getCurrentCity();
			if (targetCity != null) {
				if (!targetCity.equals(currentCity)) {
					CityChangedDialog cityChangedDialog = CityChangedDialog
							.build(mContext, currentCity, targetCity,
									new OnCityChangedListener() {

										@Override
										public void onOk(
												CityChangedDialog cityChangedDialog,
												String targetCity) {
											Preferences.setMyCity(targetCity);
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
				Toast.makeText(context, R.string.common_toast_location_failed,
						Toast.LENGTH_SHORT).show();
			}
		}
	}

}
