package com.gammainfo.qipei;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
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
import com.gammainfo.qipei.widget.ContextMenuDialog;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.loopj.android.http.JsonHttpResponseHandler;

public class ProfileMyReleaseListActivity extends BaseActivity implements
		OnItemClickListener, PullToRefreshBase.OnRefreshListener2<ListView> {
	public static final int REQ_VIEW_SUPPLY_DEMAND = 1;
	public static final int REQ_EDIT_SUPPLY_DEMAND = 2;
	public static final int REQ_ADD_SUPPLY_DEMAND = 3;
	private LayoutInflater mLayoutInflater;
	private View mEmptyView;
	private RelativeLayout viewContainer;
	private PullToRefreshListView mSupplyListView;
	private PullToRefreshListView mDemandListView;
	private RadioGroup mProfileMyReleaseType;
	private SupplyDemandAdapter mSupplyAdapter;
	private SupplyDemandAdapter mDemandAdapter;
	private int mCurrentPosition;
	private User mAccount;
	private ContextMenuDialog mContextMenuDialog;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		viewContainer = (RelativeLayout) getLayoutInflater().inflate(
				R.layout.profile_my_release_list, null);
		setContentView(viewContainer);
		mAccount = Preferences.getAccountUser();
		mSupplyListView = (PullToRefreshListView) getLayoutInflater().inflate(
				R.layout.profile_my_release_listview, null);
		mDemandListView = (PullToRefreshListView) getLayoutInflater().inflate(
				R.layout.profile_my_release_listview, null);
		mLayoutInflater = getLayoutInflater();
		mEmptyView = mLayoutInflater.inflate(
				R.layout.listview_emptyview_profile_my_release_list_activity,
				null);
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
				RelativeLayout.LayoutParams.MATCH_PARENT,
				RelativeLayout.LayoutParams.MATCH_PARENT);
		params.addRule(RelativeLayout.BELOW, R.id.profile_my_release_type);
		mSupplyListView.setLayoutParams(params);
		mDemandListView.setLayoutParams(params);
		mEmptyView.setLayoutParams(params);
		viewContainer.addView(mDemandListView);
		viewContainer.addView(mSupplyListView);
		viewContainer.addView(mEmptyView);
		mSupplyAdapter = new SupplyDemandAdapter(new ArrayList<SupplyDemand>(),
				this);
		mDemandAdapter = new SupplyDemandAdapter(new ArrayList<SupplyDemand>(),
				this);
		mSupplyListView.setOnItemClickListener(this);
		mSupplyListView.setOnRefreshListener(this);
		mSupplyListView.setMode(Mode.BOTH);
		mSupplyListView.setAdapter(mSupplyAdapter);

		mDemandListView.setOnItemClickListener(this);
		mDemandListView.setOnRefreshListener(this);
		mDemandListView.setMode(Mode.BOTH);
		mDemandListView.setAdapter(mDemandAdapter);

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
								getSupplyList();
							} else {
								doEmptyViewVisibility(mSupplyListView);
							}
							break;
						case R.id.profile_my_demand_list:
							mSupplyListView.setVisibility(View.GONE);
							mDemandListView.setVisibility(View.VISIBLE);
							if (mDemandAdapter.getPageNumber() == 1) {
								getDemandList();
							} else {
								doEmptyViewVisibility(mDemandListView);
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
							View arg1, int position, long arg3) {
						mCurrentPosition = position - 1;// 记住选择的item
						showContextMenu();
						return true;
					}
				});

		mDemandListView.getRefreshableView().setOnItemLongClickListener(
				new OnItemLongClickListener() {
					@Override
					public boolean onItemLongClick(AdapterView<?> arg0,
							View arg1, int position, long arg3) {
						mCurrentPosition = position - 1;// 记住选择的item
						showContextMenu();
						return false;
					}
				});
	}

	public void doEmptyViewVisibility(PullToRefreshListView currentListview) {
		int visibility = View.GONE;
		if (mSupplyListView.getVisibility() == View.VISIBLE) {
			if (currentListview == mSupplyListView) {
				if (mSupplyAdapter.getDataSource().isEmpty()) {
					visibility = View.VISIBLE;
				}
			}
		} else {
			if (currentListview == mDemandListView) {
				if (mDemandAdapter.getDataSource().isEmpty()) {
					visibility = View.VISIBLE;
				}
			}
		}
		mEmptyView.setVisibility(visibility);
	}

	public void deleteSupplyDemand(PullToRefreshListView currentView,
			SupplyDemand delItem) {
		final SupplyDemand supplyDemand = delItem;
		final PullToRefreshListView currentListView = currentView;
		int id = Preferences.getAccountUserId();
		RequestParams params = new RequestParams();
		params.put("user_id", String.valueOf(id));
		params.put("id", String.valueOf(supplyDemand.getId()));
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
										if (currentListView == mSupplyListView) {
											mSupplyAdapter.getDataSource()
													.remove(supplyDemand);
											mSupplyAdapter
													.notifyDataSetChanged();
											doEmptyViewVisibility(mSupplyListView);
										} else {
											mDemandAdapter.getDataSource()
													.remove(supplyDemand);
											mDemandAdapter
													.notifyDataSetChanged();
											doEmptyViewVisibility(mDemandListView);
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
						}));
	}

	private void getSupplyList() {
		int id = Preferences.getAccountUserId();
		RequestParams params = new RequestParams();
		params.put("user_id", String.valueOf(id));
		params.put("page_index", String.valueOf(mSupplyAdapter.getPageNumber()));
		params.put("page_size", String.valueOf(mSupplyAdapter.getPageSize()));
		params.put("type", "1");
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
									} else {
										Toast.makeText(
												getBaseContext(),
												rlt.getString(Constant.JSON_MESSAGE),
												Toast.LENGTH_SHORT).show();
									}
									doEmptyViewVisibility(mSupplyListView);
								} catch (JSONException e) {
									e.printStackTrace();
								}
							}

							@Override
							public void onFinish() {
								mSupplyListView.onRefreshComplete();
								super.onFinish();
							}
						}));
	}

	private void getDemandList() {
		int id = Preferences.getAccountUserId();
		RequestParams params = new RequestParams();
		params.put("user_id", String.valueOf(id));
		params.put("page_index", String.valueOf(mDemandAdapter.getPageNumber()));
		params.put("page_size", String.valueOf(mDemandAdapter.getPageSize()));
		params.put("type", "2");
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
									} else {
										Toast.makeText(
												getBaseContext(),
												rlt.getString(Constant.JSON_MESSAGE),
												Toast.LENGTH_SHORT).show();
									}
									doEmptyViewVisibility(mDemandListView);
								} catch (JSONException e) {
									e.printStackTrace();
								}
							}

							@Override
							public void onFinish() {
								mDemandListView.onRefreshComplete();
								super.onFinish();
							}
						}));
	}

	/* 跳转到发布页面 */
	public void onReleaseClick(View v) {
		// ProfileReleaseActivity
		Intent intent = new Intent(ProfileMyReleaseListActivity.this,
				SupplyDemandPublishActivity.class);
		startActivityForResult(intent, REQ_ADD_SUPPLY_DEMAND);
	}

	/* 返回上一页 */
	public void onBackClick(View v) {
		finish();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_OK) {
			if (requestCode == REQ_ADD_SUPPLY_DEMAND) {
				// 在数据源插入一条数据，放在第一条
				SupplyDemand supplydemand = data
						.getParcelableExtra(SupplyDemandPublishActivity.EXTRA_SUPPLY_DEMAND);
				supplydemand.setUserId(mAccount.getId());
				supplydemand.setPhone(mAccount.getPhone());
				supplydemand.setCompanyName(mAccount.getCompanyName());
				supplydemand.setUpdateTime(System.currentTimeMillis());
				int type = supplydemand.getType();
				if (type == SupplyDemand.TYPE_SUPLLY) {
					mSupplyAdapter.getDataSource().add(0, supplydemand);
					mSupplyAdapter.notifyDataSetChanged();
					mSupplyListView.getRefreshableView().setSelection(0);
				} else {
					mDemandAdapter.getDataSource().add(0, supplydemand);
					mDemandAdapter.notifyDataSetChanged();
					mDemandListView.getRefreshableView().setSelection(0);
				}

			} else if (requestCode == REQ_VIEW_SUPPLY_DEMAND) {
				// TODO
				SupplyDemand selectSupplyDemand = data
						.getParcelableExtra(SupplyDemandInfoActivity.EXTRA_SUPPLY_DEMAND);
				ArrayList<SupplyDemand> supplyDemandList;
				if (mProfileMyReleaseType.getCheckedRadioButtonId() == R.id.profile_my_supply_list) {
					supplyDemandList = mSupplyAdapter.getDataSource();
				} else {
					supplyDemandList = mDemandAdapter.getDataSource();
				}
				supplyDemandList.set(mCurrentPosition, selectSupplyDemand);// 更改数据源，将更新后的User加入数据源
				if (mProfileMyReleaseType.getCheckedRadioButtonId() == R.id.profile_my_supply_list) {
					mSupplyAdapter.notifyDataSetChanged();
				} else {
					mDemandAdapter.notifyDataSetChanged();
				}
			} else if (requestCode == REQ_EDIT_SUPPLY_DEMAND) {
				// TODOD 如果用户修改了供需类型，需要把之前的从数据源中移除
				SupplyDemand selectSupplyDemand = data
						.getParcelableExtra(SupplyDemandPublishActivity.EXTRA_SUPPLY_DEMAND);
				ArrayList<SupplyDemand> supplyDemandList;
				if (mProfileMyReleaseType.getCheckedRadioButtonId() == R.id.profile_my_supply_list) {
					supplyDemandList = mSupplyAdapter.getDataSource();
				} else {
					supplyDemandList = mDemandAdapter.getDataSource();
				}
				supplyDemandList.set(mCurrentPosition, selectSupplyDemand);// 更改数据源，将更新后的User加入数据源
				if (mProfileMyReleaseType.getCheckedRadioButtonId() == R.id.profile_my_supply_list) {
					mSupplyAdapter.notifyDataSetChanged();
				} else {
					mDemandAdapter.notifyDataSetChanged();
				}
			}
			doEmptyViewVisibility(mSupplyListView.getVisibility() == View.VISIBLE ? mSupplyListView
					: mDemandListView);
		}
	}

	@Override
	public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
		if (refreshView.equals(mSupplyListView)) {
			mSupplyAdapter.setPageNumber(1);
			getSupplyList();
		} else if (refreshView.equals(mDemandListView)) {
			mDemandAdapter.setPageNumber(1);
			getDemandList();
		}
	}

	@Override
	public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
		if (refreshView.equals(mSupplyListView)) {
			getSupplyList();
		} else if (refreshView.equals(mDemandListView)) {
			getDemandList();
		}
	}

	@Override
	public void onItemClick(AdapterView<?> targetView, View arg1, int position,
			long id) {
		mCurrentPosition = position - 1;
		SupplyDemand selectSupplyDemand;
		if (mSupplyListView.getVisibility() == View.VISIBLE) {
			selectSupplyDemand = mSupplyAdapter.getDataSource().get(
					mCurrentPosition);
		} else {
			selectSupplyDemand = mDemandAdapter.getDataSource().get(
					mCurrentPosition);
		}
		Intent intent = new Intent(ProfileMyReleaseListActivity.this,
				SupplyDemandInfoActivity.class);
		Bundle bundle = new Bundle();
		intent.putExtra(SupplyDemandInfoActivity.EXTRA_SUPPLY_DEMAND,
				selectSupplyDemand);
		intent.putExtras(bundle);
		startActivityForResult(intent, REQ_VIEW_SUPPLY_DEMAND);
	}

	private void showContextMenu() {
		if (mContextMenuDialog == null) {
			ArrayList<String> datasource = new ArrayList<String>();
			datasource.add("编辑");
			datasource.add("删除");
			mContextMenuDialog = ContextMenuDialog.build(this,
					new ContextMenuDialog.OnContextMenuListener() {
						@Override
						public void onItemClick(ContextMenuDialog target,
								int index) {
							target.dismiss();
							if (index == 0) {
								SupplyDemand supplyDemand;
								if (mSupplyListView.getVisibility() == View.VISIBLE) {
									supplyDemand = mSupplyAdapter
											.getDataSource().get(
													mCurrentPosition);
								} else {
									supplyDemand = mDemandAdapter
											.getDataSource().get(
													mCurrentPosition);
								}
								// 编辑
								Intent intent = new Intent(
										ProfileMyReleaseListActivity.this,
										SupplyDemandPublishActivity.class);
								intent.putExtra(
										SupplyDemandPublishActivity.EXTRA_SUPPLY_DEMAND,
										supplyDemand);
								startActivityForResult(intent,
										REQ_EDIT_SUPPLY_DEMAND);
							} else {
								// 删除
								if (mSupplyListView.getVisibility() == View.VISIBLE) {
									deleteSupplyDemand(
											mSupplyListView,
											mSupplyAdapter.getDataSource().get(
													mCurrentPosition));
								} else {
									deleteSupplyDemand(
											mDemandListView,
											mDemandAdapter.getDataSource().get(
													mCurrentPosition));
								}
							}
						}

						@Override
						public void onCancelClick(ContextMenuDialog target) {
							target.dismiss();
						}
					}, datasource);
		}
		mContextMenuDialog.show();
	}
}
