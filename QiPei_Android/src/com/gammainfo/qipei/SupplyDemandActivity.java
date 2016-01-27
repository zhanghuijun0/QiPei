package com.gammainfo.qipei;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

import com.gammainfo.qipei.adapter.SupplyDemandAdapter;
import com.gammainfo.qipei.convert.SupplyDemandJSONConvert;
import com.gammainfo.qipei.db.Preferences;
import com.gammainfo.qipei.http.AsyncHttpResponseHandler;
import com.gammainfo.qipei.http.RequestParams;
import com.gammainfo.qipei.model.SupplyDemand;
import com.gammainfo.qipei.model.helper.SupplyDemandHelper;
import com.gammainfo.qipei.utils.Constant;
import com.gammainfo.qipei.widget.AlertDialog;
import com.gammainfo.qipei.widget.AlertDialog.OnAlertDialogListener;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;

public class SupplyDemandActivity extends BaseActivity implements
		OnItemClickListener, OnClickListener,
		PullToRefreshBase.OnRefreshListener2<ListView> {
	private static final int TYPE_SUPPLY = 1;
	private static final int TYPE_DEMAND = 2;

	private EditText mEditText;
	private TextView mSearchHint;
	private TextView mDeleteButtonTextView;
	private ImageButton mImgSearch;
	private LayoutInflater mLayoutInflater;
	private PullToRefreshListView mPullToRefreshListView;
	private SupplyDemandAdapter mSupplyDemandAdapter;
	private SupplyDemandAdapter mSupplyAdapter;
	private SupplyDemandAdapter mDemandAdapter;

	private int mSelectType = TYPE_SUPPLY;// 检索类型
	private RadioGroup mListRadioGroup;
	private String mSearchKey = "";
	private AlertDialog mLoginAlertDialog;
	private View mEmptyLayout;
	private AsyncHttpClient mHttpClient = new AsyncHttpClient();

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_supply_and_demand);
		initView();
	}

	private void initView() {
		mLayoutInflater = getLayoutInflater();
		mPullToRefreshListView = (PullToRefreshListView) findViewById(R.id.supple_demand_list);
		mListRadioGroup = (RadioGroup) findViewById(R.id.supple_demand_select_type);
		mSupplyAdapter = new SupplyDemandAdapter(new ArrayList<SupplyDemand>(),
				this);
		mDemandAdapter = new SupplyDemandAdapter(new ArrayList<SupplyDemand>(),
				this);
		mSupplyDemandAdapter = mSupplyAdapter;
		mEditText = (EditText) findViewById(R.id.demand_search_keyword);
		mDeleteButtonTextView = (TextView) findViewById(R.id.ibtn_search_deltext);
		mSearchHint = (TextView) findViewById(R.id.supply_demand_search_hint);
		mImgSearch = (ImageButton) findViewById(R.id.supply_demand_search_image);
		mEmptyLayout = (LinearLayout) findViewById(R.id.layout_supplydemand_empty);
		mEditText.setOnClickListener(this);
		mPullToRefreshListView.setAdapter(mSupplyDemandAdapter);
		mPullToRefreshListView.setMode(Mode.BOTH);
		mPullToRefreshListView.setOnRefreshListener(this);
		mPullToRefreshListView.setOnItemClickListener(this);

		loadTestDataByType();
		/* 对供求区添加单击跳转事件 */
		mListRadioGroup
				.setOnCheckedChangeListener(new OnCheckedChangeListener() {
					@Override
					public void onCheckedChanged(RadioGroup group, int checkedId) {
						// TODO Auto-generated method stub
						switch (checkedId) {
						case R.id.select_supply:
							mSelectType = TYPE_SUPPLY;
							mSupplyDemandAdapter = mSupplyAdapter;
							break;
						case R.id.select_demand:
							mSelectType = TYPE_DEMAND;
							mSupplyDemandAdapter = mDemandAdapter;
							break;
						}
						if (mSupplyDemandAdapter.getDataSource().size() == 0) {
							loadTestDataByType();
						} else {
							mEmptyLayout.setVisibility(View.GONE);
						}
						mPullToRefreshListView.setAdapter(mSupplyDemandAdapter);
					}
				});
		mEditText.setOnFocusChangeListener(new OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				// TODO Auto-generated method stub
				if (mImgSearch.getVisibility() != View.VISIBLE) {
					mImgSearch.setVisibility(View.VISIBLE);
					mSearchHint.setVisibility(View.INVISIBLE);
					mDeleteButtonTextView.setVisibility(View.VISIBLE);
					/* 弹出软键盘 */
					InputMethodManager imm = (InputMethodManager) mEditText
							.getContext().getSystemService(
									Context.INPUT_METHOD_SERVICE);
					if (imm.isActive()) { // 如果未开启
						imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT,
								InputMethodManager.HIDE_NOT_ALWAYS);
					}
				} else {
					searchHide();
				}
			}
		});
		/*
		 * 对软键盘的搜索按键进行监听
		 */
		mEditText.setOnEditorActionListener(new OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId,
					KeyEvent event) {
				if (actionId == EditorInfo.IME_ACTION_SEARCH
						|| actionId == EditorInfo.IME_ACTION_DONE
						|| actionId == EditorInfo.IME_ACTION_GO
						|| actionId == EditorInfo.IME_ACTION_SEND
						|| actionId == EditorInfo.IME_ACTION_UNSPECIFIED) {
					mSupplyDemandAdapter.setPageNumber(1);
					mSearchKey = mEditText.getText().toString();
					getDataSource();
					/* 关闭软键盘 */
					InputMethodManager imm = (InputMethodManager) mEditText
							.getContext().getSystemService(
									Context.INPUT_METHOD_SERVICE);
					if (imm.isActive()) {
						imm.hideSoftInputFromWindow(mEditText.getWindowToken(),
								0); // 强制隐藏键盘
					}
				}
				return false;
			}
		});
	}

	/*
	 * 搜索框回归不可编辑状态
	 */
	private void searchHide() {
		mEditText.setText("");
		mImgSearch.setVisibility(View.INVISIBLE);
		mSearchHint.setVisibility(View.VISIBLE);
		mDeleteButtonTextView.setVisibility(View.GONE);
		/* 关闭软键盘 */
		InputMethodManager imm = (InputMethodManager) mEditText.getContext()
				.getSystemService(Context.INPUT_METHOD_SERVICE);
		if (imm.isActive()) {
			imm.hideSoftInputFromWindow(mEditText.getWindowToken(), 0); // 强制隐藏键盘
		}
	}

	/* 通过所选用户的种类获取并加载所需要的数据 */
	private void loadTestDataByType() {
		ArrayList<SupplyDemand> userList = SupplyDemandHelper
				.select(mSelectType);
		mSupplyDemandAdapter.setDataSource(userList);
		if (mSupplyDemandAdapter.getPageNumber() == 1) {
			getDataSource();
		} else {
			if (mSupplyDemandAdapter.getCount() == 0) {
				mEmptyLayout.setVisibility(View.VISIBLE);
			} else {
				mEmptyLayout.setVisibility(View.GONE);
			}
		}
	}

	/* 获取并加载数据 */
	private void getDataSource() {
		mHttpClient.cancelRequests(this, true);
		final SupplyDemandAdapter supplyDemandAdapter = mSupplyDemandAdapter;
		RequestParams params = new RequestParams();
		params.put("user_id", Preferences.getAccountUserId() + "");
		params.put("key", mSearchKey);
		params.put("type", mSelectType + "");
		params.put("area", Preferences.getMyCity());
		params.put("page_index", supplyDemandAdapter.getPageNumber() + "");
		params.put("page_size", String.valueOf(Constant.PAGE_SIZE));
		mHttpClient.post(Constant.API_SUPPLY_DEMAND_LIST_GET, params,
				new AsyncHttpResponseHandler(SupplyDemandActivity.this,
						new JsonHttpResponseHandler() {

							public void onSuccess(JSONObject res) {

								JSONObject jsonInfo = null;
								ArrayList<SupplyDemand> supplyDemandList;
								try {
									if (res.getInt(Constant.JSON_KEY_CODE) == Constant.CODE_SUCCESS) {
										jsonInfo = res
												.getJSONObject(Constant.JSON_KEY_INFO);
										JSONArray jsonListArray = jsonInfo
												.getJSONArray("list");
										supplyDemandList = SupplyDemandJSONConvert
												.convertJsonArrayToItemList(jsonListArray);
										if (supplyDemandAdapter.getPageNumber() == 1) {

											supplyDemandAdapter
													.setDataSource(supplyDemandList);
											if (mSearchKey.equals("")) {
												SupplyDemandHelper
														.deleteBefore(mSelectType);
												SupplyDemandHelper
														.insert(SupplyDemandActivity.this,
																supplyDemandList);
											}
										} else {
											if (jsonListArray.length() == 0) {
												Toast.makeText(
														SupplyDemandActivity.this,
														R.string.common_label_last_page,
														Toast.LENGTH_SHORT)
														.show();
											}
											supplyDemandAdapter
													.appendDataSource(supplyDemandList);
										}

										supplyDemandAdapter
												.setPageNumber(supplyDemandAdapter
														.getPageNumber() + 1);
									} else {
										Toast.makeText(
												SupplyDemandActivity.this,
												res.getString(Constant.JSON_MESSAGE),
												Toast.LENGTH_SHORT).show();
									}
								} catch (JSONException e) {
									e.printStackTrace();
								}
								if (mSupplyDemandAdapter.getCount() == 0) {
									mEmptyLayout.setVisibility(View.VISIBLE);
								} else {
									mEmptyLayout.setVisibility(View.GONE);
								}
							}

							@Override
							public void onFinish() {
								mPullToRefreshListView.onRefreshComplete();

								super.onFinish();
							}
						}));
	}

	public void release(View v) {
		if (Preferences.isLogin()) {
			startActivity(new Intent(this, ProfileReleaseActivity.class));
		} else {
			if (mLoginAlertDialog == null) {
				mLoginAlertDialog = AlertDialog.build(this,
						R.string.common_label_unlogin,
						R.string.common_label_gotologin,
						R.string.common_label_cancel,
						new OnAlertDialogListener() {

							@Override
							public void onOk(AlertDialog alertDialogView) {
								startActivity(new Intent(
										SupplyDemandActivity.this,
										LoginActivity.class));
								alertDialogView.dismiss();
							}

							@Override
							public void onCancel(AlertDialog alertDialogView) {
								alertDialogView.dismiss();
							}
						});
			}
			mLoginAlertDialog.show();
		}
	}

	public void back(View v) {
		finish();
	}

	/*
	 * 对取消按钮进行监听
	 */
	public void delete(View v) {
		setKeywordEditorFocusable(false);
	}

	@Override
	public void onClick(View v) {
		setKeywordEditorFocusable(true);
		mEditText.requestFocus();
	}

	public void setKeywordEditorFocusable(boolean focusable) {
		mEditText.setFocusable(focusable);
		mEditText.setFocusableInTouchMode(focusable);
	}

	@Override
	public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
		String label = DateUtils.formatDateTime(this,
				System.currentTimeMillis(), DateUtils.FORMAT_SHOW_TIME
						| DateUtils.FORMAT_SHOW_DATE
						| DateUtils.FORMAT_ABBREV_ALL);
		mSearchKey = mEditText.getText().toString();
		refreshView.getLoadingLayoutProxy().setLastUpdatedLabel(label);
		mSupplyDemandAdapter.setPageNumber(1);
		getDataSource();
	}

	@Override
	public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
		// TODO Auto-generated method stub
		getDataSource();
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int position,
			long id) {
		// TODO Auto-generated method stub
		ArrayList<SupplyDemand> userList = mSupplyDemandAdapter.getDataSource();
		SupplyDemand selectSupplyDemand = userList.get(position - 1);
		Intent intent = new Intent(SupplyDemandActivity.this,
				SupplyDemandInfoActivity.class);
		Bundle bundle = new Bundle();
		bundle.putInt("selectIndex", position - 1);
		intent.putExtra("selectSupplyDemand", selectSupplyDemand);
		intent.putExtras(bundle);
		startActivityForResult(intent, (int) 0);
	}

	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == 0 && resultCode == Activity.RESULT_OK) {
			ArrayList<SupplyDemand> supplyDemands = mSupplyDemandAdapter
					.getDataSource();
			int index = data.getIntExtra("selectIndex", 0);
			SupplyDemand selectSupplyDemand = (SupplyDemand) data
					.getParcelableExtra("selectSupplyDemand");
			supplyDemands.set(index, selectSupplyDemand);// 更改数据源，将更新后的User加入数据源
			mSupplyDemandAdapter.setDataSource(supplyDemands);
		}
	}
}
