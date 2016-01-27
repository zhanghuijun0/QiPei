package com.gammainfo.qipei;

import java.util.ArrayList;
import java.util.Date;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnCreateContextMenuListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.HeaderViewListAdapter;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.gammainfo.qipei.adapter.SupplyDemandAdapter;
import com.gammainfo.qipei.convert.SupplyDemandJSONConvert;
import com.gammainfo.qipei.db.Preferences;
import com.gammainfo.qipei.http.AsyncHttpResponseHandler;
import com.gammainfo.qipei.http.RequestParams;
import com.gammainfo.qipei.http.RestClient;
import com.gammainfo.qipei.model.SupplyDemand;
import com.gammainfo.qipei.model.User;
import com.gammainfo.qipei.utils.Constant;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.loopj.android.http.JsonHttpResponseHandler;

public class ProfileMyReleaseListActivityBk extends BaseActivity implements
		OnItemClickListener, PullToRefreshBase.OnRefreshListener2<ListView> {
	private LayoutInflater mLayoutInflater;
	private View emptyView;
	// private LinearLayout emptyView;
	private RelativeLayout viewContainer;
	private PullToRefreshListView mSupplyListView;
	private PullToRefreshListView mDemandListView;
	private RadioGroup mProfileMyReleaseType;
	private SupplyDemandAdapter mSupplyAdapter;
	private SupplyDemandAdapter mDemandAdapter;
	private int supplyLocation = 0;
	private int demandLocation = 0;
	private final int MENU_SUPPLY_SURE = 0x111;
	private final int MENU_SUPPLY_CANCEL = 0x112;
	private final int MENU_DEMAND_SURE = 0x113;
	private final int MENU_DEMAND_CANCEL = 0x114;
	private int mDelectReleaseId = 0;
	private boolean isFirst = true;
	private User user;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		viewContainer = (RelativeLayout) getLayoutInflater().inflate(
				R.layout.profile_my_release_list, null);
		setContentView(viewContainer);
		user = Preferences.getAccountUser();
		mSupplyListView = (PullToRefreshListView) getLayoutInflater().inflate(
				R.layout.profile_my_release_listview, null);
		mDemandListView = (PullToRefreshListView) getLayoutInflater().inflate(
				R.layout.profile_my_release_listview, null);
		mLayoutInflater = getLayoutInflater();
		emptyView = mLayoutInflater.inflate(
				R.layout.listview_emptyview_profile_my_release_list_activity,
				null);
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
				RelativeLayout.LayoutParams.MATCH_PARENT,
				RelativeLayout.LayoutParams.MATCH_PARENT);
		params.addRule(RelativeLayout.BELOW, R.id.profile_my_release_type);
		mSupplyListView.setLayoutParams(params);
		mDemandListView.setLayoutParams(params);
		emptyView.setLayoutParams(params);
		viewContainer.addView(mDemandListView);
		viewContainer.addView(mSupplyListView);
		mSupplyAdapter = new SupplyDemandAdapter(new ArrayList<SupplyDemand>(),
				this);
		mDemandAdapter = new SupplyDemandAdapter(new ArrayList<SupplyDemand>(),
				this);
		mSupplyListView.setOnItemClickListener(this);
		mSupplyListView.setOnRefreshListener(this);
		mSupplyListView.setMode(Mode.BOTH);

		mDemandListView.setOnItemClickListener(this);
		mDemandListView.setOnRefreshListener(this);
		mDemandListView.setMode(Mode.BOTH);

		mProfileMyReleaseType = (RadioGroup) findViewById(R.id.profile_my_release_type_group);
		mProfileMyReleaseType
				.setOnCheckedChangeListener(new OnCheckedChangeListener() {
					@Override
					public void onCheckedChanged(RadioGroup group, int checkedId) {
						switch (checkedId) {
						case R.id.profile_my_supply_list:
							mDemandListView.setVisibility(View.GONE);
							mSupplyListView.setVisibility(View.VISIBLE);
							if (mSupplyAdapter.getPageNumber() == 1) {
								getInternetSupplyList();
							}
							break;
						case R.id.profile_my_demand_list:
							mSupplyListView.setVisibility(View.GONE);
							mDemandListView.setVisibility(View.VISIBLE);
							if (mDemandAdapter.getPageNumber() == 1) {
								getInternetDemandList();
							}
							break;
						}
					}
				});
		((RadioButton) findViewById(R.id.profile_my_supply_list))
				.setChecked(true);
		mSupplyListView.getRefreshableView().setOnItemLongClickListener(
				new OnItemLongClickListener() {
					@Override
					public boolean onItemLongClick(AdapterView<?> arg0,
							View arg1, int arg2, long arg3) {
						supplyLocation = arg2;// 记住选择的item
						return false;
					}
				});

		mSupplyListView
				.setOnCreateContextMenuListener(new OnCreateContextMenuListener() {
					@Override
					public void onCreateContextMenu(ContextMenu menu, View v,
							ContextMenuInfo menuInfo) {
						menu.add(0, MENU_SUPPLY_SURE, 0,
								getString(R.string.profile_label_delete_text));
						menu.add(0, MENU_SUPPLY_CANCEL, 0,
								getString(R.string.profile_label_cancel_text));
					}
				});

		mDemandListView.getRefreshableView().setOnItemLongClickListener(
				new OnItemLongClickListener() {
					@Override
					public boolean onItemLongClick(AdapterView<?> arg0,
							View arg1, int arg2, long arg3) {
						demandLocation = arg2;// 记住选择的item
						return false;
					}
				});

		mDemandListView
				.setOnCreateContextMenuListener(new OnCreateContextMenuListener() {
					@Override
					public void onCreateContextMenu(ContextMenu menu, View v,
							ContextMenuInfo menuInfo) {
						menu.add(0, MENU_DEMAND_SURE, 0,
								getString(R.string.profile_label_delete_text));
						menu.add(0, MENU_DEMAND_CANCEL, 0,
								getString(R.string.profile_label_cancel_text));
					}
				});
	}

	/* 上下文菜单被单机时执行的操作 */
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case MENU_SUPPLY_SURE:
			mDelectReleaseId = mSupplyAdapter.removeOneDataSource(
					supplyLocation).getId();
			moveInternetRelaese(MENU_SUPPLY_SURE);
			visibleEmptyView(mSupplyAdapter);
			break;
		case MENU_SUPPLY_CANCEL:
			Toast.makeText(this,
					getString(R.string.profile_toast_you_choose_cancel_text),
					Toast.LENGTH_SHORT).show();
			break;
		case MENU_DEMAND_SURE:
			mDelectReleaseId = mDemandAdapter.removeOneDataSource(
					demandLocation).getId();
			moveInternetRelaese(MENU_DEMAND_SURE);
			visibleEmptyView(mDemandAdapter);
			break;
		case MENU_DEMAND_CANCEL:
			// Toast.makeText(this,
			// getString(R.string.profile_toast_you_choose_cancel_text),
			// Toast.LENGTH_SHORT).show();
			break;
		}
		return super.onContextItemSelected(item);
	}

	/* 删除的时候如果列表内容为空的话显示emptyview */
	public void visibleEmptyView(SupplyDemandAdapter Adapter) {
		if (Adapter.getCount() == 0) {
			emptyView.setVisibility(View.VISIBLE);
		}
	}

	/* 移除网上数据库的“供应”信息 */
	public void moveInternetRelaese(int item) {
		int id = Preferences.getAccountUserId();
		RequestParams params = new RequestParams();
		params.put("user_id", String.valueOf(id));
		params.put("id", String.valueOf(mDelectReleaseId));
		RestClient.post(Constant.API_DELETE_SUPPLY_DEMAND, params,
				new AsyncHttpResponseHandler(getBaseContext(),
						new JsonHttpResponseHandler() {

							@Override
							public void onSuccess(JSONObject rlt) {
								try {
									if (rlt.getInt(Constant.JSON_KEY_CODE) == Constant.CODE_SUCCESS) {
										Toast.makeText(
												getBaseContext(),
												getString(R.string.profile_toast_delete_success_text),
												Toast.LENGTH_SHORT).show();
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
						}));
	}

	/* 获取网上数据库中的“供应”信息 */
	private void getInternetSupplyList() {
		int id = Preferences.getAccountUserId();
		RequestParams params = new RequestParams();
		params.put("user_id", String.valueOf(id));
		params.put("page_index", String.valueOf(mSupplyAdapter.getPageNumber()));
		params.put("page_size", String.valueOf(mSupplyAdapter.getPageSize()));
		params.put("type", String.valueOf(1));
		RestClient.post(Constant.API_GET_SUPPLY_DEMAND, params,
				new AsyncHttpResponseHandler(getBaseContext(),
						new JsonHttpResponseHandler() {

							@Override
							public void onStart() {
								if (!mSupplyListView.isRefreshing()) {
									mSupplyListView.setRefreshing(true);
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
										if (list.length() == 0
												&& mSupplyAdapter
														.getPageNumber() != 1) {
											Toast.makeText(
													getBaseContext(),
													getString(R.string.common_label_last_page),
													Toast.LENGTH_SHORT).show();
										}
										ArrayList<SupplyDemand> supllyList = SupplyDemandJSONConvert
												.convertJsonArrayToItemList(list);
										if (mSupplyAdapter.incPageNumber() == 2) {
											mSupplyAdapter
													.setDataSource(supllyList);
										} else {
											mSupplyAdapter
													.appendDataSource(supllyList);
										}
										if (mSupplyAdapter.getCount() == 0) {
											if (isFirst == true) {
												viewContainer
														.addView(emptyView);
												isFirst = false;
											} else {
												emptyView
														.setVisibility(View.VISIBLE);
											}
										} else {
											emptyView.setVisibility(View.GONE);
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
								if (mSupplyListView.getRefreshableView()
										.getAdapter() == null) {
									mSupplyListView.setAdapter(mSupplyAdapter);
								}
								mSupplyListView.onRefreshComplete();
								super.onFinish();
							}
						}));
	}

	/* 获取网上数据库中的“求购”信息 */
	private void getInternetDemandList() {
		int id = Preferences.getAccountUserId();
		RequestParams params = new RequestParams();
		params.put("user_id", String.valueOf(id));
		params.put("page_index", String.valueOf(mDemandAdapter.getPageNumber()));
		params.put("page_size", String.valueOf(mDemandAdapter.getPageSize()));
		params.put("type", String.valueOf(2));
		RestClient.post(Constant.API_GET_SUPPLY_DEMAND, params,
				new AsyncHttpResponseHandler(getBaseContext(),
						new JsonHttpResponseHandler() {

							@Override
							public void onStart() {
								if (!mDemandListView.isRefreshing()) {
									mDemandListView.setRefreshing(true);
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
										if (list.length() == 0
												&& mDemandAdapter
														.getPageNumber() != 1) {
											Toast.makeText(
													getBaseContext(),
													getString(R.string.common_label_last_page),
													Toast.LENGTH_SHORT).show();
										}
										ArrayList<SupplyDemand> demandList = SupplyDemandJSONConvert
												.convertJsonArrayToItemList(list);
										if (mDemandAdapter.incPageNumber() == 2) {
											mDemandAdapter
													.setDataSource(demandList);
										} else {
											mDemandAdapter
													.appendDataSource(demandList);
										}
										if (mDemandAdapter.getCount() == 0) {
											if (isFirst == true) {
												viewContainer
														.addView(emptyView);
												isFirst = false;
											} else {
												emptyView
														.setVisibility(View.VISIBLE);
											}
										} else {
											emptyView.setVisibility(View.GONE);
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
								if (mDemandListView.getRefreshableView()
										.getAdapter() == null) {
									mDemandListView.setAdapter(mDemandAdapter);
								}
								mDemandListView.onRefreshComplete();
								super.onFinish();
							}
						}));
	}

	/* 跳转到发布页面 */
	public void onReleaseClick(View v) {
		Intent intent = new Intent(ProfileMyReleaseListActivityBk.this,
				ProfileReleaseActivity.class);
		startActivityForResult(intent, 0);
	}

	/* 返回上一页 */
	public void onBackClick(View v) {
		finish();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == 0 && resultCode == Activity.RESULT_OK) {
			emptyView.setVisibility(View.GONE);// 把提示空的页面给以藏掉
			// 在数据源插入一条数据，放在第一条
			SupplyDemand supplydemand = new SupplyDemand();
			supplydemand.setId(data.getIntExtra("id", 0));
			supplydemand.setTitle((data.getStringExtra("title")).toString());
			supplydemand.setArea((data.getStringExtra("area")).toString());
			supplydemand
					.setContent((data.getStringExtra("content")).toString());
			supplydemand.setUserId(Preferences.getAccountUserId());
			supplydemand.setPhone(user.getPhone());
			supplydemand.setCompanyName(user.getCompanyName());
			supplydemand.setType(data.getIntExtra("type", 1));
			supplydemand.setIsFavorite(false);
			supplydemand.setUpdateTime((int) new Date().getTime());
			if (data.getIntExtra("type", 1) == 1) {
				mSupplyAdapter.getDataSource().add(0, supplydemand);
				mSupplyAdapter.notifyDataSetChanged();
			} else {
				mDemandAdapter.getDataSource().add(0, supplydemand);
				mDemandAdapter.notifyDataSetChanged();
			}
		} else if (requestCode == 1 && resultCode == Activity.RESULT_OK) {
			SupplyDemand selectSupplyDemand = (SupplyDemand) (data
					.getParcelableExtra("selectSupplyDemand"));
			ArrayList<SupplyDemand> supplyDemandList;
			if (mProfileMyReleaseType.getCheckedRadioButtonId() == R.id.profile_my_supply_list) {
				supplyDemandList = mSupplyAdapter.getDataSource();
			} else {
				supplyDemandList = mDemandAdapter.getDataSource();
			}
			int index = data.getIntExtra("selectIndex", 0);
			supplyDemandList.set(index, (SupplyDemand) selectSupplyDemand);// 更改数据源，将更新后的User加入数据源
			if (mProfileMyReleaseType.getCheckedRadioButtonId() == R.id.profile_my_supply_list) {
				mSupplyAdapter.setDataSource(supplyDemandList);
			} else {
				mDemandAdapter.setDataSource(supplyDemandList);
			}
		}
	}

	@Override
	public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
		if (refreshView.equals(mSupplyListView)) {
			mSupplyAdapter.setPageNumber(1);
			getInternetSupplyList();
		} else if (refreshView.equals(mDemandListView)) {
			mDemandAdapter.setPageNumber(1);
			getInternetDemandList();
		}
	}

	@Override
	public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
		if (refreshView.equals(mSupplyListView)) {
			getInternetSupplyList();
		} else if (refreshView.equals(mDemandListView)) {
			getInternetDemandList();
		}
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int position,
			long id) {
		HeaderViewListAdapter headerView = (HeaderViewListAdapter) (((ListView) arg0)
				.getAdapter());
		SupplyDemandAdapter adapter = (SupplyDemandAdapter) headerView
				.getWrappedAdapter();
		SupplyDemand selectSupplyDemand = adapter.getDataSource().get(
				position - 1);
		Intent intent = new Intent(ProfileMyReleaseListActivityBk.this,
				SupplyDemandInfoActivity.class);
		Bundle bundle = new Bundle();
		bundle.putInt("selectIndex", position - 1);
		intent.putExtra("selectSupplyDemand", selectSupplyDemand);
		intent.putExtras(bundle);
		startActivityForResult(intent, 1);
	}

}
