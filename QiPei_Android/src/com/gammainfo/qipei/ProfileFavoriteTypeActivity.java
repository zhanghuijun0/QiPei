package com.gammainfo.qipei;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.gammainfo.qipei.adapter.SupplyDemandAdapter;
import com.gammainfo.qipei.adapter.UserAdapter;
import com.gammainfo.qipei.convert.SupplyDemandJSONConvert;
import com.gammainfo.qipei.convert.UserJSONConvert;
import com.gammainfo.qipei.db.Preferences;
import com.gammainfo.qipei.http.AsyncHttpResponseHandler;
import com.gammainfo.qipei.http.RequestParams;
import com.gammainfo.qipei.http.RestClient;
import com.gammainfo.qipei.model.SupplyDemand;
import com.gammainfo.qipei.model.User;
import com.gammainfo.qipei.utils.Constant;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.loopj.android.http.JsonHttpResponseHandler;

public class ProfileFavoriteTypeActivity extends BaseActivity implements
		OnItemClickListener, PullToRefreshBase.OnRefreshListener2<ListView> {
	private LinearLayout mProfileFavoriteEmptyView;
	private Context mContext;
	private UserAdapter mTypeFavoriteTypeAdapter;
	private SupplyDemandAdapter mSuplyDemandFavoriteAdapter;
	private com.handmark.pulltorefresh.library.PullToRefreshListView mListView;
	private Button mFavoriteButton;
	private int type;
	private TextView mProfileEveryTypeFavorite;

	@SuppressLint("NewApi")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.profile_every_type_favorite);
		mProfileFavoriteEmptyView = (LinearLayout) findViewById(R.id.profile_favorite_emptyview);
		mProfileEveryTypeFavorite = (TextView) findViewById(R.id.profile_every_type_favorite);
		Intent intent = getIntent();
		type = intent.getIntExtra("type", 1);
		mTypeFavoriteTypeAdapter = new UserAdapter(new ArrayList<User>(),
				ProfileFavoriteTypeActivity.this);
		mSuplyDemandFavoriteAdapter = new SupplyDemandAdapter(
				new ArrayList<SupplyDemand>(), ProfileFavoriteTypeActivity.this);
		mListView = (com.handmark.pulltorefresh.library.PullToRefreshListView) findViewById(R.id.profile_favorite_lisview);
		mListView.setOnItemClickListener(this);
		mListView.setOnRefreshListener(this);
		mListView.setMode(Mode.BOTH);
		if (type == 1) {
			mProfileEveryTypeFavorite.setText(R.string.list_companyinfo_lable);
		} else if (type == 2) {
			mProfileEveryTypeFavorite
					.setText(R.string.profile_label_dealers_text);
		} else if (type == 3) {
			mProfileEveryTypeFavorite
					.setText(R.string.profile_label_terminal_customers_text);
		} else if (type == 4) {
			mProfileEveryTypeFavorite
					.setText(R.string.profile_label_my_favorite_supply_text);
		} else if (type == 5) {
			mProfileEveryTypeFavorite
					.setText(R.string.profile_label_my_favorite_demand_text);
		}
		getIntenretFavoriteType(type);
	}

	public void back(View v) {
		Intent data = new Intent();
		setResult(Activity.RESULT_OK, data);
		finish();
	}

	/* 得到网上收藏商家的信息 */
	public void getIntenretFavoriteType(final int type) {
		int id = Preferences.getAccountUserId();
		int mPageNum = 0;
		if (type == 1 || type == 2 || type == 3) {
			mPageNum = mTypeFavoriteTypeAdapter.getPageNumber();
		} else if (type == 4 || type == 5) {
			mPageNum = mSuplyDemandFavoriteAdapter.getPageNumber();
		}
		RequestParams params = new RequestParams();
		params.put("id", String.valueOf(id));
		params.put("type", String.valueOf(type));
		params.put("page_index", String.valueOf(mPageNum));
		params.put("page_size", String.valueOf(Constant.PAGE_SIZE));
		RestClient.post(Constant.API_GET_FAVORITE_VENDOR, params,
				new AsyncHttpResponseHandler(getBaseContext(),
						new JsonHttpResponseHandler() {

							@Override
							public void onStart() {
								if (!mListView.isRefreshing()) {
									mListView.setRefreshing(true);
								}
							}

							@Override
							public void onSuccess(JSONObject rlt) {
								try {
									if (rlt.getInt(Constant.JSON_KEY_CODE) == Constant.CODE_SUCCESS) {
										JSONObject info = rlt
												.getJSONObject("info");
										JSONArray list = info
												.getJSONArray("list");

										if (type == 1 || type == 2 || type == 3) {
											if (list.length() == 0
													&& mTypeFavoriteTypeAdapter
															.getPageNumber() != 1) {
												Toast.makeText(
														getBaseContext(),
														getString(R.string.common_label_last_page),
														Toast.LENGTH_SHORT)
														.show();
											}
											ArrayList<User> userList = UserJSONConvert
													.convertJsonArrayToItemList(list);
											for (User user : userList) {
												user.setIsFavorite(true);
											}

											mTypeFavoriteTypeAdapter
													.appendDataSource(userList);
											if (mTypeFavoriteTypeAdapter
													.getCount() == 0) {
												mProfileFavoriteEmptyView
														.setVisibility(View.VISIBLE);
											} else {
												mProfileFavoriteEmptyView
														.setVisibility(View.GONE);
											}
											mTypeFavoriteTypeAdapter
													.incPageNumber();
										} else if (type == 4 || type == 5) {
											if (list.length() == 0
													&& mSuplyDemandFavoriteAdapter
															.getPageNumber() != 1) {
												Toast.makeText(
														getBaseContext(),
														getString(R.string.common_label_last_page),
														Toast.LENGTH_SHORT)
														.show();
											}
											ArrayList<SupplyDemand> suplyDemandList = SupplyDemandJSONConvert
													.convertJsonArrayToItemList(list);
											for (SupplyDemand sd : suplyDemandList) {
												sd.setIsFavorite(true);
											}
											mSuplyDemandFavoriteAdapter
													.appendDataSource(suplyDemandList);
											if (mSuplyDemandFavoriteAdapter
													.getCount() == 0) {
												mProfileFavoriteEmptyView
														.setVisibility(View.VISIBLE);
											} else {
												mProfileFavoriteEmptyView
														.setVisibility(View.GONE);
											}
											mSuplyDemandFavoriteAdapter
													.incPageNumber();
										}
									} else {
										Toast.makeText(
												getBaseContext(),
												rlt.getString(Constant.JSON_MESSAGE),
												Toast.LENGTH_SHORT).show();
									}
								} catch (JSONException e) {
									e.printStackTrace();
								}
							}

							@Override
							public void onFinish() {
								super.onFinish();
								if (mListView.getRefreshableView().getAdapter() == null) {
									if (type == 1 || type == 2 || type == 3) {
										mListView
												.setAdapter(mTypeFavoriteTypeAdapter);
									} else if (type == 4 || type == 5) {
										mListView
												.setAdapter(mSuplyDemandFavoriteAdapter);
									}
								}
								mListView.onRefreshComplete();
							}
						}));
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == 0 && resultCode == Activity.RESULT_OK) {
			User selectUser = (User) (data.getParcelableExtra("selectUser"));
			ArrayList<User> userList = mTypeFavoriteTypeAdapter.getDataSource();
			int index = data.getIntExtra("selectIndex", 0);
			userList.set(index, (User) selectUser);// 更改数据源，将更新后的User加入数据源
			mTypeFavoriteTypeAdapter.setDataSource(userList);
		}
		if (requestCode == 1 && resultCode == Activity.RESULT_OK) {
			SupplyDemand selectSupplyDemand = (SupplyDemand) (data
					.getParcelableExtra("selectSupplyDemand"));
			ArrayList<SupplyDemand> supplyDemands = mSuplyDemandFavoriteAdapter
					.getDataSource();
			int index = data.getIntExtra("selectIndex", 0);
			supplyDemands.set(index, (SupplyDemand) selectSupplyDemand);// 更改数据源，将更新后的SupplyDemand加入数据源
			mSuplyDemandFavoriteAdapter.setDataSource(supplyDemands);
		}
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int position,
			long id) {
		User selectUser = null;
		SupplyDemand selectSupplyDemand = null;
		Intent intent = null;
		if (type == 1 || type == 2 || type == 3) {
			ArrayList<User> userList = mTypeFavoriteTypeAdapter.getDataSource();
			selectUser = userList.get(position - 1);
			intent = new Intent(ProfileFavoriteTypeActivity.this,
					CompanyInfoActivity.class);
		} else if (type == 4 || type == 5) {
			ArrayList<SupplyDemand> adapter = mSuplyDemandFavoriteAdapter
					.getDataSource();
			selectSupplyDemand = adapter.get(position - 1);
			intent = new Intent(ProfileFavoriteTypeActivity.this,
					SupplyDemandInfoActivity.class);
		}
		Bundle data = new Bundle();
		data.putInt("selectIndex", position - 1);
		if (type == 1 || type == 2 || type == 3) {
			intent.putExtra("selectUser", selectUser);
			intent.putExtras(data);
			startActivityForResult(intent, 0);
		} else if (type == 4 || type == 5) {
			intent.putExtra("selectSupplyDemand", selectSupplyDemand);
			intent.putExtras(data);
			startActivityForResult(intent, 1);
		}
	}

	/* 下拉刷新 */
	@Override
	public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
		if (type == 1 || type == 2 || type == 3) {
			mTypeFavoriteTypeAdapter.removeDataSource();
			mTypeFavoriteTypeAdapter.setPageNumber(1);
		} else if (type == 4 || type == 5) {
			mSuplyDemandFavoriteAdapter.getDataSource().clear();
			mSuplyDemandFavoriteAdapter.notifyDataSetChanged();
			mSuplyDemandFavoriteAdapter.setPageNumber(1);
		}
		getIntenretFavoriteType(type);
	}

	/* 上拉刷新 */
	@Override
	public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
		getIntenretFavoriteType(type);
	}
}
