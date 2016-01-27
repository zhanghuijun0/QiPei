package com.gammainfo.qipei.widget;

import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.capricorn.VaryArcMenu;
import com.gammainfo.qipei.CompanyInfoActivity;
import com.gammainfo.qipei.R;
import com.gammainfo.qipei.adapter.UserAdapter;
import com.gammainfo.qipei.convert.UserJSONConvert;
import com.gammainfo.qipei.db.Preferences;
import com.gammainfo.qipei.http.AsyncHttpResponseHandler;
import com.gammainfo.qipei.http.RequestParams;
import com.gammainfo.qipei.model.User;
import com.gammainfo.qipei.utils.Constant;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;

@SuppressLint("NewApi")
public class SearchResultUserView extends RelativeLayout implements
		OnItemClickListener, PullToRefreshBase.OnRefreshListener2<ListView> {
	public static final int ORDER_TYPE_HOT = 1;
	public static final int ORDER_TYPE_LOCATION = 2;
	private Context mContext;
	private LayoutInflater mLayoutInflater;
	private PullToRefreshListView mUserHotNumListView;
	private UserAdapter mUserHotNumAdapter;
	private PullToRefreshListView mUserNearbyListView;
	private UserAdapter mUserNearbyAdapter;
	private RadioGroup mOrderByRg;
	private ViewGroup mViewContainer;

	private double mLongitude;
	private double mLatitude;
	private OnLocationListener mOnLocationListener;
	private boolean mIsRequestLocation;
	private View mUserHotNumListViewEmptyView;
	private View mUserNearbyListViewEmptyView;
	private VaryArcMenu mVaryArcMenu;
	private SearchView mSearchView;
	private int mEndUserSearchSubType;
	private AsyncHttpClient mAsyncHttpClient;

	public SearchResultUserView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public SearchResultUserView(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	public SearchResultUserView(Context context) {
		super(context);
		init(context);
	}

	@Override
	public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
		String label = DateUtils.formatDateTime(mContext,
				System.currentTimeMillis(), DateUtils.FORMAT_SHOW_TIME
						| DateUtils.FORMAT_SHOW_DATE
						| DateUtils.FORMAT_ABBREV_ALL);

		// Update the LastUpdatedLabel
		refreshView.getLoadingLayoutProxy().setLastUpdatedLabel(label);
		if (mOrderByRg.getCheckedRadioButtonId() == R.id.rbtn_search_user_nearby) {
			mUserNearbyAdapter.setPageNumber(1);
			search(ORDER_TYPE_LOCATION);
		} else {
			mUserHotNumAdapter.setPageNumber(1);
			search(ORDER_TYPE_HOT);
		}
	}

	@Override
	public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
		if (mOrderByRg.getCheckedRadioButtonId() == R.id.rbtn_search_user_nearby) {
			search(ORDER_TYPE_LOCATION);
		} else {
			search(ORDER_TYPE_HOT);
		}
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int position,
			long arg3) {
		position = position - 1;
		ArrayList<User> userList;
		if (mOrderByRg.getCheckedRadioButtonId() == R.id.rbtn_search_user_nearby) {
			userList = mUserNearbyAdapter.getDataSource();
		} else {
			userList = mUserHotNumAdapter.getDataSource();
		}
		User selectUser = userList.get(position);
		Intent companyActivityIntent = new Intent(mContext,
				CompanyInfoActivity.class);
		Bundle bundle = new Bundle();
		bundle.putInt("selectIndex", position);
		companyActivityIntent.putExtra("selectUser", selectUser);
		companyActivityIntent.putExtras(bundle);
		mContext.startActivity(companyActivityIntent);
	}

	private void init(Context context) {
		mContext = context;
		mLayoutInflater = LayoutInflater.from(context);
		mLayoutInflater.inflate(R.layout.search_result_user_layout, this);
		mViewContainer = (ViewGroup) findViewById(R.id.fl_search_user_view_container);
		mVaryArcMenu = (VaryArcMenu) findViewById(R.id.vary_arc_menu);
		mOrderByRg = (RadioGroup) findViewById(R.id.rg_search_result_user_orderby);
		mOrderByRg.check(R.id.rbtn_search_user_hot);
		mOrderByRg.setOnCheckedChangeListener(mOnCheckedChangeListener);
		mEndUserSearchSubType = SearchView.SEARCH_TYPE_ENDUSER;
		smartSelectListView();
	}

	private void buildVaryArcMenu() {
		if (mVaryArcMenu.isEmpty()) {
			final OnClickListener onClickListener = new OnClickListener() {

				@Override
				public void onClick(View v) {
					int resId = v.getId();
					mVaryArcMenu.setHintView(resId);
					int prevEndUserSearchSubType = mEndUserSearchSubType;
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
					if (prevEndUserSearchSubType != mEndUserSearchSubType) {
						reset();
						search();
					}
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

	private OnCheckedChangeListener mOnCheckedChangeListener = new OnCheckedChangeListener() {

		@Override
		public void onCheckedChanged(RadioGroup group, int checkedId) {
			if (checkedId == R.id.rbtn_search_user_nearby) {
				if (mUserNearbyAdapter == null
						|| mUserNearbyAdapter.getPageNumber() == 1) {
					search(ORDER_TYPE_LOCATION);
				} else {
					smartSelectListView();
				}
			} else {
				if (mUserHotNumAdapter == null
						|| mUserHotNumAdapter.getPageNumber() == 1) {
					search(ORDER_TYPE_HOT);
				} else {
					smartSelectListView();
				}
			}
		}
	};

	public void search() {
		if (mOrderByRg.getCheckedRadioButtonId() == R.id.rbtn_search_user_nearby) {
			search(ORDER_TYPE_LOCATION);
		} else {
			search(ORDER_TYPE_HOT);
		}
	}

	private void search(int type) {
		smartSelectListView();
		String city = mSearchView.getCurrentCity();
		int searchType = mSearchView.getCurrentSearchType();
		// 如果当前搜索的是终端客户，用其子类替换
		if (searchType == SearchView.SEARCH_TYPE_ENDUSER) {
			searchType = mEndUserSearchSubType;
		}
		String keyword = mSearchView.getCurrentKeyword();
		final int orderBy = type;
		mIsRequestLocation = false;
		if (orderBy == ORDER_TYPE_LOCATION) {
			if (System.currentTimeMillis()
					- Preferences.getMyLocationLatestTimestamp() <= Constant.REQUEST_GPS_LOCATION_INTERVAL) {
				mLongitude = Preferences.getMyLongitude();
				mLatitude = Preferences.getMyLatitude();
			} else {
				if (mOnLocationListener != null) {
					mIsRequestLocation = true;
					mOnLocationListener.onReuqest(this);
					return;
				} else {
					mLongitude = Preferences.getMyLongitude();
					mLatitude = Preferences.getMyLatitude();
				}
			}
		}

		RequestParams params = new RequestParams();
		params.put("user_id", String.valueOf(Preferences.getAccountUserId()));
		params.put("role_id", String.valueOf(searchType));
		params.put("area", city);
		params.put("key", keyword);
		if (orderBy == ORDER_TYPE_HOT) {
			params.put("type", "1");
		} else {
			params.put("type", "2");
			params.put("longitude", String.valueOf(mLongitude));
			params.put("latitude", String.valueOf(mLatitude));
		}
		if (mOrderByRg.getCheckedRadioButtonId() == R.id.rbtn_search_user_nearby) {
			params.put("page_index",
					String.valueOf(mUserNearbyAdapter.getPageNumber()));
			params.put("page_size",
					String.valueOf(mUserNearbyAdapter.getPageSize()));
		} else {
			params.put("page_index",
					String.valueOf(mUserHotNumAdapter.getPageNumber()));
			params.put("page_size",
					String.valueOf(mUserHotNumAdapter.getPageSize()));
		}
		if (mAsyncHttpClient == null) {
			mAsyncHttpClient = new AsyncHttpClient();
		} else {
			mAsyncHttpClient.cancelRequests(mContext, true);
		}
		mAsyncHttpClient.get(mContext, Constant.API_COMPANY_LIST_GET, params,
				new AsyncHttpResponseHandler(mContext,
						new JsonHttpResponseHandler() {

							@Override
							public void onStart() {
								super.onStart();
								hideEmptyViewIfNeed();
							}

							@Override
							public void onSuccess(JSONObject rlt) {
								try {
									if (rlt.getInt(Constant.JSON_KEY_CODE) == Constant.CODE_SUCCESS) {
										JSONObject infoJsonObject = rlt
												.getJSONObject(Constant.JSON_KEY_INFO);
										ArrayList<User> userList = UserJSONConvert
												.convertJsonArrayToItemList(infoJsonObject
														.getJSONArray(Constant.JSON_KEY_LIST));
										smartSelectListView();
										if (orderBy == ORDER_TYPE_LOCATION) {
											if (mUserNearbyAdapter
													.incPageNumber() == 2) {
												mUserNearbyAdapter
														.setDataSource(userList);
											} else if (userList.isEmpty() == false) {
												mUserNearbyAdapter
														.appendDataSource(userList);
											} else {
												if (userList.isEmpty()) {
													Toast.makeText(
															mContext,
															R.string.common_label_last_page,
															Toast.LENGTH_SHORT)
															.show();
												}
											}
										} else {
											if (mUserHotNumAdapter
													.incPageNumber() == 2) {
												mUserHotNumAdapter
														.setDataSource(userList);
											} else if (userList.isEmpty() == false) {
												mUserHotNumAdapter
														.appendDataSource(userList);
											} else {
												if (userList.isEmpty()) {
													Toast.makeText(
															mContext,
															R.string.common_label_last_page,
															Toast.LENGTH_SHORT)
															.show();
												}
											}
										}
										showEmptyViewIfNeed();
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
								if (orderBy == ORDER_TYPE_LOCATION) {
									if (mUserNearbyListView != null) {
										mUserNearbyListView.onRefreshComplete();
									}
								} else {
									if (mUserHotNumListView != null) {
										mUserHotNumListView.onRefreshComplete();
									}
								}
								super.onFinish();
							}

						}));
	}

	private void smartSelectListView() {
		if (mOrderByRg.getCheckedRadioButtonId() == R.id.rbtn_search_user_nearby) {
			if (mUserHotNumListView != null) {
				mUserHotNumListView.setVisibility(View.GONE);
				mUserHotNumListViewEmptyView.setVisibility(View.GONE);
			}
			if (mUserNearbyListView == null) {
				mUserNearbyListView = (PullToRefreshListView) mLayoutInflater
						.inflate(R.layout.search_result_user_layout_listview,
								null);

				mUserNearbyAdapter = new UserAdapter(new ArrayList<User>(),
						mContext);
				mUserNearbyListView.setAdapter(mUserNearbyAdapter);
				mUserNearbyAdapter.setDistanceStatus(true);
				mUserNearbyListView.setOnItemClickListener(this);
				mUserNearbyListView.setOnRefreshListener(this);
				mUserNearbyListViewEmptyView = mLayoutInflater.inflate(
						R.layout.listview_emptyview_search_user_nearby, null);
				mUserNearbyListViewEmptyView.setVisibility(View.GONE);
				mViewContainer.addView(mUserNearbyListView);
				mViewContainer.addView(mUserNearbyListViewEmptyView);
			} else {
				mUserNearbyListView.setVisibility(View.VISIBLE);
				if (mUserNearbyAdapter.isEmpty()) {
					mUserNearbyListViewEmptyView.setVisibility(View.VISIBLE);
				}
			}
		} else {
			if (mUserNearbyListView != null) {
				mUserNearbyListView.setVisibility(View.GONE);
				mUserNearbyListViewEmptyView.setVisibility(View.GONE);
			}
			if (mUserHotNumListView == null) {
				mUserHotNumListView = (PullToRefreshListView) mLayoutInflater
						.inflate(R.layout.search_result_user_layout_listview,
								null);

				mUserHotNumAdapter = new UserAdapter(new ArrayList<User>(),
						mContext);
				mUserHotNumListView.setAdapter(mUserHotNumAdapter);
				mUserHotNumListView.setOnItemClickListener(this);
				mUserHotNumListView.setOnRefreshListener(this);
				mUserHotNumListViewEmptyView = mLayoutInflater.inflate(
						R.layout.listview_emptyview_search_user_hotnum, null);
				mUserHotNumListViewEmptyView.setVisibility(View.GONE);
				mViewContainer.addView(mUserHotNumListView);
				mViewContainer.addView(mUserHotNumListViewEmptyView);
			} else {
				mUserHotNumListView.setVisibility(View.VISIBLE);
				if (mUserHotNumAdapter.isEmpty()) {
					mUserHotNumListViewEmptyView.setVisibility(View.VISIBLE);
				}
			}
		}
	}

	public void reset() {
		if (mUserHotNumListView != null) {
			mUserHotNumAdapter.removeDataSource();
		}

		if (mUserNearbyListView != null) {
			mUserNearbyAdapter.removeDataSource();
		}
		if (mAsyncHttpClient != null) {
			mAsyncHttpClient.cancelRequests(mContext, true);
		}
	}

	public void setOrderBy(boolean isLocation) {
		if (isLocation) {
			((RadioButton) mOrderByRg
					.findViewById(R.id.rbtn_search_user_nearby))
					.setChecked(true);
		} else {
			((RadioButton) mOrderByRg.findViewById(R.id.rbtn_search_user_hot))
					.setChecked(true);
		}
	}

	public static interface OnLocationListener {
		void onReuqest(SearchResultUserView targetView);
	}

	public void setOnLocationListener(OnLocationListener onLocationListener) {
		mOnLocationListener = onLocationListener;
	}

	public boolean isRequestLocation() {
		if (mIsRequestLocation) {
			return true;
		}
		return false;
	}

	public boolean isSearchNearby() {
		if (mOrderByRg.getCheckedRadioButtonId() == R.id.rbtn_search_user_nearby) {
			return true;
		}
		return false;
	}

	private void hideEmptyViewIfNeed() {
		if (mOrderByRg.getCheckedRadioButtonId() == R.id.rbtn_search_user_nearby) {
			if (mUserNearbyListView != null) {
				if (mUserNearbyListViewEmptyView.getVisibility() == View.VISIBLE) {
					mUserNearbyListViewEmptyView.setVisibility(View.GONE);
				}
			}
		} else {
			if (mUserHotNumListView != null) {
				if (mUserHotNumListViewEmptyView.getVisibility() == View.VISIBLE) {
					mUserHotNumListViewEmptyView.setVisibility(View.GONE);
				}
			}
		}
	}

	private void showEmptyViewIfNeed() {
		if (mOrderByRg.getCheckedRadioButtonId() == R.id.rbtn_search_user_nearby) {
			if (mUserNearbyListView != null) {
				if (mUserNearbyAdapter.isEmpty() == false
						&& mUserNearbyListViewEmptyView.getVisibility() == View.VISIBLE) {
					mUserNearbyListViewEmptyView.setVisibility(View.GONE);
				}
			}
		} else {
			if (mUserHotNumListView != null) {
				if (mUserHotNumAdapter.isEmpty() == false
						&& mUserHotNumListViewEmptyView.getVisibility() == View.VISIBLE) {
					mUserHotNumListViewEmptyView.setVisibility(View.GONE);
				}
			}
		}
	}

	@Override
	public void setVisibility(int visibility) {
		super.setVisibility(visibility);
		if (visibility == VISIBLE) {
			int searchType = mSearchView.getCurrentSearchType();
			if (searchType == SearchView.SEARCH_TYPE_ENDUSER
					|| searchType == SearchView.SEARCH_TYPE_ENDUSER_4SDIAN
					|| searchType == SearchView.SEARCH_TYPE_ENDUSER_KUAIXINDIAN
					|| searchType == SearchView.SEARCH_TYPE_ENDUSER_MEIRONGDIAN
					|| searchType == SearchView.SEARCH_TYPE_ENDUSER_QIXIUCHANG) {
				buildVaryArcMenu();
				mVaryArcMenu.setVisibility(VISIBLE);
			} else {
				mVaryArcMenu.setVisibility(GONE);
			}
		} else {
			mVaryArcMenu.setVisibility(GONE);
		}
	}

	public void setSearchView(SearchView searchView) {
		mSearchView = searchView;
	}
}
