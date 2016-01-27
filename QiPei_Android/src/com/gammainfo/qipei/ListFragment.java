package com.gammainfo.qipei;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.format.DateUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.HeaderViewListAdapter;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.Toast;

import com.capricorn.VaryArcMenu;
import com.gammainfo.qipei.CityChangedDialog.OnCityChangedListener;
import com.gammainfo.qipei.MainActivity.OnTabChangedListener;
import com.gammainfo.qipei.adapter.UserAdapter;
import com.gammainfo.qipei.convert.UserJSONConvert;
import com.gammainfo.qipei.db.Preferences;
import com.gammainfo.qipei.http.AsyncHttpResponseHandler;
import com.gammainfo.qipei.http.RequestParams;
import com.gammainfo.qipei.model.User;
import com.gammainfo.qipei.model.helper.UserHelper;
import com.gammainfo.qipei.utils.Constant;
import com.gammainfo.qipei.utils.ToastHelper;
import com.gammainfo.qipei.widget.SearchView;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;

public class ListFragment extends Fragment implements OnItemClickListener,
		PullToRefreshBase.OnRefreshListener2<ListView>, OnTabChangedListener {
	private static final int MANUFACTURER = 1;// 厂商种类常量
	private static final int AGENTS = 2;// 营销商
	public static final int CUSTOMER = 3;// 终端客户
	private static final int SUPPLY_AND_DEMAND = 4;
	private static final int REQ_SWITCH_CITY = 0x01;
	private static final int REQ_SEARCH = 0x02;

	private MainActivity mContext;
	private PullToRefreshListView mPullToRefreshListView;
	private RadioGroup mListRadioGroup;
	private SearchView mSearchView;
	private LayoutInflater mLayoutInflater;
	private UserAdapter mManAdapter; // 盛放厂商的集合
	private UserAdapter mAgentsAdapter;// 盛放经销商的集合
	private UserAdapter mCusAdapter;// 盛放终端客户的集合
	private UserAdapter mUserAdapter;// 当前List
	private View mEmptyLayout;
	private VaryArcMenu mVaryArcMenu;
	private int mSelectType;// 用户所选择的的种类，默认为厂商
	private int mEndUserSearchSubType = 3;
	private BroadcastReceiver mGpsLocationChangedReceiver;
	private ToastHelper mToastHelper;
	private AsyncHttpClient mHttpClient = new AsyncHttpClient();

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View contactsLayout = inflater.inflate(R.layout.fragment_list,
				container, false);
		return contactsLayout;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		initViews();
	}

	@Override
	public void onChanged(MainActivity mainActivity, int selectedTabId) {
		// TODO 选中的标签发生变化时执行
		if (selectedTabId == R.id.tab_list) {
			syncCityIfNeed();
		} else {
			if (mHttpClient != null) {
				mHttpClient.cancelRequests(mContext, true);
			}

		}
	}

	private void initViews() {
		mSelectType = MANUFACTURER;
		mContext = (MainActivity) getActivity();
		mLayoutInflater = mContext.getLayoutInflater();
		mContext.registerOnTabChangedListener(this);
		mSearchView = (SearchView) mContext.findViewById(R.id.list_search_view);
		mListRadioGroup = (RadioGroup) mContext
				.findViewById(R.id.list_radio_group);
		mPullToRefreshListView = (PullToRefreshListView) mContext
				.findViewById(R.id.prlv_list_hotuser);// 获得组件PullToRefreshListView对象
		mVaryArcMenu = (VaryArcMenu) mContext
				.findViewById(R.id.list_vary_arc_menu);
		mPullToRefreshListView.setMode(Mode.BOTH);
		mEmptyLayout = (LinearLayout) mContext
				.findViewById(R.id.layout_list_empty);
		mManAdapter = new UserAdapter(new ArrayList<User>(), mContext);
		mAgentsAdapter = new UserAdapter(new ArrayList<User>(), mContext);
		mCusAdapter = new UserAdapter(new ArrayList<User>(), mContext);
		// mUserAdapter = mManAdapter;

		mPullToRefreshListView.setOnRefreshListener(this);
		mPullToRefreshListView.setOnItemClickListener(this);

		// mPullToRefreshListView.setAdapter(mUserAdapter);
		// buildVaryArcMenu();
		// loadTestDataByType();

		mSearchView
				.setOnSearchViewListener(new SearchView.OnSearchViewListener() {

					@Override
					public void onSwitchCity(SearchView targetView) {
						Intent intent = new Intent(mContext,
								ProvinceActivity.class);
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
					public void onCityChanged(SearchView targetView,
							String oldCity, String newCity) {
						mManAdapter.setPageNumber(1);
						mAgentsAdapter.setPageNumber(1);
						mCusAdapter.setPageNumber(1);
						getDataById();
					}

					@Override
					public void onCancel(SearchView targetView,
							View cancelButton) {

					}

					@Override
					public void onSearch(SearchView targetView, String city,
							int searchType, String keyword) {

					}

					@Override
					public void onKeywordChanged(SearchView targetView,
							String keyword) {

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
						int offsetY = ((MainActivity) mContext)
								.getTabbarHeight();
						mToastHelper.show(mSearchView,
								Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM, 0,
								offsetY + 5);
						if (mGpsLocationChangedReceiver == null) {
							mGpsLocationChangedReceiver = new GpsSerivceBroadcastReceiver();
						}
						mContext.registerReceiver(
								mGpsLocationChangedReceiver,
								new IntentFilter(
										Constant.ACTION_GPS_LOCATION_CHANGED_RECEIVER));
						mContext.sendBroadcast(new Intent(
								Constant.ACTION_GPS_SERVICE_RECEIVER));
					}

				});
		/* 对供求区添加单击跳转事件 */
		mListRadioGroup
				.setOnCheckedChangeListener(new OnCheckedChangeListener() {
					RadioButton ckeckedButton = (RadioButton) mContext
							.findViewById(R.id.type_one);// 上一个选中的Button

					@SuppressLint("ResourceAsColor")
					@Override
					public void onCheckedChanged(RadioGroup group, int checkedId) {
						/* 获取所选择的的用户种类 */
						mVaryArcMenu.setVisibility(View.GONE);
						switch (checkedId) {
						case R.id.type_one:
							mSelectType = MANUFACTURER;

							mUserAdapter = mManAdapter;
							break;
						case R.id.type_two:
							mSelectType = AGENTS;

							mUserAdapter = mAgentsAdapter;
							break;
						case R.id.type_three:
							mSelectType = CUSTOMER;
							buildVaryArcMenu();
							mUserAdapter = mCusAdapter;
							mVaryArcMenu.setVisibility(View.VISIBLE);
							break;
						case R.id.type_four:
							mSelectType = SUPPLY_AND_DEMAND;
							// 单击供求区是让其跳转
							ckeckedButton.setChecked(true);
							startActivity(new Intent(mContext,
									SupplyDemandActivity.class));
							return;
						default:
							mSelectType = MANUFACTURER;

							break;
						}
						if (mUserAdapter.getPageNumber() == 1) {
							loadTestDataByType();
						} else {
							mEmptyLayout.setVisibility(View.GONE);
						}
						mPullToRefreshListView.setAdapter(mUserAdapter);
						/* 记录上一次所选商户种类，以便返回 */
						if (checkedId != R.id.type_four) {
							ckeckedButton = ((RadioButton) mContext
									.findViewById(checkedId));
						}

					}

				});
		((RadioButton) mListRadioGroup.findViewById(R.id.type_one))
				.setChecked(true);
	}

	private void buildVaryArcMenu() {
		if (mVaryArcMenu.isEmpty()) {
			final OnClickListener onClickListener = new OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO
					reset();
					int resId = v.getId();
					mVaryArcMenu.setHintView(resId);
					switch (resId) {
					case R.drawable.ic_search_user_4sdian:
						Toast.makeText(mContext,
								R.string.search_label_enduser_4sdian,
								Toast.LENGTH_SHORT).show();
						mEndUserSearchSubType = SearchView.SEARCH_TYPE_ENDUSER_4SDIAN;
						break;
					case R.drawable.ic_search_user_kuaixiudian:
						Toast.makeText(mContext,
								R.string.search_label_enduser_kuaixiudian,
								Toast.LENGTH_SHORT).show();
						mEndUserSearchSubType = SearchView.SEARCH_TYPE_ENDUSER_KUAIXINDIAN;
						break;
					case R.drawable.ic_search_user_meirongdian:
						Toast.makeText(mContext,
								R.string.search_label_enduser_meirongdian,
								Toast.LENGTH_SHORT).show();
						mEndUserSearchSubType = SearchView.SEARCH_TYPE_ENDUSER_MEIRONGDIAN;
						break;
					case R.drawable.ic_search_user_qixiuchang:
						Toast.makeText(mContext,
								R.string.search_label_enduser_qixiuchang,
								Toast.LENGTH_SHORT).show();
						mEndUserSearchSubType = SearchView.SEARCH_TYPE_ENDUSER_QIXIUCHANG;
						break;
					case R.drawable.ic_search_user_zhongduan:
						Toast.makeText(mContext, R.string.search_label_enduser,
								Toast.LENGTH_SHORT).show();
						mEndUserSearchSubType = SearchView.SEARCH_TYPE_ENDUSER;
						break;
					}
					getDataById();
				}
			};

			final int[] ITEM_DRAWABLES = { R.drawable.ic_search_user_4sdian,
					R.drawable.ic_search_user_kuaixiudian,
					R.drawable.ic_search_user_meirongdian,
					R.drawable.ic_search_user_qixiuchang,
					R.drawable.ic_search_user_zhongduan };
			for (int i = 0; i < ITEM_DRAWABLES.length; i++) {
				ImageView ivItem = new ImageView(mContext);
				ivItem.setId(ITEM_DRAWABLES[i]);
				ivItem.setScaleType(ScaleType.FIT_CENTER);
				ivItem.setImageResource(ITEM_DRAWABLES[i]);
				mVaryArcMenu.addItem(ivItem, onClickListener);
			}
		}
	}

	public void reset() {
		if (mPullToRefreshListView != null) {
			mUserAdapter.removeDataSource();
		}

		if (mPullToRefreshListView != null) {
			mUserAdapter.removeDataSource();
		}
	}

	/* 通过所选用户的种类获取并加载所需要的数据 */
	private void loadTestDataByType() {
		ArrayList<User> userList = UserHelper.select(mSelectType);
		mUserAdapter.setDataSource(userList);
		if (mUserAdapter.getPageNumber() == 1) {
			getDataById();
		} else {
			if (mUserAdapter.getCount() == 0) {
				mEmptyLayout.setVisibility(View.VISIBLE);
			} else {
				mEmptyLayout.setVisibility(View.GONE);
			}
		}
	}

	/* 从数据库获取数据 */
	private void getDataById() {
		/*
		 * if(mPullToRefreshListView.isRefreshing()) {
		 * 
		 * mPullToRefreshListView.onRefreshComplete(); }
		 */
		mHttpClient.cancelRequests(mContext, true);
		int type;
		if (mSelectType == SearchView.SEARCH_TYPE_ENDUSER) {
			type = mEndUserSearchSubType;
		} else {
			type = mSelectType;
		}
		final int selectType = type;
		final UserAdapter selectUserAdapter = mUserAdapter;
		RequestParams params = new RequestParams();
		params.put("role_id", selectType + "");
		params.put("user_id", Preferences.getAccountUserId() + "");
		params.put("area", Preferences.getMyCity());
		params.put("page_index", selectUserAdapter.getPageNumber() + "");
		params.put("page_size", String.valueOf(Constant.PAGE_SIZE));
		mHttpClient.post(Constant.API_COMPANY_LIST_GET, params,
				new AsyncHttpResponseHandler(mContext,
						new JsonHttpResponseHandler() {
							@Override
							public void onStart() {
								super.onStart();
//								if (!mPullToRefreshListView.isRefreshing()) {
//									mPullToRefreshListView.setRefreshing(true);
//								}
							}

							public void onSuccess(JSONObject res) {
								JSONObject jsonInfo = null;
								ArrayList<User> userList;
								try {
									if (res.getInt(Constant.JSON_KEY_CODE) == Constant.CODE_SUCCESS) {
										jsonInfo = res
												.getJSONObject(Constant.JSON_KEY_INFO);
										JSONArray jsonListArray = jsonInfo
												.getJSONArray("list");

										userList = UserJSONConvert
												.convertJsonArrayToItemList(jsonListArray);

										if (selectUserAdapter.incPageNumber() == 2) {

											selectUserAdapter
													.setDataSource(userList);
											if (selectType == SearchView.SEARCH_TYPE_ENDUSER_QIXIUCHANG
													|| selectType == CUSTOMER
													|| selectType == SearchView.SEARCH_TYPE_ENDUSER_4SDIAN
													|| selectType == SearchView.SEARCH_TYPE_ENDUSER_KUAIXINDIAN
													|| selectType == SearchView.SEARCH_TYPE_ENDUSER_MEIRONGDIAN) {
												UserHelper
														.deleteBefore(SearchView.SEARCH_TYPE_ENDUSER_4SDIAN);
												UserHelper
														.deleteBefore(SearchView.SEARCH_TYPE_ENDUSER_KUAIXINDIAN);
												UserHelper
														.deleteBefore(SearchView.SEARCH_TYPE_ENDUSER_QIXIUCHANG);
												UserHelper
														.deleteBefore(SearchView.SEARCH_TYPE_ENDUSER_MEIRONGDIAN);
											} else {
												UserHelper
														.deleteBefore(selectType);// 删除上一次访问产生的缓存数据
											}
											UserHelper.insert(mContext,
													userList);// 将接口返回的数据加入到本地数据库
										} else {
											selectUserAdapter
													.appendDataSource(userList);
											if (jsonListArray.length() == 0) {
												Toast.makeText(
														mContext,
														R.string.common_label_last_page,
														Toast.LENGTH_SHORT)
														.show();
											}
										}
									} else {
										Toast.makeText(
												mContext,
												res.getString(Constant.JSON_MESSAGE),
												Toast.LENGTH_SHORT).show();
									}
								} catch (JSONException e) {
									e.printStackTrace();
								}
								if (mUserAdapter.getCount() == 0) {
									mEmptyLayout.setVisibility(View.VISIBLE);
								} else {
									mEmptyLayout.setVisibility(View.GONE);
								}
							}

							@Override
							public void onFinish() {
								mPullToRefreshListView.onRefreshComplete();
								if (mUserAdapter.getCount() == 0) {
									mEmptyLayout.setVisibility(View.VISIBLE);
								} else {
									mEmptyLayout.setVisibility(View.GONE);
								}
								super.onFinish();
							}
						}));
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int position,
			long id) {
		HeaderViewListAdapter headerAdapter;
		headerAdapter = (HeaderViewListAdapter) (((ListView) arg0).getAdapter());
		UserAdapter userAdapter = (UserAdapter) headerAdapter
				.getWrappedAdapter();
		ArrayList<User> userList = userAdapter.getDataSource();
		User selectUser = userList.get(position - 1);
		Intent companyActivityIntent = new Intent(mContext,
				CompanyInfoActivity.class);
		Bundle bundle = new Bundle();
		bundle.putInt("selectIndex", position - 1);
		companyActivityIntent.putExtra("selectUser", selectUser);
		companyActivityIntent.putExtras(bundle);
		startActivityForResult(companyActivityIntent, (int) 0);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == 0 && resultCode == Activity.RESULT_OK) {
			ArrayList<User> userList = mUserAdapter.getDataSource();
			int index = data.getIntExtra("selectIndex", 0);
			User selectUser = (User) data.getParcelableExtra("selectUser");
			userList.set(index, selectUser);// 更改数据源，将更新后的User加入数据源
			mUserAdapter.setDataSource(userList);
		}

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
	public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
		String label = DateUtils.formatDateTime(mContext,
				System.currentTimeMillis(), DateUtils.FORMAT_SHOW_TIME
						| DateUtils.FORMAT_SHOW_DATE
						| DateUtils.FORMAT_ABBREV_ALL);
		refreshView.getLoadingLayoutProxy().setLastUpdatedLabel(label);
		mUserAdapter.setPageNumber(1);
		getDataById();
	}

	@Override
	public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {

		getDataById();
	}

	@Override
	public void onResume() {
		super.onResume();
		syncCityIfNeed();
	}

	private void syncCityIfNeed() {
		String myCity = Preferences.getMyCity();
		if (!mSearchView.getCurrentCity().equals(myCity)) {
			mSearchView.setCurrentCity(myCity);
		}
	}

	private class GpsSerivceBroadcastReceiver extends BroadcastReceiver {

		public void onReceive(Context context, Intent intent) {
			if (mToastHelper != null) {
				mToastHelper.dismiss(
						getString(R.string.common_toast_location_complete),
						1000);
			}

			String targetCity = intent.getStringExtra(GpsService.EXTRA_CITY);
			String currentCity = mSearchView.getCurrentCity();
			if (targetCity != null) {
				if (!mSearchView.isShown()) {
					return;
				}
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

	@Override
	public void onDestroy() {
		if (mToastHelper != null) {
			mToastHelper.dismiss();
		}
		super.onDestroy();
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
}
